package com.example;

import com.alibaba.fastjson.JSON;
import com.example.parse.JsonToDrlConverter;
import com.example.parse.refactor.converter.JsonToDrlConverterRefactor;
import com.example.service.RuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class SendFundTest {
    @Autowired
    private RuleService ruleService;

    @Autowired
    private JsonToDrlConverterRefactor converterRefactor;

    @Test
    void parse() {
        String inputJsonPath = "src/main/resources/flows/send_fund_flow.json";
        String outputDrlPath = "src/main/resources/rules/send_fund_flow_rules.drl";
        try {
            String drlContent = converterRefactor.convertJsonToDrl(inputJsonPath);
            converterRefactor.writeDrlFile(drlContent, outputDrlPath);
            System.out.println("成功将JSON转换为DRL文件: " + outputDrlPath);
        } catch (Exception e) {
            System.err.println("转换失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    void testFundRule() {
        Map<String, Object> context = new HashMap<>();
        context.put("userFund", 190.5);
        context.put("orderAmt", 152.5);
        context.put("storeId", 99999);
        context = ruleService.executeRule(context, "send_fund_flow");
        System.out.println(JSON.toJSONString(context));
    }
}
