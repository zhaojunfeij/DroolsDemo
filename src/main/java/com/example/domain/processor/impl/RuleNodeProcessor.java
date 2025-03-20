package com.example.domain.processor.impl;

import com.example.domain.model.Node;
import com.example.domain.model.FlowChart;
import com.example.domain.processor.NodeProcessor;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * 规则节点处理器
 */
@Component
public class RuleNodeProcessor implements NodeProcessor {
    
    @Override
    public String getNodeType() {
        return "rule";
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
        drlBuilder.append("        // 规则节点: ").append(node.getName()).append("\n");
        drlBuilder.append("        System.out.println(\"执行规则节点: ").append(node.getName()).append("\");\n");
        
        // 获取规则条件
        String condition = node.getProperties().containsKey("condition") 
                          ? node.getProperties().get("condition").toString() 
                          : "true";
                          
        // 获取规则动作
        List<String> actions = node.getProperties().containsKey("actions") 
                              ? (List<String>) node.getProperties().get("actions")
                              : new ArrayList<>();
                              
        // 添加规则条件
        drlBuilder.append("        if (").append(condition).append(") {\n");
        
        // 添加规则动作
        for (String action : actions) {
            drlBuilder.append("            ").append(action).append(";\n");
        }
        
        drlBuilder.append("        }\n");
        
        // 添加日志输出
        drlBuilder.append("        System.out.println(\"规则执行完成: ").append(node.getName()).append("\");\n");
    }
} 