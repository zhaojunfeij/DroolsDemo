package com.example.model;

import java.util.List;

/**
 * 函数对象模型
 */
public class Func {
    private String type;
    private String code;
    private String className;
    private List<Param> params;
    /**
     * 1: 本地函数，2: 远程函数 ，3：标签函数
     */

    private Integer methodSource;

    private String methodType;


    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }

    public Integer getMethodSource() {
        return methodSource;
    }

    public void setMethodSource(Integer methodSource) {
        this.methodSource = methodSource;
    }

    public String getMethodType() {
        return methodType;
    }

    public void setMethodType(String methodType) {
        this.methodType = methodType;
    }
}
