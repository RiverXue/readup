package com.xreadup.ai.userservice.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订阅实体类 - 商业化功能
 */
@Data
@TableName("subscription")
public class Subscription {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    
    private String planType;  // BASIC, PRO, ENTERPRISE
    
    private BigDecimal price;
    
    private String currency;  // CNY, USD
    
    private String status;  // ACTIVE, CANCELLED, EXPIRED
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    private String paymentMethod;  // ALIPAY, WECHAT, CREDIT_CARD
    
    private String transactionId;  // 支付平台交易号
    
    private Boolean autoRenew;  // 是否自动续费
    
    private Integer maxArticlesPerMonth;  // 每月最大文章数
    
    private Integer maxWordsPerArticle;  // 每篇文章最大字数
    
    private Boolean aiFeaturesEnabled;  // 是否启用AI功能
    
    private Boolean prioritySupport;  // 是否优先支持
    
    private Boolean isTrial;  // 是否为试用订阅
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    @TableLogic
    private Integer deleted;
}