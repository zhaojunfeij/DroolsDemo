package com.example.parse.refactor.processor;

import com.example.model.*;
import com.example.parse.refactor.converter.DrlContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 判断节点处理器
 */
@Component
public class JudgeNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        drlBuilder.append("        // 判断节点\n");
        
        // 获取节点变量和关系
        List<Variable> variableList = node.getProperties().getNodeVariableList();
        // 处理变量列表
        for (Variable variable : variableList) {
            buildVariable(context, variable);
        }
        
        List<RelationShipGroup> relationShipGroupList = node.getProperties().getRelationShipGroupList();
        Map<String, Boolean> variableNameMap = new HashMap<>();  // 变量是否计算赋值，防止drools变量名重复
        
        // 处理判断逻辑
        if (relationShipGroupList != null && !relationShipGroupList.isEmpty()) {
            for (RelationShipGroup relationShipGroup : relationShipGroupList) {
                List<RelationShip> relationShipList = relationShipGroup.getRelationShipList();
                
                if (relationShipList != null && !relationShipList.isEmpty()) {
                    // 生成条件判断
                    drlBuilder.append("        // 条件分支\n");
                    
                    // 查找对应的边
                    if (context.getEdgeMap().containsKey(nodeId)) {
                        List<Edge> edges = context.getEdgeMap().get(nodeId);
                        edges.sort(Comparator.comparing(Edge::getLabel));
                        
                        for (Edge edge : edges) {
                            Integer conditionLabel = edge.getLabel();
                            if (conditionLabel != null && conditionLabel > 0) {
                                // 生成条件判断
                                generateConditionBranch(drlBuilder, relationShipList, conditionLabel, 
                                        edge.getTarget(), context, variableNameMap);
                            }
                        }
                    }
                }
            }
        } else {
            drlBuilder.append("        // 无条件判断，继续执行下一节点\n");
            // 处理下一个节点
            processNextNode(nodeId, context);
        }
    }
    
    private void generateConditionBranch(StringBuilder drlBuilder, List<RelationShip> relationShipList, 
            int conditionLabel, String targetNodeId, DrlContext context, Map<String, Boolean> variableNameMap) {
        
        for (RelationShip relationShip : relationShipList) {
            if (relationShip.getRelationshipNo() != null && 
                    Integer.parseInt(relationShip.getRelationshipNo()) == conditionLabel) {
                
                // 获取判断条件
                String variableNum = relationShip.getVariableNo();
                String variableNo = Objects.isNull(context.getVariableMap().get(variableNum)) ? 
                        variableNum : context.getVariableMap().get(variableNum);
                String operator = relationShip.getOperator();
                String operatorValue = relationShip.getOperatorValue();
                String operatorValueType = relationShip.getOperatorValueType();
                
                drlBuilder.append("        // 条件 ").append(conditionLabel).append("\n");
                // 变量取值
                String variableConvert = variableNo + "Convert";
                if (!variableNameMap.containsKey(variableNo) || !variableNameMap.get(variableNo)) {
                    drlBuilder.append("        Object ").append(variableConvert).append("= ");
                    drlBuilder.append("VariableUtils.getVariableValue(flowContext, \"").append(variableNo).append("\");\n");
                    
                    if (isNumeric(operatorValue)) {
                        drlBuilder.append("        BigDecimal ").append(variableNo).append("=new BigDecimal(")
                                .append(variableConvert).append(".toString());\n");
                    } else if (isBoolean(operatorValue)) {
                        drlBuilder.append("        Boolean ").append(variableNo).append("=(Boolean)")
                                .append(variableConvert).append(";\n");
                    } else {
                        drlBuilder.append("        String ").append(variableNo).append("=(String)")
                                .append(variableConvert).append(";\n");
                    }
                    
                    drlBuilder.append("        System.out.println(\"开始计算变量取值");
                    drlBuilder.append(variableNo).append(":\"+").append(variableNo).append(");\n");
                    variableNameMap.put(variableNo, true);
                }
                
                // 构建条件表达式
                if ("data".equals(operatorValueType)) {
                    // 变量与常量比较
                    generateConditionExpression(drlBuilder, variableNo, operator, operatorValue);
                } else if ("var".equals(operatorValueType)) {
                    // 变量与变量比较
                    generateVariableComparisonExpression(drlBuilder, variableNo, operator, operatorValue);
                }
                
                drlBuilder.append(") {\n");
                
                // 递归处理目标节点
                processNode(targetNodeId, context);
                
                drlBuilder.append("        }\n");
            }
        }
    }
    
    private void generateConditionExpression(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        // 检查是否是数字类型
        if (isNumeric(value) && !isEqOrNotEq(operator)) {
            buildNumberRuleExpress(drlBuilder, variableNo, operator, value);
        } else if (isBoolean(value)) {
            // 检查是否是布尔类型
            buildDefaultRuleExpress(drlBuilder, variableNo, operator, value);
        } else {
            buildDefaultRuleExpress(drlBuilder, variableNo, operator, value);
        }
    }
    
    private void buildDefaultRuleExpress(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        drlBuilder.append("        if (").append(variableNo);
        switch (operator) {
            case "EQ":
                drlBuilder.append(" == ");
                break;
            case "NOT_EQ":
                drlBuilder.append(" != ");
                break;
            default:
                drlBuilder.append(" ").append(operator).append(" ");
        }
        drlBuilder.append(value);
    }
    
    private void buildNumberRuleExpress(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        drlBuilder.append("        if (").append(variableNo).append(".").append("compareTo(new BigDecimal(");
        drlBuilder.append(value);
        drlBuilder.append("))");
        switch (operator) {
            case "GT":
                drlBuilder.append(" > ");
                break;
            case "GE":
                drlBuilder.append(" >= ");
                break;
            case "LT":
                drlBuilder.append(" < ");
                break;
            case "LE":
                drlBuilder.append(" <= ");
                break;
            default:
                drlBuilder.append(" ").append(operator).append(" ");
        }
        drlBuilder.append("0");
    }
    
    private void generateVariableComparisonExpression(StringBuilder drlBuilder, String variable1, String operator, String variable2) {
        drlBuilder.append("VariableUtils.compareVariables(flowContext, \"").append(variable1).append("\", \"")
                .append(operator).append("\", \"").append(variable2).append("\")");
    }
    
    @Override
    public String getNodeType() {
        return NodeType.JUDGE.getType();
    }
} 