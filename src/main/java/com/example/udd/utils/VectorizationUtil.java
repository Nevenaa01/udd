package com.example.udd.utils;

import ai.djl.MalformedModelException;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Batchifier;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VectorizationUtil {
    private static final String DJL_MODEL = "sentence-transformers/all-mpnet-base-v2";

    private static final String DJL_PATH = "djl://ai.djl.huggingface.pytorch/" + DJL_MODEL;

    private static Predictor<String, float[]> predictor;

    @Autowired
    public VectorizationUtil() throws ModelNotFoundException, MalformedModelException, IOException {
        Criteria<String, float[]> criteria = Criteria.builder()
                .setTypes(String.class, float[].class)
                .optModelUrls(DJL_PATH)
                .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                .optProgress(new ProgressBar())
                .build();

        ZooModel<String, float[]> model = ModelZoo.loadModel(criteria);
        predictor = model.newPredictor();
    }

    public static double cosineSimilarity(INDArray vectorA, INDArray vectorB) {
        double dotProduct = vectorA.mul(vectorB).sumNumber().doubleValue();
        double magnitudeA = vectorA.norm2Number().doubleValue();
        double magnitudeB = vectorB.norm2Number().doubleValue();
        return dotProduct / (magnitudeA * magnitudeB);
    }

    public static float[] getEmbedding(String text) throws TranslateException {
        return predictor.predict(text);
    }
}
