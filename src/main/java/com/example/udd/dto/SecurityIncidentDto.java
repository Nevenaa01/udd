package com.example.udd.dto;

import com.example.udd.model.IncidentSeverity;

public class SecurityIncidentDto {
    public Long id;
    public String fileName;
    public String fullName;
    public String securityOrganizationName;
    public String attackedOrganizationName;
    public IncidentSeverity incidentSeverity;
    public String address;

    public SecurityIncidentDto(String fileName, String fullName, String securityOrganizationName, String attackedOrganizationName, IncidentSeverity incidentSeverity, String address) {
        this.fileName = fileName;
        this.fullName = fullName;
        this.securityOrganizationName = securityOrganizationName;
        this.attackedOrganizationName = attackedOrganizationName;
        this.incidentSeverity = incidentSeverity;
        this.address = address;
    }
}
