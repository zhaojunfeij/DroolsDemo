package com.example.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * 变量工具类 - 用于处理规则中的变量操作
 */
public class VariableUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取变量值
     */
    public static Object getVariableValue(Map<String, Object> context, String variableNo) {
        return context.get(variableNo);
    }

    /**
     * 比较两个变量
     */
    public static boolean compareVariables(Map<String, Object> context, String variable1, String operator, String variable2) {
        Object value1 = context.get(variable1);
        Object value2 = context.get(variable2);

        if (value1 == null || value2 == null) {
            return false;
        }

        // 尝试转换为数值比较
        try {
            BigDecimal num1 = new BigDecimal(value1.toString());
            BigDecimal num2 = new BigDecimal(value2.toString());

            switch (operator) {
                case "EQ":
                    return num1.compareTo(num2) == 0;
                case "NE":
                    return num1.compareTo(num2) != 0;
                case "GT":
                    return num1.compareTo(num2) > 0;
                case "GE":
                    return num1.compareTo(num2) >= 0;
                case "LT":
                    return num1.compareTo(num2) < 0;
                case "LE":
                    return num1.compareTo(num2) <= 0;
                case "ADD":
                    return true; // 用于计算节点
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            // 如果不是数值，按字符串比较
            String str1 = value1.toString();
            String str2 = value2.toString();

            switch (operator) {
                case "EQ":
                    return str1.equals(str2);
                case "NE":
                    return !str1.equals(str2);
                default:
                    return false;
            }
        }
    }

    /**
     * 执行表达式
     */
    public static Object evaluateExpression(Map<String, Object> context, String expressionJson) {
        try {
            return ParamProcessor.process(expressionJson, context);
            //JsonNode expressionTree = objectMapper.readTree(expressionJson);
            //return evaluateExpressionNode(expressionTree, context);
        } catch (Exception e) {
            System.err.println("表达式计算错误: " + e.getMessage());
            return null;
        }
    }

    /**
     * 递归计算表达式节点
     */
    private static Object evaluateExpressionNode(JsonNode node, Map<String, Object> context) {
        if (node == null) {
            return null;
        }

        String type = node.get("type").asText();

        switch (type) {
            case "FUNC":
                return evaluateFunction(node, context);
            case "PARAM":
                return evaluateParameter(node, context);
            case "FIXED":
                return node.get("value").asText();
            default:
                return null;
        }
    }

    /**
     * 计算函数表达式
     */
    private static Object evaluateFunction(JsonNode node, Map<String, Object> context) {
        String funcCode = node.get("code").asText();
        JsonNode params = node.get("params");

        switch (funcCode) {
            case "SET_RESULT":
                if (params.size() > 0) {
                    return evaluateExpressionNode(params.get(0), context);
                }
                return null;
            case "ADD":
                BigDecimal result = BigDecimal.ZERO;
                for (JsonNode param : params) {
                    Object value = evaluateExpressionNode(param, context);
                    if (value != null) {
                        try {
                            result = result.add(new BigDecimal(value.toString()));
                        } catch (NumberFormatException e) {
                            System.err.println("无法将 " + value + " 转换为数字进行加法运算");
                        }
                    }
                }
                return result;
            // 可以添加更多函数支持
            default:
                System.err.println("不支持的函数: " + funcCode);
                return null;
        }
    }

    /**
     * 计算参数引用
     */
    private static Object evaluateParameter(JsonNode node, Map<String, Object> context) {
        String paramCode = node.get("code").asText();

        // 处理 $.xxx 格式的参数引用
        if (paramCode.startsWith("$.")) {
            String paramName = paramCode.substring(2);
            return context.get(paramName);
        }

        // 处理直接变量引用
        return context.get(paramCode);
    }

    /**
     * 执行操作
     */
    public static Object performOperation(Map<String, Object> context, String variableNo, String operator, String operatorValue, String operatorValueType) {
        Object variable = context.get(variableNo);
        Object value;
        Object result = null;

        if ("data".equals(operatorValueType)) {
            // 直接使用值
            try {
                value = new BigDecimal(operatorValue);
            } catch (NumberFormatException e) {
                value = operatorValue;
            }
        } else {
            // 使用另一个变量的值
            value = context.get(operatorValue);
        }

        if (variable == null || value == null) {
            return value;
        }

        switch (operator) {
            case "ADD":
                try {
                    BigDecimal num1 = new BigDecimal(variable.toString());
                    BigDecimal num2 = new BigDecimal(value.toString());
                    result=num1.add(num2);
                    //context.put(variableNo, num1.add(num2));
                } catch (NumberFormatException e) {
                    // 如果不是数字，当作字符串拼接
                    System.err.println("无法执行加法操作，非数值类型");
                }

                break;
            case "SUBTRACT":
                try {
                    BigDecimal num1 = new BigDecimal(variable.toString());
                    BigDecimal num2 = new BigDecimal(value.toString());
                    result=num1.subtract(num2);
                    //context.put(variableNo, num1.subtract(num2));
                } catch (NumberFormatException e) {
                    System.err.println("无法执行减法操作，非数值类型");
                }
                break;
            case "MULTIPLY":
                try {
                    BigDecimal num1 = new BigDecimal(variable.toString());
                    BigDecimal num2 = new BigDecimal(value.toString());
                    result=num1.multiply(num2);
                    //context.put(variableNo, num1.multiply(num2));
                } catch (NumberFormatException e) {
                    System.err.println("无法执行乘法操作，非数值类型");
                }
                break;
            case "DIVIDE":
                try {
                    BigDecimal num1 = new BigDecimal(variable.toString());
                    BigDecimal num2 = new BigDecimal(value.toString());
                    result=num1.divide(num2);
                    //context.put(variableNo, num1.divide(num2));
                } catch (NumberFormatException e) {
                    System.err.println("无法执行除法操作，非数值类型");
                }
                break;
            case "IN":
                if (value instanceof Collection) {
                    boolean isIn = ((Collection<?>) value).contains(variable);
                    result= isIn;
                    //context.put(operatorValue + "_" + operator, isIn);
                } else {
                    throw new IllegalArgumentException("For 'IN' operation, the right operand must be a collection.");
                }
                break;
            case "NOT_IN":
                if (value instanceof Collection) {
                    boolean isNotIn = !((Collection<?>) value).contains(variable);
                    result= isNotIn;
                    //context.put(operatorValue + "_" + operator, isNotIn);
                } else {
                    throw new IllegalArgumentException("For 'NOT_IN' operation, the right operand must be a collection.");
                }
                break;
        }
        return result;
    }
}
