package com.example.domain.processor.impl;

import com.example.domain.model.Node;
import com.example.domain.model.FlowChart;
import com.example.domain.processor.AbstractNodeProcessor;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Map;

/**
 * 开始节点处理器
 */
@Component
public class StartNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public String getNodeType() {
        return "start";
    }
    
    @Override
    public void process(StringBuilder drlBuilder,
                       Node node,
                       FlowChart flowChart,
                       Set<String> visitedNodes,
                       Map<String, Boolean> variableNameMap) {
        if (visitedNodes.contains(node.getId())) {
            return;
        }
        visitedNodes.add(node.getId());
        
        generateCode(drlBuilder, node, flowChart, visitedNodes, variableNameMap);
        processNextNodes(drlBuilder, node, flowChart, visitedNodes, variableNameMap);
    }
    
    @Override
    public void generateCode(StringBuilder drlBuilder,
                           Node node,
                           FlowChart flowChart,
                           Set<String> visitedNodes,
                           Map<String, Boolean> variableNameMap) {
        drlBuilder.append("        // 开始节点: ").append(node.getName()).append("\n");
        drlBuilder.append("        System.out.println(\"执行开始节点: ").append(node.getName()).append("\");\n");
    }
} 