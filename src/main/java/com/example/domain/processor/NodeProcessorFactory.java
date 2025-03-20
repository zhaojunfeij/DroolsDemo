package com.example.domain.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * 节点处理器工厂
 */
@Component
public class NodeProcessorFactory {
    
    private final Map<String, NodeProcessor> processorMap = new HashMap<>();
    
    @Autowired
    public NodeProcessorFactory(List<NodeProcessor> processors) {
        processors.forEach(processor -> 
            processorMap.put(processor.getNodeType(), processor));
    }
    
    /**
     * 获取节点处理器
     */
    public NodeProcessor getProcessor(String nodeType) {
        NodeProcessor processor = processorMap.get(nodeType);
        if (processor == null) {
            throw new IllegalArgumentException("不支持的节点类型: " + nodeType);
        }
        return processor;
    }
    
    /**
     * 注册节点处理器
     */
    public void registerProcessor(NodeProcessor processor) {
        processorMap.put(processor.getNodeType(), processor);
    }
} 