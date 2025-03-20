package com.example.domain.processor;

import com.example.domain.model.Node;
import com.example.domain.model.FlowChart;
import java.util.Set;
import java.util.Map;

/**
 * 节点处理器接口
 */
public interface NodeProcessor {
    /**
     * 获取节点类型
     */
    String getNodeType();
    
    /**
     * 处理节点
     */
    void process(StringBuilder drlBuilder,
                Node node,
                FlowChart flowChart,
                Set<String> visitedNodes,
                Map<String, Boolean> variableNameMap);
                
    /**
     * 生成节点处理代码
     */
    void generateCode(StringBuilder drlBuilder,
                     Node node,
                     FlowChart flowChart,
                     Set<String> visitedNodes,
                     Map<String, Boolean> variableNameMap);

    void processNextNodes(StringBuilder drlBuilder,
                          Node currentNode,
                          FlowChart flowChart,
                          Set<String> visitedNodes,
                          Map<String, Boolean> variableNameMap);
} 