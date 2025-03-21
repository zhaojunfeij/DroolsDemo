package com.example.parse.refactor.processor;

import com.example.parse.refactor.converter.DrlContext;
import com.example.model.Node;
import com.example.model.NodeType;
import com.example.model.RelationShip;
import com.example.model.RelationShipGroup;
import com.example.model.Variable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 赋值节点处理器
 */
@Component
public class AssignNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        drlBuilder.append("        // 赋值节点\n");
        
        // 处理变量列表
        List<Variable> variableList = node.getProperties().getNodeVariableList();
        if (variableList != null) {
            for (Variable variable : variableList) {
                buildVariable(context, variable);
            }
        }
        
        // 处理关系列表
        List<RelationShipGroup> relationShipGroupList = node.getProperties().getRelationShipGroupList();
        if (relationShipGroupList != null) {
            for (RelationShipGroup relationShipGroup : relationShipGroupList) {
                List<RelationShip> relationShipList = relationShipGroup.getRelationShipList();
                if (relationShipList != null) {
                    for (RelationShip relationShip : relationShipList) {
                        processRelationShip(drlBuilder, relationShip, node, context);
                    }
                }
            }
        }
        
        // 处理下一个节点
        processNextNode(nodeId, context);
    }
    
    private void processRelationShip(StringBuilder drlBuilder, RelationShip relationShip, Node node, DrlContext context) {
        String operator = relationShip.getOperator();
        
        if ("SET_RESULT".equals(operator)) {
            String variableNo = relationShip.getVariableNo();
            String operatorValue = relationShip.getOperatorValue();
            
            drlBuilder.append("        // 设置结果\n");
            drlBuilder.append("        flowContext.put(\"").append(variableNo).append("\", ");
            drlBuilder.append("\"").append(operatorValue).append("\"");
            drlBuilder.append(");\n");
        }
    }
    
    @Override
    public String getNodeType() {
        return NodeType.ASSIGN.getType();
    }
}