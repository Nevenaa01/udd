package com.example.udd.utils;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.ml.InferTrainedModelRequest;
import co.elastic.clients.elasticsearch.ml.InferTrainedModelResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TextVectorization {
    private final ElasticsearchClient client;

    public TextVectorization(String host, int port) {
        BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "sifraElastic123"));

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, "http"))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credsProvider)
                );

        RestClient restClient = builder.build();
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.client = new ElasticsearchClient(transport);
    }

    public List<Double> vectorize(String modelId, String fieldName, String text) throws IOException {
        InferTrainedModelRequest request = InferTrainedModelRequest.of(r -> r
                .modelId(modelId)
                .docs(List.of(Map.of(fieldName, JsonData.of(text))))
        );

        InferTrainedModelResponse response = client.ml().inferTrainedModel(request);

        var result = response.inferenceResults().get(0).predictedValue();
        List<Double> vector = new ArrayList<>();

        if (result instanceof List<?> vectorList) {
            for (var element : result) {
                vector.add(element.doubleValue());
            }
        }

        return vector;
    }
}
