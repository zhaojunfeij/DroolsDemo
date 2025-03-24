package com.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.util.HashMap;
import java.util.Map;

public class TestMain {
    public static void main(String[] args) {
        String str = "{\"type\":\"FUNC\",\"code\":\"SET_RESULT\",\"params\":[{\"type\":\"PARAM\",\"code\":\"$.orderAmt\",\"showText\":\"param\"}]}";
        JSONObject json = JSON.parseObject(str);
        json.put("className", "com.example.utils.ParamUtils");
        // 手动转义字符串
        String jsonString = json.toJSONString();
        jsonString = jsonString.replace("\"", "\\\"");
        jsonString = "\"" + jsonString + "\"";
        System.out.println(jsonString);
    }
}