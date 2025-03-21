package com.example.model;

/**
 * 节点类型枚举
 */
public enum NodeType {
    START("start"),
    END("end"),
    JUDGE("judge"),
    COMPUTE("compute"),
    ASSIGN("assign"),
    RULE("rule");
    
    private final String type;
    
    NodeType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }
    
    public static NodeType fromString(String text) {
        for (NodeType nodeType : NodeType.values()) {
            if (nodeType.type.equalsIgnoreCase(text)) {
                return nodeType;
            }
        }
        throw new IllegalArgumentException("未知节点类型: " + text);
    }
} 