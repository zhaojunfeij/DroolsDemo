package com.example.parse.node;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.List;
import java.util.Set;
import com.example.model.EdgeInfo;

/**
 * 节点处理器接口
 */
public interface NodeProcessor {
    
    /**
     * 处理节点并生成DRL内容
     * 
     * @param drlBuilder DRL内容构建器
     * @param node 当前节点
     * @param nodeId 当前节点ID
     * @param nodeMap 节点映射
     * @param edgeMap 边映射
     * @param visitedNodes 已访问节点集合
     * @param variableNameMap 变量名映射
     */
    void process(StringBuilder drlBuilder, 
                JsonNode node, 
                String nodeId, 
                Map<String, JsonNode> nodeMap, 
                Map<String, List<EdgeInfo>> edgeMap, 
                Set<String> visitedNodes,
                Map<String, Boolean> variableNameMap);
    
    /**
     * 获取处理器支持的节点类型
     * 
     * @return 节点类型
     */
    String getNodeType();
} 