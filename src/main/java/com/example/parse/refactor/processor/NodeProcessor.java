package com.example.parse.refactor.processor;

import com.example.parse.refactor.converter.DrlContext;
import com.example.model.Node;

/**
 * 节点处理器接口
 */
public interface NodeProcessor {
    
    /**
     * 处理节点
     * 
     * @param node 节点
     * @param nodeId 节点ID
     * @param context DRL生成上下文
     */
    void process(Node node, String nodeId, DrlContext context);
    
    /**
     * 获取支持的节点类型
     * 
     * @return 节点类型
     */
    String getNodeType();
} 