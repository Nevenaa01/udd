package com.example.udd.service;

import com.example.udd.dto.SecurityIncidentDto;
import com.example.udd.modelIndex.SecurityIncidentIndex;
import com.example.udd.repositoryIndex.SecurityIncidentIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecurityIncidentService {
    @Autowired
    private SecurityIncidentIndexRepository securityIncidentIndexRepository;

    public Iterable<SecurityIncidentIndex> getAll() {
        /*Page<SecurityIncident> page = securityIncidentRepository.findAll(PageRequest.of(0, 10));
        return page.getContent();*/
        return securityIncidentIndexRepository.findAll();
    }

    public SecurityIncidentIndex create(SecurityIncidentDto securityIncident) {
        SecurityIncidentIndex securityIncidentIndex = new SecurityIncidentIndex(securityIncident.id.toString(),
                securityIncident.fullName,
                securityIncident.securityOrganizationName,
                securityIncident.attackedOrganizationName,
                securityIncident.incidentSeverity,
                0,
                null);

        return securityIncidentIndexRepository.save(securityIncidentIndex);
    }

    public SecurityIncidentIndex update(SecurityIncidentDto securityIncident) {
        SecurityIncidentIndex securityIncidentIndex = new SecurityIncidentIndex(securityIncident.id.toString(),
                securityIncident.fullName,
                securityIncident.securityOrganizationName,
                securityIncident.attackedOrganizationName,
                securityIncident.incidentSeverity,
                0,
                null);

        return securityIncidentIndexRepository.save(securityIncidentIndex);
    }

    public void deleteById(String id) {
        securityIncidentIndexRepository.deleteById(id);
    }
}
