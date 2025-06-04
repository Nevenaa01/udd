package com.example.udd.repositoryIndex;

import com.example.udd.model.IncidentSeverity;
import com.example.udd.modelIndex.SecurityIncidentIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SecurityIncidentIndexRepository extends ElasticsearchRepository<SecurityIncidentIndex, String> {
}
