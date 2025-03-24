package com.example.utils;

import com.alibaba.fastjson.JSON;
import com.example.model.Func;
import com.example.model.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 参数处理工具类
 */
public class ParamUtils {
    private static final Logger LOGGER = Logger.getLogger(ParamUtils.class.getName());
    private static final Map<String, Function<Map<String, Object>, Object>> FUNCTION_REGISTRY = new HashMap<>();

    static {
        // 注册函数
        FUNCTION_REGISTRY.put("add", ParamUtils::add);
        //FUNCTION_REGISTRY.put("test", ParamUtils::test);

        // 可以注册更多函数...
    }

    /**
     * 处理参数JSON字符串
     *
     * @param jsonStr JSON表达式字符串
     * @param values  上下文值
     * @return 处理结果
     */
    public static Object process(String jsonStr, Map<String, Object> values) {
        try {
            return new ParamUtils().processJson(jsonStr, values);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "处理参数时发生错误", e);
            return null;
        }
    }

    /**
     * 处理JSON表达式
     */
    public Object processJson(String jsonStr, Map<String, Object> values) {
        Func func = JSON.parseObject(jsonStr, Func.class);

        return Optional.ofNullable(func)
                .filter(f -> "FUNC".equals(f.getType()))
                .map(f -> executeFunction(f, values))
                .orElse(null);
    }

    /**
     * 执行函数
     */
    private Object executeFunction(Func func, Map<String, Object> values) {
        // 处理SET_RESULT赋值情况
        if ("setResult".equals(func.getCode()) || "SET_RESULT".equals(func.getCode())) {
            return Optional.ofNullable(func.getParams())
                    .filter(params -> !params.isEmpty())
                    .map(params -> params.get(0))
                    .map(param -> processParam(param, values))
                    .orElse(null);
        }

        // 检查是否包含className属性，如果有则尝试通过Spring Bean调用
        String className = func.getClassName();
        if (className != null && !className.isEmpty()) {
            try {
                return invokeBeanMethod(className, func.getCode(), values, func.getParams());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Spring Bean方法调用失败: " + className + "." + func.getCode(), e);
                // 如果Bean调用失败，尝试使用函数注册表
            }
        }

        // 使用函数注册表执行函数
        return Optional.ofNullable(FUNCTION_REGISTRY.get(func.getCode()))
                .map(function -> function.apply(values))
                .orElseGet(() -> {
                    LOGGER.warning("未找到函数: " + func.getCode());
                    return null;
                });
    }

    /**
     * 通过Spring Bean调用指定的方法
     *
     * @param beanName      Bean名称
     * @param methodName    方法名
     * @param contextValues 上下文值
     * @param params        函数参数
     * @return 方法执行结果
     */
    private Object invokeBeanMethod(String beanName, String methodName,
                                    Map<String, Object> contextValues,
                                    List<Param> params) throws Exception {
        try {
            // 从Spring上下文获取Bean
            Object bean = SpringContextHolder.getBean(beanName);
            if (bean == null) {
                LOGGER.warning("未找到Bean: " + beanName);
                return null;
            }

            // 尝试调用bean的方法
            return bean.getClass().getMethod(methodName, Map.class).invoke(bean, contextValues);

        } catch (Exception e) {
            LOGGER.warning("Bean方法调用失败: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 处理参数
     */
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

    /**
     * 设置结果
     */
    public static Object setResult(Map<String, Object> values, String key) {
        return values.get(key.replaceAll("\\$.", ""));
    }

    /**
     * 加法函数
     */
    public static Object add(Map<String, Object> values) {
        return values.values().stream()
                .filter(Integer.class::isInstance)
                .mapToInt(value -> (Integer) value)
                .sum();
    }

}