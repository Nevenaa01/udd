package com.example.udd.repository;

import com.example.udd.model.SecurityIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityIncidentRepository extends JpaRepository<SecurityIncident, Long> {
}
