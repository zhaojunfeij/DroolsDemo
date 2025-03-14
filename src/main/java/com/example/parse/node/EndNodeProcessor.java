package com.example.parse.node;

import com.example.model.EdgeInfo;
import com.example.utils.DrlUtils;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 结束节点处理器
 */
public class EndNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(StringBuilder drlBuilder, 
                      JsonNode node, 
                      String nodeId, 
                      Map<String, JsonNode> nodeMap, 
                      Map<String, List<EdgeInfo>> edgeMap, 
                      Set<String> visitedNodes,
                      Map<String, Boolean> variableNameMap) {
        drlBuilder.append("        // 结束节点\n");
        
        // 获取结果设置
        JsonNode propertiesNode = node.get("properties");
        if (propertiesNode.has("resultSettingList")) {
            JsonNode resultSettingListNode = propertiesNode.get("resultSettingList");
            
            for (JsonNode resultSetting : resultSettingListNode) {
                String key = resultSetting.get("key").asText();
                String type = resultSetting.get("type").asText();
                String value = resultSetting.get("value").asText();
                
                drlBuilder.append("        // 设置结果: ").append(key).append("\n");
                
                switch (type) {
                    case "requestParams":
                        drlBuilder.append("        update($inputData); // 更新请求参数\n");
                        break;
                    case "node":
                        drlBuilder.append("        // 节点结果处理\n");
                        drlBuilder.append("        $inputData.put(\"").append(key).append("\", flowContext.get(\"").append(value + "_0").append("\"));\n");
                        break;
                    case "contextParams":
                        drlBuilder.append("        // 上下文参数处理\n");
                        drlBuilder.append("        $inputData.put(\"").append(key).append("\", flowContext.get(\"").append(value).append("\"));\n");
                        break;
                }
            }
        }
        
        drlBuilder.append("        System.out.println(\"规则流程执行完成\");\n");
    }
    
    @Override
    public String getNodeType() {
        return DrlUtils.END_NODE;
    }
} 