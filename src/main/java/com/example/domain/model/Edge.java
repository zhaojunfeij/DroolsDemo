package com.example.domain.model;

import lombok.Data;
import lombok.Builder;

import java.util.Map;

/**
 * 边领域模型
 */
@Data
@Builder
public class Edge {
    private String id;
    private String sourceId;
    private String targetId;
    private int label;
    private String caseId;
    private Map<String, Object> properties;
} 