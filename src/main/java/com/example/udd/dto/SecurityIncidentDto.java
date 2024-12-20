package com.example.udd.dto;

import com.example.udd.model.IncidentSeverity;

public class SecurityIncidentDto {
    public Long id;
    public String fullName;
    public String securityOrganizationName;
    public String attackedOrganizationName;
    public IncidentSeverity incidentSeverity;
    public String address;
}
