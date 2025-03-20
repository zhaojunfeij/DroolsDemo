package com.example.service;

import com.example.domain.model.FlowChart;
import com.example.domain.model.Node;
import com.example.domain.model.Edge;
import com.example.domain.processor.NodeProcessorFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * JSON流程规则转Drools DRL文件服务
 */
@Service
public class JsonToDrlConverter {

    private final ObjectMapper objectMapper;
    private final NodeProcessorFactory nodeProcessorFactory;

    @Autowired
    public JsonToDrlConverter(ObjectMapper objectMapper, NodeProcessorFactory nodeProcessorFactory) {
        this.objectMapper = objectMapper;
        this.nodeProcessorFactory = nodeProcessorFactory;
    }

    /**
     * 将JSON文件转换为DRL内容
     */
    public String convertJsonToDrl(String jsonFilePath) throws IOException {
        // 读取JSON文件
        FlowChart flowChart = readJsonFile(jsonFilePath);
        
        // 构建DRL内容
        StringBuilder drlBuilder = new StringBuilder();
        
        // 添加包声明和导入
        addPackageAndImports(drlBuilder);
        
        // 添加规则声明
        String ruleName = new File(jsonFilePath).getName().replaceAll("\\.json$", "");
        addRuleDeclaration(drlBuilder, ruleName);
        
        // 处理节点
        processNodes(drlBuilder, flowChart);
        
        return drlBuilder.toString();
    }
    
    /**
     * 读取JSON文件
     */
    private FlowChart readJsonFile(String jsonFilePath) throws IOException {
        File jsonFile = new File(jsonFilePath);
        JsonNode rootNode = objectMapper.readTree(jsonFile);
        
        List<Node> nodes = objectMapper.convertValue(
            rootNode.get("nodes"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Node.class)
        );
        
        List<Edge> edges = objectMapper.convertValue(
            rootNode.get("edges"),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Edge.class)
        );
        
        FlowChart flowChart = FlowChart.builder()
            .id(rootNode.get("id").asText())
            .name(rootNode.get("name").asText())
            .nodes(nodes)
            .edges(edges)
            .build();
            
        flowChart.initializeMaps();
        return flowChart;
    }
    
    /**
     * 添加包声明和导入
     */
    private void addPackageAndImports(StringBuilder drlBuilder) {
        drlBuilder.append("package com.example.rules;\n\n");
        drlBuilder.append("import java.util.Map;\n");
        drlBuilder.append("import java.util.HashMap;\n");
        drlBuilder.append("import java.util.List;\n");
        drlBuilder.append("import java.util.ArrayList;\n\n");
    }
    
    /**
     * 添加规则声明
     */
    private void addRuleDeclaration(StringBuilder drlBuilder, String ruleName) {
        drlBuilder.append("rule \"").append(ruleName).append("\"\n");
        drlBuilder.append("    agenda-group \"").append(ruleName).append("\"\n");
        drlBuilder.append("    no-loop true\n");
        drlBuilder.append("    when\n");
        drlBuilder.append("        $inputData : Map()\n");
        drlBuilder.append("    then\n");
        drlBuilder.append("        // 流程开始\n");
        drlBuilder.append("        System.out.println(\"开始执行规则流程: ").append(ruleName).append("\");\n");
    }
    
    /**
     * 处理节点
     */
    private void processNodes(StringBuilder drlBuilder, FlowChart flowChart) {
        Node startNode = flowChart.getStartNode();
        Set<String> visitedNodes = new HashSet<>();
        Map<String, Boolean> variableNameMap = new HashMap<>();
        
        nodeProcessorFactory.getProcessor(startNode.getType())
            .process(drlBuilder, startNode, flowChart, visitedNodes, variableNameMap);
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