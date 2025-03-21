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

/**
 * 计算节点处理器 - 负责处理计算类型节点的DRL代码生成
 * 遵循单一职责原则，专注于计算节点的业务逻辑转换
 */
@Component
public class ComputeNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        drlBuilder.append("        // 计算节点\n");
        
        // 处理变量列表
        processVariables(node, context);
        
        // 处理关系列表
        processRelationships(node, context);
        
        // 处理下一个节点
        processNextNode(nodeId, context);
    }
    
    /**
     * 处理节点中的变量列表
     * 优化点：使用Optional避免空指针检查，使用函数式编程风格处理集合
     */
    private void processVariables(Node node, DrlContext context) {
        Optional.ofNullable(node.getProperties().getNodeVariableList())
                .ifPresent(variables -> variables.forEach(variable -> buildVariable(context, variable)));
    }
    
    /**
     * 处理节点中的关系列表
     * 优化点：使用Optional避免多层嵌套，提高代码可读性
     */
    private void processRelationships(Node node, DrlContext context) {
        Optional.ofNullable(node.getProperties().getRelationShipGroupList())
                .ifPresent(groups -> groups.forEach(group -> 
                    Optional.ofNullable(group.getRelationShipList())
                            .ifPresent(relationships -> relationships.forEach(
                                    relationship -> buildRelationShip(context.getDrlBuilder(), relationship, node, context)
                            ))
                ));
    }
    
    /**
     * 构建关系的DRL代码
     * 优化点：方法职责清晰，代码分段有序，提高可读性
     */
    private void buildRelationShip(StringBuilder drlBuilder, RelationShip relationShip, Node node, DrlContext context) {
        // 获取操作相关参数
        String operator = relationShip.getOperator();
        String operatorValue = getContextValue(context, relationShip.getOperatorValue());
        String operatorValueType = relationShip.getOperatorValueType();
        String variableNo = getContextValue(context, relationShip.getVariableNo());
        
        // 生成操作注释
        drlBuilder.append("        // 操作: ").append(operator).append("\n");
        
        // 构建节点ID
        String nodeIdStr = buildNodeId(node, relationShip);
        
        // 生成计算操作代码
        buildOperationCode(drlBuilder, nodeIdStr, variableNo, operator, operatorValue, operatorValueType);
        
        // 将结果放入上下文并记录日志
        addResultToContext(drlBuilder, nodeIdStr);
    }

    /**
     * 构建节点ID
     * 优化点：抽取方法让构建逻辑清晰可见
     */
    private String buildNodeId(Node node, RelationShip relationShip) {
        String nodeId = node.getId();
        if (Objects.nonNull(relationShip.getRelationshipNo())) {
            return nodeId.concat("_" + relationShip.getRelationshipNo());
        }
        return nodeId;
    }
    
    /**
     * 构建操作代码
     * 优化点：抽取方法降低复杂度
     */
    private void buildOperationCode(StringBuilder drlBuilder, String nodeIdStr, String variableNo, 
                                   String operator, String operatorValue, String operatorValueType) {
        drlBuilder.append("        Object ").append(nodeIdStr).append("=");
        drlBuilder.append("VariableUtils.performOperation(flowContext, \"")
                .append(variableNo).append("\", \"")
                .append(operator).append("\", \"")
                .append(operatorValue).append("\", \"")
                .append(operatorValueType).append("\")")
                .append(";\n");
    }
    
    /**
     * 将计算结果添加到上下文并记录日志
     * 优化点：抽取方法让职责单一
     */
    private void addResultToContext(StringBuilder drlBuilder, String nodeIdStr) {
        // 将结果放入上下文
        drlBuilder.append("        flowContext.put(\"").append(nodeIdStr).append("\", ");
        drlBuilder.append(nodeIdStr);
        drlBuilder.append(");\n");
        
        // 生成日志代码
        drlBuilder.append("        System.out.println(\"变量计算结果");
        drlBuilder.append(nodeIdStr).append("_result:\"+");
        drlBuilder.append(nodeIdStr).append(");\n");
    }
    
    @Override
    public String getNodeType() {
        return NodeType.COMPUTE.getType();
    }
} 