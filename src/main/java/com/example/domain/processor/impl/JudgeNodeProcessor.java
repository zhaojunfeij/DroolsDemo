package com.example.domain.processor.impl;

import com.example.domain.model.Node;
import com.example.domain.model.Edge;
import com.example.domain.model.FlowChart;
import com.example.domain.processor.NodeProcessor;
import com.example.domain.processor.NodeProcessorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

/**
 * 判断节点处理器
 */
@Component
public class JudgeNodeProcessor implements NodeProcessor {
    
    @Autowired
    private NodeProcessorFactory nodeProcessorFactory;
    
    @Override
    public String getNodeType() {
        return "judge";
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
        
        // 判断节点不使用通用的处理下一节点方法，而是根据条件分支处理
        processConditionalBranches(drlBuilder, node, flowChart, visitedNodes, variableNameMap);
    }
    
    @Override
    public void generateCode(StringBuilder drlBuilder,
                           Node node,
                           FlowChart flowChart,
                           Set<String> visitedNodes,
                           Map<String, Boolean> variableNameMap) {
        drlBuilder.append("        // 判断节点: ").append(node.getName()).append("\n");
        drlBuilder.append("        System.out.println(\"执行判断节点: ").append(node.getName()).append("\");\n");
        
        // 获取判断条件
        String condition = node.getProperties().containsKey("condition") 
                          ? node.getProperties().get("condition").toString() 
                          : "$inputData.get(\"value\") != null";
        
        drlBuilder.append("        if (").append(condition).append(") {\n");
    }
    
    /**
     * 处理条件分支
     */
    private void processConditionalBranches(StringBuilder drlBuilder,
                                          Node node,
                                          FlowChart flowChart,
                                          Set<String> visitedNodes,
                                          Map<String, Boolean> variableNameMap) {
        List<Edge> outgoingEdges = flowChart.getOutgoingEdges(node.getId());
        
        // 按标签排序，确保按顺序处理
        outgoingEdges.sort(Comparator.comparingInt(Edge::getLabel));
        
        // 存储每个分支的子图访问记录
        Map<Integer, Set<String>> branchVisitedMap = new HashMap<>();
        
        // 处理条件为true的分支
        Edge trueEdge = outgoingEdges.stream()
                                    .filter(e -> e.getLabel() == 1)
                                    .findFirst()
                                    .orElse(null);
        
        if (trueEdge != null) {
            Node trueNode = flowChart.getNodeMap().get(trueEdge.getTargetId());
            if (trueNode != null) {
                // 为true分支创建一个新的访问集合
                Set<String> trueVisited = new HashSet<>(visitedNodes);
                branchVisitedMap.put(1, trueVisited);
                
                // 处理true分支节点
                nodeProcessorFactory.getProcessor(trueNode.getType())
                        .process(drlBuilder, trueNode, flowChart, trueVisited, variableNameMap);
            }
        }
        
        // 添加else部分
        drlBuilder.append("        } else {\n");
        
        // 处理条件为false的分支
        Edge falseEdge = outgoingEdges.stream()
                                    .filter(e -> e.getLabel() == 0)
                                    .findFirst()
                                    .orElse(null);
        
        if (falseEdge != null) {
            Node falseNode = flowChart.getNodeMap().get(falseEdge.getTargetId());
            if (falseNode != null) {
                // 为false分支创建一个新的访问集合
                Set<String> falseVisited = new HashSet<>(visitedNodes);
                branchVisitedMap.put(0, falseVisited);
                
                // 处理false分支节点
                nodeProcessorFactory.getProcessor(falseNode.getType())
                        .process(drlBuilder, falseNode, flowChart, falseVisited, variableNameMap);
            }
        }
        
        drlBuilder.append("        }\n");
        
        // 将所有分支的访问记录合并回主访问记录
        branchVisitedMap.values().forEach(visitedNodes::addAll);
    }
} 