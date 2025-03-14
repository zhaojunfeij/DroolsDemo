package com.example.parse.node;

import com.example.parse.model.EdgeInfo;
import com.example.parse.util.DrlUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

/**
 * 判断节点处理器
 */
public class JudgeNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(StringBuilder drlBuilder, 
                      JsonNode node, 
                      String nodeId, 
                      Map<String, JsonNode> nodeMap, 
                      Map<String, List<EdgeInfo>> edgeMap, 
                      Set<String> visitedNodes,
                      Map<String, Boolean> variableNameMap) {
        drlBuilder.append("        // 判断节点\n");
        
        // 获取节点变量和关系
        JsonNode propertiesNode = node.get("properties");
        JsonNode variableListNode = propertiesNode.get("nodeVariableList");
        // 处理变量列表
        for (JsonNode variable : variableListNode) {
            DrlUtils.buildVariable(drlBuilder, variable);
        }
        
        JsonNode relationShipGroupListNode = propertiesNode.get("relationShipGroupList");
        // 处理判断逻辑
        if (relationShipGroupListNode != null && !relationShipGroupListNode.isEmpty()) {
            for (JsonNode relationShipGroup : relationShipGroupListNode) {
                JsonNode relationShipListNode = relationShipGroup.get("relationShipList");
                
                if (relationShipListNode != null && !relationShipListNode.isEmpty()) {
                    // 生成条件判断
                    drlBuilder.append("        // 条件分支\n");
                    
                    // 查找对应的边
                    if (edgeMap.containsKey(nodeId)) {
                        List<EdgeInfo> edges = edgeMap.get(nodeId);
                        edges.sort(Comparator.comparingInt(EdgeInfo::getLabel));
                        for (EdgeInfo edge : edges) {
                            int conditionLabel = edge.getLabel();
                            if (conditionLabel > 0) {
                                // 生成条件判断
                                generateConditionBranch(drlBuilder, relationShipListNode, conditionLabel, edge.getTargetId(), nodeMap, edgeMap, visitedNodes, variableNameMap);
                            }
                        }
                    }
                }
            }
        } else {
            drlBuilder.append("        // 无条件判断，继续执行下一节点\n");
            // 处理下一个节点
            processNextNode(drlBuilder, nodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
        }
    }
    
    /**
     * 生成条件分支
     */
    private void generateConditionBranch(StringBuilder drlBuilder, 
                                       JsonNode relationShipListNode, 
                                       int conditionLabel, 
                                       String targetNodeId, 
                                       Map<String, JsonNode> nodeMap, 
                                       Map<String, List<EdgeInfo>> edgeMap, 
                                       Set<String> visitedNodes,
                                       Map<String, Boolean> variableNameMap) {
        for (JsonNode relationship : relationShipListNode) {
            if (relationship.has("relationshipNo") && relationship.get("relationshipNo").asInt() == conditionLabel) {
                // 获取判断条件
                String variableNum = relationship.get("variableNo").asText();
                String variableNo = Objects.isNull(DrlUtils.getVariableName(variableNum)) ? variableNum : DrlUtils.getVariableName(variableNum);
                String operator = relationship.get("operator").asText();
                String operatorValue = relationship.get("operatorValue").asText();
                String operatorValueType = relationship.get("operatorValueType").asText();
                
                drlBuilder.append("        // 条件 ").append(conditionLabel).append("\n");
                //变量取值
                String variableConvert = variableNo + "Convert";
                if (!variableNameMap.containsKey(variableNo) || !variableNameMap.get(variableNo)) {
                    drlBuilder.append("        Object ").append(variableConvert).append("= ");
                    
                    drlBuilder.append("VariableUtils.getVariableValue(flowContext, \"").append(variableNo).append("\");\n");
                    if (DrlUtils.isNumeric(operatorValue)) {
                        drlBuilder.append("        BigDecimal ").append(variableNo).append("=new BigDecimal(").append(variableConvert).append(".toString());\n");
                    } else if (DrlUtils.isBoolean(operatorValue)) {
                        drlBuilder.append("        Boolean ").append(variableNo).append("=(Boolean)").append(variableConvert).append(";\n");
                    } else {
                        drlBuilder.append("        String ").append(variableNo).append("=(String)").append(variableConvert).append(";\n");
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
                processNodeContent(drlBuilder, targetNodeId, nodeMap, edgeMap, new HashSet<>(visitedNodes), variableNameMap);
                
                drlBuilder.append("        }\n");
            }
        }
    }
    
    @Override
    public String getNodeType() {
        return DrlUtils.JUDGE_NODE;
    }
} 