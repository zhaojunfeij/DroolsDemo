package com.example.model;

/**
 * 变量模型类
 */
public class Variable {
    private String name;
    private String variableNo;
    private VariableData data;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVariableNo() {
        return variableNo;
    }
    
    public void setVariableNo(String variableNo) {
        this.variableNo = variableNo;
    }
    
    public VariableData getData() {
        return data;
    }
    
    public void setData(VariableData data) {
        this.data = data;
    }
    
    /**
     * 变量数据
     */
    public static class VariableData {
        private String expressionTreeJson;
        
        public String getExpressionTreeJson() {
            return expressionTreeJson;
        }
        
        public void setExpressionTreeJson(String expressionTreeJson) {
            this.expressionTreeJson = expressionTreeJson;
        }
    }
} 