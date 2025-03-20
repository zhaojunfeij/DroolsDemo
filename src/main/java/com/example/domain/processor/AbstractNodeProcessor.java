package com.example.domain.processor;

import com.example.domain.model.Node;
import com.example.domain.model.Edge;
import com.example.domain.model.FlowChart;
import com.example.domain.processor.NodeProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Set;
import java.util.Map;

/**
 * 抽象节点处理器基类
 */
public abstract class AbstractNodeProcessor implements NodeProcessor {
    
    @Autowired
    protected NodeProcessorFactory nodeProcessorFactory;
    
    @Override
    public void processNextNodes(StringBuilder drlBuilder,
                               Node currentNode,
                               FlowChart flowChart,
                               Set<String> visitedNodes,
                               Map<String, Boolean> variableNameMap) {
        flowChart.getOutgoingEdges(currentNode.getId())
                .forEach(edge -> {
                    Node nextNode = flowChart.getNodeMap().get(edge.getTargetId());
                    if (nextNode != null && !visitedNodes.contains(nextNode.getId())) {
                        nodeProcessorFactory.getProcessor(nextNode.getType())
                                .process(drlBuilder, nextNode, flowChart, visitedNodes, variableNameMap);
                    }
                });
    }
    
    /**
     * 获取节点处理器，便于子类调用
     * 
     * @param nodeType 节点类型
     * @return 节点处理器
     */
    protected NodeProcessor getProcessor(String nodeType) {
        return nodeProcessorFactory.getProcessor(nodeType);
    }
} 