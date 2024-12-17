package com.example.udd.controller;

import com.example.udd.model.SecurityIncident;
import com.example.udd.service.SecurityIncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/securityIncident")
public class SecurityIncidentController {
    @Autowired
    private SecurityIncidentService securityIncidentService;

    @GetMapping("/all")
    public List<SecurityIncident> getAll(){
        return securityIncidentService.getAll();
    }

    @PostMapping("/create")
    public SecurityIncident create(@RequestBody SecurityIncident securityIncident){
        return securityIncidentService.create(securityIncident);
    }
}
