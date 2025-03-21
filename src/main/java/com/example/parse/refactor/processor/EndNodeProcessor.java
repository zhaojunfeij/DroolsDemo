package com.example.parse.refactor.processor;

import com.example.parse.refactor.converter.DrlContext;
import com.example.model.Node;
import com.example.model.NodeType;
import com.example.model.ResultSetting;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 结束节点处理器
 */
@Component
public class EndNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        drlBuilder.append("        // 结束节点\n");
        
        // 获取结果设置
        List<ResultSetting> resultSettingList = node.getProperties().getResultSettingList();
        if (resultSettingList != null && !resultSettingList.isEmpty()) {
            for (ResultSetting resultSetting : resultSettingList) {
                String key = resultSetting.getKey();
                String type = resultSetting.getType();
                String value = resultSetting.getValue();
                
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
        return NodeType.END.getType();
    }
} 