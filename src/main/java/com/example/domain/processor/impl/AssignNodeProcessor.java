package com.example.domain.processor.impl;

import com.example.domain.model.Node;
import com.example.domain.model.FlowChart;
import com.example.domain.processor.NodeProcessor;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.Map;
import java.util.UUID;

/**
 * 赋值节点处理器
 */
@Component
public class AssignNodeProcessor implements NodeProcessor {
    
    @Override
    public String getNodeType() {
        return "assign";
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
        drlBuilder.append("        // 赋值节点: ").append(node.getName()).append("\n");
        drlBuilder.append("        System.out.println(\"执行赋值节点: ").append(node.getName()).append("\");\n");
        
        // 获取赋值表达式和目标变量
        String expression = node.getProperties().containsKey("expression") 
                          ? node.getProperties().get("expression").toString() 
                          : "null";
                          
        String targetVar = node.getProperties().containsKey("target") 
                         ? node.getProperties().get("target").toString()
                         : "var_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 8);
                         
        // 将目标变量添加到变量名映射
        variableNameMap.put(targetVar, true);
        
        // 添加赋值代码
        drlBuilder.append("        // 赋值表达式: ").append(expression).append("\n");
        drlBuilder.append("        Object ").append(targetVar).append(" = ");
        
        // 处理不同类型的表达式
        if (expression.startsWith("\"") || expression.startsWith("'")) {
            // 字符串字面量
            drlBuilder.append(expression).append(";\n");
        } else if (expression.matches("\\d+(\\.\\d+)?")) {
            // 数字字面量
            drlBuilder.append(expression).append(";\n");
        } else if (expression.equals("true") || expression.equals("false")) {
            // 布尔字面量
            drlBuilder.append(expression).append(";\n");
        } else if (expression.equals("null")) {
            // null值
            drlBuilder.append("null;\n");
        } else {
            // 变量引用
            drlBuilder.append("$inputData.get(\"").append(expression).append("\");\n");
        }
        
        // 将结果存入输入数据
        drlBuilder.append("        $inputData.put(\"").append(targetVar).append("\", ").append(targetVar).append(");\n");
        drlBuilder.append("        System.out.println(\"赋值结果: \" + ").append(targetVar).append(");\n");
    }
} 