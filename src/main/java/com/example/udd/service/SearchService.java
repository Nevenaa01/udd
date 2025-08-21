package com.example.udd.service;

import ai.djl.translate.TranslateException;
import co.elastic.clients.elasticsearch._types.KnnQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.KnnQuery;

import com.example.udd.modelIndex.SecurityIncidentIndex;
import com.example.udd.service.interfaces.ISearchService;
import com.example.udd.utils.TextVectorization;
import com.example.udd.utils.VectorizationUtil;
import joptsimple.internal.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.common.unit.Fuzziness;
import org.springframework.data.domain.Page;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService implements ISearchService {
    private final ElasticsearchOperations elasticsearchOperations;

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
                System.out.println("Vector: " + vector1);

                var vector = VectorizationUtil.getEmbedding(Strings.join(keywords, " "));

                double norm = 0;
                for (float v : vector) norm += v * v;
                norm = Math.sqrt(norm);
                System.out.println("Vector norm: " + norm);

                return knnSearch(new float[]{0.0283f, 0.0418f, -0.027f, -0.0485f, 0.0569f, 0.0425f, -0.0499f, 0.0023f, -0.0304f, 0.0277f, -0.055f, -0.0162f, 0.0057f, -0.0531f, 0.0008f, 0.0077f, 0.0012f, 0.0105f, -0.0047f, 0.0025f, -0.0026f, -0.041f, 0.0194f, 0.0158f, 0.0404f, -0.0111f, 0.0063f, 0.0233f, 0.0306f, -0.0395f, 0.0507f, 0.0302f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
                });
            } catch (TranslateException e) {
                System.out.println(e.getMessage());
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
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

    private List<SecurityIncidentIndex> knnSearch(float[] queryVector){
        /*Float[] floatObjects = new Float[queryVector.length];

        for(int i = 0; i < queryVector.length; i++){
            floatObjects[i] = queryVector[i];
        }

        List<Float> floatList = Arrays.stream(floatObjects).collect(Collectors.toList());

        var knnQuery = new KnnQuery.Builder()
                .field("vectorizedContent.predicted_values")
                .queryVector(floatList)
                .numCandidates(100)
                .k(10)
                .boost(10.0f)
                .build();

        *//*var searchQuery = NativeQuery.builder()
                .withQuery(knnQuery._toQuery())
                .withMaxResults(5)
                .withSearchType(null)
                .build();*//*

        var searchQuery = NativeQuery.builder()
                .withKnnQuery(knnQuery)
                .withMaxResults(5)
                .withSearchType(null) // obavezno treba imati
                .build();

        SearchHits<SecurityIncidentIndex> searchHits = elasticsearchOperations.search(searchQuery, SecurityIncidentIndex.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());*/
        return null;
    }
}
