package com.example.parse.refactor.processor;

import com.example.model.Func;
import com.example.parse.refactor.converter.DrlContext;
import com.example.model.Node;
import com.example.model.Variable;
import com.alibaba.fastjson.JSON;
import com.example.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

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
    protected Map<String, String> getFunctionCodeMap() {
        return functionService.getFunctionCodeMap();
    }
    
    /**
     * 处理节点中的变量
     * 构建变量定义和初始化的DRL代码
     */
    protected void buildVariable(DrlContext context, Variable variable) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        String variableName = variable.getName();
        String variableNum = variable.getVariableNo();
        String variableNo = getContextValue(context, variableNum);
       // 处理表达式树或使用默认值
        Optional<Object> expressionOpt = getExpressionValue(variable);
        if (!expressionOpt.isPresent()) {
            return;
        }
        Object expressionJson = expressionOpt.get();

        Func func = JSON.parseObject(expressionJson.toString(), Func.class);

        if ("SET_RESULT".equals(func.getCode()) && CollectionUtils.isEmpty(func.getParams())) {
            return;
        }
        // 添加变量注释
        drlBuilder.append("        // 变量: ").append(variableName).append("\n");

        // 变量存入上下文
        drlBuilder.append("        flowContext.put(\"").append(variableNo).append("\", ");

        // 使用动态获取的函数代码映射
        Map<String, String> functionCodeMap = getFunctionCodeMap();
        String methodName = functionCodeMap.getOrDefault(func.getCode(), func.getCode());
        
        drlBuilder.append("VariableUtils.evaluateExpression(")
                .append("flowContext, ")
                .append(JSON.toJSONString(expressionJson).replaceAll(func.getCode(), methodName))
                .append(")");

        drlBuilder.append(");\n");

        // 添加日志输出
        drlBuilder.append("        System.out.println(\"变量取值结果");
        drlBuilder.append(variableNo).append(":\"+flowContext.get(\"").append(variableNo).append("\"));\n");
    }

    /**
     * 获取表达式树
     * 优化点：使用Optional简化空值处理
     */
    private Optional<Object> getExpressionValue(Variable variable) {
        return Optional.ofNullable(variable.getData())
                .map(data -> data.getExpressionTreeJson());
    }

    /**
     * 获取上下文中的变量值，如果不存在则返回原值
     * 在多个子类中共享的工具方法
     */
    protected String getContextValue(DrlContext context, String key) {
        return Objects.isNull(context.getVariableMap().get(key)) ? key : context.getVariableMap().get(key);
    }

    /**
     * 检查字符串是否为数字
     * 使用函数式编程风格和更安全的异常处理
     */
    protected boolean isNumeric(String str) {
        return checkString(str, s -> {
            try {
                Double.parseDouble(s);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        });
    }

    /**
     * 检查字符串是否为boolean
     * 使用函数式编程风格提高代码一致性
     */
    protected boolean isBoolean(String str) {
        return checkString(str, s -> {
            String lowerStr = s.toLowerCase().trim();
            return lowerStr.equals("true") || lowerStr.equals("false");
        });
    }

    /**
     * 字符串校验的通用方法
     * 优化点：抽取共用逻辑，减少重复代码
     */
    private boolean checkString(String str, Predicate<String> checker) {
        if (str == null) {
            return false;
        }
        try {
            return checker.test(str);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 判断是否为等于或不等于操作符
     * 优化点：更清晰的方法命名和实现
     */
    protected boolean isEqOrNotEq(String operator) {
        return "EQ".equals(operator) || "NOT_EQ".equals(operator);
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