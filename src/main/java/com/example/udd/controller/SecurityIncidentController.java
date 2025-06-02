package com.example.udd.controller;

import com.example.udd.dto.SecurityIncidentDto;
import com.example.udd.model.SecurityIncident;
import com.example.udd.modelIndex.SecurityIncidentIndex;
import com.example.udd.service.SecurityIncidentService;
import com.example.udd.utils.MinIOUtils;
import com.example.udd.utils.PDFExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private final PDFExtractor pdfExtractor = PDFExtractor.getInstance();
    @Autowired
    private MinIOUtils minIOUtils;

    @GetMapping("/all")
    public Iterable<SecurityIncidentIndex> getAll() {
        return securityIncidentService.getAll();
    }

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> create(@RequestBody SecurityIncidentDto securityIncident) {
        SecurityIncident savedSecurityIncident = securityIncidentService.createDB(securityIncident);
        //SecurityIncidentIndex savedSecurityIncidentIndex = securityIncidentService.create(savedSecurityIncident);
        return  savedSecurityIncident != null ?
                ResponseEntity.ok("Index created successfully") :
                ResponseEntity.internalServerError().body("Error saving security incident");
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
            //clear from any previous inputs
            pdfExtractor.clearHandler();
            // save raw pdf in minio bucket
            minIOUtils.uploadPDF(file, "security-incidents");
            // save locally and parse pdf to string
            String pdfString = pdfExtractor.importPDF(file);

            SecurityIncidentDto securityIncidentDto = securityIncidentService.extractFromString(pdfString, file.getOriginalFilename());

            return ResponseEntity.ok().body(securityIncidentDto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Upload failed");
        }
    }
}
