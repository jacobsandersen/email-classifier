package dev.jacobandersen.emailclassifier.model.dto.request;

public record GenerateEmbeddingRequest(String category, String subcategory, String content) {
}
