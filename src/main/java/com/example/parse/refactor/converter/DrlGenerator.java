package com.example.parse.refactor.converter;

import com.example.model.Node;
import com.example.model.NodeType;
import com.example.parse.refactor.processor.NodeProcessor;
import com.example.parse.refactor.processor.NodeProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * DRL规则生成器 - 负责将流程图转换为Drools规则
 */
@Component
public class DrlGenerator {
    
    private final NodeProcessorFactory nodeProcessorFactory;
    
    @Autowired
    public DrlGenerator(NodeProcessorFactory nodeProcessorFactory) {
        this.nodeProcessorFactory = nodeProcessorFactory;
    }
    
    /**
     * 生成DRL规则内容
     */
    public String generateDrl(DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        addPackageAndImports(drlBuilder);
        buildRuleStructure(context);
        
        return drlBuilder.toString();
    }
    
    /**
     * 构建规则结构
     */
    private void buildRuleStructure(DrlContext context) {

        StringBuilder drlBuilder = context.getDrlBuilder();
        
        appendRuleHeader(context, drlBuilder);

        appendRuleBody(context, drlBuilder);

        appendRuleFooter(drlBuilder);
    }
    
    /**
     * 添加规则头部
     */
    private void appendRuleHeader(DrlContext context, StringBuilder drlBuilder) {
        drlBuilder.append("rule \"").append(context.getRuleName()).append("\"\n")
                 .append("    agenda-group \"").append(context.getRuleName()).append("\"\n")
                 .append("    no-loop true\n")
                 .append("    when\n")
                 .append("        $inputData : Map()\n")
                 .append("    then\n");
    }
    
    /**
     * 添加规则主体
     */
    private void appendRuleBody(DrlContext context, StringBuilder drlBuilder) {
        // 添加规则开始信息
        drlBuilder.append("        // 流程开始\n")
                 .append("        System.out.println(\"开始执行规则流程: ")
                 .append(context.getRuleName()).append("\");\n");
        
        // 处理流程节点
        processFlowNodes(context);
    }
    
    /**
     * 处理流程节点
     */
    private void processFlowNodes(DrlContext context) {
        String startNodeId = findStartNode(context);
        if (startNodeId == null) {
            throw new IllegalArgumentException("未找到开始节点");
        }
        
        processNodeSequence(startNodeId, context);
    }
    
    /**
     * 添加规则尾部
     */
    private void appendRuleFooter(StringBuilder drlBuilder) {
        drlBuilder.append("end\n");
    }
    
    /**
     * 添加包声明和导入语句
     */
    private void addPackageAndImports(StringBuilder drlBuilder) {
        drlBuilder.append("package org.example.ruleEngine.V3;\n\n")
                 .append("import java.util.Map;\n")
                 .append("import java.util.HashMap;\n")
                 .append("import java.util.List;\n")
                 .append("import java.util.ArrayList;\n")
                 .append("import java.math.BigDecimal;\n")
                 .append("import com.example.utils.VariableUtils;\n\n");
    }
    
    /**
     * 查找起始节点
     */
    private String findStartNode(DrlContext context) {
        return context.getNodeMap().entrySet().stream()
                .filter(entry -> NodeType.START.getType().equals(entry.getValue().getType()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 处理节点序列
     */
    private void processNodeSequence(String nodeId, DrlContext context) {
        if (context.getVisitedNodes().contains(nodeId)) {
            return;  // 避免循环处理
        }
        
        context.getVisitedNodes().add(nodeId);
        Node node = context.getNodeMap().get(nodeId);
        
        if (node != null) {
            processNode(node, nodeId, context);
        }
    }
    
    /**
     * 处理单个节点
     */
    private void processNode(Node node, String nodeId, DrlContext context) {
        NodeProcessor processor = nodeProcessorFactory.getProcessor(node.getType());
        
        if (processor != null) {
            processor.process(node, nodeId, context);
        } else {
            context.getDrlBuilder()
                  .append("        // 未知节点类型: ")
                  .append(node.getType())
                  .append("\n");
        }
    }
} 