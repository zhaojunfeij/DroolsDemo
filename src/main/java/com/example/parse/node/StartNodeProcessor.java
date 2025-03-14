package com.example.parse.node;

import com.example.parse.model.EdgeInfo;
import com.example.parse.util.DrlUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 开始节点处理器
 */
public class StartNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(StringBuilder drlBuilder, 
                      JsonNode node, 
                      String nodeId, 
                      Map<String, JsonNode> nodeMap, 
                      Map<String, List<EdgeInfo>> edgeMap, 
                      Set<String> visitedNodes,
                      Map<String, Boolean> variableNameMap) {
        drlBuilder.append("        // 开始节点\n");
        drlBuilder.append("        Map<String, Object> flowContext = new HashMap<>();\n");
        drlBuilder.append("        flowContext.putAll($inputData);\n");
        
        // 处理下一个节点
        processNextNode(drlBuilder, nodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
    }
    
    @Override
    public String getNodeType() {
        return DrlUtils.START_NODE;
    }
} 