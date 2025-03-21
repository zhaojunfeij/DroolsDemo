package com.example.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 流程图模型类
 */
public class FlowGraph {
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    
    public List<Node> getNodes() {
        return nodes;
    }
    
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }
    
    public List<Edge> getEdges() {
        return edges;
    }
    
    public void setEdges(List<Edge> edges) {
        this.edges = edges;
    }
} 