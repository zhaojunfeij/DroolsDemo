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
 * 支持多组条件逻辑与单组条件逻辑的灵活组合
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
     * 处理关系组列表
     * 支持多组条件的复杂组合
     */
    private void processRelationshipGroups(Node node, String nodeId, DrlContext context) {
        List<RelationShipGroup> relationShipGroups = node.getProperties().getRelationShipGroupList();
        StringBuilder drlBuilder = context.getDrlBuilder();

        if (relationShipGroups != null && !relationShipGroups.isEmpty()) {
            // 先构建所有组的关系表达式
            relationShipGroups.forEach(group -> 
                buildRelationships(group.getRelationShipList(), node, context));
            
            // 开始构建条件判断语句
            drlBuilder.append("       if (");
            
            // 处理多个关系组之间的条件组合
            IntStream.range(0, relationShipGroups.size()).forEach(i -> {
                RelationShipGroup group = relationShipGroups.get(i);
                
                // 添加组的开始括号
                drlBuilder.append("(");
                
                // 构建组内条件
                buildGroupConditions(group.getRelationShipList(), node, drlBuilder);
                
                // 添加组的结束括号
                drlBuilder.append(")");
                
                // 添加组间关系运算符 (默认为 AND)
                if (i < relationShipGroups.size() - 1) {
                    String groupOperator = group.getOperator() != null ? group.getOperator() : "AND";
                    if ("AND".equals(groupOperator)) {
                        drlBuilder.append(" && ");
                    } else if ("OR".equals(groupOperator)) {
                        drlBuilder.append(" || ");
                    }
                }
            });
            
            drlBuilder.append(") {\n");
            
            // 处理满足条件时的下一个节点
            processNextNode(nodeId, context);
            
            // 添加else分支
            drlBuilder.append("       } else {\n");
            drlBuilder.append("           // 条件不满足，直接返回\n");
            drlBuilder.append("           return;\n");
            drlBuilder.append("       }\n");
        } else {
            // 没有条件组，直接过渡到下一个节点
            processNextNode(nodeId, context);
        }
    }

    /**
     * 构建组内条件表达式
     */
    private void buildGroupConditions(List<RelationShip> relationships, Node node, StringBuilder drlBuilder) {
        if (relationships == null || relationships.isEmpty()) {
            // 空组默认为true
            drlBuilder.append("true");
            return;
        }
        
        IntStream.range(0, relationships.size()).forEach(i -> {
            RelationShip relationship = relationships.get(i);
            String nodeIdStr = buildNodeId(node.getId(), relationship.getRelationshipNo());

            drlBuilder.append("(boolean)flowContext.get(\"").append(nodeIdStr).append("\")");

            // 添加组内关系运算符
            if (i < relationships.size() - 1) {
                String relationOperator = relationship.getRelationOperator() != null ? 
                                         relationship.getRelationOperator() : "AND";
                appendRelationOperator(drlBuilder, relationOperator);
            }
        });
    }

    /**
     * 构建所有关系
     */
    private void buildRelationships(List<RelationShip> relationships, Node node, DrlContext context) {
        if (relationships != null) {
            relationships.forEach(relationship ->
                buildRelationShip(context.getDrlBuilder(), relationship, node, context));
        }
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
        } else {
            // 默认使用AND
            drlBuilder.append(" && ");
        }
    }

    /**
     * 构建节点ID
     */
    private String buildNodeId(String nodeId, String relationshipNo) {
        if (Objects.nonNull(relationshipNo)) {
            return nodeId.concat("_" + relationshipNo).replaceAll("-", "_");
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
        drlBuilder.append("        boolean ").append(nodeIdStr).append("=(boolean)");
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
        drlBuilder.append("        System.out.println(\"变量计算结果 ");
        drlBuilder.append(nodeIdStr).append("_result: \"+");
        drlBuilder.append(nodeIdStr).append(");\n");
    }

    @Override
    public String getNodeType() {
        return NodeType.RULE.getType();
    }
}