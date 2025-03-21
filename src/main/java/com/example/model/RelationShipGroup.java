package com.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 关系组模型类
 */
public class RelationShipGroup {
    private String operator;
    private List<RelationShip> relationShipList = new ArrayList<>();

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<RelationShip> getRelationShipList() {
        return relationShipList;
    }

    public void setRelationShipList(List<RelationShip> relationShipList) {
        this.relationShipList = relationShipList;
    }
}