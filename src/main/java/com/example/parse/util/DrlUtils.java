package com.example.parse.util;

import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * DRL工具类
 */
public class DrlUtils {
    
    // 节点类型常量
    public static final String START_NODE = "start";
    public static final String END_NODE = "end";
    public static final String JUDGE_NODE = "judge";
    public static final String COMPUTE_NODE = "compute";
    public static final String ASSIGN_NODE = "assign";
    public static final String RULE_NODE = "rule";
    
    // 变量映射
    private static final Map<String, String> variableMap = new HashMap<>();
    
    static {
        variableMap.put("a68e8ebd-3024-42d9-bbb9-14dac635e8fa", "userFund");
        variableMap.put("b6ef56c0-003a-4518-95d8-b93c927be38f", "orderAmt");
        variableMap.put("f8335c59-b1ee-4d50-8c64-5877e555a213", "storeId");
        variableMap.put("220db748-9fd9-405e-83a5-c24fb5de5f13", "businessId");
        variableMap.put("154782fd-71a6-4a65-889e-a5cb2101c1c1", "weComFriend");
    }
    
    /**
     * 添加包声明和导入
     */
    public static void addPackageAndImports(StringBuilder drlBuilder) {
        drlBuilder.append("package org.example.ruleEngine.V3;\n\n");
        drlBuilder.append("import java.util.Map;\n");
        drlBuilder.append("import java.util.HashMap;\n");
        drlBuilder.append("import java.util.List;\n");
        drlBuilder.append("import java.util.ArrayList;\n");
        drlBuilder.append("import java.math.BigDecimal;\n");
        drlBuilder.append("import com.example.utils.VariableUtils;\n\n");
    }
    
    /**
     * 检查字符串是否为数字
     */
    public static boolean isNumeric(String str) {
        if (str == null) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * 检查字符串是否为boolean
     */
    public static boolean isBoolean(String str) {
        if (str == null) {
            return false;
        }
        try {
            // 转为小写，简化比较
            String lowerStr = str.toLowerCase().trim();
            
            // 检查是否为常见的布尔值表示
            return lowerStr.equals("true") || lowerStr.equals("false");
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查操作符是否为等于或不等于
     */
    public static boolean isEqOrNotEq(String operator) {
        return "EQ".equals(operator) || "NOT_EQ".equals(operator);
    }
    
    /**
     * 获取变量名
     */
    public static String getVariableName(String variableNum) {
        return variableMap.getOrDefault(variableNum, variableNum);
    }
    
    /**
     * 构建变量
     */
    public static void buildVariable(StringBuilder drlBuilder, JsonNode variable) {
        String variableName = variable.get("name").asText();
        String variableNum = variable.get("variableNo").asText();
        String variableNo = getVariableName(variableNum);
        
        drlBuilder.append("        // 变量: ").append(variableName).append("\n");
        drlBuilder.append("        flowContext.put(\"").append(variableNo).append("\", ");
        
        // 如果有表达式树，处理表达式
        if (variable.has("data") && variable.get("data").has("expression_tree_json")) {
            drlBuilder.append("VariableUtils.evaluateExpression(").append("flowContext, ")
                    .append(com.alibaba.fastjson.JSON.toJSONString(variable.get("data").get("expression_tree_json").asText())).append(")");
        } else {
            drlBuilder.append("null");
        }
        drlBuilder.append(");\n");
        drlBuilder.append("        System.out.println(\"变量取值结果");
        drlBuilder.append(variableNo).append(":\"+flowContext.get(\"").append(variableNo).append("\"));\n");
    }
    
    /**
     * 构建关系
     */
    public static void buildRelationShip(StringBuilder drlBuilder, JsonNode relationship, JsonNode node) {
        String operator = relationship.get("operator").asText();
        String operatorValueNum = relationship.get("operatorValue").asText();
        String operatorValue = getVariableName(operatorValueNum);
        String operatorValueType = relationship.get("operatorValueType").asText();
        String variableNum = relationship.get("variableNo").asText();
        String variableNo = getVariableName(variableNum);
        
        drlBuilder.append("        // 操作: ").append(operator).append("\n");
        String nodeIdStr = node.get("id").asText();
        if (relationship.has("relationshipNo")) {
            nodeIdStr = nodeIdStr.concat("_" + relationship.get("relationshipNo").asText());
        }
        
        drlBuilder.append("        Object " + nodeIdStr).append("=");
        drlBuilder.append("VariableUtils.performOperation(flowContext, \"").append(variableNo)
                .append("\", \"").append(operator).append("\", \"").append(operatorValue)
                .append("\", \"").append(operatorValueType).append("\")").append(";\n");
        
        //节点
        drlBuilder.append("        flowContext.put(\"").append(nodeIdStr).append("\", ");
        drlBuilder.append(nodeIdStr);
        drlBuilder.append(");\n");
        
        drlBuilder.append("        System.out.println(\"变量计算结果");
        drlBuilder.append(nodeIdStr).append("_result:\"+");
        drlBuilder.append(nodeIdStr).append(");\n");
    }
    
    /**
     * 构建默认规则表达式
     */
    public static void buildDefaultRuleExpress(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        drlBuilder.append("        if (").append(variableNo);
        switch (operator) {
            case "EQ":
                drlBuilder.append(" == ");
                break;
            case "NOT_EQ":
                drlBuilder.append(" != ");
                break;
            default:
                drlBuilder.append(" ").append(operator).append(" ");
        }
        drlBuilder.append(value);
        drlBuilder.append(")");
    }
    
    /**
     * 构建数字规则表达式
     */
    public static void buildNumberRuleExpress(StringBuilder drlBuilder, String variableNo, String operator, String value) {
        drlBuilder.append("        if (").append(variableNo).append(".").append("compareTo(new BigDecimal(");
        drlBuilder.append(value);
        drlBuilder.append("))");
        switch (operator) {
            case "GT":
                drlBuilder.append(" > ");
                break;
            case "GE":
                drlBuilder.append(" >= ");
                break;
            case "LT":
                drlBuilder.append(" < ");
                break;
            case "LE":
                drlBuilder.append(" <= ");
                break;
            default:
                drlBuilder.append(" ").append(operator).append(" ");
        }
        drlBuilder.append("0");
    }
} 