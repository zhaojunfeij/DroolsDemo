package com.example.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class TestBeanService {

    /**
     * 测试函数
     */
    public Object test(Map<String, Object> values) {
        Object orderAmt = values.get("orderAmt");
        return new BigDecimal(orderAmt.toString()).multiply(new BigDecimal(100));
    }
}
