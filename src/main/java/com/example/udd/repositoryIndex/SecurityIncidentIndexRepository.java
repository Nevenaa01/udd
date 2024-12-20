package com.example.udd.repositoryIndex;

import com.example.udd.modelIndex.SecurityIncidentIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityIncidentIndexRepository extends ElasticsearchRepository<SecurityIncidentIndex, String> {
}
