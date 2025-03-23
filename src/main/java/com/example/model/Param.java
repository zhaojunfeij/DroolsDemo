package com.example.model;
public class Param {
    private String type;
    private String code;
    private String showText;

    private Object value;

    // Getters and Setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getShowText() { return showText; }
    public void setShowText(String showText) { this.showText = showText; }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
