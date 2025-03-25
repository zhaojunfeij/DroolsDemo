package com.example.parse.refactor.processor;

import com.example.model.*;
import com.example.parse.refactor.converter.DrlContext;
import com.example.utils.NodeProcessorUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 判断节点处理器 - 生成判断节点的DRL规则代码
 */
@Component
public class JudgeNodeProcessor extends AbstractNodeProcessor {

    // 常量定义
    private static final String OPERATOR_TYPE_DATA = "data";
    private static final String OPERATOR_TYPE_VAR = "var";
    private static final String TYPE_NUMBER = "BigDecimal";
    private static final String TYPE_BOOLEAN = "Boolean";
    private static final String TYPE_STRING = "String";

    // 运算符映射
    private static final Map<String, String> NUMBER_OPERATOR_SYMBOLS = new HashMap<>();
    private static final Map<String, BiConsumer<StringBuilder, ComparisonContext>> COMPARISON_STRATEGIES = new HashMap<>();
    // 比较表达式生成器映射
    private static final Map<String, BiConsumer<StringBuilder, ComparisonContext>> EXPRESSION_BUILDERS = new HashMap<>();

    static {
        // 初始化映射
        NUMBER_OPERATOR_SYMBOLS.put("GT", " > ");
        NUMBER_OPERATOR_SYMBOLS.put("GE", " >= ");
        NUMBER_OPERATOR_SYMBOLS.put("LT", " < ");
        NUMBER_OPERATOR_SYMBOLS.put("LE", " <= ");
        
        COMPARISON_STRATEGIES.put(OPERATOR_TYPE_DATA, JudgeNodeProcessor::buildDataComparisonExpression);
        COMPARISON_STRATEGIES.put(OPERATOR_TYPE_VAR, JudgeNodeProcessor::buildVariableComparisonExpression);
        
        // 初始化表达式生成器
        EXPRESSION_BUILDERS.put("EQ", JudgeNodeProcessor::buildEqualsExpression);
        EXPRESSION_BUILDERS.put("NOT_EQ", JudgeNodeProcessor::buildNotEqualsExpression);
        EXPRESSION_BUILDERS.put("NUMBER", JudgeNodeProcessor::buildNumberComparisonExpression);
        EXPRESSION_BUILDERS.put("DEFAULT", JudgeNodeProcessor::buildDefaultComparisonExpression);
    }

    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        drlBuilder.append("        // 判断节点\n");

        // 处理节点变量
        Optional.ofNullable(node.getProperties().getNodeVariableList())
                .ifPresent(variables -> variables.forEach(variable -> buildVariable(context, variable)));

        // 处理判断逻辑
        List<RelationShipGroup> groups = node.getProperties().getRelationShipGroupList();
        if (groups != null && !groups.isEmpty() && groups.stream()
                .anyMatch(g -> g.getRelationShipList() != null && !g.getRelationShipList().isEmpty())) {
            processRelationships(nodeId, groups, context);
        } else {
            // 无条件判断，继续执行下一节点
            drlBuilder.append("        // 无条件判断，继续执行下一节点\n");
            processNextNode(nodeId, context);
        }
    }

    /**
     * 处理关系组和条件分支
     */
    private void processRelationships(String nodeId, List<RelationShipGroup> groups, DrlContext context) {
        Map<String, Boolean> variableNameMap = new HashMap<>();
        
        // 获取节点的边并按标签排序
        List<Edge> edges = context.getEdgeMap().getOrDefault(nodeId, Collections.emptyList());
        edges.sort(Comparator.comparing(Edge::getLabel));

        // 处理每个关系组
        groups.stream()
            .filter(group -> group.getRelationShipList() != null && !group.getRelationShipList().isEmpty())
            .forEach(group -> {
                context.getDrlBuilder().append("        // 条件分支\n");
                processConditionBranches(group.getRelationShipList(), edges, context, variableNameMap);
            });
    }

    /**
     * 处理条件分支
     */
    private void processConditionBranches(List<RelationShip> relationships, List<Edge> edges, 
                                         DrlContext context, Map<String, Boolean> variableNameMap) {
        // 为每个有效边生成条件分支
        edges.stream()
            .filter(edge -> edge.getLabel() != null && edge.getLabel() > 0)
            .forEach(edge -> {
                int label = edge.getLabel();
                String targetNodeId = edge.getTarget();
                
                // 查找匹配的关系定义
                relationships.stream()
                    .filter(r -> r.getRelationshipNo() != null && 
                                 Integer.parseInt(r.getRelationshipNo()) == label)
                    .findFirst()
                    .ifPresent(relation -> buildConditionBranch(
                        relation, label, targetNodeId, context, variableNameMap));
            });
    }

    /**
     * 构建单个条件分支
     */
    private void buildConditionBranch(RelationShip relation, int label, String targetNodeId, 
                                     DrlContext context, Map<String, Boolean> variableNameMap) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        // 获取判断条件参数
        String variableNo = getContextValue(context, relation.getVariableNo());
        String operator = relation.getOperator();
        String operatorValue = relation.getOperatorValue();
        String operatorValueType = relation.getOperatorValueType();
        
        // 添加条件注释和变量处理
        drlBuilder.append("        // 条件 ").append(label).append("\n");
        processVariableValue(drlBuilder, variableNo, operatorValue, variableNameMap);
        
        // 构建条件表达式
        drlBuilder.append("        if (");
        
        // 使用策略模式选择比较表达式构建方法
        ComparisonContext ctx = new ComparisonContext(
            drlBuilder, variableNo, operator, operatorValue, operatorValueType);
        
        COMPARISON_STRATEGIES.getOrDefault(operatorValueType, 
            (builder, c) -> builder.append("true /* 未知比较类型 */")).accept(drlBuilder, ctx);
        
        // 条件分支代码块
        drlBuilder.append(") {\n");
        processNode(targetNodeId, context);
        drlBuilder.append("        }\n");
    }

    /**
     * 处理变量取值逻辑
     */
    private void processVariableValue(StringBuilder drlBuilder, String variableNo, 
                                    String operatorValue, Map<String, Boolean> variableNameMap) {
        // 已处理过的变量，跳过
        if (variableNameMap.getOrDefault(variableNo, false)) {
            return;
        }
        
        // 变量名和转换后的变量名
        String variableConvert = variableNo + "Convert";
        String variableType = determineVariableType(operatorValue);
        
        // 添加变量处理代码
        drlBuilder.append("        Object ").append(variableConvert)
                 .append(" = VariableUtils.getVariableValue(flowContext, \"")
                 .append(variableNo).append("\");\n")
                 .append("        ").append(variableType)
                 .append(" ").append(variableNo)
                 .append(" = ");
        
        // 根据变量类型生成转换代码
        switch (variableType) {
            case TYPE_NUMBER:
                drlBuilder.append("new BigDecimal(").append(variableConvert).append(".toString())");
                break;
            case TYPE_BOOLEAN:
                drlBuilder.append("(Boolean)").append(variableConvert);
                break;
            default:
                drlBuilder.append("(String)").append(variableConvert);
        }
        
        drlBuilder.append(";\n")
                 .append("        System.out.println(\"开始计算变量取值")
                 .append(variableNo).append(": \" + ").append(variableNo).append(");\n");
                
        // 标记变量已处理
        variableNameMap.put(variableNo, true);
    }
    
    /**
     * 确定变量类型
     */
    private String determineVariableType(String value) {
        if (NodeProcessorUtils.isNumeric(value)) {
            return TYPE_NUMBER;
        } else if (NodeProcessorUtils.isBoolean(value)) {
            return TYPE_BOOLEAN;
        } else {
            return TYPE_STRING;
        }
    }
    
    /**
     * 构建常量比较表达式
     */
    private static void buildDataComparisonExpression(StringBuilder drlBuilder, ComparisonContext ctx) {
        // 选择合适的表达式生成器
        BiConsumer<StringBuilder, ComparisonContext> builder;
        
        if (NodeProcessorUtils.isNumeric(ctx.value) && !NodeProcessorUtils.isEqOrNotEq(ctx.operator)) {
            builder = EXPRESSION_BUILDERS.get("NUMBER");
        } else if ("EQ".equals(ctx.operator)) {
            builder = EXPRESSION_BUILDERS.get("EQ");
        } else if ("NOT_EQ".equals(ctx.operator)) {
            builder = EXPRESSION_BUILDERS.get("NOT_EQ");
        } else {
            builder = EXPRESSION_BUILDERS.get("DEFAULT");
        }
        
        // 生成表达式
        builder.accept(drlBuilder, ctx);
    }
    
    /**
     * 构建数值比较表达式
     */
    private static void buildNumberComparisonExpression(StringBuilder drlBuilder, ComparisonContext ctx) {
        drlBuilder.append(ctx.variable)
                 .append(".compareTo(new BigDecimal(")
                 .append(ctx.value)
                 .append("))")
                 .append(NUMBER_OPERATOR_SYMBOLS.getOrDefault(ctx.operator, " " + ctx.operator + " "))
                 .append("0");
    }
    
    /**
     * 构建等于比较表达式
     */
    private static void buildEqualsExpression(StringBuilder drlBuilder, ComparisonContext ctx) {
        drlBuilder.append(ctx.variable).append(" != null && ");
        appendEqualsCheck(drlBuilder, ctx.variable, ctx.value);
    }
    
    /**
     * 构建不等于比较表达式
     */
    private static void buildNotEqualsExpression(StringBuilder drlBuilder, ComparisonContext ctx) {
        drlBuilder.append(ctx.variable).append(" == null || !(");
        appendEqualsCheck(drlBuilder, ctx.variable, ctx.value);
        drlBuilder.append(")");
    }
    
    /**
     * 构建默认比较表达式
     */
    private static void buildDefaultComparisonExpression(StringBuilder drlBuilder, ComparisonContext ctx) {
        drlBuilder.append(ctx.variable).append(" ").append(ctx.operator).append(" ");
        
        if (!NodeProcessorUtils.isNumeric(ctx.value) && !NodeProcessorUtils.isBoolean(ctx.value)) {
            drlBuilder.append("\"").append(ctx.value).append("\"");
        } else {
            drlBuilder.append(ctx.value);
        }
    }
    
    /**
     * 添加相等性检查代码
     */
    private static void appendEqualsCheck(StringBuilder drlBuilder, String variable, String value) {
        if (NodeProcessorUtils.isNumeric(value)) {
            drlBuilder.append("new BigDecimal(\"").append(value).append("\").equals(")
                     .append(variable).append(")");
        } else if (NodeProcessorUtils.isBoolean(value)) {
            drlBuilder.append(variable).append(".equals(").append(value).append(")");
        } else {
            drlBuilder.append(variable).append(".equals(\"").append(value).append("\")");
        }
    }
    
    /**
     * 构建变量比较表达式
     */
    private static void buildVariableComparisonExpression(StringBuilder drlBuilder, ComparisonContext ctx) {
        drlBuilder.append("VariableUtils.compareVariables(flowContext, \"")
                 .append(ctx.variable).append("\", \"")
                 .append(ctx.operator).append("\", \"")
                 .append(ctx.value).append("\")");
    }
    
    @Override
    public String getNodeType() {
        return NodeType.JUDGE.getType();
    }
    
    /**
     * 比较上下文类
     */
    private static class ComparisonContext {
        private final StringBuilder builder;
        private final String variable;
        private final String operator;
        private final String value;
        private final String valueType;
        
        public ComparisonContext(StringBuilder builder, String variable, 
                               String operator, String value, String valueType) {
            this.builder = builder;
            this.variable = variable;
            this.operator = operator;
            this.value = value;
            this.valueType = valueType;
        }
    }
} 