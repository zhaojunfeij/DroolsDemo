package com.example.parse.node;

import com.example.model.EdgeInfo;
import com.example.utils.DrlUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 抽象节点处理器
 */
public abstract class AbstractNodeProcessor implements NodeProcessor {
    
    /**
     * 处理下一个节点
     */
    protected void processNextNode(StringBuilder drlBuilder, 
                                 String currentNodeId, 
                                 Map<String, JsonNode> nodeMap, 
                                 Map<String, List<EdgeInfo>> edgeMap, 
                                 Set<String> visitedNodes,
                                 Map<String, Boolean> variableNameMap) {
        if (edgeMap.containsKey(currentNodeId)) {
            List<EdgeInfo> nextEdges = edgeMap.get(currentNodeId);
            if (!nextEdges.isEmpty()) {
                // 取第一个边（非判断节点只有一个出边）
                String nextNodeId = nextEdges.get(0).getTargetId();
                processNodeContent(drlBuilder, nextNodeId, nodeMap, edgeMap, new HashSet<>(visitedNodes), variableNameMap);
            }
        }
    }
    
    /**
     * 递归处理节点内容
     */
    protected void processNodeContent(StringBuilder drlBuilder, 
                                    String currentNodeId, 
                                    Map<String, JsonNode> nodeMap, 
                                    Map<String, List<EdgeInfo>> edgeMap, 
                                    Set<String> visitedNodes,
                                    Map<String, Boolean> variableNameMap) {
        // 避免循环
        if (visitedNodes.contains(currentNodeId)) {
            return;
        }
        visitedNodes.add(currentNodeId);
        
        JsonNode currentNode = nodeMap.get(currentNodeId);
        if (currentNode == null) {
            return;
        }
        
        String nodeType = currentNode.get("type").asText();
        
        // 获取对应的处理器处理节点
        NodeProcessor processor = NodeProcessorFactory.getProcessor(nodeType);
        processor.process(drlBuilder, currentNode, currentNodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
    }
    
    /**
     * 生成条件表达式
     */
    protected void generateConditionExpression(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        // 检查是否是数字类型
        if (DrlUtils.isNumeric(value) && !DrlUtils.isEqOrNotEq(operator)) {
            DrlUtils.buildNumberRuleExpress(drlBuilder, variableNo, operator, value);
        }
        // 检查是否是布尔类型
        else if (DrlUtils.isBoolean(value)) {
            DrlUtils.buildDefaultRuleExpress(drlBuilder, variableNo, operator, value);
        } else {
            DrlUtils.buildDefaultRuleExpress(drlBuilder, variableNo, operator, value);
        }
    }
    
    /**
     * 生成变量比较表达式
     */
    protected void generateVariableComparisonExpression(StringBuilder drlBuilder, String variable1, String operator, String variable2) {
        drlBuilder.append("VariableUtils.compareVariables(flowContext, \"").append(variable1)
                .append("\", \"").append(operator).append("\", \"").append(variable2).append("\")");
    }
} 