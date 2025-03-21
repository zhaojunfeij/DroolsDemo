package com.example.parse.refactor.processor;

import com.example.parse.refactor.converter.DrlContext;
import com.example.model.Node;
import com.example.model.NodeType;
import com.example.model.RelationShip;
import com.example.model.RelationShipGroup;
import com.example.model.Variable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * 计算节点处理器
 */
@Component
public class ComputeNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        drlBuilder.append("        // 计算节点\n");
        
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
                        buildRelationShip(drlBuilder, relationShip, node, context);
                    }
                }
            }
        }
        
        // 处理下一个节点
        processNextNode(nodeId, context);
    }
    
    private void buildRelationShip(StringBuilder drlBuilder, RelationShip relationShip, Node node, DrlContext context) {
        String operator = relationShip.getOperator();
        String operatorValueNum = relationShip.getOperatorValue();
        String operatorValue = Objects.isNull(context.getVariableMap().get(operatorValueNum)) ? 
                operatorValueNum : context.getVariableMap().get(operatorValueNum);
        String operatorValueType = relationShip.getOperatorValueType();
        String variableNum = relationShip.getVariableNo();
        String variableNo = Objects.isNull(context.getVariableMap().get(variableNum)) ? 
                variableNum : context.getVariableMap().get(variableNum);
                
        drlBuilder.append("        // 操作: ").append(operator).append("\n");
        
        String nodeIdStr = node.getId();
        if (Objects.nonNull(relationShip.getRelationshipNo())) {
            nodeIdStr = nodeIdStr.concat("_" + relationShip.getRelationshipNo());
        }
        
        drlBuilder.append("        Object " + nodeIdStr).append("=");
        drlBuilder.append("VariableUtils.performOperation(flowContext, \"")
                .append(variableNo).append("\", \"")
                .append(operator).append("\", \"")
                .append(operatorValue).append("\", \"")
                .append(operatorValueType).append("\")")
                .append(";\n");
                
        // 节点
        drlBuilder.append("        flowContext.put(\"").append(nodeIdStr).append("\", ");
        drlBuilder.append(nodeIdStr);
        drlBuilder.append(");\n");
        
        drlBuilder.append("        System.out.println(\"变量计算结果");
        drlBuilder.append(nodeIdStr).append("_result:\"+");
        drlBuilder.append(nodeIdStr).append(");\n");
    }
    
    @Override
    public String getNodeType() {
        return NodeType.COMPUTE.getType();
    }
} 