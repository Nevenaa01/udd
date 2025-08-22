package com.example.udd.modelIndex;

import jakarta.persistence.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Document(indexName = "security_incident_index")
@Setting(settingPath = "/configuration/analyzerConfig.json")
public class SecurityIncidentIndex {
    @Id
    private String id;

    @Field(type = FieldType.Text, store = true, name = "full_name", analyzer = "serbian_simple", searchAnalyzer = "serbian_simple")
    private String fullName;
    @Field(type = FieldType.Text, store = true, name = "security_organization_name", analyzer = "serbian_simple", searchAnalyzer = "serbian_simple")
    private String securityOrganizationName;
    @Field(type = FieldType.Text, store = true, name = "attacked_organization_name", analyzer = "serbian_simple", searchAnalyzer = "serbian_simple")
    private String attackedOrganizationName;
    @Field(type = FieldType.Keyword, store = true, name = "incident_severity")
    private String incidentSeverity;
    @Field(type = FieldType.Integer, store = true, name = "database_id")
    private Integer databaseId;
    @Field(store = true, name = "location")
    private GeoPoint location;
    @Field(type = FieldType.Object)
    private VectorizedContent vectorizedContent;
    @Field(type = FieldType.Text, store = true, name = "pdf_content", analyzer = "serbian_simple", searchAnalyzer = "serbian_simple")
    private String pdfContent;

    public SecurityIncidentIndex() {
    }

    public SecurityIncidentIndex(String id, String fullName, String securityOrganizationName, String attackedOrganizationName, String incidentSeverity, Integer databaseId, GeoPoint location, VectorizedContent vectorizedContent) {
        this.id = id;
        this.fullName = fullName;
        this.securityOrganizationName = securityOrganizationName;
        this.attackedOrganizationName = attackedOrganizationName;
        this.incidentSeverity = incidentSeverity;
        this.databaseId = databaseId;
        this.location = location;
        this.vectorizedContent = vectorizedContent;
    }

    public SecurityIncidentIndex(String fullName, String securityOrganizationName, String attackedOrganizationName, String incidentSeverity, Integer databaseId, GeoPoint location, VectorizedContent vectorizedContent, String pdfContent) {
        this.fullName = fullName;
        this.securityOrganizationName = securityOrganizationName;
        this.attackedOrganizationName = attackedOrganizationName;
        this.incidentSeverity = incidentSeverity;
        this.databaseId = databaseId;
        this.location = location;
        this.vectorizedContent = vectorizedContent;
        this.pdfContent = pdfContent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getIncidentSeverity() {
        return incidentSeverity;
    }

    public void setIncidentSeverity(String incidentSeverity) {
        this.incidentSeverity = incidentSeverity;
    }

    public Integer getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(Integer databaseId) {
        this.databaseId = databaseId;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public VectorizedContent getVectorizedContent() {
        return vectorizedContent;
    }

    public void setVectorizedContent(VectorizedContent vectorizedContent) {
        this.vectorizedContent = vectorizedContent;
    }

    public String getPdfContent() {
        return pdfContent;
    }

    public void setPdfContent(String pdfContent) {
        this.pdfContent = pdfContent;
    }
}
