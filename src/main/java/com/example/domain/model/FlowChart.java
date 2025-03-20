package com.example.domain.model;

import lombok.Data;
import lombok.Builder;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 流程图领域模型
 */
@Data
@Builder
public class FlowChart {
    private String id;
    private String name;
    private List<Node> nodes;
    private List<Edge> edges;
    
    @Builder.Default
    private Map<String, Node> nodeMap = new HashMap<>();
    
    @Builder.Default
    private Map<String, List<Edge>> edgeMap = new HashMap<>();
    
    public void initializeMaps() {
        // 初始化节点映射
        nodes.forEach(node -> nodeMap.put(node.getId(), node));
        
        // 初始化边映射
        edges.forEach(edge -> {
            edgeMap.computeIfAbsent(edge.getSourceId(), k -> new ArrayList<>())
                  .add(edge);
        });
    }
    
    public Node getStartNode() {
        return nodes.stream()
                   .filter(node -> "start".equalsIgnoreCase(node.getType()))
                   .findFirst()
                   .orElseThrow(() -> new IllegalStateException("未找到开始节点"));
    }
    
    public List<Edge> getOutgoingEdges(String nodeId) {
        return edgeMap.getOrDefault(nodeId, new ArrayList<>());
    }
} 