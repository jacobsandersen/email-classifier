package dev.jacobandersen.emailclassifier.repository;

import dev.jacobandersen.emailclassifier.model.dto.response.ClassifyResponse;
import dev.jacobandersen.emailclassifier.model.entity.CentroidEntity;
import dev.jacobandersen.emailclassifier.model.entity.EmbeddingEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Repository
public interface CentroidRepository extends ReactiveCrudRepository<CentroidEntity, UUID> {
    @Query(value = """
        select category, subcategory, 1 - (embedding <=> :embedding) as score
        from centroids
        order by score desc
        limit 5
    """)
    Flux<ClassifyResponse.ClassificationBucket> findClosestCentroids(@Param("embedding") float[] embedding);
}
