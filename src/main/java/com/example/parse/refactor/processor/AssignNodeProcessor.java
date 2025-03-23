package com.example.parse.refactor.processor;

import com.example.model.Node;
import com.example.model.NodeType;
import com.example.model.RelationShip;
import com.example.model.RelationShipGroup;
import com.example.parse.refactor.converter.DrlContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 赋值节点处理器 - 处理流程中的变量赋值操作
 * 遵循单一职责原则，只负责赋值节点的DRL规则生成
 */
@Component
public class AssignNodeProcessor extends AbstractNodeProcessor {

    private static final String OPERATOR_SET_RESULT = "SET_RESULT";

    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        drlBuilder.append("        // 赋值节点\n");

        // 处理变量列表
        processVariables(node, context);

        // 处理关系列表和赋值操作
        processRelationships(node, context);

        // 处理下一个节点
        processNextNode(nodeId, context);
    }

    /**
     * 处理节点中的变量列表
     */
    private void processVariables(Node node, DrlContext context) {
        Optional.ofNullable(node.getProperties().getNodeVariableList())
                .ifPresent(variables ->
                        variables.forEach(variable -> buildVariable(context, variable)));
    }

    /**
     * 处理节点中的关系列表
     */
    private void processRelationships(Node node, DrlContext context) {
        Optional.ofNullable(node.getProperties().getRelationShipGroupList())
                .ifPresent(groups ->
                        groups.stream()
                                .map(RelationShipGroup::getRelationShipList)
                                .filter(list -> list != null && !list.isEmpty())
                                .flatMap(List::stream)
                                .forEach(relation -> processRelationShip(context.getDrlBuilder(), relation)));
    }

    /**
     * 处理单个关系定义，生成对应的DRL代码
     * 目前支持SET_RESULT操作，可扩展支持更多操作类型
     */
    private void processRelationShip(StringBuilder drlBuilder, RelationShip relationShip) {
        String operator = relationShip.getOperator();

        if (OPERATOR_SET_RESULT.equals(operator)) {
            generateSetResultCode(drlBuilder, relationShip);
        }
        // 可扩展支持更多操作类型...
    }

    /**
     * 生成设置结果的DRL代码
     */
    private void generateSetResultCode(StringBuilder drlBuilder, RelationShip relationShip) {
        String variableNo = relationShip.getVariableNo();
        String operatorValue = relationShip.getOperatorValue();
        String operatorValueType = relationShip.getOperatorValueType();//data node
        drlBuilder.append("        // 设置结果\n")
                .append("        flowContext.put(\"").append(variableNo).append("\", ")
                .append(operatorValue);
        if (operatorValueType.equals("node")) {
            drlBuilder.append("_0");
        }
        drlBuilder
                .append(");\n");
    }

    @Override
    public String getNodeType() {
        return NodeType.ASSIGN.getType();
    }
}