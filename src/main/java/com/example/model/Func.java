package com.example.model;

import java.util.List;

public class Func {
    private String type;
    private String code;
    private String className;
    private List<Param> params;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public List<Param> getParams() { return params; }
    public void setParams(List<Param> params) { this.params = params; }
}
