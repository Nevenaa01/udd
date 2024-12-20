package com.example.udd.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Id;

@Entity
@Table(name = "securityIncident")
public class SecurityIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String securityOrganizationName;
    private String attackedOrganizationName;
    private IncidentSeverity incidentSeverity;
    private String address;

    public SecurityIncident() {
    }

    public SecurityIncident(Long id, String fullName, String securityOrganizationName, String attackedOrganizationName, IncidentSeverity incidentSeverity, String address) {
        this.id = id;
        this.fullName = fullName;
        this.securityOrganizationName = securityOrganizationName;
        this.attackedOrganizationName = attackedOrganizationName;
        this.incidentSeverity = incidentSeverity;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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
