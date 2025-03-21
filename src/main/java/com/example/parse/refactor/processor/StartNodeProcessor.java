package com.example.parse.refactor.processor;

import com.example.parse.refactor.converter.DrlContext;
import com.example.model.Node;
import com.example.model.NodeType;
import org.springframework.stereotype.Component;

/**
 * 开始节点处理器
 */
@Component
public class StartNodeProcessor extends AbstractNodeProcessor {
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        drlBuilder.append("        // 开始节点\n");
        drlBuilder.append("        Map<String, Object> flowContext = new HashMap<>();\n");
        drlBuilder.append("        flowContext.putAll($inputData);\n");
        
        // 处理下一个节点
        processNextNode(nodeId, context);
    }
    
    @Override
    public String getNodeType() {
        return NodeType.START.getType();
    }
} 