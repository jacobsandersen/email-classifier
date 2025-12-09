package dev.jacobandersen.emailclassifier.service;

import dev.jacobandersen.emailclassifier.exception.QueueFullException;
import dev.jacobandersen.emailclassifier.model.dto.request.GenerateEmbeddingRequest;
import dev.jacobandersen.emailclassifier.model.entity.EmbeddingEntity;
import dev.jacobandersen.emailclassifier.repository.EmbeddingRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EmbeddingService {
    private static final int MAX_QUEUE_SIZE = 2000;
    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final EmbeddingModel model;
    private final EmbeddingRepository embeddingRepository;
    private final Sinks.Many<GenerateEmbeddingRequest> embeddingRequestSink;
    private final AtomicInteger currentQueueSize = new AtomicInteger(0);
    private final ExecutorService executor = Executors.newFixedThreadPool(8);

    @Value("${app.prompt.prefix.embedding}")
    private String embeddingPrefix;

    @Value("${app.prompt.prefix.classification}")
    private String classificationPrefix;

    @Autowired
    public EmbeddingService(final EmbeddingModel model, EmbeddingRepository embeddingRepository) {
        this.model = model;
        this.embeddingRepository = embeddingRepository;
        embeddingRequestSink = Sinks.many().unicast().onBackpressureBuffer();
    }

    public Mono<float[]> embedForClassification(String text) {
        return embedText("%s%s".formatted(classificationPrefix, text));
    }

    public Mono<float[]> embedText(String text) {
        return Mono.fromCallable(() -> model.embed(text)).subscribeOn(Schedulers.fromExecutor(executor));
    }

    public Mono<Void> enqueueEmbedding(GenerateEmbeddingRequest request) {
        if (currentQueueSize.incrementAndGet() > MAX_QUEUE_SIZE) {
            currentQueueSize.decrementAndGet();
            return Mono.error(new QueueFullException());
        }

        final var emitResult = embeddingRequestSink.tryEmitNext(request);
        if (emitResult.isFailure()) {
            currentQueueSize.decrementAndGet();
            return Mono.error(new IllegalStateException("Embedding request failed: " + emitResult));
        }

        return Mono.empty();
    }

    public Mono<List<GenerateEmbeddingRequest>> enqueueBatch(List<GenerateEmbeddingRequest> requests) {
        final int batchSize = requests.size();

        while (true) {
            final int current = currentQueueSize.get();
            final int newSize = current + batchSize;

            if (newSize > MAX_QUEUE_SIZE) {
                return Mono.error(new QueueFullException());
            }

            if (currentQueueSize.compareAndSet(current, newSize)) {
                break;
            }
        }

        int pushed = 0;
        try {
            for (; pushed < batchSize; pushed++) {
                final var emitResult = embeddingRequestSink.tryEmitNext(requests.get(pushed));
                if (emitResult.isFailure()) {
                    break;
                }
            }
        } finally {
            int notPushed = batchSize - pushed;
            if (notPushed > 0) {
                currentQueueSize.addAndGet(-notPushed);
            }
        }

        return Mono.just(requests.subList(pushed, batchSize));
    }

    @PostConstruct
    private void processEmbeddingQueue() {
        embeddingRequestSink
                .asFlux()
                .flatMap(req -> processEmbedding(req)
                        .doFinally(_ -> currentQueueSize.decrementAndGet()))
                .subscribe();
    }

    private Mono<EmbeddingEntity> processEmbedding(GenerateEmbeddingRequest request) {
        final var content = request.content();

        return embedText("%s%s".formatted(embeddingPrefix, content))
                .map(embedding -> new EmbeddingEntity(request.category(), request.subcategory(), content, embedding))
                .flatMap(embeddingRepository::save)
                .onErrorResume(throwable -> {
                    log.error("Embedding job failed", throwable);
                    return Mono.empty();
                });
    }
}
