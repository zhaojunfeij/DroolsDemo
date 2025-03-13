package com.example;

import com.alibaba.fastjson.JSON;
import com.example.model.Order;
import com.example.service.RuleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class OrderDiscountTest {
    @Autowired
    private RuleService ruleService;

    @Test
    public void testDiscounts() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);

        // 测试场景1：VIP用户享受8折优惠
        Order vipOrder = new Order();
        vipOrder.setId(1L);
        vipOrder.setCustomerName("VIP用户张三");
        vipOrder.setOriginalPrice(new BigDecimal("500"));
        vipOrder.setVip(true);
        vipOrder.setScore(500);

        // 测试场景2：订单价格超过1000元，享受9折优惠
        Order bigOrder = new Order();
        bigOrder.setId(2L);
        bigOrder.setCustomerName("普通用户李四");
        bigOrder.setOriginalPrice(new BigDecimal("1200"));
        bigOrder.setVip(false);
        bigOrder.setScore(500);

        // 测试场景3：积分超过1000分，享受9.5折优惠
        Order scoreOrder = new Order();
        scoreOrder.setId(3L);
        scoreOrder.setCustomerName("普通用户王五");
        scoreOrder.setOriginalPrice(new BigDecimal("800"));
        scoreOrder.setVip(false);
        scoreOrder.setScore(1500);

        // 测试场景4：不满足任何折扣条件的普通订单
        Order normalOrder = new Order();
        normalOrder.setId(4L);
        normalOrder.setCustomerName("普通用户赵六");
        normalOrder.setOriginalPrice(new BigDecimal("500"));
        normalOrder.setVip(false);
        normalOrder.setScore(500);

        // 执行规则
        result.put("vipUser", ruleService.executeRule(vipOrder,null));
        result.put("bigOrder", ruleService.executeRule(bigOrder,null));
        result.put("scoreOrder", ruleService.executeRule(scoreOrder,null));
        result.put("normalOrder", ruleService.executeRule(normalOrder,null));

        System.out.println(JSON.toJSONString(result));
    }
}
