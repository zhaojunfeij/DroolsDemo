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
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * 规则节点处理器 - 负责处理规则节点的DRL代码生成
 * 遵循单一职责原则，专注于规则条件判断逻辑的转换
 */
@Component
public class RuleNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        // 添加规则节点注释
        drlBuilder.append("       //规则节点: ").append(node.getName()).append("\n");
        
        // 处理节点变量
        processNodeVariables(node, context);
        
        // 处理关系判断条件
        processRelationshipGroups(node, nodeId, context);
    }
    
    /**
     * 处理节点变量列表
     */
    private void processNodeVariables(Node node, DrlContext context) {
        Optional.ofNullable(node.getProperties().getNodeVariableList())
                .ifPresent(variables -> variables.forEach(variable -> buildVariable(context, variable)));
    }
    
    /**
     * 处理关系组
     */
    private void processRelationshipGroups(Node node, String nodeId, DrlContext context) {
        List<RelationShipGroup> relationShipGroups = node.getProperties().getRelationShipGroupList();
        
        if (relationShipGroups != null && !relationShipGroups.isEmpty()) {
            // 处理AND/OR组合条件
            for (RelationShipGroup group : relationShipGroups) {
                processRelationshipGroup(group, node, nodeId, context);
            }
        } else {
            // 没有条件组，直接过渡到下一个节点
            processNextNode(nodeId, context);
        }
    }
    
    /**
     * 处理单个关系组
     */
    private void processRelationshipGroup(RelationShipGroup group, Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        String groupOperator = group.getOperator() != null ? group.getOperator() : "AND";
        List<RelationShip> relationships = group.getRelationShipList();
        
        if (relationships != null && !relationships.isEmpty()) {
            // 构建关系条件
            buildRelationships(relationships, node, context);
            
            // 添加条件组注释
            drlBuilder.append("       // 条件组 (").append(groupOperator).append(")\n");
            
            // 构建条件判断语句
            buildConditionStatement(relationships, node, drlBuilder);
            
            // 处理下一个节点
            processNextNode(nodeId, context);
            
            // 添加else分支
            addElseBranch(drlBuilder);
        } else {
            // 没有条件，直接过渡到下一个节点
            processNextNode(nodeId, context);
        }
    }
    
    /**
     * 构建所有关系
     */
    private void buildRelationships(List<RelationShip> relationships, Node node, DrlContext context) {
        relationships.forEach(relationship -> 
            buildRelationShip(context.getDrlBuilder(), relationship, node, context));
    }
    
    /**
     * 构建条件判断语句
     */
    private void buildConditionStatement(List<RelationShip> relationships, Node node, StringBuilder drlBuilder) {
        drlBuilder.append("       if (");
        
        IntStream.range(0, relationships.size()).forEach(i -> {
            RelationShip relationship = relationships.get(i);
            String nodeIdStr = buildNodeId(node.getId(), relationship.getRelationshipNo());
            
            drlBuilder.append("flowContext.get(\"").append(nodeIdStr).append("\")");
            
            // 添加关系运算符
            if (i < relationships.size() - 1) {
                appendRelationOperator(drlBuilder, relationship.getRelationOperator());
            }
        });
        
        drlBuilder.append("){\n");
    }
    
    /**
     * 添加关系运算符
     */
    private void appendRelationOperator(StringBuilder drlBuilder, String relationOperator) {
        if (relationOperator != null) {
            if ("AND".equals(relationOperator)) {
                drlBuilder.append(" && ");
            } else if ("OR".equals(relationOperator)) {
                drlBuilder.append(" || ");
            }
        }
    }
    
    /**
     * 添加else分支
     */
    private void addElseBranch(StringBuilder drlBuilder) {
        drlBuilder.append("       } else {\n");
        drlBuilder.append("       // 条件不满足，直接返回\n");
        drlBuilder.append("       return;\n");
        drlBuilder.append("       }\n");
    }
    
    /**
     * 构建节点ID
     */
    private String buildNodeId(String nodeId, String relationshipNo) {
        if (Objects.nonNull(relationshipNo)) {
            return nodeId.concat("_" + relationshipNo);
        }
        return nodeId;
    }
    
    /**
     * 构建关系的DRL代码
     */
    private void buildRelationShip(StringBuilder drlBuilder, RelationShip relationShip, Node node, DrlContext context) {
        // 获取操作相关参数
        String operator = relationShip.getOperator();
        String operatorValueNum = relationShip.getOperatorValue();
        String operatorValue = getContextValue(context, operatorValueNum);
        String operatorValueType = relationShip.getOperatorValueType();
        String variableNum = relationShip.getVariableNo();
        String variableNo = getContextValue(context, variableNum);
        
        // 生成操作注释
        drlBuilder.append("        // 操作: ").append(operator).append("\n");
        
        // 构建节点ID
        String nodeIdStr = buildNodeId(node.getId(), relationShip.getRelationshipNo());
        
        // 生成计算操作代码
        drlBuilder.append("        Object ").append(nodeIdStr).append("=");
        drlBuilder.append("VariableUtils.performOperation(flowContext, \"")
                .append(variableNo).append("\", \"")
                .append(operator).append("\", \"")
                .append(operatorValue).append("\", \"")
                .append(operatorValueType).append("\")")
                .append(";\n");
        
        // 将结果放入上下文
        drlBuilder.append("        flowContext.put(\"").append(nodeIdStr).append("\", ");
        drlBuilder.append(nodeIdStr);
        drlBuilder.append(");\n");
        
        // 生成日志代码
        drlBuilder.append("        System.out.println(\"变量计算结果");
        drlBuilder.append(nodeIdStr).append("_result:\"+");
        drlBuilder.append(nodeIdStr).append(");\n");
    }
    
    /**
     * 获取上下文中的变量值，如果不存在则返回原值
     */
    private String getContextValue(DrlContext context, String key) {
        return Objects.isNull(context.getVariableMap().get(key)) ? key : context.getVariableMap().get(key);
    }
    
    @Override
    public String getNodeType() {
        return NodeType.RULE.getType();
    }
}