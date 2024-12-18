package com.example.udd.model;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = "security_incident_index")
public class SecurityIncident {
    private String id;
    private String firstName;
    private String lastName;
    private String securityOrganizationName;
    private String attackedOrganizationName;
    private IncidentSeverity incidentSeverity;
    private String address;

    public SecurityIncident() {
    }

    public SecurityIncident(String id, String firstName, String lastName, String securityOrganizationName, String attackedOrganizationName, IncidentSeverity incidentSeverity, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.securityOrganizationName = securityOrganizationName;
        this.attackedOrganizationName = attackedOrganizationName;
        this.incidentSeverity = incidentSeverity;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSecurityOrganizationName() {
        return securityOrganizationName;
    }

    public void setSecurityOrganizationName(String securityOrganizationName) {
        this.securityOrganizationName = securityOrganizationName;
    }

    public String getAttackedOrganizationName() {
        return attackedOrganizationName;
    }

    public void setAttackedOrganizationName(String attackedOrganizationName) {
        this.attackedOrganizationName = attackedOrganizationName;
    }

    public IncidentSeverity getIncidentSeverity() {
        return incidentSeverity;
    }

    public void setIncidentSeverity(IncidentSeverity incidentSeverity) {
        this.incidentSeverity = incidentSeverity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
