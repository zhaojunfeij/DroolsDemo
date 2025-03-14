package com.example.parse.node;

import com.example.parse.model.EdgeInfo;
import com.example.parse.util.DrlUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 规则节点处理器
 */
public class RuleNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(StringBuilder drlBuilder, 
                      JsonNode node, 
                      String nodeId, 
                      Map<String, JsonNode> nodeMap, 
                      Map<String, List<EdgeInfo>> edgeMap, 
                      Set<String> visitedNodes,
                      Map<String, Boolean> variableNameMap) {
        drlBuilder.append("       //规则节点: ").append(node.path("data").path("name").asText()).append("\n");
        
        // 获取节点变量
        JsonNode nodeVariables = node.path("properties").path("nodeVariableList");
        
        for (JsonNode variable : nodeVariables) {
            DrlUtils.buildVariable(drlBuilder, variable);
        }
        
        // 获取关系判断条件
        JsonNode relationShipGroups = node.path("properties").path("relationShipGroupList");
        
        if (relationShipGroups.size() > 0) {
            // 处理AND/OR组合条件
            for (JsonNode group : relationShipGroups) {
                String groupOperator = group.has("operator") ? group.path("operator").asText() : "AND";
                JsonNode relationships = group.path("relationShipList");
                
                if (relationships.size() > 0) {
                    for (JsonNode relationship : relationships) {
                        DrlUtils.buildRelationShip(drlBuilder, relationship, node);
                    }
                    drlBuilder.append("       // 条件组 (").append(groupOperator).append(")\n");
                    // 根据条件结果决定流程
                    drlBuilder.append("       if (");
                    for (JsonNode relationship : relationships) {
                        String nodeIdStr = node.get("id").asText();
                        if (Objects.nonNull(relationship.get("relationshipNo"))) {
                            nodeIdStr = nodeIdStr.concat("_" + relationship.get("relationshipNo").asText());
                        }
                        drlBuilder.append("flowContext.get(\"");
                        drlBuilder.append(nodeIdStr).append("\")");
                        
                        String relationOperator = relationship.has("relationOperator") ? relationship.path("relationOperator").asText() : null;
                        
                        if (relationOperator != null) {
                            if ("AND".equals(relationOperator)) {
                                drlBuilder.append(" && ");
                            } else if ("OR".equals(relationOperator)) {
                                drlBuilder.append(" || ");
                            }
                        }
                    }
                    drlBuilder.append("){\n");
                    
                    // 处理下一个节点
                    processNextNode(drlBuilder, nodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
                    
                    drlBuilder.append("       } else {\n");
                    drlBuilder.append("       // 条件不满足，直接返回\n");
                    drlBuilder.append("       return;\n");
                    drlBuilder.append("       }\n");
                } else {
                    // 没有条件，直接过渡到下一个节点
                    processNextNode(drlBuilder, nodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
                }
            }
        } else {
            // 没有条件组，直接过渡到下一个节点
            processNextNode(drlBuilder, nodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
        }
    }
    
    @Override
    public String getNodeType() {
        return DrlUtils.RULE_NODE;
    }
} 