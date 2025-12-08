package dev.jacobandersen.emailclassifier.controller;

import dev.jacobandersen.emailclassifier.model.dto.request.ClassifyRequest;
import dev.jacobandersen.emailclassifier.model.dto.response.ClassifyResponse;
import dev.jacobandersen.emailclassifier.service.ClassificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("classify")
public class ClassificationController {
    private final ClassificationService classificationService;

    @Autowired
    public ClassificationController(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PostMapping
    public Mono<ResponseEntity<ClassifyResponse>> classify(@RequestBody ClassifyRequest request) {
        return classificationService.classify(request).map(ResponseEntity::ok);
    }
}
