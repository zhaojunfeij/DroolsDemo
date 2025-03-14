package com.example.parse.model;

/**
 * 边信息类
 */
public class EdgeInfo {
    private String sourceId;
    private String targetId;
    private int label;
    private String caseId;

    public EdgeInfo(String sourceId, String targetId, int label, String caseId) {
        this.sourceId = sourceId;
        this.targetId = targetId;
        this.label = label;
        this.caseId = caseId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
} 