package com.example;

import com.example.parse.JsonToDrlConverter;
import com.example.service.RuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest

public class QywxUserTest {
    @Autowired
    private RuleService ruleService;
    @Autowired
    private JsonToDrlConverter converter;

    @Test
    void parse() {
        String inputJsonPath = "src/main/resources/flows/qywx_flow.json";
        String outputDrlPath = "src/main/resources/rules/qywx_flow_rules.drl2";
        try {
            String drlContent = converter.convertJsonToDrl(inputJsonPath);
            converter.writeDrlFile(drlContent, outputDrlPath);
            System.out.println("成功将JSON转换为DRL文件: " + outputDrlPath);
        } catch (Exception e) {
            System.err.println("转换失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
