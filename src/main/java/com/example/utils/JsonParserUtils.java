package com.example.utils;

import com.example.model.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * JSON解析工具类
 */
@Component
public class JsonParserUtils {
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    /**
     * 解析JSON文件，构建流程图
     */
    public FlowGraph parseJson(String jsonFilePath) throws IOException {
        File jsonFile = new File(jsonFilePath);
        JsonNode rootNode = mapper.readTree(jsonFile);
        
        FlowGraph flowGraph = new FlowGraph();
        
        // 解析节点
        JsonNode nodesNode = rootNode.get("nodes");
        List<Node> nodes = parseNodes(nodesNode);
        flowGraph.setNodes(nodes);
        
        // 解析边
        JsonNode edgesNode = rootNode.get("edges");
        List<Edge> edges = parseEdges(edgesNode);
        flowGraph.setEdges(edges);
        
        return flowGraph;
    }
    
    /**
     * 解析节点
     */
    private List<Node> parseNodes(JsonNode nodesNode) {
        List<Node> nodes = new ArrayList<>();
        
        for (JsonNode jsonNode : nodesNode) {
            Node node = parseNode(jsonNode);
            nodes.add(node);
        }
        
        return nodes;
    }
    
    /**
     * 解析单个节点
     */
    private Node parseNode(JsonNode jsonNode) {
        String id = jsonNode.get("id").asText();
        String type = jsonNode.get("type").asText();

        Node node = new Node();

        node.setId(id);
        node.setType(type);
        
        // 解析name属性，可能在data中
        if (jsonNode.has("data") && jsonNode.get("data").has("name")) {
            node.setName(jsonNode.get("data").get("name").asText());
        }
        
        // 解析属性
        if (jsonNode.has("properties")) {
            Node.Properties properties = parseProperties(jsonNode.get("properties"));
            node.setProperties(properties);
        } else {
            node.setProperties(new Node.Properties());
        }
        
        return node;
    }
    
    /**
     * 解析节点属性
     */
    private Node.Properties parseProperties(JsonNode propertiesNode) {
        Node.Properties properties = new Node.Properties();
        
        // 解析变量列表
        if (propertiesNode.has("nodeVariableList")) {
            List<Variable> variables = parseVariables(propertiesNode.get("nodeVariableList"));
            properties.setNodeVariableList(variables);
        }
        
        // 解析关系组
        if (propertiesNode.has("relationShipGroupList")) {
            List<RelationShipGroup> relationShipGroups = parseRelationShipGroups(propertiesNode.get("relationShipGroupList"));
            properties.setRelationShipGroupList(relationShipGroups);
        }
        
        // 解析结果设置
        if (propertiesNode.has("resultSettingList")) {
            List<ResultSetting> resultSettings = parseResultSettings(propertiesNode.get("resultSettingList"));
            properties.setResultSettingList(resultSettings);
        }
        
        return properties;
    }
    
    /**
     * 解析变量列表
     */
    private List<Variable> parseVariables(JsonNode variablesNode) {
        List<Variable> variables = new ArrayList<>();
        
        for (JsonNode jsonNode : variablesNode) {
            Variable variable = new Variable();
            
            if (jsonNode.has("name")) {
                variable.setName(jsonNode.get("name").asText());
            }
            
            if (jsonNode.has("variableNo")) {
                variable.setVariableNo(jsonNode.get("variableNo").asText());
            }
            
            // 解析变量数据
            if (jsonNode.has("data")) {
                Variable.VariableData data = new Variable.VariableData();
                
                if (jsonNode.get("data").has("expression_tree_json")) {
                    data.setExpressionTreeJson(jsonNode.get("data").get("expression_tree_json").asText());
                }
                
                variable.setData(data);
            }
            
            variables.add(variable);
        }
        
        return variables;
    }
    
    /**
     * 解析关系组
     */
    private List<RelationShipGroup> parseRelationShipGroups(JsonNode relationShipGroupsNode) {
        List<RelationShipGroup> relationShipGroups = new ArrayList<>();
        
        for (JsonNode jsonNode : relationShipGroupsNode) {
            RelationShipGroup group = new RelationShipGroup();
            
            if (jsonNode.has("operator")) {
                group.setOperator(jsonNode.get("operator").asText());
            }
            
            // 解析关系列表
            if (jsonNode.has("relationShipList")) {
                List<RelationShip> relationShips = parseRelationShips(jsonNode.get("relationShipList"));
                group.setRelationShipList(relationShips);
            }
            
            relationShipGroups.add(group);
        }
        
        return relationShipGroups;
    }
    
    /**
     * 解析关系列表
     */
    private List<RelationShip> parseRelationShips(JsonNode relationShipsNode) {
        List<RelationShip> relationShips = new ArrayList<>();
        
        for (JsonNode jsonNode : relationShipsNode) {
            RelationShip relationShip = new RelationShip();
            
            if (jsonNode.has("relationshipNo")) {
                relationShip.setRelationshipNo(jsonNode.get("relationshipNo").asText());
            }
            
            if (jsonNode.has("variableNo")) {
                relationShip.setVariableNo(jsonNode.get("variableNo").asText());
            }
            
            if (jsonNode.has("operator")) {
                relationShip.setOperator(jsonNode.get("operator").asText());
            }
            
            if (jsonNode.has("operatorValue")) {
                relationShip.setOperatorValue(jsonNode.get("operatorValue").asText());
            }
            
            if (jsonNode.has("operatorValueType")) {
                relationShip.setOperatorValueType(jsonNode.get("operatorValueType").asText());
            }
            
            if (jsonNode.has("variableType")) {
                relationShip.setVariableType(jsonNode.get("variableType").asText());
            }
            
            if (jsonNode.has("relationOperator")) {
                relationShip.setRelationOperator(jsonNode.get("relationOperator").asText());
            }
            
            relationShips.add(relationShip);
        }
        
        return relationShips;
    }
    
    /**
     * 解析结果设置
     */
    private List<ResultSetting> parseResultSettings(JsonNode resultSettingsNode) {
        List<ResultSetting> resultSettings = new ArrayList<>();
        
        for (JsonNode jsonNode : resultSettingsNode) {
            ResultSetting resultSetting = new ResultSetting();
            
            if (jsonNode.has("key")) {
                resultSetting.setKey(jsonNode.get("key").asText());
            }
            
            if (jsonNode.has("type")) {
                resultSetting.setType(jsonNode.get("type").asText());
            }
            
            if (jsonNode.has("value")) {
                resultSetting.setValue(jsonNode.get("value").asText());
            }
            
            resultSettings.add(resultSetting);
        }
        
        return resultSettings;
    }
    
    /**
     * 解析边
     */
    private List<Edge> parseEdges(JsonNode edgesNode) {
        List<Edge> edges = new ArrayList<>();
        
        for (JsonNode jsonNode : edgesNode) {
            Edge edge = new Edge();
            
            if (jsonNode.has("id")) {
                edge.setId(jsonNode.get("id").asText());
            }
            
            if (jsonNode.has("source")) {
                edge.setSource(jsonNode.get("source").asText());
            }
            
            if (jsonNode.has("target")) {
                edge.setTarget(jsonNode.get("target").asText());
            }
            
            if (jsonNode.has("label")) {
                edge.setLabel(jsonNode.get("label").asInt());
            }
            
            // 解析边属性
            if (jsonNode.has("properties")) {
                Edge.EdgeProperties properties = new Edge.EdgeProperties();
                
                if (jsonNode.get("properties").has("decisionCase")) {
                    properties.setDecisionCase(jsonNode.get("properties").get("decisionCase").asText());
                }
                
                edge.setProperties(properties);
            }
            
            edges.add(edge);
        }
        
        return edges;
    }
    
    /**
     * 构建节点映射
     */
    public Map<String, Node> buildNodeMap(List<Node> nodes) {
        Map<String, Node> nodeMap = new HashMap<>();
        for (Node node : nodes) {
            nodeMap.put(node.getId(), node);
        }
        return nodeMap;
    }
    
    /**
     * 构建边映射
     */
    public Map<String, List<Edge>> buildEdgeMap(List<Edge> edges) {
        Map<String, List<Edge>> edgeMap = new HashMap<>();
        
        for (Edge edge : edges) {
            String sourceId = edge.getSource();
            String targetId = edge.getTarget();
            
            // 获取标签
            int label = edge.getLabel() != null ? edge.getLabel() : 0;
            
            // 获取决策用例ID
            String caseId = "";
            if (edge.getProperties() != null && edge.getProperties().getDecisionCase() != null) {
                caseId = edge.getProperties().getDecisionCase();
            }
            
            // 添加到边映射
            if (!edgeMap.containsKey(sourceId)) {
                edgeMap.put(sourceId, new ArrayList<>());
            }
            
            edgeMap.get(sourceId).add(new Edge(sourceId, targetId, label, caseId));
        }
        
        return edgeMap;
    }
    
    /**
     * 寻找指定类型的节点ID
     */
    public String findNodeIdByType(Map<String, Node> nodeMap, String nodeType) {
        for (Map.Entry<String, Node> entry : nodeMap.entrySet()) {
            Node node = entry.getValue();
            if (node.getType().equalsIgnoreCase(nodeType)) {
                return entry.getKey();
            }
        }
        return null;
    }
}