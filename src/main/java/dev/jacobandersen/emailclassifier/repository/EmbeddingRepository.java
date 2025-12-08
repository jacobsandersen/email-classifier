package dev.jacobandersen.emailclassifier.repository;

import dev.jacobandersen.emailclassifier.model.entity.EmbeddingEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EmbeddingRepository extends ReactiveCrudRepository<EmbeddingEntity, UUID> {
}
