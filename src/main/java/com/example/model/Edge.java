package com.example.model;

/**
 * 边模型类
 */
public class Edge {
    private String id;
    private String source;
    private String target;
    private Integer label;
    private EdgeProperties properties;
    
    // 默认构造函数
    public Edge() {
    }
    
    // 添加用于替代EdgeInfo的构造函数
    public Edge(String source, String target, int label, String decisionCase) {
        this.source = source;
        this.target = target;
        this.label = label;
        
        if (decisionCase != null && !decisionCase.isEmpty()) {
            this.properties = new EdgeProperties();
            this.properties.setDecisionCase(decisionCase);
        }
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getTarget() {
        return target;
    }
    
    public void setTarget(String target) {
        this.target = target;
    }
    
    public Integer getLabel() {
        return label;
    }
    
    public void setLabel(Integer label) {
        this.label = label;
    }
    
    public EdgeProperties getProperties() {
        return properties;
    }
    
    public void setProperties(EdgeProperties properties) {
        this.properties = properties;
    }
    
    /**
     * 边属性
     */
    public static class EdgeProperties {
        private String decisionCase;
        
        public String getDecisionCase() {
            return decisionCase;
        }
        
        public void setDecisionCase(String decisionCase) {
            this.decisionCase = decisionCase;
        }
    }
} 