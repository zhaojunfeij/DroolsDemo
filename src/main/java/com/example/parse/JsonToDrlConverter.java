package com.example.parse;

import com.example.model.EdgeInfo;
import com.example.parse.node.NodeProcessorFactory;
import com.example.utils.DrlUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * JSON流程规则转Drools DRL文件工具
 */
@Service
public class JsonToDrlConverter {

    /**
     * 将JSON文件转换为DRL内容
     */
    public String convertJsonToDrl(String jsonFilePath) throws IOException {
        // 读取JSON文件
        File jsonFile = new File(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonFile);

        // 解析节点和边
        JsonNode nodesNode = rootNode.get("nodes");
        JsonNode edgesNode = rootNode.get("edges");

        // 构建节点映射和边映射
        Map<String, JsonNode> nodeMap = buildNodeMap(nodesNode);
        Map<String, List<EdgeInfo>> edgeMap = buildEdgeMap(edgesNode);

        // 找到开始节点
        String startNodeId = findNodeIdByType(nodeMap, DrlUtils.START_NODE);
        if (startNodeId == null) {
            throw new IllegalArgumentException("未找到开始节点");
        }

        // 构建DRL内容
        StringBuilder drlBuilder = new StringBuilder();
        // 添加包声明和导入
        DrlUtils.addPackageAndImports(drlBuilder);

        // 规则名称使用文件名
        String ruleName = new File(jsonFilePath).getName().replaceAll("\\.json$", "");
        drlBuilder.append("rule \"").append(ruleName).append("\"\n");
        drlBuilder.append("    agenda-group \"").append(ruleName).append("\"\n");
        drlBuilder.append("    no-loop true\n");
        drlBuilder.append("    when\n");
        drlBuilder.append("        $inputData : Map()\n");
        drlBuilder.append("    then\n");

        // 添加规则内容
        drlBuilder.append("        // 流程开始\n");
        drlBuilder.append("        System.out.println(\"开始执行规则流程: ").append(ruleName).append("\");\n");

        // 从开始节点遍历并生成规则内容
        processNodeContent(drlBuilder, startNodeId, nodeMap, edgeMap, new HashSet<>(), new HashMap<>());

        // 关闭规则
        drlBuilder.append("end\n");

        return drlBuilder.toString();
    }

    /**
     * 递归处理节点内容
     */
    private void processNodeContent(StringBuilder drlBuilder, 
                                  String currentNodeId, 
                                  Map<String, JsonNode> nodeMap, 
                                  Map<String, List<EdgeInfo>> edgeMap, 
                                  Set<String> visitedNodes,
                                  Map<String, Boolean> variableNameMap) {
        // 避免循环
        if (visitedNodes.contains(currentNodeId)) {
            return;
        }
        visitedNodes.add(currentNodeId);
        
        JsonNode currentNode = nodeMap.get(currentNodeId);
        if (currentNode == null) {
            return;
        }
        
        String nodeType = currentNode.get("type").asText();
        
        // 获取对应的处理器处理节点
        NodeProcessorFactory.getProcessor(nodeType)
                .process(drlBuilder, currentNode, currentNodeId, nodeMap, edgeMap, visitedNodes, variableNameMap);
    }

    /**
     * 构建节点映射
     */
    private Map<String, JsonNode> buildNodeMap(JsonNode nodesNode) {
        Map<String, JsonNode> nodeMap = new HashMap<>();
        for (JsonNode node : nodesNode) {
            String id = node.get("id").asText();
            nodeMap.put(id, node);
        }
        return nodeMap;
    }

    /**
     * 寻找指定类型的节点ID
     */
    private String findNodeIdByType(Map<String, JsonNode> nodeMap, String nodeType) {
        for (Map.Entry<String, JsonNode> entry : nodeMap.entrySet()) {
            JsonNode node = entry.getValue();
            if (node.has("type") && node.get("type").asText().equalsIgnoreCase(nodeType)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * 构建边映射
     */
    private Map<String, List<EdgeInfo>> buildEdgeMap(JsonNode edgesNode) {
        Map<String, List<EdgeInfo>> edgeMap = new HashMap<>();
        for (JsonNode edge : edgesNode) {
            String sourceId = edge.get("source").asText();
            String targetId = edge.get("target").asText();

            // 获取标签（用于判断条件分支）
            int label = edge.has("label") ? edge.get("label").asInt() : 0;

            // 获取决策用例ID
            String caseId = "";
            if (edge.has("properties") && edge.get("properties").has("decisionCase")) {
                caseId = edge.get("properties").get("decisionCase").asText();
            }

            // 添加到边映射
            if (!edgeMap.containsKey(sourceId)) {
                edgeMap.put(sourceId, new ArrayList<>());
            }
            edgeMap.get(sourceId).add(new EdgeInfo(sourceId, targetId, label, caseId));
        }
        return edgeMap;
    }

    /**
     * 写入DRL文件
     */
    public void writeDrlFile(String content, String filePath) throws IOException {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(content);
        }
    }
}
