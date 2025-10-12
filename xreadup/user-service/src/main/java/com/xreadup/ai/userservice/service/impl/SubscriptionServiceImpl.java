package com.xreadup.ai.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xreadup.ai.userservice.config.SubscriptionPlanProperties;
import com.xreadup.ai.userservice.entity.Subscription;
import com.xreadup.ai.userservice.mapper.SubscriptionMapper;
import com.xreadup.ai.userservice.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订阅服务实现类
 */
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionMapper subscriptionMapper;
    private final SubscriptionPlanProperties subscriptionPlanProperties;

    @Override
    public Map<String, Map<String, Object>> getPlanPrices() {
        Map<String, Map<String, Object>> planPrices = new HashMap<>();
        Map<String, SubscriptionPlanProperties.PlanConfig> plans = subscriptionPlanProperties.getPlans();
        String defaultCurrency = subscriptionPlanProperties.getDefaultCurrency();
        
        // 添加免费套餐
        Map<String, Object> freePlan = new HashMap<>();
        SubscriptionPlanProperties.PlanConfig freeConfig = plans.get("free");
        freePlan.put("name", freeConfig.getName());
        freePlan.put("description", freeConfig.getDescription());
        freePlan.put("price", freeConfig.getPriceCny());
        freePlan.put("currency", defaultCurrency);
        freePlan.put("maxArticlesPerMonth", freeConfig.getMaxArticlesPerMonth());
        freePlan.put("maxWordsPerArticle", freeConfig.getMaxWordsPerArticle());
        freePlan.put("aiFeaturesEnabled", freeConfig.getAiFeaturesEnabled());
        freePlan.put("prioritySupport", freeConfig.getPrioritySupport());
        planPrices.put("FREE", freePlan);
        
        // 添加基础套餐
        Map<String, Object> basicPlan = new HashMap<>();
        SubscriptionPlanProperties.PlanConfig basicConfig = plans.get("basic");
        basicPlan.put("name", basicConfig.getName());
        basicPlan.put("description", basicConfig.getDescription());
        basicPlan.put("price", basicConfig.getPriceCny());
        basicPlan.put("currency", defaultCurrency);
        basicPlan.put("maxArticlesPerMonth", basicConfig.getMaxArticlesPerMonth());
        basicPlan.put("maxWordsPerArticle", basicConfig.getMaxWordsPerArticle());
        basicPlan.put("aiFeaturesEnabled", basicConfig.getAiFeaturesEnabled());
        basicPlan.put("prioritySupport", basicConfig.getPrioritySupport());
        planPrices.put("BASIC", basicPlan);
        
        // 添加专业套餐
        Map<String, Object> proPlan = new HashMap<>();
        SubscriptionPlanProperties.PlanConfig proConfig = plans.get("pro");
        proPlan.put("name", proConfig.getName());
        proPlan.put("description", proConfig.getDescription());
        proPlan.put("price", proConfig.getPriceCny());
        proPlan.put("currency", defaultCurrency);
        proPlan.put("maxArticlesPerMonth", proConfig.getMaxArticlesPerMonth());
        proPlan.put("maxWordsPerArticle", proConfig.getMaxWordsPerArticle());
        proPlan.put("aiFeaturesEnabled", proConfig.getAiFeaturesEnabled());
        proPlan.put("prioritySupport", proConfig.getPrioritySupport());
        planPrices.put("PRO", proPlan);
        
        // 添加企业套餐
        Map<String, Object> enterprisePlan = new HashMap<>();
        SubscriptionPlanProperties.PlanConfig enterpriseConfig = plans.get("enterprise");
        enterprisePlan.put("name", enterpriseConfig.getName());
        enterprisePlan.put("description", enterpriseConfig.getDescription());
        enterprisePlan.put("price", enterpriseConfig.getPriceCny());
        enterprisePlan.put("currency", defaultCurrency);
        enterprisePlan.put("maxArticlesPerMonth", enterpriseConfig.getMaxArticlesPerMonth());
        enterprisePlan.put("maxWordsPerArticle", enterpriseConfig.getMaxWordsPerArticle());
        enterprisePlan.put("aiFeaturesEnabled", enterpriseConfig.getAiFeaturesEnabled());
        enterprisePlan.put("prioritySupport", enterpriseConfig.getPrioritySupport());
        planPrices.put("ENTERPRISE", enterprisePlan);
        
        return planPrices;
    }

    @Override
    public Subscription createSubscription(Long userId, String planType, String paymentMethod) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setPlanType(planType);
        subscription.setPaymentMethod(paymentMethod);
        subscription.setStartDate(LocalDateTime.now());
        
        // 从配置中获取套餐参数
        Map<String, SubscriptionPlanProperties.PlanConfig> plans = subscriptionPlanProperties.getPlans();
        String defaultCurrency = subscriptionPlanProperties.getDefaultCurrency();
        SubscriptionPlanProperties.PlanConfig config = null;
        
        // 根据套餐类型设置参数
        switch (planType.toUpperCase()) {
            case "BASIC":
                config = plans != null ? plans.get("basic") : null;
                subscription.setPrice(config != null && config.getPriceCny() != null ? config.getPriceCny() : new BigDecimal("19"));
                subscription.setCurrency(defaultCurrency != null ? defaultCurrency : "CNY");
                subscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 10);
                subscription.setMaxWordsPerArticle(config != null && config.getMaxWordsPerArticle() != null ? config.getMaxWordsPerArticle() : 1000);
                subscription.setAiFeaturesEnabled(config != null && config.getAiFeaturesEnabled() != null ? config.getAiFeaturesEnabled() : false);
                subscription.setPrioritySupport(config != null && config.getPrioritySupport() != null ? config.getPrioritySupport() : false);
                break;
            case "PRO":
                config = plans != null ? plans.get("pro") : null;
                subscription.setPrice(config != null && config.getPriceCny() != null ? config.getPriceCny() : new BigDecimal("39"));
                subscription.setCurrency(defaultCurrency != null ? defaultCurrency : "CNY");
                subscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 50);
                subscription.setMaxWordsPerArticle(config != null && config.getMaxWordsPerArticle() != null ? config.getMaxWordsPerArticle() : 5000);
                subscription.setAiFeaturesEnabled(config != null && config.getAiFeaturesEnabled() != null ? config.getAiFeaturesEnabled() : true);
                subscription.setPrioritySupport(config != null && config.getPrioritySupport() != null ? config.getPrioritySupport() : true);
                break;
            case "ENTERPRISE":
                config = plans != null ? plans.get("enterprise") : null;
                subscription.setPrice(config != null && config.getPriceCny() != null ? config.getPriceCny() : new BigDecimal("99"));
                subscription.setCurrency(defaultCurrency != null ? defaultCurrency : "CNY");
                subscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 200);
                subscription.setMaxWordsPerArticle(config != null && config.getMaxWordsPerArticle() != null ? config.getMaxWordsPerArticle() : 20000);
                subscription.setAiFeaturesEnabled(config != null && config.getAiFeaturesEnabled() != null ? config.getAiFeaturesEnabled() : true);
                subscription.setPrioritySupport(config != null && config.getPrioritySupport() != null ? config.getPrioritySupport() : true);
                break;
            default:
                throw new IllegalArgumentException("无效的套餐类型");
        }
        
        subscription.setEndDate(LocalDateTime.now().plusDays(30));
        subscription.setStatus("ACTIVE");
        subscription.setAutoRenew(true);
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        
        subscriptionMapper.insert(subscription);
        return subscription;
    }

    @Override
    public Subscription getCurrentSubscription(Long userId) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getUserId, userId)
               .eq(Subscription::getStatus, "ACTIVE")
               .orderByDesc(Subscription::getCreatedAt)
               .last("LIMIT 1");
        
        return subscriptionMapper.selectOne(wrapper);
    }

    @Override
    public List<Subscription> getSubscriptionHistory(Long userId) {
        LambdaQueryWrapper<Subscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Subscription::getUserId, userId)
               .orderByDesc(Subscription::getCreatedAt);
        
        return subscriptionMapper.selectList(wrapper);
    }

    @Override
    public void cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionMapper.selectById(subscriptionId);
        if (subscription != null) {
            subscription.setStatus("CANCELLED");
            subscription.setAutoRenew(false);
            subscription.setUpdatedAt(LocalDateTime.now());
            subscriptionMapper.updateById(subscription);
        }
    }

    @Override
    public boolean hasActiveSubscription(Long userId) {
        Subscription subscription = getCurrentSubscription(userId);
        return subscription != null && 
               "ACTIVE".equals(subscription.getStatus()) && 
               subscription.getEndDate().isAfter(LocalDateTime.now());
    }

    @Override
    public boolean checkUsageLimit(Long userId, int articlesCount, int wordsCount) {
        Subscription subscription = getCurrentSubscription(userId);
        if (subscription == null) {
            return false; // 免费用户限制
        }
        
        return articlesCount <= subscription.getMaxArticlesPerMonth() && 
               wordsCount <= subscription.getMaxWordsPerArticle();
    }

    @Override
    public Map<String, Object> getRemainingQuota(Long userId) {
        Subscription subscription = getCurrentSubscription(userId);
        Map<String, Object> quota = new HashMap<>();
        
        if (subscription == null) {
            quota.put("hasSubscription", false);
            quota.put("maxArticles", 3); // 免费用户限制
            quota.put("maxWords", 500);
            quota.put("planType", "FREE");
        } else {
            quota.put("hasSubscription", true);
            quota.put("maxArticles", subscription.getMaxArticlesPerMonth());
            quota.put("maxWords", subscription.getMaxWordsPerArticle());
            quota.put("planType", subscription.getPlanType());
            quota.put("endDate", subscription.getEndDate());
        }
        
        return quota;
    }
}