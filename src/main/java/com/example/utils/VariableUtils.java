package com.example.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;

/**
 * 变量工具类 - 用于处理规则中的变量操作
 * 支持数值、字符串、集合等多种数据类型的比较和运算
 */
public class VariableUtils {

    private static final Logger log = LoggerFactory.getLogger(VariableUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 定义数值比较操作
    private static final Map<String, BiPredicate<BigDecimal, BigDecimal>> NUMBER_COMPARATORS;
    
    // 定义数值运算操作
    private static final Map<String, BiFunction<BigDecimal, BigDecimal, BigDecimal>> NUMBER_OPERATIONS;
    
    // 定义字符串比较操作
    private static final Map<String, BiPredicate<String, String>> STRING_COMPARATORS;
    
    static {
        // 初始化数值比较器
        Map<String, BiPredicate<BigDecimal, BigDecimal>> numberComparators = new HashMap<>();
        numberComparators.put("EQ", BigDecimal::equals);
        numberComparators.put("NE", (a, b) -> !a.equals(b));
        numberComparators.put("GT", (a, b) -> a.compareTo(b) > 0);
        numberComparators.put("GE", (a, b) -> a.compareTo(b) >= 0);
        numberComparators.put("LT", (a, b) -> a.compareTo(b) < 0);
        numberComparators.put("LE", (a, b) -> a.compareTo(b) <= 0);
        NUMBER_COMPARATORS = Collections.unmodifiableMap(numberComparators);
        
        // 初始化数值运算操作
        Map<String, BiFunction<BigDecimal, BigDecimal, BigDecimal>> numberOperations = new HashMap<>();
        numberOperations.put("ADD", BigDecimal::add);
        numberOperations.put("SUBTRACT", BigDecimal::subtract);
        numberOperations.put("MULTIPLY", BigDecimal::multiply);
        numberOperations.put("DIVIDE", (a, b) -> {
            if (b.compareTo(BigDecimal.ZERO) == 0) {
                throw new ArithmeticException("除数不能为零");
            }
            return a.divide(b, 10, RoundingMode.HALF_UP);
        });
        NUMBER_OPERATIONS = Collections.unmodifiableMap(numberOperations);
        
        // 初始化字符串比较操作
        Map<String, BiPredicate<String, String>> stringComparators = new HashMap<>();
        stringComparators.put("EQ", String::equals);
        stringComparators.put("NE", (a, b) -> !a.equals(b));
        stringComparators.put("CONTAINS", String::contains);
        stringComparators.put("STARTS_WITH", String::startsWith);
        stringComparators.put("ENDS_WITH", String::endsWith);
        STRING_COMPARATORS = Collections.unmodifiableMap(stringComparators);
    }

    /**
     * 获取变量值
     */
    public static Object getVariableValue(Map<String, Object> context, String variableNo) {
        return context.get(variableNo);
    }

    /**
     * 执行表达式
     */
    public static Object evaluateExpression(Map<String, Object> context, String expressionJson) {
        try {
            return ParamUtils.process(expressionJson, context);
        } catch (Exception e) {
            log.error("表达式计算错误: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 执行变量操作 - 支持各种比较和计算操作
     */
    public static Object performOperation(Map<String, Object> context, String variableNo, String operator, 
                                         String operatorValue, String operatorValueType) {
        Object variable = Optional.ofNullable(context.get(variableNo)).orElse(null);
        Object value = "data".equals(operatorValueType) ? operatorValue : context.get(operatorValue);

        // 处理空值情况
        if (variable == null || value == null) {
            log.warn("操作数存在空值, 变量: {}, 值: {}", variable, value);
            return operator.startsWith("N") || operator.equals("NOT_IN"); // 对于非/不包含操作，空值返回true
        }

        try {
            // 尝试进行数值比较
            if (NUMBER_COMPARATORS.containsKey(operator)) {
                return compareAsNumbers(variable.toString(), value.toString(), operator);
            }
            
            // 尝试进行数值运算
            if (NUMBER_OPERATIONS.containsKey(operator)) {
                return performNumberOperation(variable.toString(), value.toString(), operator);
            }
            
            // 进行集合操作
            if ("IN".equals(operator) || "NOT_IN".equals(operator)) {
                return performCollectionOperation(variable, value, operator);
            }
            
            // 进行字符串比较
            if (STRING_COMPARATORS.containsKey(operator)) {
                return compareAsStrings(variable.toString(), value.toString(), operator);
            }
            
            // 不支持的操作
            log.warn("不支持的操作: {}", operator);
            return false;
        } catch (Exception e) {
            log.error("执行操作时发生错误, 操作: {}, 错误: {}", operator, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 数值比较操作
     */
    private static Boolean compareAsNumbers(String val1, String val2, String operator) {
        try {
            BigDecimal num1 = new BigDecimal(val1);
            BigDecimal num2 = new BigDecimal(val2);
            
            return NUMBER_COMPARATORS.getOrDefault(operator, (a, b) -> false).test(num1, num2);
        } catch (NumberFormatException e) {
            log.debug("无法作为数值比较，将尝试字符串比较");
            return compareAsStrings(val1, val2, operator);
        }
    }

    /**
     * 字符串比较操作
     */
    private static Boolean compareAsStrings(String val1, String val2, String operator) {
        return STRING_COMPARATORS.getOrDefault(operator, (a, b) -> false).test(val1, val2);
    }

    /**
     * 数值运算操作
     */
    private static BigDecimal performNumberOperation(String val1, String val2, String operator) {
        try {
            BigDecimal num1 = new BigDecimal(val1);
            BigDecimal num2 = new BigDecimal(val2);
            
            return NUMBER_OPERATIONS.getOrDefault(operator, (a, b) -> a).apply(num1, num2);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法将值转换为数字进行运算: " + val1 + ", " + val2);
        }
    }

    /**
     * 集合操作
     */
    private static Boolean performCollectionOperation(Object variable, Object collection, String operator) {
        if (collection instanceof Collection) {
            boolean contains = ((Collection<?>) collection).contains(variable);
            return "IN".equals(operator) ? contains : !contains;
        } else if (collection instanceof String && variable instanceof String) {
            // 字符串包含检查
            boolean contains = ((String) collection).contains((String) variable);
            return "IN".equals(operator) ? contains : !contains;
        }
        
        throw new IllegalArgumentException("集合操作要求操作数为集合类型");
    }
}
