package com.example.parse.node;

import com.example.model.EdgeInfo;
import com.example.utils.DrlUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 计算节点处理器
 */
public class ComputeNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(StringBuilder drlBuilder, 
                      JsonNode node, 
                      String nodeId, 
                      Map<String, JsonNode> nodeMap, 
                      Map<String, List<EdgeInfo>> edgeMap, 
                      Set<String> visitedNodes,
                      Map<String, Boolean> variableNameMap) {
        drlBuilder.append("        // 计算节点\n");
        
        // 获取节点变量和关系
        JsonNode propertiesNode = node.get("properties");
        if (propertiesNode.has("nodeVariableList") && propertiesNode.has("relationShipGroupList")) {
            JsonNode variableListNode = propertiesNode.get("nodeVariableList");
            JsonNode relationShipGroupListNode = propertiesNode.get("relationShipGroupList");
            
            // 处理变量列表
            for (JsonNode variable : variableListNode) {
                DrlUtils.buildVariable(drlBuilder, variable);
            }
            
            // 处理关系列表
            for (JsonNode relationShipGroup : relationShipGroupListNode) {
                if (relationShipGroup.has("relationShipList")) {
                    JsonNode relationShipListNode = relationShipGroup.get("relationShipList");
                    
                    for (JsonNode relationship : relationShipListNode) {
                        DrlUtils.buildRelationShip(drlBuilder, relationship, node);
                    }
                }
            }
        }
        
        // 处理下一个节点
        processNextNode(drlBuilder, nodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
    }
    
    @Override
    public String getNodeType() {
        return DrlUtils.COMPUTE_NODE;
    }
} 