package com.example.udd.service;

import com.example.udd.dto.SecurityIncidentDto;
import com.example.udd.model.IncidentSeverity;
import com.example.udd.model.SecurityIncident;
import com.example.udd.modelIndex.SecurityIncidentIndex;
import com.example.udd.repository.SecurityIncidentRepository;
import com.example.udd.repositoryIndex.SecurityIncidentIndexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.Severity;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class SecurityIncidentService {
    @Autowired
    private SecurityIncidentIndexRepository securityIncidentIndexRepository;
    @Autowired
    private SecurityIncidentRepository securityIncidentRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    @Autowired
    public SecurityIncidentService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public Iterable<SecurityIncidentIndex> getAll() {
        /*Page<SecurityIncident> page = securityIncidentRepository.findAll(PageRequest.of(0, 10));
        return page.getContent();*/
        return securityIncidentIndexRepository.findAll();
    }

    public SecurityIncidentDto extractFromString(String pdfDocument, String fileName){
        int fullNameIndex = pdfDocument.lastIndexOf("Full name: ");
        int SONameStartIndex = pdfDocument.indexOf("Security organization name: ");
        int AONameStartIndex = pdfDocument.indexOf("Attacked organization name: ");
        int incidentSeverityStartIndex = pdfDocument.indexOf("Incident severity: ");
        int addressStartIndex = pdfDocument.indexOf("Address: ");
        int endSquareBracketsIndex = pdfDocument.lastIndexOf("]]");

        String fullName = pdfDocument.substring(fullNameIndex + 11, SONameStartIndex - 3);
        String securityOrganizationName = pdfDocument.substring(SONameStartIndex + 28, AONameStartIndex - 3);
        String attackedOrganizationName = pdfDocument.substring(AONameStartIndex + 28, incidentSeverityStartIndex - 3);
        IncidentSeverity incidentSeverity = stringToIncidentSeverity(pdfDocument.substring(incidentSeverityStartIndex + 19, addressStartIndex - 3));
        String address = pdfDocument.substring(addressStartIndex + 9, endSquareBracketsIndex - 1);

        return new SecurityIncidentDto(fileName,
                fullName,
                securityOrganizationName,
                attackedOrganizationName,
                incidentSeverity,
                address);
    }

    private IncidentSeverity stringToIncidentSeverity(String incidentSeverity){
        return switch (incidentSeverity.toLowerCase()) {
            case "low" -> IncidentSeverity.LOW;
            case "medium" -> IncidentSeverity.MEDIUM;
            case "high" -> IncidentSeverity.HIGH;
            case "critical" -> IncidentSeverity.CRITICAL;
            default -> null;
        };
    }

    public SecurityIncident create(SecurityIncidentDto securityIncidentDto){
        SecurityIncident securityIncident = new SecurityIncident(securityIncidentDto.fileName,
                securityIncidentDto.fullName,
                securityIncidentDto.securityOrganizationName,
                securityIncidentDto.attackedOrganizationName,
                securityIncidentDto.incidentSeverity,
                securityIncidentDto.address);

        SecurityIncident savedIncident = securityIncidentRepository.save(securityIncident);

        // Log to file
        logToFile(savedIncident);

        return savedIncident;
    }

    private void logToFile(SecurityIncident securityIncident){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        String logMessage = String.format("Attacked Org: %s where Security Org: %s with Member of incident response team: %s and severity: %s, on location: 45.2671,19.8335, id in database: %d",
                securityIncident.getAttackedOrganizationName(),
                securityIncident.getSecurityOrganizationName(),
                securityIncident.getFullName(),
                securityIncident.getIncidentSeverity(),
                //securityIncident.getLocation(),
                securityIncident.getId());

        try {
            String projectRoot = System.getProperty("user.dir");
            Path logDir = Paths.get(projectRoot, "elk", "logstash", "logstash-ingest-data");
            Files.createDirectories(logDir); // Create folders if they don't exist

            Path logFilePath = logDir.resolve("application.log");

            // Append log entry
            Files.write(logFilePath, (logMessage + System.lineSeparator()).getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<SecurityIncidentIndex> search(String input, String tpyeOfSearch){
        Criteria criteria;

        switch (tpyeOfSearch){
            case "fullNameAndSeverity":
                criteria = new Criteria("full_name").contains(input.toLowerCase())
                        .or(new Criteria("incident_severity").contains(input.toUpperCase()));

                break;
            case "organizationsName":
                criteria = new Criteria("security_organization_name").contains(input.toLowerCase())
                        .or(new Criteria("attacked_organization_name").contains(input.toLowerCase()));
                break;

            default:
                return null;
        }

        Query query = new CriteriaQuery(criteria);
        return elasticsearchOperations
                .search(query, SecurityIncidentIndex.class)
                .stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }
}
