package com.example.udd.repository;

import com.example.udd.model.SecurityIncident;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityIncidentRepository extends ElasticsearchRepository<SecurityIncident, String> {
}
