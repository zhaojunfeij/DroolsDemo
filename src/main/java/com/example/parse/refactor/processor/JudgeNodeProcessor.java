package com.example.parse.refactor.processor;

import com.example.model.*;
import com.example.parse.refactor.converter.DrlContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 判断节点处理器 - 负责生成判断节点的DRL规则代码
 */
@Component
public class JudgeNodeProcessor extends AbstractNodeProcessor {
    
    private static final String OPERATOR_TYPE_DATA = "data";
    private static final String OPERATOR_TYPE_VAR = "var";
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        drlBuilder.append("        // 判断节点\n");
        
        // 处理节点变量
        processNodeVariables(node, context);
        
        // 处理判断逻辑和条件分支
        List<RelationShipGroup> relationShipGroups = node.getProperties().getRelationShipGroupList();
        if (hasRelationships(relationShipGroups)) {
            processRelationships(nodeId, relationShipGroups, context);
        } else {
            drlBuilder.append("        // 无条件判断，继续执行下一节点\n");
            processNextNode(nodeId, context);
        }
    }
    
    /**
     * 处理节点变量列表
     */
    private void processNodeVariables(Node node, DrlContext context) {
        List<Variable> variables = node.getProperties().getNodeVariableList();
        for (Variable variable : variables) {
            buildVariable(context, variable);
        }
    }
    
    /**
     * 检查是否存在关系定义
     */
    private boolean hasRelationships(List<RelationShipGroup> relationShipGroups) {
        return relationShipGroups != null && !relationShipGroups.isEmpty() && 
               relationShipGroups.get(0).getRelationShipList() != null && 
               !relationShipGroups.get(0).getRelationShipList().isEmpty();
    }
    
    /**
     * 处理关系组和条件分支
     */
    private void processRelationships(String nodeId, List<RelationShipGroup> relationShipGroups, DrlContext context) {
        Map<String, Boolean> variableNameMap = new HashMap<>();
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        for (RelationShipGroup group : relationShipGroups) {
            List<RelationShip> relationships = group.getRelationShipList();
            if (relationships == null || relationships.isEmpty()) {
                continue;
            }
            
            drlBuilder.append("        // 条件分支\n");
            List<Edge> edges = getEdgesForNode(nodeId, context);
            
            if (edges.isEmpty()) {
                continue;
            }
            
            // 按标签排序边
            edges.sort(Comparator.comparing(Edge::getLabel));
            
            // 为每个有效边生成条件分支
            for (Edge edge : edges) {
                Integer conditionLabel = edge.getLabel();
                if (conditionLabel != null && conditionLabel > 0) {
                    generateConditionBranch(drlBuilder, relationships, conditionLabel, 
                            edge.getTarget(), context, variableNameMap);
                }
            }
        }
    }
    
    /**
     * 获取节点的所有边
     */
    private List<Edge> getEdgesForNode(String nodeId, DrlContext context) {
        Map<String, List<Edge>> edgeMap = context.getEdgeMap();
        return edgeMap.getOrDefault(nodeId, Collections.emptyList());
    }
    
    /**
     * 生成条件分支代码
     */
    private void generateConditionBranch(StringBuilder drlBuilder, List<RelationShip> relationShipList, 
            int conditionLabel, String targetNodeId, DrlContext context, Map<String, Boolean> variableNameMap) {
        
        relationShipList.stream()
                .filter(relation -> matchesConditionLabel(relation, conditionLabel))
                .forEach(relation -> buildConditionBranch(relation, drlBuilder, conditionLabel, 
                        targetNodeId, context, variableNameMap));
    }
    
    /**
     * 检查关系是否匹配条件标签
     */
    private boolean matchesConditionLabel(RelationShip relation, int conditionLabel) {
        String relationshipNo = relation.getRelationshipNo();
        return relationshipNo != null && Integer.parseInt(relationshipNo) == conditionLabel;
    }
    
    /**
     * 构建单个条件分支
     */
    private void buildConditionBranch(RelationShip relation, StringBuilder drlBuilder, 
            int conditionLabel, String targetNodeId, DrlContext context, Map<String, Boolean> variableNameMap) {
        
        // 获取判断条件参数
        String variableId = relation.getVariableNo();
        String variableNo = getMappedVariableName(context.getVariableMap(), variableId);
        String operator = relation.getOperator();
        String operatorValue = relation.getOperatorValue();
        String operatorValueType = relation.getOperatorValueType();
        
        drlBuilder.append("        // 条件 ").append(conditionLabel).append("\n");
        
        // 处理变量取值
        processVariableValue(drlBuilder, variableNo, operatorValue, variableNameMap);
        
        // 构建条件表达式
        if (OPERATOR_TYPE_DATA.equals(operatorValueType)) {
            // 变量与常量比较
            generateConditionExpression(drlBuilder, variableNo, operator, operatorValue);
        } else if (OPERATOR_TYPE_VAR.equals(operatorValueType)) {
            // 变量与变量比较
            generateVariableComparisonExpression(drlBuilder, variableNo, operator, operatorValue);
        }
        
        drlBuilder.append(") {\n");
        
        // 递归处理目标节点
        processNode(targetNodeId, context);
        
        drlBuilder.append("        }\n");
    }
    
    /**
     * 获取映射后的变量名
     */
    private String getMappedVariableName(Map<String, String> variableMap, String variableId) {
        return variableMap.containsKey(variableId) ? variableMap.get(variableId) : variableId;
    }
    
    /**
     * 处理变量取值逻辑
     */
    private void processVariableValue(StringBuilder drlBuilder, String variableNo, 
            String operatorValue, Map<String, Boolean> variableNameMap) {
        
        if (variableNameMap.containsKey(variableNo) && variableNameMap.get(variableNo)) {
            return; // 已处理过的变量，跳过
        }
        
        // 变量取值
        String variableConvert = variableNo + "Convert";
        drlBuilder.append("        Object ").append(variableConvert).append(" = ")
                .append("VariableUtils.getVariableValue(flowContext, \"").append(variableNo).append("\");\n");
        
        // 根据操作值类型确定变量类型
        if (isNumeric(operatorValue)) {
            drlBuilder.append("        BigDecimal ").append(variableNo).append(" = new BigDecimal(")
                    .append(variableConvert).append(".toString());\n");
        } else if (isBoolean(operatorValue)) {
            drlBuilder.append("        Boolean ").append(variableNo).append(" = (Boolean)")
                    .append(variableConvert).append(";\n");
        } else {
            drlBuilder.append("        String ").append(variableNo).append(" = (String)")
                    .append(variableConvert).append(";\n");
        }
        
        drlBuilder.append("        System.out.println(\"开始计算变量取值")
                .append(variableNo).append(": \" + ").append(variableNo).append(");\n");
                
        // 标记变量已处理
        variableNameMap.put(variableNo, true);
    }
    
    /**
     * 生成条件表达式
     */
    private void generateConditionExpression(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        if (isNumeric(value) && !isEqOrNotEq(operator)) {
            buildNumberRuleExpress(drlBuilder, variableNo, operator, value);
        } else {
            buildDefaultRuleExpress(drlBuilder, variableNo, operator, value);
        }
    }
    
    /**
     * 构建默认规则表达式
     */
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
        
        // 如果是字符串且不是布尔值，需要添加引号
        if (!isNumeric(value) && !isBoolean(value)) {
            drlBuilder.append("\"").append(value).append("\"");
        } else {
            drlBuilder.append(value);
        }
    }
    
    /**
     * 构建数字类型规则表达式
     */
    private void buildNumberRuleExpress(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        drlBuilder.append("        if (").append(variableNo).append(".compareTo(new BigDecimal(")
                .append(value).append("))");
                
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
    
    /**
     * 生成变量比较表达式
     */
    private void generateVariableComparisonExpression(StringBuilder drlBuilder, String variable1, String operator, String variable2) {
        drlBuilder.append("        if (VariableUtils.compareVariables(flowContext, \"")
                .append(variable1).append("\", \"")
                .append(operator).append("\", \"")
                .append(variable2).append("\")");
    }
    
    @Override
    public String getNodeType() {
        return NodeType.JUDGE.getType();
    }
} 