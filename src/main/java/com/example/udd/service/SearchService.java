package com.example.udd.service;

import ai.djl.translate.TranslateException;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;

import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import com.example.udd.dto.SecurityIncidentDto;
import com.example.udd.exceptionHandling.exception.MalformedQueryException;
import com.example.udd.modelIndex.*;
import com.example.udd.modelIndex.AST.Node;
import com.example.udd.modelIndex.AST.OperatorNode;
import com.example.udd.modelIndex.AST.TermNode;
import com.example.udd.service.interfaces.ISearchService;
import com.example.udd.utils.Parser;
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
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService implements ISearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    @Autowired
    private RestClient restClient;

    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "\\(|\\)|AND|OR|NOT|\\w+:\"[^\"]+\"|\\w+:[^\\s()]+|\"[^\"]+\"|[^\\s()]+"
    );

    public SearchService(ElasticsearchOperations elasticsearchOperations){
        this.elasticsearchOperations = elasticsearchOperations;
    }

    @Override
    public List<SecurityIncidentDto> search(List<String> keywords, String typeOfSearch) {
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

        if(keywords.contains("OR") || keywords.contains("AND") || keywords.contains("NOT"))
            typeOfSearch = "combinedBooleanSemiStructured";

        List<HighlightField> highlightFields = new ArrayList<>();
        highlightFields.add(new HighlightField("full_name"));
        highlightFields.add(new HighlightField("security_organization_name"));
        highlightFields.add(new HighlightField("attacked_organization_name"));
        highlightFields.add(new HighlightField("incident_severity"));
        highlightFields.add(new HighlightField("pdf_content"));

        HighlightParameters params = HighlightParameters.builder()
                .withPreTags("<em class=\"highlight\">")
                .withPostTags("</em>")
                .build();

        NativeQueryBuilder searchQueryBuilder = new NativeQueryBuilder()
                .withQuery(buildSimpleSearchQuery(keywords, typeOfSearch))
                .withHighlightQuery(new HighlightQuery(new Highlight(params, highlightFields), SecurityIncidentIndex.class)
                );

        return runQuery(searchQueryBuilder.build());
    }

    private Query buildSimpleSearchQuery(List<String> tokens, String typeOfSearch){
        switch(typeOfSearch){
            case "simple":
                return BoolQuery.of(q -> q.should(mb -> mb.bool(b -> {
                            tokens.forEach(token -> {
                                //fullNameAndSeverity
                                b.should(sb -> sb.match(m -> m.field("full_name").fuzziness(Fuzziness.AUTO.asString()).query(token)));
                                b.should(sb -> sb.term(m -> m.field("incident_severity").value(token.toUpperCase())));
                                //organizationsName
                                b.should(sb -> sb.match(m -> m.field("security_organization_name").fuzziness(Fuzziness.AUTO.asString()).query(token)));
                                b.should(sb -> sb.match(m -> m.field("attacked_organization_name").fuzziness(Fuzziness.AUTO.asString()).query(token)));
                                //pdf_content
                                b.should(sb -> sb.match(m -> m.field("pdf_content").fuzziness(Fuzziness.AUTO.asString()).query(token)));
                            });
                            return b;
                        })
                ))._toQuery();
            case "combinedBooleanSemiStructured":
                //one full expression has 2 operands and an operator
                if(tokens.size() < 3){
                    throw new MalformedQueryException("Search query malformed");
                }

                List<String> boolTokens = tokenize(Strings.join(tokens, " "));
                Parser parser = new Parser(boolTokens);
                Node ast = parser.parse(); //root node
                return buildQueryFromNode(ast);
            default:
                return null;
        }
    }

    private List<SecurityIncidentDto> runQuery(NativeQuery searchQuery) {
        var searchHits = elasticsearchOperations.search(
                searchQuery,
                SecurityIncidentIndex.class,
                IndexCoordinates.of("security_incident_index")
        );

        List<SecurityIncidentDto> result = new ArrayList<>();
        for(var hit : searchHits){
            SecurityIncidentIndex entity = hit.getContent();
            SecurityIncidentDto dto = new SecurityIncidentDto(entity);

            dto.databaseId = (long) entity.getDatabaseId();
            dto.incidentSeverityString = dto.incidentSeverity.toString();

            var highlights = hit.getHighlightFields();
            if(highlights != null && !highlights.isEmpty()){
                replaceValueForField(dto, highlights);
            }

            result.add(dto);
        }

        return result;
    }

    private void replaceValueForField(SecurityIncidentDto dto, Map<String, List<String>> highlights){
        for(var entry : highlights.entrySet()){
            String value = "";
            for(var valueEntry : entry.getValue()){
                value += valueEntry;
            }

            switch (entry.getKey()){
                case "fullName":
                    dto.fullName = value;
                    break;
                case "securityOrganizationName":
                    dto.securityOrganizationName = value;
                    break;
                case "attackedOrganizationName":
                    dto.attackedOrganizationName = value;
                    break;
                case "pdfContent":
                    dto.pdfContent = value;
                    break;
                case "incidentSeverity":
                    dto.incidentSeverityString = value;
                default:
                    break;
            }
        }
    }

    private List<SecurityIncidentDto> knnSearch(float[] vectors) throws Exception {
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

        List<SecurityIncidentDto> securityIncidents = new ArrayList<>();
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

            SecurityIncidentDto dto = new SecurityIncidentDto(incident);
            dto.databaseId = (long) incident.getDatabaseId();
            dto.incidentSeverityString = incident.getIncidentSeverity().toString();

            securityIncidents.add(dto);
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

    private Query buildQueryFromNode(Node node) {
        if (node instanceof TermNode term) {
            if (term.isPhrase) {
                if(term.field != null) {
                    return Query.of(q -> q.matchPhrase(m -> m
                            .field(term.field)
                            .query(term.value)
                    ));
                }
                else{
                    return Query.of(q -> q.multiMatch(m -> m
                            .fields("full_name",
                                    "security_organization_name",
                                    "attacked_organization_name",
                                    "incident_severity",
                                    "pdf_content"
                            )
                            .query(term.value)
                            .type(TextQueryType.Phrase)
                    ));
                }
            } else {
                if(term.field != null) {
                    return Query.of(q -> q.match(m -> m
                            .field(term.field)
                            .query(term.value)
                            .fuzziness(Fuzziness.ONE.asString())
                    ));
                } else{
                    return Query.of(q -> q.multiMatch(m -> m
                            .fields("full_name",
                                    "security_organization_name",
                                    "attacked_organization_name",
                                    "incident_severity",
                                    "pdf_content"
                            )
                            .query(term.value)
                            .fuzziness(Fuzziness.ONE.asString())
                    ));
                }
            }
        } else if (node instanceof OperatorNode op) {
            return Query.of(q -> q.bool(b -> {
                for (Node child : op.children) {
                    Query childQuery = buildQueryFromNode(child); // recursively build
                    switch (op.operator) {
                        case "AND" -> b.must(childQuery);   // pass Query directly
                        case "OR"  -> b.should(childQuery);
                        case "NOT" -> b.mustNot(childQuery);
                    }
                }
                return b;
            }));
        }
        throw new IllegalStateException("Unknown node type");
    }

    public static List<String> tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        while (matcher.find()) {
            tokens.add(matcher.group());
        }
        return tokens;
    }
}
