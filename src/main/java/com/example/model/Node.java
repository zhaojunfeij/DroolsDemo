package com.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程节点抽象基类
 */
public class Node {
    protected String id;
    protected String type;
    protected String name;
    protected Properties properties;
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
    
    /**
     * 节点属性类
     */
    public static class Properties {
        private List<Variable> nodeVariableList = new ArrayList<>();
        private List<RelationShipGroup> relationShipGroupList = new ArrayList<>();
        private List<ResultSetting> resultSettingList = new ArrayList<>();
        
        public List<Variable> getNodeVariableList() {
            return nodeVariableList;
        }
        
        public void setNodeVariableList(List<Variable> nodeVariableList) {
            this.nodeVariableList = nodeVariableList;
        }
        
        public List<RelationShipGroup> getRelationShipGroupList() {
            return relationShipGroupList;
        }
        
        public void setRelationShipGroupList(List<RelationShipGroup> relationShipGroupList) {
            this.relationShipGroupList = relationShipGroupList;
        }
        
        public List<ResultSetting> getResultSettingList() {
            return resultSettingList;
        }
        
        public void setResultSettingList(List<ResultSetting> resultSettingList) {
            this.resultSettingList = resultSettingList;
        }
    }
} 