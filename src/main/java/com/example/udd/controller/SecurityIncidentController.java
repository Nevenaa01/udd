package com.example.udd.controller;

import com.example.udd.dto.SecurityIncidentDto;
import com.example.udd.modelIndex.SecurityIncidentIndex;
import com.example.udd.service.SecurityIncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/securityIncident")
public class SecurityIncidentController {
    @Autowired
    private SecurityIncidentService securityIncidentService;

    @GetMapping("/all")
    public Iterable<SecurityIncidentIndex> getAll() {
        return securityIncidentService.getAll();
    }

    @PostMapping()
    public SecurityIncidentIndex create(@RequestBody SecurityIncidentDto securityIncident) {
        return securityIncidentService.create(securityIncident);
    }

    @PutMapping()
    public SecurityIncidentIndex update(@RequestBody SecurityIncidentDto securityIncident) {
        return securityIncidentService.update(securityIncident);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable String id) {
        securityIncidentService.deleteById(id);
    }
}
