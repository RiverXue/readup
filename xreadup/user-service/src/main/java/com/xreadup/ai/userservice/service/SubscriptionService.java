package com.xreadup.ai.userservice.service;

import com.xreadup.ai.userservice.entity.Subscription;

import java.math.BigDecimal;
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
    
    /**
     * 获取所有套餐价格配置
     */
    Map<String, Map<String, Object>> getPlanPrices();
    
    /**
     * 升级订阅套餐
     * @param userId 用户ID
     * @param newPlanType 新套餐类型
     * @param paymentMethod 支付方式
     * @return 升级后的订阅信息
     */
    Subscription upgradeSubscription(Long userId, String newPlanType, String paymentMethod);
    
    /**
     * 计算升级差价
     * @param currentPlanType 当前套餐类型
     * @param newPlanType 新套餐类型
     * @param remainingDays 剩余天数
     * @return 需要支付的差价
     */
    BigDecimal calculateUpgradePrice(String currentPlanType, String newPlanType, int remainingDays);
    
    /**
     * 开始试用
     * @param userId 用户ID
     * @return 试用结果
     */
    Map<String, Object> startTrial(Long userId);
    
    /**
     * 检查试用状态
     * @param userId 用户ID
     * @return 试用状态信息
     */
    Map<String, Object> checkTrialStatus(Long userId);
}