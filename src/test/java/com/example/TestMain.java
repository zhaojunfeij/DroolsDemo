package com.example;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.util.HashMap;
import java.util.Map;

public class TestMain {
    public static void main(String[] args) {
        String str1="数据ID\t门店编码\t门店名\t老板\t电话\t门店简称\n" +
                "37950\t\"\tY59R\"\t丹巴县龙一益民药店（个人独资）\t色尔江\t18227465207\t龙一益民药店\n" +
                "37954\tY593\t成都市守正合大药房有限公司\t周赞\t17692908129\t守正合大药房\n" +
                "38078\tY5AN\t雅安市雨城区福爱龙一药品零售连锁加盟店（个人独资）\t张琼\t13778762161\t福爱加盟店\n" +
                "38080\tY5BA\t荥经县一心龙一药房加盟店（个人独资）\t杨梅\t15528655706\t一心龙一药房加盟店\n" +
                "38082\tY5B9\t荥经县仁心龙一药房加盟店（个人独资）\t杨梅\t15528655706\t仁心龙一药房加盟店\n" +
                "38084\tY598\t四川龙一药品零售连锁有限公司普格九九药店\t宾天\t19983825235\t普格九九药店\n" +
                "38142\tY5C6\t资阳市临空经济区龙一康仁药店(个人独资)\t王慧\t18982990605\t龙一康仁药店\n" +
                "38144\tY5CB\t资阳市临空经济区龙一曾敏药店(个人独资)\t曾敏\t15983220044\t龙一曾敏药店\n" +
                "38154\tY5CA\t资阳市雁江区龙一金带药店(个人独资)\t黄云跃\t18982960375\t龙一金带药店\n" +
                "38226\tY596\t四川龙一药品零售连锁有限公司资阳一店\t王艺燕\t18982990605\t资阳一店\n" +
                "38232\tY5CN\t天全县龙一药品零售连锁为民药店（个人独资）\t杨俊\t15378691780\t为民药店\n" +
                "38240\tY5CR\t资阳市雁江区龙一佛鑫药店(个人独资)\t罗文玉\t13708242861\t龙一佛鑫药店\n" +
                "38296\tY5DJ\t成都东部新区逸成大药房（个人独资）\t杨怡\t18782294650\t逸成大药房\n" +
                "38306\tY5DP\t雅安市雨城区宝芝龙一药品零售连锁加盟店（个人独资）\t杨玉洁\t18080585804\t雨城区宝芝加盟店\n" +
                "38310\tY5DT\t泸定县龙一康福药店(个人独资)\t钟杰\t18708118620\t龙一康福药店\n" +
                "38316\tY5EM\t四川龙一药品零售连锁有限公司南江百信药房分店\t简宁\t13608242107\t南江百信药房分店\n" +
                "38416\tY5AK\t资阳市雁江区龙一何氏药店(个人独资)\t何军\t18982084667\t资阳市龙一何氏药店_1\n" +
                "38422\tY5F2\t成都市武阳百姓堂大药房有限公司\t张丽萍\t15881110837\t武阳百姓堂大药房\n" +
                "38476\tY5F8\t炉霍县龙一永康大药房\t降拥志玛\t18919547772\t龙一永康大药房";
        String str2="store_id\tstore_no\n" +
                "3895334857207\tY593\n" +
                "3906830857207\tY596\n" +
                "3896796857207\tY598\n" +
                "3890097257207\tY59R\n" +
                "3916757057207\tY5AK\n" +
                "3896358857207\tY5AN\n" +
                "3896630257207\tY5B9\n" +
                "3896569857207\tY5BA\n" +
                "3900176157207\tY5C6\n" +
                "3901133557207\tY5CA\n" +
                "3900614357207\tY5CB\n" +
                "3907135857207\tY5CN\n" +
                "3907729657207\tY5CR\n" +
                "3910796357207\tY5DJ\n" +
                "3911232157207\tY5DP\n" +
                "3911420557207\tY5DT\n" +
                "3911747757207\tY5EM\n" +
                "3917079357207\tY5F2\n" +
                "3919722057207\tY5F8";
        
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