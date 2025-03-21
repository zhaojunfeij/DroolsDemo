package com.example.parse.refactor.processor;

import com.example.parse.refactor.converter.DrlContext;
import com.example.model.Node;
import com.example.model.Variable;
import com.alibaba.fastjson.JSON;

import java.util.Objects;

/**
 * 抽象节点处理器
 */
public abstract class AbstractNodeProcessor implements NodeProcessor {
    
    /**
     * 处理节点中的变量
     */
    protected void buildVariable(DrlContext context, Variable variable) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        String variableName = variable.getName();
        String variableNum = variable.getVariableNo();
        String variableNo = Objects.isNull(context.getVariableMap().get(variableNum)) ? 
                variableNum : context.getVariableMap().get(variableNum);

        drlBuilder.append("        // 变量: ").append(variableName).append("\n");
        drlBuilder.append("        flowContext.put(\"").append(variableNo).append("\", ");

        // 如果有表达式树，处理表达式
        if (variable.getData() != null && variable.getData().getExpressionTreeJson() != null) {
            drlBuilder.append("VariableUtils.evaluateExpression(").append("flowContext, ")
                    .append(JSON.toJSONString(variable.getData().getExpressionTreeJson())).append(")");
        } else {
            drlBuilder.append("null");
        }
        drlBuilder.append(");\n");
        drlBuilder.append("        System.out.println(\"变量取值结果");
        drlBuilder.append(variableNo).append(":\"+flowContext.get(\"").append(variableNo).append("\"));\n");
    }
    
    /**
     * 检查字符串是否为数字
     */
    protected boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 检查字符串是否为boolean
     */
    protected boolean isBoolean(String str) {
        if (str == null) {
            return false;
        }
        try {
            String lowerStr = str.toLowerCase().trim();
            return lowerStr.equals("true") || lowerStr.equals("false");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断是否为等于或不等于操作符
     */
    protected boolean isEqOrNotEq(String operator) {
        return "EQ".equals(operator) || "NOT_EQ".equals(operator);
    }
    
    /**
     * 处理下一个节点
     */
    protected void processNextNode(String currentNodeId, DrlContext context) {
        if (context.getEdgeMap().containsKey(currentNodeId)) {
            if (!context.getEdgeMap().get(currentNodeId).isEmpty()) {
                String nextNodeId = context.getEdgeMap().get(currentNodeId).get(0).getTarget();
                processNode(nextNodeId, context);
            }
        }
    }
    
    /**
     * 处理节点
     */
    protected void processNode(String nodeId, DrlContext context) {
//        if (context.getVisitedNodes().contains(nodeId)) {
//            return;
//        }
        context.getVisitedNodes().add(nodeId);
        
        Node node = context.getNodeMap().get(nodeId);
        if (node == null) {
            return;
        }
        
        // 找到对应的处理器
        NodeProcessorFactory factory = NodeProcessorFactory.getInstance();
        NodeProcessor processor = factory.getProcessor(node.getType());
        if (processor != null) {
            processor.process(node, nodeId, context);
        } else {
            context.getDrlBuilder().append("        // 未知节点类型: ").append(node.getType()).append("\n");
        }
    }
} 