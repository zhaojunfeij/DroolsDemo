package com.example.parse.refactor.processor;

import com.example.model.FunctionResponse.FunctionInfo;
import com.example.model.Node;
import com.example.model.Variable;
import com.example.parse.refactor.converter.DrlContext;
import com.example.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 抽象节点处理器 - 实现模板方法模式
 * 为各种节点处理器提供通用的基础功能和处理流程框架
 * 子类只需实现特定的处理逻辑，提高代码复用性和一致性
 */
public abstract class AbstractNodeProcessor implements NodeProcessor {

    @Autowired
    private FunctionService functionService;

    /**
     * 获取函数代码映射
     * 从FunctionService动态获取，而非使用静态硬编码
     */
    private Map<String, FunctionInfo> getFunctionCodeMap() {
        return functionService.getFunctionCodeMap();
    }

    /**
     * 构建变量定义和初始化的DRL代码
     * 使用函数式编程和构建者模式，提高代码的可读性和可维护性
     */
    protected void buildVariable(DrlContext context, Variable variable) {
        // 1. 提取变量信息
        variable.extractInfo(getFunctionCodeMap());
        if (!variable.isValid()) {
            return;
        }

        // 2. 构建DRL代码
        StringBuilder drlBuilder = context.getDrlBuilder();
        buildVariableCode(drlBuilder, variable);

        // 3. 添加日志输出
        addVariableLog(drlBuilder, variable);
    }

    /**
     * 构建变量DRL代码
     */
    private void buildVariableCode(StringBuilder drlBuilder, Variable variable) {
        // 添加变量注释
        drlBuilder.append("        // 变量: ").append(variable.getName()).append("\n");

        // 变量存入上下文
        drlBuilder.append("        flowContext.put(\"")
                .append(variable.getVariableNo())
                .append("\", VariableUtils.evaluateExpression(flowContext, ")
                .append(variable.getExpression())
                .append("));\n");
    }

    /**
     * 添加变量日志输出
     */
    private void addVariableLog(StringBuilder drlBuilder, Variable variable) {
        drlBuilder.append("        System.out.println(\"变量取值结果")
                .append(variable.getVariableNo())
                .append(":\"+flowContext.get(\"")
                .append(variable.getVariableNo())
                .append("\"));\n");
    }

    /**
     * 获取上下文中的变量值，如果不存在则返回原值
     * 在多个子类中共享的工具方法
     */
    protected String getContextValue(DrlContext context, String key) {
        return Objects.isNull(context.getVariableMap().get(key)) ? key : context.getVariableMap().get(key);
    }

    /**
     * 处理下一个节点
     * 模板方法模式中的一部分，处理节点间的流转
     */
    protected void processNextNode(String currentNodeId, DrlContext context) {
        Optional.ofNullable(context.getEdgeMap().get(currentNodeId))
                .filter(edges -> !edges.isEmpty())
                .ifPresent(edges -> {
                    String nextNodeId = edges.get(0).getTarget();
                    processNode(nextNodeId, context);
                });
    }

    /**
     * 处理节点
     * 核心处理逻辑，遵循模板方法模式
     */
    protected void processNode(String nodeId, DrlContext context) {
        // 记录已访问节点
        context.getVisitedNodes().add(nodeId);

        // 获取并处理节点
        Optional<Node> nodeOpt = Optional.ofNullable(context.getNodeMap().get(nodeId));
        if (nodeOpt.isPresent()) {
            Node node = nodeOpt.get();
            // 工厂模式获取适当的处理器
            NodeProcessorFactory factory = NodeProcessorFactory.getInstance();
            NodeProcessor processor = factory.getProcessor(node.getType());

            if (processor != null) {
                processor.process(node, nodeId, context);
            } else {
                handleUnknownNodeType(node.getType(), context);
            }
        }
    }

    /**
     * 处理未知节点类型
     * 优化点：抽取方法提高可读性和可维护性
     */
    private void handleUnknownNodeType(String nodeType, DrlContext context) {
        context.getDrlBuilder().append("        // 未知节点类型: ").append(nodeType).append("\n");
    }
} 