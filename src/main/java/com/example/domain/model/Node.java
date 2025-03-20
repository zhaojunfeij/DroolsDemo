package com.example.domain.model;

import lombok.Data;
import lombok.Builder;
import java.util.Map;

/**
 * 节点领域模型
 */
@Data
@Builder
public class Node {
    private String id;
    private String type;
    private String name;
    private Map<String, Object> properties;
    private Position position;
    
    @Data
    @Builder
    public static class Position {
        private int x;
        private int y;
    }
} 