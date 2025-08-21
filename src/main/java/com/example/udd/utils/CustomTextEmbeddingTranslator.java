package com.example.udd.utils;

import ai.djl.huggingface.tokenizers.Encoding;
import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;

public class CustomTextEmbeddingTranslator implements Translator<String, float[]> {
    private HuggingFaceTokenizer tokenizer;

    @Override
    public void prepare(TranslatorContext translatorContext)throws Exception {
        tokenizer = HuggingFaceTokenizer.newInstance("sentence-transformers/all-mpnet-base-v2");
    }

    @Override
    public Batchifier getBatchifier() {
        return Translator.super.getBatchifier();
    }

    @Override
    public NDList processInput(TranslatorContext translatorContext, String s) throws Exception {
        Encoding encoding = tokenizer.encode(s);
        NDManager manager = translatorContext.getNDManager();

        NDArray inputIds = manager.create(encoding.getIds());
        NDArray attentionMask = manager.create(encoding.getAttentionMask());

        // 🔥 Critical: attach attention mask for use in processOutput
        translatorContext.setAttachment("attention_mask", attentionMask);

        return new NDList(inputIds, attentionMask);
    }

    @Override
    public float[] processOutput(TranslatorContext translatorContext, NDList ndList) throws Exception {
        NDArray tokenEmbeddings = ndList.get(0); // shape: [1, seq_length, hidden_size]
        NDArray attentionMask = (NDArray) translatorContext.getAttachment("attention_mask"); // shape: [1, seq_length]

        // Ensure attention mask is float and expand to [1, seq_length, 1]
        attentionMask = attentionMask.toType(DataType.FLOAT32, false).expandDims(-1);

        // Multiply: [1, seq_length, hidden_size] * [1, seq_length, 1]
        NDArray maskedEmbeddings = tokenEmbeddings.mul(attentionMask);

        // Sum over sequence (dim 1): [1, hidden_size]
        NDArray sumEmbeddings = maskedEmbeddings.sum(new int[]{1});

        // Sum attention mask over sequence (dim 1): [1, 1]
        NDArray maskSum = attentionMask.sum(new int[]{1});

        // Avoid division by zero
        maskSum = maskSum.add(1e-9);

        // Mean pooling
        NDArray meanPooled = sumEmbeddings.div(maskSum); // [1, hidden_size]

        return meanPooled.squeeze().toFloatArray(); // [hidden_size]
    }
}
