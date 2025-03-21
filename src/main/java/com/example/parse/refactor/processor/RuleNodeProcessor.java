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
 * 规则节点处理器
 */
@Component
public class RuleNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        drlBuilder.append("       //规则节点: ").append(node.getName()).append("\n");
        
        // 获取节点变量
        List<Variable> nodeVariables = node.getProperties().getNodeVariableList();
        for (Variable variable : nodeVariables) {
            buildVariable(context, variable);
        }
        
        // 获取关系判断条件
        List<RelationShipGroup> relationShipGroups = node.getProperties().getRelationShipGroupList();
        if (relationShipGroups != null && !relationShipGroups.isEmpty()) {
            // 处理AND/OR组合条件
            for (RelationShipGroup group : relationShipGroups) {
                String groupOperator = group.getOperator() != null ? group.getOperator() : "AND";
                List<RelationShip> relationships = group.getRelationShipList();
                
                if (relationships != null && !relationships.isEmpty()) {
                    for (RelationShip relationship : relationships) {
                        buildRelationShip(drlBuilder, relationship, node, context);
                    }
                    
                    drlBuilder.append("       // 条件组 (").append(groupOperator).append(")\n");
                    // 根据条件结果决定流程
                    drlBuilder.append("       if (");
                    
                    for (int i = 0; i < relationships.size(); i++) {
                        RelationShip relationship = relationships.get(i);
                        String nodeIdStr = node.getId();
                        if (Objects.nonNull(relationship.getRelationshipNo())) {
                            nodeIdStr = nodeIdStr.concat("_" + relationship.getRelationshipNo());
                        }
                        
                        drlBuilder.append("flowContext.get(\"");
                        drlBuilder.append(nodeIdStr).append("\")");
                        
                        String relationOperator = relationship.getRelationOperator();
                        // 只有当不是最后一个元素，且有关系运算符时才添加
                        if (i < relationships.size() - 1 && relationOperator != null) {
                            if ("AND".equals(relationOperator)) {
                                drlBuilder.append(" && ");
                            } else if ("OR".equals(relationOperator)) {
                                drlBuilder.append(" || ");
                            }
                        }
                    }
                    
                    drlBuilder.append("){\n");
                    
                    // 处理下一个节点
                    processNextNode(nodeId, context);
                    
                    drlBuilder.append("       } else {\n");
                    drlBuilder.append("       // 条件不满足，直接返回\n");
                    drlBuilder.append("       return;\n");
                    drlBuilder.append("       }\n");
                } else {
                    // 没有条件，直接过渡到下一个节点
                    processNextNode(nodeId, context);
                }
            }
        } else {
            // 没有条件组，直接过渡到下一个节点
            processNextNode(nodeId, context);
        }
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
        return NodeType.RULE.getType();
    }
}