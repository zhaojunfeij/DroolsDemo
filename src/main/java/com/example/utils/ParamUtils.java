package com.example.utils;

import com.alibaba.fastjson.JSON;
import com.example.model.Func;
import com.example.model.Param;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ParamUtils {
    private static final Logger LOGGER = Logger.getLogger(ParamUtils.class.getName());
    private static final Map<String, Function<Map<String, Object>, Object>> FUNCTION_REGISTRY = new HashMap<>();

    static {
        // 注册函数
        FUNCTION_REGISTRY.put("add", ParamUtils::add);
        FUNCTION_REGISTRY.put("test", ParamUtils::test);

        // 可以注册更多函数...
    }

    public static Object process(String jsonStr, Map<String, Object> values) {
        try {
            return new ParamUtils().processJson(jsonStr, values);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "处理参数时发生错误", e);
            return null;
        }
    }

    public Object processJson(String jsonStr, Map<String, Object> values) {
        Func func = JSON.parseObject(jsonStr, Func.class);

        return Optional.ofNullable(func)
                .filter(f -> "FUNC".equals(f.getType()))
                .map(f -> executeFunction(f, values))
                .orElse(null);
    }

    private Object executeFunction(Func func, Map<String, Object> values) {
        // 处理SET_RESULT赋值情况
        if ("setResult".equals(func.getCode())) {
            return Optional.ofNullable(func.getParams())
                    .filter(params -> !params.isEmpty())
                    .map(params -> params.get(0))
                    .map(param -> processParam(param, values))
                    .orElse(null);
        }

        // 使用函数注册表执行函数
        return Optional.ofNullable(FUNCTION_REGISTRY.get(func.getCode()))
                .map(function -> function.apply(values))
                .orElseGet(() -> {
                    LOGGER.warning("未找到函数: " + func.getCode());
                    return null;
                });
    }

    private Object processParam(Param param, Map<String, Object> values) {
        if (param == null) {
            return null;
        }

        String type = param.getType();
        switch (type) {
            case "FIXED":
                return param.getValue();
            case "PARAM":
                return setResult(values, param.getCode());
            default:
                return null;
        }
    }

    // 示例方法，应与JSON中的code字段相匹配
    public Object setResult(Map<String, Object> values, String key) {
        return values.get(key.replaceAll("\\$.", ""));
    }

    public static Object add(Map<String, Object> values) {
        return values.values().stream()
                .filter(Integer.class::isInstance)
                .mapToInt(value -> (Integer) value)
                .sum();
    }

    public static Object test(Map<String, Object> values) {
        Object orderAmt = values.get("orderAmt");
        return new BigDecimal(orderAmt.toString()).multiply(new BigDecimal(10));
    }

    public static void main(String[] args) {
        // 示例1: 使用add函数
        String json = "{\"type\":\"FUNC\",\"code\":\"add\",\"params\":[{\"type\":\"PARAM\",\"code\":\"userFund\",\"showText\":\"param\"}]}";

        Map<String, Object> values = new HashMap<>();
        values.put("userFund", 100);
        values.put("orderAmt", 200);

        Object result = process(json, values);
        System.out.println("计算结果: " + result);

        // 示例2: 使用SET_RESULT函数
        String json2 = "{\"type\":\"FUNC\",\"code\":\"SET_RESULT\",\"params\":[{\"type\":\"PARAM\",\"code\":\"userFund\",\"showText\":\"param\"}]}";
        System.out.println("获取参数: " + process(json2, values));
    }
}