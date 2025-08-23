package com.example.udd.dto;

import com.example.udd.model.IncidentSeverity;
import com.example.udd.modelIndex.SecurityIncidentIndex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityIncidentDto {
    public Long id;
    public String fileName;
    public String fullName;
    public String securityOrganizationName;
    public String attackedOrganizationName;
    public IncidentSeverity incidentSeverity;
    public String address;
    public String pdfContent;
    public Map<String, List<String>> highlitedValues = new HashMap<>();

    public SecurityIncidentDto(String fileName, String fullName, String securityOrganizationName, String attackedOrganizationName, IncidentSeverity incidentSeverity, String address, String pdfContent) {
        this.fileName = fileName;
        this.fullName = fullName;
        this.securityOrganizationName = securityOrganizationName;
        this.attackedOrganizationName = attackedOrganizationName;
        this.incidentSeverity = incidentSeverity;
        this.address = address;
        this.pdfContent = pdfContent;
    }

    public SecurityIncidentDto(String fileName, String fullName, String securityOrganizationName, String attackedOrganizationName, IncidentSeverity incidentSeverity, String address, String pdfContent, Map<String, List<String>> highlitedValues) {
        this.fileName = fileName;
        this.fullName = fullName;
        this.securityOrganizationName = securityOrganizationName;
        this.attackedOrganizationName = attackedOrganizationName;
        this.incidentSeverity = incidentSeverity;
        this.address = address;
        this.pdfContent = pdfContent;
        this.highlitedValues = highlitedValues;
    }

    public SecurityIncidentDto(SecurityIncidentIndex index){
        this.fullName = index.getFullName();
        this.securityOrganizationName = index.getSecurityOrganizationName();
        this.attackedOrganizationName = index.getAttackedOrganizationName();
        this.incidentSeverity = ConvertToIncidentSeverity(index.getIncidentSeverity());
        this.address = index.getLocation().toString();
        this.pdfContent = index.getPdfContent();
    }

    public IncidentSeverity ConvertToIncidentSeverity(String severity){
        switch (severity){
            case "LOW":
                return IncidentSeverity.LOW;
            case "MEDIUM":
                return IncidentSeverity.MEDIUM;
            case "HIGH":
                return IncidentSeverity.HIGH;
            case "CRITICAL":
                return IncidentSeverity.CRITICAL;
            default:
                return null;
        }
    }
}
