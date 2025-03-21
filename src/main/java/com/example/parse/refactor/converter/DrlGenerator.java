package com.example.parse.refactor.converter;

import com.example.model.Node;
import com.example.model.NodeType;
import com.example.parse.refactor.processor.NodeProcessor;
import com.example.parse.refactor.processor.NodeProcessorFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * DRL生成器
 */
@Component
public class DrlGenerator {
    
    /**
     * 生成DRL内容
     * 
     * @param context DRL生成上下文
     * @return DRL内容
     */
    public String generateDrl(DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        // 添加包声明和导入
        addPackageAndImports(drlBuilder);
        
        // 规则名称
        drlBuilder.append("rule \"").append(context.getRuleName()).append("\"\n");
        drlBuilder.append("    agenda-group \"").append(context.getRuleName()).append("\"\n");
        drlBuilder.append("    no-loop true\n");
        drlBuilder.append("    when\n");
        drlBuilder.append("        $inputData : Map()\n");
        drlBuilder.append("    then\n");
        
        // 添加规则内容
        drlBuilder.append("        // 流程开始\n");
        drlBuilder.append("        System.out.println(\"开始执行规则流程: ").append(context.getRuleName()).append("\");\n");
        
        // 找到开始节点
        String startNodeId = findStartNode(context);
        if (startNodeId == null) {
            throw new IllegalArgumentException("未找到开始节点");
        }
        
        // 从开始节点遍历并生成规则内容
        buildRuleContent(startNodeId, context);
        
        // 关闭规则
        drlBuilder.append("end\n");
        
        return drlBuilder.toString();
    }
    
    /**
     * 添加包声明和导入
     */
    private void addPackageAndImports(StringBuilder drlBuilder) {
        drlBuilder.append("package org.example.ruleEngine.V3;\n\n");
        drlBuilder.append("import java.util.Map;\n");
        drlBuilder.append("import java.util.HashMap;\n");
        drlBuilder.append("import java.util.List;\n");
        drlBuilder.append("import java.util.ArrayList;\n");
        drlBuilder.append("import java.math.BigDecimal;\n");
        drlBuilder.append("import com.example.utils.VariableUtils;\n\n");
    }
    
    /**
     * 寻找开始节点
     */
    private String findStartNode(DrlContext context) {
        for (Map.Entry<String, Node> entry : context.getNodeMap().entrySet()) {
            Node node = entry.getValue();
            if (NodeType.START.getType().equals(node.getType())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * 递归构建规则内容
     */
    private void buildRuleContent(String currentNodeId, DrlContext context) {
        // 避免循环
//        if (context.getVisitedNodes().contains(currentNodeId)) {
//            return;
//        }
        context.getVisitedNodes().add(currentNodeId);
        
        Node currentNode = context.getNodeMap().get(currentNodeId);
        if (currentNode == null) {
            return;
        }
        
        // 处理节点
        processNode(currentNode, currentNodeId, context);
    }
    
    /**
     * 处理节点
     */
    private void processNode(Node node, String nodeId, DrlContext context) {
        // 使用工厂获取对应的处理器
        NodeProcessorFactory factory = NodeProcessorFactory.getInstance();
        
        NodeProcessor processor = factory.getProcessor(node.getType());
        if (processor != null) {
            processor.process(node, nodeId, context);
        } else {
            context.getDrlBuilder().append("        // 未知节点类型: ").append(node.getType()).append("\n");
        }
    }
} 