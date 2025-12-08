package dev.jacobandersen.emailclassifier.service;

import dev.jacobandersen.emailclassifier.model.dto.request.ClassifyRequest;
import dev.jacobandersen.emailclassifier.model.dto.response.ClassifyResponse;
import dev.jacobandersen.emailclassifier.repository.CentroidRepository;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ClassificationService {
    private final EmbeddingService embeddingService;
    private final CentroidRepository centroidRepository;

    @Autowired
    public ClassificationService(EmbeddingService embeddingService, CentroidRepository centroidRepository) {
        this.embeddingService = embeddingService;
        this.centroidRepository = centroidRepository;
    }

    public Mono<ClassifyResponse> classify(@RequestBody ClassifyRequest request) {
        return embeddingService.embedForClassification(request.text())
                .flatMapMany(centroidRepository::findClosestCentroids)
                .collectList()
                .map(ClassifyResponse::new);
    }
}
