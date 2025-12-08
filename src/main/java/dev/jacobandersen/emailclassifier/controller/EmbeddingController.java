package dev.jacobandersen.emailclassifier.controller;

import dev.jacobandersen.emailclassifier.model.dto.request.GenerateEmbeddingRequest;
import dev.jacobandersen.emailclassifier.service.EmbeddingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("embedding")
public class EmbeddingController {
    private final EmbeddingService embedService;

    @Autowired
    public EmbeddingController(EmbeddingService embedService) {
        this.embedService = embedService;
    }

    @PostMapping("generate")
    public Mono<ResponseEntity<Void>> generate(@RequestBody GenerateEmbeddingRequest request) {
        return embedService.enqueueEmbedding(request).thenReturn(ResponseEntity.ok().build());
    }

    @PostMapping("generateMany")
    public Mono<ResponseEntity<List<GenerateEmbeddingRequest>>> generateMany(@RequestBody List<GenerateEmbeddingRequest> requests) {
        return embedService.enqueueBatch(requests).map(ResponseEntity::ok);
    }
}
