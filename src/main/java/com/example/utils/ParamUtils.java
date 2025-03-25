package com.example.utils;

import com.alibaba.fastjson.JSON;
import com.example.model.Func;
import com.example.model.Param;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 参数处理工具类
 */
public class ParamUtils {
    private static final Logger LOGGER = Logger.getLogger(ParamUtils.class.getName());
    private static final Map<String, Function<Map<String, Object>, Object>> FUNCTION_REGISTRY = new HashMap<>();
    
    // 微服务调用方式常量
    private static final Integer METHOD_SOURCE_LOCAL = 1;  // 本地Spring Bean调用
    private static final Integer METHOD_SOURCE_REMOTE = 2;  // 远程服务调用
    
    // HTTP请求类型常量
    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";

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
            return Optional.ofNullable(jsonStr)
                .filter(str -> !str.isEmpty())
                .map(str -> new ParamUtils().processJson(str, values))
                .orElse(null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "处理参数时发生错误", e);
            return null;
        }
    }

    /**
     * 处理JSON表达式
     */
    public Object processJson(String jsonStr, Map<String, Object> values) {
        return Optional.ofNullable(JSON.parseObject(jsonStr, Func.class))
            .filter(f -> "FUNC".equals(f.getType()))
            .map(f -> executeFunction(f, values))
            .orElse(null);
    }

    /**
     * 执行函数
     * 支持本地Bean调用和远程服务调用
     */
    private Object executeFunction(Func func, Map<String, Object> values) {
        // 处理null情况
        if (func == null) {
            return null;
        }
        
        // 处理SET_RESULT赋值情况
        if (isSetResultFunction(func.getCode())) {
            return handleSetResultFunction(func, values);
        }

        // 根据调用来源类型选择调用方式
        Integer methodSource = func.getMethodSource();
        if (methodSource != null) {
            return callMethodBySource(methodSource, func, values);
        }
        
        // 向后兼容：如果没有指定methodSource但有className，尝试通过Spring Bean调用
        if (hasValidClassName(func)) {
            try {
                return invokeBeanMethod(func.getClassName(), func.getCode(), values, func.getParams());
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Spring Bean方法调用失败: " + func.getClassName() + "." + func.getCode(), e);
                // 如果Bean调用失败，尝试使用函数注册表
            }
        }

        // 最后尝试使用函数注册表执行函数
        return callRegisteredFunction(func.getCode(), values);
    }
    
    /**
     * 判断是否为设置结果的函数
     */
    private boolean isSetResultFunction(String functionCode) {
        return "setResult".equals(functionCode) || "SET_RESULT".equals(functionCode);
    }
    
    /**
     * 处理设置结果的函数
     */
    private Object handleSetResultFunction(Func func, Map<String, Object> values) {
        return Optional.ofNullable(func.getParams())
                .filter(params -> !params.isEmpty())
                .map(params -> params.get(0))
                .map(param -> processParam(param, values))
                .orElse(null);
    }
    
    /**
     * 根据调用来源选择并执行方法
     */
    private Object callMethodBySource(Integer methodSource, Func func, Map<String, Object> values) {
        // 定义方法调用策略映射
        Map<Integer, FunctionExecutor> executorMap = new HashMap<>();
        executorMap.put(METHOD_SOURCE_LOCAL, this::invokeLocalBean);
        executorMap.put(METHOD_SOURCE_REMOTE, this::invokeRemoteService);
        
        // 获取对应的执行器并执行
        FunctionExecutor executor = executorMap.get(methodSource);
        if (executor != null) {
            return executor.execute(func, values);
        }
        
        LOGGER.warning("不支持的方法来源类型: " + methodSource);
        return null;
    }
    
    /**
     * 方法执行器接口
     */
    @FunctionalInterface
    private interface FunctionExecutor {
        Object execute(Func func, Map<String, Object> values);
    }
    
    /**
     * 检查是否有有效的类名
     */
    private boolean hasValidClassName(Func func) {
        String className = func.getClassName();
        return className != null && !className.isEmpty();
    }
    
    /**
     * 从注册表中调用函数
     */
    private Object callRegisteredFunction(String functionCode, Map<String, Object> values) {
        return Optional.ofNullable(FUNCTION_REGISTRY.get(functionCode))
                .map(function -> function.apply(values))
                .orElseGet(() -> {
                    LOGGER.warning("未找到函数: " + functionCode);
                    return null;
                });
    }
    
    /**
     * 执行本地Spring Bean调用
     */
    private Object invokeLocalBean(Func func, Map<String, Object> values) {
        return invokeMethodWithParamValidation(
            func, 
            values,
            (funcObj, paramsMap) -> {
                try {
                    return invokeBeanMethod(funcObj.getClassName(), funcObj.getCode(), paramsMap, funcObj.getParams());
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "本地Bean调用失败: " + funcObj.getClassName() + "." + funcObj.getCode(), e);
                    return null;
                }
            },
            "className", "methodName"
        );
    }
    
    /**
     * 执行远程服务调用
     */
    private Object invokeRemoteService(Func func, Map<String, Object> values) {
        return invokeMethodWithParamValidation(
            func, 
            values,
            (funcObj, paramsMap) -> {
                try {
                    // 从Spring上下文获取RestTemplateUtils
                    RestTemplateUtils restTemplateUtils = SpringContextHolder.getBean(RestTemplateUtils.class);
                    
                    if (restTemplateUtils == null) {
                        LOGGER.warning("未找到RestTemplateUtils实例");
                        return null;
                    }
                    
                    // 构建请求参数
                    Map<String, Object> requestParams = extractRequestParams(funcObj.getParams(), paramsMap);
                    
                    // 根据请求类型选择调用方式
                    String methodType = funcObj.getMethodType();
                    if (HTTP_METHOD_GET.equalsIgnoreCase(methodType)) {
                        // GET请求
                        return restTemplateUtils.executeGetRequest(funcObj.getClassName(), funcObj.getCode(), requestParams);
                    } else {
                        // POST请求
                        return restTemplateUtils.executePostRequest(funcObj.getClassName(), funcObj.getCode(), requestParams);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "远程服务调用失败: " + funcObj.getClassName() + "." + funcObj.getCode(), e);
                    return null;
                }
            },
            "serviceName", "methodName"
        );
    }
    
    /**
     * 使用参数验证执行方法
     * 
     * @param func 函数对象
     * @param values 上下文值
     * @param executor 执行器函数
     * @param nameParam 名称参数（className或serviceName）
     * @param methodParam 方法参数
     * @return 执行结果
     */
    private Object invokeMethodWithParamValidation(
            Func func, 
            Map<String, Object> values, 
            BiFunction<Func, Map<String, Object>, Object> executor,
            String nameParam,
            String methodParam) {
        
        String name = func.getClassName();  // 类名或服务名
        String method = func.getCode();     // 方法名
        
        // 验证必要参数
        if (name == null || name.isEmpty() || method == null || method.isEmpty()) {
            LOGGER.warning("调用缺少必要参数: " + nameParam + "=" + name + ", " + methodParam + "=" + method);
            return null;
        }
        
        // 执行实际调用
        return executor.apply(func, values);
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
        if (type == null) {
            return null;
        }
        
        switch (type) {
            case "FIXED":
                return param.getValue();
            case "PARAM":
                return Optional.ofNullable(param.getCode())
                    .map(code -> setResult(values, code))
                    .orElse(null);
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

    /**
     * 从参数列表中提取请求参数
     */
    private Map<String, Object> extractRequestParams(List<Param> params, Map<String, Object> contextValues) {
        Map<String, Object> requestParams = new HashMap<>();
        
        if (params != null) {
            for (int i = 0; i < params.size(); i++) {
                Param param = params.get(i);
                if (param != null) {
                    // 处理参数并添加到请求参数中
                    Object paramValue = processParam(param, contextValues);
                    if (paramValue != null) {
                        // 使用参数索引作为键名，如果有自定义名称则使用
                        String paramName = param.getName() != null ? param.getName() : "param" + i;
                        requestParams.put(paramName, paramValue);
                    }
                }
            }
        }
        
        return requestParams;
    }
}