package com.example.udd.model;

import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Document(indexName = "security_incident_index")
public class SecurityIncident {
    private Integer id;
    private String firstName;
    private String lastName;
    private String securityOrganizationName;
    private String attackedOrganizationName;
    private IncidentSeverity incidentSeverity;
    private String address;
}
