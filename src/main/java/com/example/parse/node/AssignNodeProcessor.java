package com.example.parse.node;

import com.example.parse.model.EdgeInfo;
import com.example.parse.util.DrlUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 赋值节点处理器
 */
public class AssignNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(StringBuilder drlBuilder, 
                      JsonNode node, 
                      String nodeId, 
                      Map<String, JsonNode> nodeMap, 
                      Map<String, List<EdgeInfo>> edgeMap, 
                      Set<String> visitedNodes,
                      Map<String, Boolean> variableNameMap) {
        drlBuilder.append("        // 赋值节点\n");
        
        // 获取节点变量和关系
        JsonNode propertiesNode = node.get("properties");
        if (propertiesNode.has("nodeVariableList") && propertiesNode.has("relationShipGroupList")) {
            JsonNode variableListNode = propertiesNode.get("nodeVariableList");
            JsonNode relationShipGroupListNode = propertiesNode.get("relationShipGroupList");
            
            // 处理变量列表
            for (JsonNode variable : variableListNode) {
                String variableName = variable.get("name").asText();
                String variableNo = variable.get("variableNo").asText();
                
                drlBuilder.append("        // 赋值变量: ").append(variableName).append("\n");
            }
            
            // 处理关系列表
            for (JsonNode relationShipGroup : relationShipGroupListNode) {
                if (relationShipGroup.has("relationShipList")) {
                    JsonNode relationShipListNode = relationShipGroup.get("relationShipList");
                    
                    for (JsonNode relationship : relationShipListNode) {
                        String operator = relationship.get("operator").asText();
                        
                        if ("SET_RESULT".equals(operator)) {
                            String variableNo = relationship.get("variableNo").asText();
                            String operatorValue = relationship.get("operatorValue").asText();
                            String operatorValueType = relationship.get("variableType").asText();
                            
                            drlBuilder.append("        // 设置结果\n");
                            drlBuilder.append("        flowContext.put(\"").append(variableNo).append("\", ");
                            drlBuilder.append("\"").append(operatorValue).append("\"");
                            drlBuilder.append(");\n");
                        }
                    }
                }
            }
        }
        
        // 处理下一个节点
        processNextNode(drlBuilder, nodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
    }
    
    @Override
    public String getNodeType() {
        return DrlUtils.ASSIGN_NODE;
    }
} 