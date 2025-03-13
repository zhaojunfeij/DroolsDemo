package com.example;

import com.alibaba.fastjson.JSON;
import com.example.parse.JsonToDrlConverter;
import com.example.service.RuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class UserFundTest {
    @Autowired
    private RuleService ruleService;
    @Autowired
    private JsonToDrlConverter converter;

    @Test
    void parse() {
        String inputJsonPath = "src/main/resources/flows/fund_flow.json";
        String outputDrlPath = "src/main/resources/rules/fund_flow_rules.drl";
        try {
            String drlContent = converter.convertJsonToDrl(inputJsonPath);
            converter.writeDrlFile(drlContent, outputDrlPath);
            System.out.println("成功将JSON转换为DRL文件: " + outputDrlPath);
        } catch (Exception e) {
            System.err.println("转换失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void testFundRule() {
        Map<String, Object> context = new HashMap<>();
        context.put("userFund", 100.5);
        context.put("orderAmt", 2);
        context = ruleService.executeRule(context, "fund_flow");
        System.out.println(JSON.toJSONString(context));
    }
}
