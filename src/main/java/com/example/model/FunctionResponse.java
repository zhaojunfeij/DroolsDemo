package com.example.model;

import java.util.List;

/**
 * 函数接口响应数据模型
 */
public class FunctionResponse {
    private Integer status;
    private String msg;
    private FunctionData data;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public FunctionData getData() {
        return data;
    }

    public void setData(FunctionData data) {
        this.data = data;
    }

    /**
     * 函数数据
     */
    public static class FunctionData {
        private List<FunctionInfo> rows;
        private Integer total;

        public List<FunctionInfo> getRows() {
            return rows;
        }

        public void setRows(List<FunctionInfo> rows) {
            this.rows = rows;
        }

        public Integer getTotal() {
            return total;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }
    }

    /**
     * 函数信息
     */
    public static class FunctionInfo {
        private String id;
        private String function_code;
        private String function_name;
        private String function_type;
        private String function_method_name;
        private String function_class_name;
        private String function_param;

        private String function_result;
        private String function_desc;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFunction_code() {
            return function_code;
        }

        public void setFunction_code(String function_code) {
            this.function_code = function_code;
        }

        public String getFunction_name() {
            return function_name;
        }

        public void setFunction_name(String function_name) {
            this.function_name = function_name;
        }

        public String getFunction_type() {
            return function_type;
        }

        public void setFunction_type(String function_type) {
            this.function_type = function_type;
        }

        public String getFunction_method_name() {
            return function_method_name;
        }

        public void setFunction_method_name(String function_method_name) {
            this.function_method_name = function_method_name;
        }

        public String getFunction_class_name() {
            return function_class_name;
        }

        public void setFunction_class_name(String function_class_name) {
            this.function_class_name = function_class_name;
        }

        public String getFunction_param() {
            return function_param;
        }

        public void setFunction_param(String function_param) {
            this.function_param = function_param;
        }

        public String getFunction_result() {
            return function_result;
        }

        public void setFunction_result(String function_result) {
            this.function_result = function_result;
        }

        public String getFunction_desc() {
            return function_desc;
        }

        public void setFunction_desc(String function_desc) {
            this.function_desc = function_desc;
        }
    }
} 