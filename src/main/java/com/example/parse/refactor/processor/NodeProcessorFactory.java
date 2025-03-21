package com.example.parse.refactor.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 节点处理器工厂
 */
@Component
public class NodeProcessorFactory {
    
    private static NodeProcessorFactory instance;
    
    @Autowired
    private List<NodeProcessor> processors;
    
    private final Map<String, NodeProcessor> processorMap = new HashMap<>();
    
    @PostConstruct
    public void init() {
        processors.forEach(processor -> processorMap.put(processor.getNodeType(), processor));
        instance = this;
    }
    
    /**
     * 获取单例实例
     */
    public static NodeProcessorFactory getInstance() {
        return instance;
    }
    
    /**
     * 获取对应类型的处理器
     */
    public NodeProcessor getProcessor(String nodeType) {
        return processorMap.get(nodeType);
    }
} 