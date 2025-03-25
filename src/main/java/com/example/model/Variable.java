package com.example.model;

import com.alibaba.fastjson.JSONObject;
import com.example.model.FunctionResponse.FunctionInfo;
import com.example.utils.NodeProcessorUtils;
import org.springframework.util.CollectionUtils;

import java.util.Map;
import java.util.Optional;

/**
 * 变量模型类
 * 用于存储和处理流程中的变量信息
 */
public class Variable {
    private String name;
    private String variableNo;
    private VariableData data;
    private String expression;
    private String methodName;
    private String beanName;
    private boolean isValid;
    private String variableField;

    private String methodSource;

    private String methodType;

    public Variable() {
        this.isValid = true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVariableNo() {
        return variableNo;
    }

    public void setVariableNo(String variableNo) {
        this.variableNo = variableNo;
    }

    public VariableData getData() {
        return data;
    }

    public void setData(VariableData data) {
        this.data = data;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public String getVariableField() {
        return variableField;
    }

    public void setVariableField(String variableField) {
        this.variableField = variableField;
    }

    public String getMethodSource() {
        return methodSource;
    }

    public void setMethodSource(String methodSource) {
        this.methodSource = methodSource;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }

    /**
     * 变量数据
     */
    public static class VariableData {
        private String expressionTreeJson;

        public String getExpressionTreeJson() {
            return expressionTreeJson;
        }

        public void setExpressionTreeJson(String expressionTreeJson) {
            this.expressionTreeJson = expressionTreeJson;
        }
    }

    /**
     * 从变量数据中提取信息
     * 包括表达式、方法名和Bean名称等
     */
    public void extractInfo(Map<String, FunctionInfo> functionCodeMap) {
        // 1. 获取表达式
        String expressionJson = Optional.ofNullable(this.data).map(data -> data.getExpressionTreeJson()).orElse(null);

        if (expressionJson == null) {
            this.isValid = false;
            return;
        }

        // 2. 解析函数信息
        Func func = JSONObject.parseObject(expressionJson, Func.class);
        if ("SET_RESULT".equals(func.getCode()) && CollectionUtils.isEmpty(func.getParams())) {
            this.isValid = false;
            return;
        }

        // 3. 获取函数映射信息
        FunctionInfo functionInfo = functionCodeMap.get(func.getCode());
        this.methodName = func.getCode();
        this.beanName = "";

        if (functionInfo != null) {
            this.methodName = functionInfo.getFunction_method_name();
            this.beanName = NodeProcessorUtils.getBeanNameFromClassName(functionInfo.getFunction_class_name());
            this.methodType = functionInfo.getFunction_method_type_Enum();
            this.methodSource = functionInfo.getFunction_source();
        }

        // 4. 构建修改后的表达式
        JSONObject modifiedExpression = JSONObject.parseObject(expressionJson);
        if (this.beanName != null && !this.beanName.isEmpty()) {
            modifiedExpression.put("className", this.beanName);
        }
        if (this.methodType != null && !this.methodType.isEmpty()) {
            modifiedExpression.put("methodType", this.methodType);
        }
        if (this.methodSource != null && !this.methodSource.isEmpty()) {
            modifiedExpression.put("methodSource", this.methodSource);
        }
        this.expression = modifiedExpression.toString().replaceAll(func.getCode(), this.methodName).replace("\"", "\\\"");
        this.expression = "\"" + this.expression + "\"";
    }
} 