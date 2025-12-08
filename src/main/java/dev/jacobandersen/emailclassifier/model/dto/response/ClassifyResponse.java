package dev.jacobandersen.emailclassifier.model.dto.response;

import java.util.List;

public record ClassifyResponse(List<ClassificationBucket> buckets) {
   public record ClassificationBucket(String category, String subcategory, Double score) {
   }
}
