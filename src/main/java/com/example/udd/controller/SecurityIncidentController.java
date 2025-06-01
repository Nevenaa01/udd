package com.example.udd.controller;

import com.example.udd.dto.SecurityIncidentDto;
import com.example.udd.modelIndex.SecurityIncidentIndex;
import com.example.udd.service.SecurityIncidentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

    @PostMapping("/uploadPDF")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> uploadPDF(@RequestParam("pdf") MultipartFile file){
        try {
            // Example: Save file, parse it, or log metadata
            String originalName = file.getOriginalFilename();
            byte[] content = file.getBytes();

            // Do something with the file...

            return ResponseEntity.ok().body("File uploaded: " + originalName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }
}
