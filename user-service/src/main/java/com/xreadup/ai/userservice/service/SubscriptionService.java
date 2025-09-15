package com.xreadup.ai.userservice.service;

import com.xreadup.ai.userservice.entity.Subscription;
import com.xreadup.ai.userservice.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 订阅服务接口 - 商业化功能
 */
public interface SubscriptionService {
    
    /**
     * 创建订阅
     */
    Subscription createSubscription(Long userId, String planType, String paymentMethod);
    
    /**
     * 获取用户当前订阅
     */
    Subscription getCurrentSubscription(Long userId);
    
    /**
     * 获取用户订阅历史
     */
    List<Subscription> getSubscriptionHistory(Long userId);
    
    /**
     * 取消订阅
     */
    void cancelSubscription(Long subscriptionId);
    
    /**
     * 检查用户是否有有效订阅
     */
    boolean hasActiveSubscription(Long userId);
    
    /**
     * 检查用户是否超出使用限制
     */
    boolean checkUsageLimit(Long userId, int articlesCount, int wordsCount);
    
    /**
     * 获取用户剩余额度
     */
    Map<String, Object> getRemainingQuota(Long userId);
}