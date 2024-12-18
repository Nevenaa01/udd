package com.example.udd.service;

import com.example.udd.model.SecurityIncident;
import com.example.udd.repository.SecurityIncidentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SecurityIncidentService {
    @Autowired
    private SecurityIncidentRepository securityIncidentRepository;

    public Iterable<SecurityIncident> getAll() {
        /*Page<SecurityIncident> page = securityIncidentRepository.findAll(PageRequest.of(0, 10));
        return page.getContent();*/
        return securityIncidentRepository.findAll();
    }

    public SecurityIncident create(SecurityIncident securityIncident){
        return securityIncidentRepository.save(securityIncident);
    }

    public SecurityIncident update(SecurityIncident securityIncident){
        return securityIncidentRepository.save(securityIncident);
    }

    public void deleteById(String id){
        securityIncidentRepository.deleteById(id);
    }
}
