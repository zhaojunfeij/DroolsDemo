package com.example.model;
public class Param {
    private String type;
    private String code;
    private String showText;
    // 参数名称，用于Feign调用时的参数命名
    private String name;
    private Object value;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getShowText() { return showText; }
    public void setShowText(String showText) { this.showText = showText; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
