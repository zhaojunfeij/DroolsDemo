package com.example.model;

/**
 * 关系模型类
 */
public class RelationShip {
    private String relationshipNo;
    private String variableNo;
    private String operator;
    private String operatorValue;
    private String operatorValueType;
    private String variableType;
    private String relationOperator;

    public String getRelationshipNo() {
        return relationshipNo;
    }

    public void setRelationshipNo(String relationshipNo) {
        this.relationshipNo = relationshipNo;
    }

    public String getVariableNo() {
        return variableNo;
    }

    public void setVariableNo(String variableNo) {
        this.variableNo = variableNo;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperatorValue() {
        return operatorValue;
    }

    public void setOperatorValue(String operatorValue) {
        this.operatorValue = operatorValue;
    }

    public String getOperatorValueType() {
        return operatorValueType;
    }

    public void setOperatorValueType(String operatorValueType) {
        this.operatorValueType = operatorValueType;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public String getRelationOperator() {
        return relationOperator;
    }

    public void setRelationOperator(String relationOperator) {
        this.relationOperator = relationOperator;
    }
}