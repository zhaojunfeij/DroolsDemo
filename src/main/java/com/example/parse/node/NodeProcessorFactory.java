package com.example.parse.node;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 节点处理器工厂
 */
public class NodeProcessorFactory {
    
    private static final Map<String, NodeProcessor> processorMap = new HashMap<>();
    
    static {
        // 使用ServiceLoader加载所有实现了NodeProcessor接口的类
        ServiceLoader<NodeProcessor> serviceLoader = ServiceLoader.load(NodeProcessor.class);
        for (NodeProcessor processor : serviceLoader) {
            processorMap.put(processor.getNodeType(), processor);
        }
        
        // 如果没有通过ServiceLoader加载到处理器，则手动注册
        if (processorMap.isEmpty()) {
            registerProcessor(new StartNodeProcessor());
            registerProcessor(new EndNodeProcessor());
            registerProcessor(new JudgeNodeProcessor());
            registerProcessor(new ComputeNodeProcessor());
            registerProcessor(new AssignNodeProcessor());
            registerProcessor(new RuleNodeProcessor());
        }
    }
    
    /**
     * 注册节点处理器
     * 
     * @param processor 节点处理器
     */
    public static void registerProcessor(NodeProcessor processor) {
        processorMap.put(processor.getNodeType(), processor);
    }
    
    /**
     * 获取节点处理器
     * 
     * @param nodeType 节点类型
     * @return 节点处理器
     */
    public static NodeProcessor getProcessor(String nodeType) {
        NodeProcessor processor = processorMap.get(nodeType);
        if (processor == null) {
            throw new IllegalArgumentException("不支持的节点类型: " + nodeType);
        }
        return processor;
    }
} 