package com.example.udd.service;

import ai.djl.translate.TranslateException;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.KnnQuery;

import com.example.udd.modelIndex.SecurityIncidentIndex;
import com.example.udd.modelIndex.VectorizedContent;
import com.example.udd.service.interfaces.ISearchService;
import com.example.udd.utils.TextVectorization;
import com.example.udd.utils.VectorizationUtil;
import joptsimple.internal.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.nd4j.shade.jackson.databind.JsonNode;
import org.nd4j.shade.jackson.databind.ObjectMapper;
import org.nd4j.shade.jackson.databind.ObjectWriter;
import org.nd4j.shade.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService implements ISearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private RestClient restClient;

    public SearchService(ElasticsearchOperations elasticsearchOperations){
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public List<SecurityIncidentIndex> search(List<String> keywords, String typeOfSearch) {
        if(typeOfSearch.equals("knn")){
            try {
                TextVectorization vectorizer = new TextVectorization("localhost", 9200);
                List<Double> vector1 = vectorizer.vectorize(
                        "sentence-transformers__all-mpnet-base-v2",
                        "text_field",
                        Strings.join(keywords, " ")
                );
                System.out.println("Vector1: " + vector1);

                var vector = VectorizationUtil.getEmbedding(Strings.join(keywords, " "));

                return knnSearch(vector);
            } catch (TranslateException e) {
                System.out.println(e.getMessage());
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        NativeQueryBuilder searchQueryBuilder = new NativeQueryBuilder().withQuery(buildSimpleSearchQuery(keywords, typeOfSearch));
        return runQuery(searchQueryBuilder.build());
    }

    private Query buildSimpleSearchQuery(List<String> tokens, String typeOfSearch){
        switch(typeOfSearch){
            case "fullNameAndSeverity":
                return BoolQuery.of(q -> q.should(mb -> mb.bool(b -> {
                            tokens.forEach(token -> {
                                b.should(sb -> sb.match(m -> m.field("full_name").fuzziness(Fuzziness.AUTO.asString()).query(token)));
                                b.should(sb -> sb.term(m -> m.field("incident_severity").value(token.toUpperCase())));
                            });
                            return b;
                        })
                ))._toQuery();
            case "organizationsName":
                return BoolQuery.of(q -> q.should(mb -> mb.bool(b -> {
                            tokens.forEach(token -> {
                                b.should(sb -> sb.match(m -> m.field("security_organization_name").fuzziness(Fuzziness.AUTO.asString()).query(token)));
                                b.should(sb -> sb.match(m -> m.field("attacked_organization_name").fuzziness(Fuzziness.AUTO.asString()).query(token)));
                            });
                            return b;
                        })
                ))._toQuery();
            case "fullTextPDF":

            default:
                return null;
        }
    }

    private List<SecurityIncidentIndex> runQuery(NativeQuery searchQuery) {

        var searchHits = elasticsearchOperations.search(
                searchQuery,
                SecurityIncidentIndex.class,
                IndexCoordinates.of("security_incident_index")
        );

        var searchHitsPaged = SearchHitSupport.searchPageFor(searchHits, searchQuery.getPageable());
        var resultPage = (Page<SecurityIncidentIndex>) SearchHitSupport.unwrapSearchHits(searchHitsPaged);

        return resultPage.getContent();
    }

    private List<SecurityIncidentIndex> knnSearch(float[] vectors) throws Exception {
        String endpoint = "/security_incident_index/_search";

        Map<String, Object> knnQuery = new HashMap<>();
        knnQuery.put("field", "vectorizedContent.predicted_value");
        knnQuery.put("k", 5);
        knnQuery.put("num_candidates", 10);

        List<Double> vectorValues = new ArrayList<>();
        for (var vector : vectors) {
            vectorValues.add((double) vector);
        }

        knnQuery.put("query_vector", vectorValues);

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("knn", knnQuery);

        //Serialize the request payload as JSON
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        ObjectWriter objectWriter = objectMapper.writer();
        String payloadJSON = objectWriter.writeValueAsString(requestPayload);

        Request request = new Request("POST", endpoint);
        request.setJsonEntity(payloadJSON);

        Response response = restClient.performRequest(request);

        //get hits
        List<JsonNode> sources = extractSources(response);

        List<SecurityIncidentIndex> securityIncidents = new ArrayList<>();
        for (JsonNode source : sources) {
            JsonNode parentVectorNode = source.get("vectorizedContent");
            JsonNode vectorNode = parentVectorNode.get("predicted_value");

            float[] vectorArray = new float[vectorNode.size()];
            for (int i = 0; i < vectorNode.size(); i++) {
                vectorArray[i] = vectorNode.get(i).floatValue();
            }

            VectorizedContent vectorizedContent = new VectorizedContent();
            vectorizedContent.setPredicted_value(vectorArray);

            SecurityIncidentIndex incident = new SecurityIncidentIndex(
                    source.get("full_name").asText(),
                    source.get("security_organization_name").asText(),
                    source.get("attacked_organization_name").asText(),
                    source.get("incident_severity").asText(),
                    source.get("database_id").asInt(),
                    new GeoPoint(source.get("lat").asDouble(), source.get("lon").asDouble()),
                    vectorizedContent,
                    source.get("pdf_content").asText()
            );

            securityIncidents.add(incident);
        }

        return securityIncidents;
    }

    public List<JsonNode> extractSources(Response response) throws Exception {
        List<JsonNode> list = new ArrayList<>();

        // Convert response entity to String
        String responseBody = EntityUtils.toString(response.getEntity());

        // Parse JSON
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(responseBody);

        // Navigate to hits array
        JsonNode hits = root.path("hits").path("hits");

        // Iterate over hits and collect _source
        if (hits.isArray()) {
            for (JsonNode hit : hits) {
                JsonNode source = hit.path("_source");
                if (!source.isMissingNode()) {
                    list.add(source);
                }
            }
        }

        return list;
    }
}
