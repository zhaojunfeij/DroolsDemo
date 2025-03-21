package com.example.parse.refactor.processor;

import com.example.parse.refactor.converter.DrlContext;
import com.example.model.Node;
import com.example.model.NodeType;
import com.example.model.ResultSetting;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.Map;
import java.util.HashMap;

/**
 * 结束节点处理器 - 负责处理流程的结束节点
 * 将流程执行结果映射到输出结果集，完成规则流程的收尾工作
 */
@Component
public class EndNodeProcessor extends AbstractNodeProcessor {
    
    // 结果处理器映射表，使用策略模式处理不同类型的结果设置
    private final Map<String, Consumer<ResultContext>> resultProcessors = new HashMap<>();
    
    // 构造函数中初始化处理器映射
    public EndNodeProcessor() {
        resultProcessors.put("requestParams", this::processRequestParams);
        resultProcessors.put("node", this::processNodeResult);
        resultProcessors.put("contextParams", this::processContextParams);
    }
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        drlBuilder.append("        // 结束节点\n");
        
        // 处理结果设置
        processResultSettings(node, drlBuilder);
        
        // 添加流程结束日志
        drlBuilder.append("        System.out.println(\"规则流程执行完成\");\n");
    }
    
    /**
     * 处理结果设置列表
     */
    private void processResultSettings(Node node, StringBuilder drlBuilder) {
        Optional.ofNullable(node.getProperties().getResultSettingList())
                .ifPresent(resultSettings -> 
                    resultSettings.forEach(resultSetting -> 
                        processSingleResult(resultSetting, drlBuilder)
                    )
                );
    }
    
    /**
     * 处理单个结果设置
     */
    private void processSingleResult(ResultSetting resultSetting, StringBuilder drlBuilder) {
        String key = resultSetting.getKey();
        String type = resultSetting.getType();
        
        // 添加结果设置注释
        drlBuilder.append("        // 设置结果: ").append(key).append("\n");
        
        // 使用策略模式处理不同类型的结果
        resultProcessors.getOrDefault(type, ctx -> 
            drlBuilder.append("        // 未知结果类型: ").append(type).append("\n")
        ).accept(new ResultContext(resultSetting, drlBuilder));
    }
    
    /**
     * 处理请求参数类型的结果
     */
    private void processRequestParams(ResultContext ctx) {
        ctx.drlBuilder.append("        update($inputData); // 更新请求参数\n");
    }
    
    /**
     * 处理节点结果类型的结果
     */
    private void processNodeResult(ResultContext ctx) {
        ResultSetting setting = ctx.resultSetting;
        StringBuilder drlBuilder = ctx.drlBuilder;
        
        drlBuilder.append("        // 节点结果处理\n");
        drlBuilder.append("        $inputData.put(\"")
                .append(setting.getKey())
                .append("\", flowContext.get(\"")
                .append(setting.getValue())
                .append("_0\"));\n");
    }
    
    /**
     * 处理上下文参数类型的结果
     */
    private void processContextParams(ResultContext ctx) {
        ResultSetting setting = ctx.resultSetting;
        StringBuilder drlBuilder = ctx.drlBuilder;
        
        drlBuilder.append("        // 上下文参数处理\n");
        drlBuilder.append("        $inputData.put(\"")
                .append(setting.getKey())
                .append("\", flowContext.get(\"")
                .append(setting.getValue())
                .append("\"));\n");
    }
    
    /**
     * 结果处理上下文，封装处理结果所需的参数
     */
    private static class ResultContext {
        final ResultSetting resultSetting;
        final StringBuilder drlBuilder;
        
        ResultContext(ResultSetting resultSetting, StringBuilder drlBuilder) {
            this.resultSetting = resultSetting;
            this.drlBuilder = drlBuilder;
        }
    }
    
    @Override
    public String getNodeType() {
        return NodeType.END.getType();
    }
} 