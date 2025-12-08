package dev.jacobandersen.emailclassifier.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("centroids")
public class CentroidEntity {
    @Id
    private UUID id;

    private String category;

    private String subcategory;

    private float[] embedding;

    public CentroidEntity(String category, String subcategory, float[] embedding) {
        this.category = category;
        this.subcategory = subcategory;
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

    public float[] getEmbedding() {
        return embedding;
    }

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }
}

