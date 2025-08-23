package com.example.udd.service.interfaces;

import com.example.udd.dto.SecurityIncidentDto;
import com.example.udd.modelIndex.SecurityIncidentIndex;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ISearchService {
    List<SecurityIncidentDto> search(List<String> keywords, String type);
}
