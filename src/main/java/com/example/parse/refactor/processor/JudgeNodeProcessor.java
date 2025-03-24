package com.example.parse.refactor.processor;

import com.example.model.*;
import com.example.parse.refactor.converter.DrlContext;
import com.example.utils.NodeProcessorUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 判断节点处理器 - 负责生成判断节点的DRL规则代码
 * 采用函数式编程和模板方法模式，提供清晰的代码结构
 */
@Component
public class JudgeNodeProcessor extends AbstractNodeProcessor {
    
    // 操作类型常量
    private static final String OPERATOR_TYPE_DATA = "data";  // 变量与常量比较
    private static final String OPERATOR_TYPE_VAR = "var";    // 变量与变量比较
    
    // 变量类型常量
    private static final String TYPE_NUMBER = "BigDecimal";
    private static final String TYPE_BOOLEAN = "Boolean";
    private static final String TYPE_STRING = "String";
    
    // 操作符映射表，提高代码可读性和可维护性
    private static final Map<String, String> OPERATOR_SYMBOLS = new HashMap<>();
    
    static {
        // 初始化操作符映射
        OPERATOR_SYMBOLS.put("GT", " > ");
        OPERATOR_SYMBOLS.put("GE", " >= ");
        OPERATOR_SYMBOLS.put("LT", " < ");
        OPERATOR_SYMBOLS.put("LE", " <= ");
        OPERATOR_SYMBOLS.put("EQ", " == ");
        OPERATOR_SYMBOLS.put("NOT_EQ", " != ");
    }
    
    @Override
    public void process(Node node, String nodeId, DrlContext context) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        // 添加节点注释
        drlBuilder.append("        // 判断节点\n");
        
        // 处理节点变量
        processNodeVariables(node, context);
        
        // 处理判断逻辑和条件分支
        List<RelationShipGroup> relationShipGroups = node.getProperties().getRelationShipGroupList();
        if (relationShipGroups != null && hasValidRelationships(relationShipGroups)) {
            processRelationships(nodeId, relationShipGroups, context);
        } else {
            // 无条件判断，继续执行下一节点
            drlBuilder.append("        // 无条件判断，继续执行下一节点\n");
            processNextNode(nodeId, context);
        }
    }
    
    /**
     * 处理节点变量列表
     */
    private void processNodeVariables(Node node, DrlContext context) {
        List<Variable> variables = node.getProperties().getNodeVariableList();
        if (variables != null) {
            variables.forEach(variable -> buildVariable(context, variable));
        }
    }
    
    /**
     * 检查是否存在有效的关系定义
     */
    private boolean hasValidRelationships(List<RelationShipGroup> relationShipGroups) {
        if (relationShipGroups.isEmpty()) {
            return false;
        }
        
        for (RelationShipGroup group : relationShipGroups) {
            if (group.getRelationShipList() != null && !group.getRelationShipList().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 处理关系组和条件分支
     */
    private void processRelationships(String nodeId, List<RelationShipGroup> relationShipGroups, DrlContext context) {
        Map<String, Boolean> variableNameMap = new HashMap<>();
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        // 获取节点的边并按标签排序
        List<Edge> edges = getEdgesForNode(nodeId, context);
        edges.sort(Comparator.comparing(Edge::getLabel));
        
        // 处理每个关系组
        for (RelationShipGroup group : relationShipGroups) {
            if (group.getRelationShipList() == null || group.getRelationShipList().isEmpty()) {
                continue;
            }
            
            drlBuilder.append("        // 条件分支\n");
            processConditionBranches(group.getRelationShipList(), edges, context, variableNameMap);
        }
    }
    
    /**
     * 获取节点的所有边
     */
    private List<Edge> getEdgesForNode(String nodeId, DrlContext context) {
        List<Edge> edges = context.getEdgeMap().get(nodeId);
        return edges != null ? edges : Collections.emptyList();
    }
    
    /**
     * 处理条件分支
     */
    private void processConditionBranches(List<RelationShip> relationships, List<Edge> edges, 
                                          DrlContext context, Map<String, Boolean> variableNameMap) {
        // 为每个有效边生成条件分支
        for (Edge edge : edges) {
            Integer label = edge.getLabel();
            if (label != null && label > 0) {
                generateConditionBranch(relationships, edge, context, variableNameMap);
            }
        }
    }
    
    /**
     * 生成单个条件分支代码
     */
    private void generateConditionBranch(List<RelationShip> relationships, Edge edge, 
                                        DrlContext context, Map<String, Boolean> variableNameMap) {
        int conditionLabel = edge.getLabel();
        String targetNodeId = edge.getTarget();
        
        // 查找对应的关系定义
        for (RelationShip relation : relationships) {
            if (matchesConditionLabel(relation, conditionLabel)) {
                buildConditionBranch(relation, conditionLabel, targetNodeId, context, variableNameMap);
                break;
            }
        }
    }
    
    /**
     * 检查关系是否匹配条件标签
     */
    private boolean matchesConditionLabel(RelationShip relation, int conditionLabel) {
        String relationshipNo = relation.getRelationshipNo();
        return relationshipNo != null && Integer.parseInt(relationshipNo) == conditionLabel;
    }
    
    /**
     * 构建单个条件分支
     */
    private void buildConditionBranch(RelationShip relation, int conditionLabel, 
                                     String targetNodeId, DrlContext context, Map<String, Boolean> variableNameMap) {
        StringBuilder drlBuilder = context.getDrlBuilder();
        
        // 获取判断条件参数
        String variableId = relation.getVariableNo();
        String variableNo = getMappedVariableName(context.getVariableMap(), variableId);
        String operator = relation.getOperator();
        String operatorValue = relation.getOperatorValue();
        String operatorValueType = relation.getOperatorValueType();
        
        // 添加条件注释
        drlBuilder.append("        // 条件 ").append(conditionLabel).append("\n");
        
        // 处理变量取值
        processVariableValue(drlBuilder, variableNo, operatorValue, variableNameMap);
        
        // 构建条件表达式
        buildConditionExpression(drlBuilder, variableNo, operator, operatorValue, operatorValueType);
        
        // 条件分支代码块开始
        drlBuilder.append(") {\n");
        
        // 递归处理目标节点
        processNode(targetNodeId, context);
        
        // 条件分支代码块结束
        drlBuilder.append("        }\n");
    }
    
    /**
     * 获取映射后的变量名
     */
    private String getMappedVariableName(Map<String, String> variableMap, String variableId) {
        String mappedName = variableMap.get(variableId);
        return mappedName != null ? mappedName : variableId;
    }
    
    /**
     * 处理变量取值逻辑
     */
    private void processVariableValue(StringBuilder drlBuilder, String variableNo, 
                                    String operatorValue, Map<String, Boolean> variableNameMap) {
        // 已处理过的变量，跳过
        Boolean processed = variableNameMap.get(variableNo);
        if (processed != null && processed) {
            return;
        }
        
        // 变量转换变量名
        String variableConvert = variableNo + "Convert";
        
        // 添加取值代码
        appendVariableGetterCode(drlBuilder, variableNo, variableConvert);
        
        // 添加类型转换代码
        appendVariableTypeConversionCode(drlBuilder, variableNo, variableConvert, operatorValue);
        
        // 添加日志输出
        drlBuilder.append("        System.out.println(\"开始计算变量取值")
                .append(variableNo).append(": \" + ").append(variableNo).append(");\n");
                
        // 标记变量已处理
        variableNameMap.put(variableNo, true);
    }
    
    /**
     * 添加变量获取代码
     */
    private void appendVariableGetterCode(StringBuilder drlBuilder, String variableNo, String variableConvert) {
        drlBuilder.append("        Object ").append(variableConvert).append(" = ")
                .append("VariableUtils.getVariableValue(flowContext, \"").append(variableNo).append("\");\n");
    }
    
    /**
     * 添加变量类型转换代码
     * 根据操作值类型确定变量的Java类型，并生成相应的类型转换代码
     */
    private void appendVariableTypeConversionCode(StringBuilder drlBuilder, String variableNo, 
                                                String variableConvert, String operatorValue) {
        String variableType;
        String conversionCode;
        
        if (NodeProcessorUtils.isNumeric(operatorValue)) {
            variableType = TYPE_NUMBER;
            conversionCode = "new BigDecimal(" + variableConvert + ".toString())";
        } else if (NodeProcessorUtils.isBoolean(operatorValue)) {
            variableType = TYPE_BOOLEAN;
            conversionCode = "(Boolean)" + variableConvert;
        } else {
            variableType = TYPE_STRING;
            conversionCode = "(String)" + variableConvert;
        }
        
        // 生成变量声明和赋值语句
        drlBuilder.append("        ").append(variableType)
                 .append(" ").append(variableNo)
                 .append(" = ").append(conversionCode)
                 .append(";\n");
    }
    
    /**
     * 构建条件表达式
     */
    private void buildConditionExpression(StringBuilder drlBuilder, String variableNo, 
                                        String operator, String operatorValue, String operatorValueType) {
        drlBuilder.append("        if (");
        
        if (OPERATOR_TYPE_DATA.equals(operatorValueType)) {
            // 变量与常量比较
            if (NodeProcessorUtils.isNumeric(operatorValue) && !NodeProcessorUtils.isEqOrNotEq(operator)) {
                buildNumberComparisonExpression(drlBuilder, variableNo, operator, operatorValue);
            } else {
                buildSimpleComparisonExpression(drlBuilder, variableNo, operator, operatorValue);
            }
        } else if (OPERATOR_TYPE_VAR.equals(operatorValueType)) {
            // 变量与变量比较
            buildVariableComparisonExpression(drlBuilder, variableNo, operator, operatorValue);
        }
    }
    
    /**
     * 构建简单比较表达式
     */
    private void buildSimpleComparisonExpression(StringBuilder drlBuilder, String variableNo, 
                                              String operator, String value) {
        String operatorSymbol = OPERATOR_SYMBOLS.get(operator);
        if (operatorSymbol == null) {
            operatorSymbol = " " + operator + " ";
        }
        
        drlBuilder.append(variableNo).append(operatorSymbol);
        
        // 如果是字符串且不是布尔值，需要添加引号
        if (!NodeProcessorUtils.isNumeric(value) && !NodeProcessorUtils.isBoolean(value)) {
            drlBuilder.append("\"").append(value).append("\"");
        } else {
            drlBuilder.append(value);
        }
    }
    
    /**
     * 构建数字比较表达式
     */
    private void buildNumberComparisonExpression(StringBuilder drlBuilder, String variableNo, 
                                              String operator, String value) {
        drlBuilder.append(variableNo).append(".compareTo(new BigDecimal(")
                .append(value).append("))");
                
        String operatorSymbol = OPERATOR_SYMBOLS.get(operator);
        if (operatorSymbol == null) {
            operatorSymbol = " " + operator + " ";
        }
        
        drlBuilder.append(operatorSymbol).append("0");
    }
    
    /**
     * 构建变量比较表达式
     */
    private void buildVariableComparisonExpression(StringBuilder drlBuilder, String variable1, 
                                                String operator, String variable2) {
        drlBuilder.append("VariableUtils.compareVariables(flowContext, \"")
                .append(variable1).append("\", \"")
                .append(operator).append("\", \"")
                .append(variable2).append("\")");
    }
    
    @Override
    public String getNodeType() {
        return NodeType.JUDGE.getType();
    }
} 