package com.example.domain.processor.impl;

import com.example.domain.model.Node;
import com.example.domain.model.FlowChart;
import com.example.domain.processor.AbstractNodeProcessor;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

/**
 * 计算节点处理器
 */
@Component
public class ComputeNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public String getNodeType() {
        return "compute";
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
        drlBuilder.append("        // 计算节点: ").append(node.getName()).append("\n");
        drlBuilder.append("        System.out.println(\"执行计算节点: ").append(node.getName()).append("\");\n");
        
        // 获取表达式和目标变量
        String expression = node.getProperties().containsKey("expression") 
                          ? node.getProperties().get("expression").toString() 
                          : "1 + 1";
                          
        String targetVar = node.getProperties().containsKey("target") 
                         ? node.getProperties().get("target").toString()
                         : "result_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
                         
        // 将目标变量添加到变量名映射
        variableNameMap.put(targetVar, true);
        
        // 添加计算代码
        drlBuilder.append("        // 计算表达式: ").append(expression).append("\n");
        drlBuilder.append("        Object ").append(targetVar).append(" = ");
        
        // 如果表达式是简单的数学表达式，直接使用
        if (expression.matches(".*[+\\-*/].*")) {
            drlBuilder.append(expression).append(";\n");
        } else {
            // 假设是变量引用
            drlBuilder.append("$inputData.get(\"").append(expression).append("\");\n");
        }
        
        // 将结果存入输入数据
        drlBuilder.append("        $inputData.put(\"").append(targetVar).append("\", ").append(targetVar).append(");\n");
        drlBuilder.append("        System.out.println(\"计算结果: \" + ").append(targetVar).append(");\n");
    }
} 