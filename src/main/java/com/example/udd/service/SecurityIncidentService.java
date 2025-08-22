package com.example.udd.service;

import com.example.udd.dto.SecurityIncidentDto;
import com.example.udd.model.IncidentSeverity;
import com.example.udd.model.SecurityIncident;
import com.example.udd.modelIndex.SecurityIncidentIndex;
import com.example.udd.repository.SecurityIncidentRepository;
import com.example.udd.repositoryIndex.SecurityIncidentIndexRepository;
import com.example.udd.utils.VectorizationUtil;
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
        int contentStartIndex = pdfDocument.indexOf("Content:");

        String fullName = pdfDocument.substring(fullNameIndex + 11, SONameStartIndex - 3);
        String securityOrganizationName = pdfDocument.substring(SONameStartIndex + 28, AONameStartIndex - 3);
        String attackedOrganizationName = pdfDocument.substring(AONameStartIndex + 28, incidentSeverityStartIndex - 3);
        IncidentSeverity incidentSeverity = stringToIncidentSeverity(pdfDocument.substring(incidentSeverityStartIndex + 19, addressStartIndex - 3));
        String address = pdfDocument.substring(addressStartIndex + 9, endSquareBracketsIndex - 1);
        String content = pdfDocument.substring(contentStartIndex + 9, pdfDocument.length() - 3).replace("\n", "").replace("\r", "");

        return new SecurityIncidentDto(fileName,
                fullName,
                securityOrganizationName,
                attackedOrganizationName,
                incidentSeverity,
                address,
                content);
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

        securityIncidentDto.id = savedIncident.getId();
        // Log to file
        logToFile(securityIncidentDto);

        return savedIncident;
    }

    private void logToFile(SecurityIncidentDto securityIncident){
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        String logMessage = String.format("Attacked Org: %s where Security Org: %s with Member of incident response team: %s and severity: %s, on location: %f,%f, id in database: %d, pdfContent: %s",
                securityIncident.attackedOrganizationName,
                securityIncident.securityOrganizationName,
                securityIncident.fullName,
                securityIncident.incidentSeverity.toString(),
                45.2671,
                19.8335,
                //securityIncident.getLocation(),
                securityIncident.id,
                securityIncident.pdfContent);

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
}
