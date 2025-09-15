package com.xreadup.ai.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @Override
    public Subscription createSubscription(Long userId, String planType, String paymentMethod) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setPlanType(planType);
        subscription.setPaymentMethod(paymentMethod);
        subscription.setStartDate(LocalDateTime.now());
        
        // 根据套餐类型设置参数
        switch (planType.toUpperCase()) {
            case "BASIC":
                subscription.setPrice(new BigDecimal("9.99"));
                subscription.setCurrency("USD");
                subscription.setMaxArticlesPerMonth(10);
                subscription.setMaxWordsPerArticle(1000);
                subscription.setAiFeaturesEnabled(false);
                subscription.setPrioritySupport(false);
                break;
            case "PRO":
                subscription.setPrice(new BigDecimal("19.99"));
                subscription.setCurrency("USD");
                subscription.setMaxArticlesPerMonth(50);
                subscription.setMaxWordsPerArticle(5000);
                subscription.setAiFeaturesEnabled(true);
                subscription.setPrioritySupport(true);
                break;
            case "ENTERPRISE":
                subscription.setPrice(new BigDecimal("49.99"));
                subscription.setCurrency("USD");
                subscription.setMaxArticlesPerMonth(200);
                subscription.setMaxWordsPerArticle(20000);
                subscription.setAiFeaturesEnabled(true);
                subscription.setPrioritySupport(true);
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