package com.example.udd.modelIndex;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class VectorizedContent {
    @Field(type = FieldType.Dense_Vector, dims = 768, similarity = "cosine")
    private float[] predicted_value;

    public float[] getPredicted_value() {
        return predicted_value;
    }

    public void setPredicted_value(float[] predicted_value) {
        this.predicted_value = predicted_value;
    }
}
