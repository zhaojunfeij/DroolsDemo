package com.example.utils;

import com.alibaba.fastjson.JSON;
import com.example.model.Func;
import com.example.model.Param;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class ParamUtils implements ApplicationContextAware {
    private static final Logger LOGGER = Logger.getLogger(ParamUtils.class.getName());
    private static final Map<String, Function<Map<String, Object>, Object>> FUNCTION_REGISTRY = new HashMap<>();
    
    // Spring应用上下文
    private static ApplicationContext applicationContext;

    static {
        // 注册函数
        FUNCTION_REGISTRY.put("add", ParamUtils::add);
        FUNCTION_REGISTRY.put("test", ParamUtils::test);

        // 可以注册更多函数...
    }
    
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
        LOGGER.info("ApplicationContext已设置到ParamUtils");
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

        // 检查是否包含className属性，如果有则尝试通过Spring Bean调用
        String className = func.getClassName();
        if (className != null && !className.isEmpty() && applicationContext != null) {
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
     * @param beanName Bean名称
     * @param methodName 方法名
     * @param contextValues 上下文值
     * @param params 函数参数
     * @return 方法执行结果
     */
    private Object invokeBeanMethod(String beanName, String methodName, 
                                   Map<String, Object> contextValues, 
                                   List<Param> params) throws Exception {
        try {
            // 从Spring上下文获取Bean
            Object bean = applicationContext.getBean(beanName);
            if (bean == null) {
                LOGGER.warning("未找到Bean: " + beanName);
                return null;
            }
            
            // 尝试调用bean的方法
            if (params == null || params.isEmpty()) {
                // 无参方法调用
                try {
                    // 尝试调用无参方法
                    return bean.getClass().getMethod(methodName).invoke(bean);
                } catch (NoSuchMethodException e) {
                    // 尝试调用带Map参数的方法
                    return bean.getClass().getMethod(methodName, Map.class).invoke(bean, contextValues);
                }
            } else {
                // 有参方法调用，默认传入整个上下文Map
                return bean.getClass().getMethod(methodName, Map.class).invoke(bean, contextValues);
            }
        } catch (Exception e) {
            LOGGER.warning("Bean方法调用失败: " + e.getMessage());
            throw e;
        }
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
        // 主方法中的测试代码在实际环境中不会执行，因为没有Spring上下文
        // 这里只是为了示例
        Map<String, Object> values = new HashMap<>();
        values.put("userFund", 100);
        values.put("orderAmt", 200);

        // 示例1: 使用add函数
        String json = "{\"type\":\"FUNC\",\"code\":\"add\",\"params\":[{\"type\":\"PARAM\",\"code\":\"userFund\",\"showText\":\"param\"}]}";
        System.out.println("计算结果: " + process(json, values));

        // 示例2: 使用SET_RESULT函数
        String json2 = "{\"type\":\"FUNC\",\"code\":\"SET_RESULT\",\"params\":[{\"type\":\"PARAM\",\"code\":\"userFund\",\"showText\":\"param\"}]}";
        System.out.println("获取参数: " + process(json2, values));
        
        // 示例3: 使用带className的函数 (在实际Spring环境中才能正常工作)
        String json3 = "{\"type\":\"FUNC\",\"code\":\"test\",\"className\":\"paramUtils\",\"params\":[]}";
        System.out.println("Bean调用: " + process(json3, values));
    }
}