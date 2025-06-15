package com.example.udd.modelIndex;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class VectorizedContent {
    @Field(type = FieldType.Dense_Vector, dims = 768, similarity = "cosine")
    private float[] predicted_value;
}
