package dev.jacobandersen.emailclassifier.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("embeddings")
public class EmbeddingEntity {
    @Id
    private UUID id;

    private String category;

    private String subcategory;

    private String content;

    private float[] embedding;

    public EmbeddingEntity(String category, String subcategory, String content, float[] embedding) {
        this.category = category;
        this.subcategory = subcategory;
        this.content = content;
        this.embedding = embedding;
    }

    public UUID getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}

