package com.example.parse.refactor.converter;

import com.example.model.Edge;
import com.example.model.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * DRL生成上下文
 */
public class DrlContext {
    private StringBuilder drlBuilder;
    private Map<String, Node> nodeMap;
    private Map<String, List<Edge>> edgeMap;
    private Set<String> visitedNodes;
    private String ruleName;
    private Map<String, Boolean> variableNameMap;
    private Map<String, String> variableMap;
    
    public DrlContext() {
        this.drlBuilder = new StringBuilder();
        this.variableNameMap = new HashMap<>();
        this.variableMap = new HashMap<>();
    }
    
    public StringBuilder getDrlBuilder() {
        return drlBuilder;
    }
    
    public void setDrlBuilder(StringBuilder drlBuilder) {
        this.drlBuilder = drlBuilder;
    }
    
    public Map<String, Node> getNodeMap() {
        return nodeMap;
    }
    
    public void setNodeMap(Map<String, Node> nodeMap) {
        this.nodeMap = nodeMap;
    }
    
    public Map<String, List<Edge>> getEdgeMap() {
        return edgeMap;
    }
    
    public void setEdgeMap(Map<String, List<Edge>> edgeMap) {
        this.edgeMap = edgeMap;
    }
    
    public Set<String> getVisitedNodes() {
        return visitedNodes;
    }
    
    public void setVisitedNodes(Set<String> visitedNodes) {
        this.visitedNodes = visitedNodes;
    }
    
    public String getRuleName() {
        return ruleName;
    }
    
    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }
    
    public Map<String, Boolean> getVariableNameMap() {
        return variableNameMap;
    }
    
    public Map<String, String> getVariableMap() {
        return variableMap;
    }
    
    public void setVariableMap(Map<String, String> variableMap) {
        this.variableMap = variableMap;
    }
    
    /**
     * 添加变量映射
     */
    public void initDefaultVariableMap() {
        variableMap.put("a68e8ebd-3024-42d9-bbb9-14dac635e8fa", "userFund");
        variableMap.put("b6ef56c0-003a-4518-95d8-b93c927be38f", "orderAmt");
        variableMap.put("0965b178-a5d3-410b-a3b4-04c221a1b457", "storeId");
        variableMap.put("220db748-9fd9-405e-83a5-c24fb5de5f13", "businessId");
        variableMap.put("154782fd-71a6-4a65-889e-a5cb2101c1c1", "weComFriend");
    }
} 