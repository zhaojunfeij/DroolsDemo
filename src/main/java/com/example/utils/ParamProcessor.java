package com.example.utils;

import com.alibaba.fastjson.JSON;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class ParamProcessor {


    public static Object process(String jsonStr, Map<String, Object> values) throws Exception {
        return new ParamProcessor().processJson(jsonStr, values);
    }

    public Object processJson(String jsonStr, Map<String, Object> values) throws Exception {
        Func func = JSON.parseObject(jsonStr, Func.class);
        List<Param> paramList = func.getParams();
        Object result = null;
        if ("FUNC".equals(func.getType())) {
            switch (func.getCode()) {
                case "SET_RESULT":
                    result = setResult(values, paramList.get(0).getCode());
                    break;
                default:
                    try {
                        // 使用反射找到并调用指定的方法
                        Method method = this.getClass().getDeclaredMethod(func.getCode(), Map.class);
                        // 调用方法
                        return method.invoke(this, values);
                    } catch (NoSuchMethodException e) {
                        System.err.println("No such method: " + func.getCode());
                    }
            }

        }
        return result;
    }

    // 示例方法，应与JSON中的code字段相匹配
    public Object setResult(Map<String, Object> values, String key) {
        System.out.println("Executing setResult with params:");
        return values.get(key.replaceAll("\\$.",""));
    }

    public Integer add(Map<String, Object> values) {
        AtomicReference<Integer> sum = new AtomicReference<>(0);
        values.values().forEach(value -> {
            sum.set(sum.get() + (Integer) value);
        });
        return sum.get();
    }

    public static void main(String[] args) throws Exception {
        // String json = "{\"type\":\"FUNC\",\"code\":\"SET_RESULT\",\"params\":[{\"type\":\"PARAM\",\"code\":\"userFund\",\"showText\":\"param\"}]}";
        String json = "{\"type\":\"FUNC\",\"code\":\"add\",\"params\":[{\"type\":\"PARAM\",\"code\":\"userFund\",\"showText\":\"param\"}]}";

        Map value = new HashMap();
        value.put("userFund", 100);
        value.put("orderAmt", 200);
        Object res = process(json, value);
        System.out.println(res);
    }
}