package com.example.model;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Order {
    private Long id;
    private String customerName;
    private BigDecimal originalPrice;   // 原始价格
    private BigDecimal discountPrice;   // 折扣后价格
    private int score;                  // 用户积分
    private boolean vip;                // 是否VIP用户
}
