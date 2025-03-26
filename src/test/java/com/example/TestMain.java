package com.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.HashMap;
import java.util.Map;

public class TestMain {
    public static void main(String[] args) {
        String str1 = "数据ID\t门店编码\t门店名\t老板\t电话\t门店简称\n" +
                "37950\t\"\tY59R\"\t丹巴县（个人独资）\t江\t99999999\t药店\n";
        String str2 = "store_id\tstore_no\n" +
                "3895334857207\tY593\n";

        // 解析str1，提取门店编码和电话
        Map<String, String> storeCodeToPhone = new HashMap<>();
        String[] lines1 = str1.split("\n");
        for (int i = 1; i < lines1.length; i++) { // 从1开始跳过表头
            String[] cols = lines1[i].split("\t");
            String storeCode = cols[1];
            // 处理特殊情况，有些门店编码带引号
            storeCode = storeCode.replace("\"", "").trim();
            String phone = cols[4];
            storeCodeToPhone.put(storeCode, phone);
        }

        // 解析str2，提取store_id和store_no
        Map<String, String> storeNoToId = new HashMap<>();
        String[] lines2 = str2.split("\n");
        for (int i = 1; i < lines2.length; i++) { // 从1开始跳过表头
            String[] cols = lines2[i].split("\t");
            String storeId = cols[0];
            String storeNo = cols[1];
            storeNoToId.put(storeNo, storeId);
        }

        // 根据关联字段，输出对应的store_id和电话
        System.out.println("store_id,电话");
        for (String storeNo : storeNoToId.keySet()) {
            if (storeCodeToPhone.containsKey(storeNo)) {
                String storeId = storeNoToId.get(storeNo);
                String phone = storeCodeToPhone.get(storeNo);
                System.out.println(storeId + "," + phone);
            }
        }
    }
}