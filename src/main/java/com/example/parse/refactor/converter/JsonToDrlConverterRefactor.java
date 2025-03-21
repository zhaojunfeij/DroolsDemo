package com.example.parse.refactor.converter;

import com.example.model.Edge;
import com.example.model.FlowGraph;
import com.example.model.Node;
import com.example.utils.FileUtils;
import com.example.utils.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * JSON流程规则转Drools DRL文件工具（重构版）
 */
@Service
public class JsonToDrlConverterRefactor {
    
    @Autowired
    private JsonParser jsonParser;
    
    @Autowired
    private DrlGenerator drlGenerator;
    
    @Autowired
    private FileUtils fileUtils;
    
    /**
     * 将JSON文件转换为DRL内容
     * 
     * @param jsonFilePath JSON文件路径
     * @return DRL内容
     * @throws IOException IO异常
     */
    public String convertJsonToDrl(String jsonFilePath) throws IOException {
        // 解析JSON文件
        FlowGraph flowGraph = jsonParser.parseJson(jsonFilePath);
        
        // 构建节点映射和边映射
        Map<String, Node> nodeMap = jsonParser.buildNodeMap(flowGraph.getNodes());
        Map<String, List<Edge>> edgeMap = jsonParser.buildEdgeMap(flowGraph.getEdges());
        
        // 创建DRL生成上下文
        DrlContext context = createDrlContext(jsonFilePath, nodeMap, edgeMap);
        
        // 生成DRL内容
        return drlGenerator.generateDrl(context);
    }
    
    /**
     * 创建DRL生成上下文
     */
    private DrlContext createDrlContext(String jsonFilePath, Map<String, Node> nodeMap, Map<String, List<Edge>> edgeMap) {
        DrlContext context = new DrlContext();
        
        // 设置规则名称
        String ruleName = new File(jsonFilePath).getName().replaceAll("\\.json$", "");
        context.setRuleName(ruleName);
        
        // 设置节点映射和边映射
        context.setNodeMap(nodeMap);
        context.setEdgeMap(edgeMap);
        
        // 初始化访问过的节点集合
        context.setVisitedNodes(new HashSet<>());
        
        // 初始化变量映射
        context.initDefaultVariableMap();
        
        return context;
    }
    
    /**
     * 将DRL内容写入文件
     * 
     * @param drlContent DRL内容
     * @param outputDrlPath 输出文件路径
     * @throws IOException IO异常
     */
    public void writeDrlFile(String drlContent, String outputDrlPath) throws IOException {
        fileUtils.writeDrlFile(drlContent, outputDrlPath);
    }
    
    /**
     * 主方法，用于测试
     */
    public void convertFile(String inputJsonPath, String outputDrlPath) {
        try {
            String drlContent = convertJsonToDrl(inputJsonPath);
            writeDrlFile(drlContent, outputDrlPath);
            System.out.println("成功将JSON转换为DRL文件: " + outputDrlPath);
        } catch (Exception e) {
            System.err.println("转换失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 