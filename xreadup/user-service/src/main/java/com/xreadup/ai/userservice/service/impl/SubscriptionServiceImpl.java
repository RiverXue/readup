package com.xreadup.ai.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xreadup.ai.userservice.config.SubscriptionPlanProperties;
import com.xreadup.ai.userservice.entity.Subscription;
import com.xreadup.ai.userservice.mapper.SubscriptionMapper;
import com.xreadup.ai.userservice.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订阅服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
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
                subscription.setPrice(config != null && config.getPriceCny() != null ? config.getPriceCny() : new BigDecimal("7"));
                subscription.setCurrency(defaultCurrency != null ? defaultCurrency : "CNY");
                subscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 100);
                subscription.setMaxWordsPerArticle(config != null && config.getMaxWordsPerArticle() != null ? config.getMaxWordsPerArticle() : 3000);
                subscription.setAiFeaturesEnabled(config != null && config.getAiFeaturesEnabled() != null ? config.getAiFeaturesEnabled() : false);
                subscription.setPrioritySupport(config != null && config.getPrioritySupport() != null ? config.getPrioritySupport() : false);
                break;
            case "PRO":
                config = plans != null ? plans.get("pro") : null;
                subscription.setPrice(config != null && config.getPriceCny() != null ? config.getPriceCny() : new BigDecimal("17"));
                subscription.setCurrency(defaultCurrency != null ? defaultCurrency : "CNY");
                subscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 300);
                subscription.setMaxWordsPerArticle(config != null && config.getMaxWordsPerArticle() != null ? config.getMaxWordsPerArticle() : 5000);
                subscription.setAiFeaturesEnabled(config != null && config.getAiFeaturesEnabled() != null ? config.getAiFeaturesEnabled() : true);
                subscription.setPrioritySupport(config != null && config.getPrioritySupport() != null ? config.getPrioritySupport() : false);
                break;
            case "ENTERPRISE":
                config = plans != null ? plans.get("enterprise") : null;
                subscription.setPrice(config != null && config.getPriceCny() != null ? config.getPriceCny() : new BigDecimal("37"));
                subscription.setCurrency(defaultCurrency != null ? defaultCurrency : "CNY");
                subscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 1000);
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

    @Override
    public Subscription upgradeSubscription(Long userId, String newPlanType, String paymentMethod) {
        // 获取当前订阅
        Subscription currentSubscription = getCurrentSubscription(userId);
        
        // 处理免费用户升级的情况
        if (currentSubscription == null) {
            // 免费用户直接创建新订阅，按全价收费
            return createSubscription(userId, newPlanType, paymentMethod);
        }
        
        // 检查是否已经是最高级别
        if (currentSubscription.getPlanType().equals("ENTERPRISE")) {
            throw new IllegalArgumentException("已经是最高级别套餐，无法升级");
        }
        
        // 检查升级方向是否正确
        String currentPlan = currentSubscription.getPlanType();
        if (isDowngrade(currentPlan, newPlanType)) {
            throw new IllegalArgumentException("只能升级到更高级别的套餐");
        }
        
        // 计算剩余天数
        int remainingDays = calculateRemainingDays(currentSubscription.getEndDate());
        
        // 计算升级差价
        BigDecimal upgradePrice = calculateUpgradePrice(currentPlan, newPlanType, remainingDays);
        
        // 创建新的升级订阅
        Subscription upgradeSubscription = new Subscription();
        upgradeSubscription.setUserId(userId);
        upgradeSubscription.setPlanType(newPlanType);
        upgradeSubscription.setPaymentMethod(paymentMethod);
        upgradeSubscription.setStartDate(LocalDateTime.now());
        upgradeSubscription.setPrice(upgradePrice);
        upgradeSubscription.setCurrency(currentSubscription.getCurrency());
        
        // 从配置中获取新套餐参数
        Map<String, SubscriptionPlanProperties.PlanConfig> plans = subscriptionPlanProperties.getPlans();
        SubscriptionPlanProperties.PlanConfig config = null;
        
        // 根据新套餐类型设置参数
        switch (newPlanType.toUpperCase()) {
            case "BASIC":
                config = plans != null ? plans.get("basic") : null;
                upgradeSubscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 100);
                upgradeSubscription.setMaxWordsPerArticle(config != null && config.getMaxWordsPerArticle() != null ? config.getMaxWordsPerArticle() : 3000);
                upgradeSubscription.setAiFeaturesEnabled(config != null && config.getAiFeaturesEnabled() != null ? config.getAiFeaturesEnabled() : false);
                upgradeSubscription.setPrioritySupport(config != null && config.getPrioritySupport() != null ? config.getPrioritySupport() : false);
                break;
            case "PRO":
                config = plans != null ? plans.get("pro") : null;
                upgradeSubscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 300);
                upgradeSubscription.setMaxWordsPerArticle(config != null && config.getMaxWordsPerArticle() != null ? config.getMaxWordsPerArticle() : 5000);
                upgradeSubscription.setAiFeaturesEnabled(config != null && config.getAiFeaturesEnabled() != null ? config.getAiFeaturesEnabled() : true);
                upgradeSubscription.setPrioritySupport(config != null && config.getPrioritySupport() != null ? config.getPrioritySupport() : false);
                break;
            case "ENTERPRISE":
                config = plans != null ? plans.get("enterprise") : null;
                upgradeSubscription.setMaxArticlesPerMonth(config != null && config.getMaxArticlesPerMonth() != null ? config.getMaxArticlesPerMonth() : 1000);
                upgradeSubscription.setMaxWordsPerArticle(config != null && config.getMaxWordsPerArticle() != null ? config.getMaxWordsPerArticle() : 20000);
                upgradeSubscription.setAiFeaturesEnabled(config != null && config.getAiFeaturesEnabled() != null ? config.getAiFeaturesEnabled() : true);
                upgradeSubscription.setPrioritySupport(config != null && config.getPrioritySupport() != null ? config.getPrioritySupport() : true);
                break;
            default:
                throw new IllegalArgumentException("无效的套餐类型");
        }
        
        // 设置结束日期为原订阅的结束日期
        upgradeSubscription.setEndDate(currentSubscription.getEndDate());
        upgradeSubscription.setStatus("ACTIVE");
        upgradeSubscription.setAutoRenew(currentSubscription.getAutoRenew());
        upgradeSubscription.setCreatedAt(LocalDateTime.now());
        upgradeSubscription.setUpdatedAt(LocalDateTime.now());
        
        // 取消当前订阅
        currentSubscription.setStatus("CANCELLED");
        currentSubscription.setUpdatedAt(LocalDateTime.now());
        subscriptionMapper.updateById(currentSubscription);
        
        // 插入新的升级订阅
        subscriptionMapper.insert(upgradeSubscription);
        
        return upgradeSubscription;
    }

    @Override
    public BigDecimal calculateUpgradePrice(String currentPlanType, String newPlanType, int remainingDays) {
        // 获取套餐价格配置
        Map<String, SubscriptionPlanProperties.PlanConfig> plans = subscriptionPlanProperties.getPlans();
        
        // 获取当前套餐和新套餐的月价格
        BigDecimal currentMonthlyPrice = getMonthlyPrice(currentPlanType, plans);
        BigDecimal newMonthlyPrice = getMonthlyPrice(newPlanType, plans);
        
        // 如果当前是免费用户，按全价收费
        if ("FREE".equals(currentPlanType.toUpperCase())) {
            return newMonthlyPrice;
        }
        
        // 计算每日价格
        BigDecimal currentDailyPrice = currentMonthlyPrice.divide(new BigDecimal("30"), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal newDailyPrice = newMonthlyPrice.divide(new BigDecimal("30"), 2, java.math.RoundingMode.HALF_UP);
        
        // 计算差价
        BigDecimal dailyDifference = newDailyPrice.subtract(currentDailyPrice);
        BigDecimal upgradePrice = dailyDifference.multiply(new BigDecimal(remainingDays));
        
        // 确保价格不为负数
        return upgradePrice.max(BigDecimal.ZERO);
    }
    
    /**
     * 获取套餐的月价格
     */
    private BigDecimal getMonthlyPrice(String planType, Map<String, SubscriptionPlanProperties.PlanConfig> plans) {
        SubscriptionPlanProperties.PlanConfig config = plans.get(planType.toLowerCase());
        if (config != null && config.getPriceCny() != null) {
            return config.getPriceCny();
        }
        
        // 默认价格
        switch (planType.toUpperCase()) {
            case "FREE": return BigDecimal.ZERO;
            case "BASIC": return new BigDecimal("7");
            case "PRO": return new BigDecimal("17");
            case "ENTERPRISE": return new BigDecimal("37");
            default: return BigDecimal.ZERO;
        }
    }
    
    /**
     * 计算剩余天数
     */
    private int calculateRemainingDays(LocalDateTime endDate) {
        LocalDateTime now = LocalDateTime.now();
        if (endDate.isBefore(now)) {
            return 0;
        }
        return (int) java.time.Duration.between(now, endDate).toDays();
    }
    
    /**
     * 判断是否为降级
     */
    private boolean isDowngrade(String currentPlan, String newPlan) {
        String[] planOrder = {"FREE", "BASIC", "PRO", "ENTERPRISE"};
        int currentIndex = java.util.Arrays.asList(planOrder).indexOf(currentPlan);
        int newIndex = java.util.Arrays.asList(planOrder).indexOf(newPlan);
        return newIndex < currentIndex;
    }
    
    @Override
    public Map<String, Object> startTrial(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查用户是否已经使用过试用
            LambdaQueryWrapper<Subscription> trialQuery = new LambdaQueryWrapper<>();
            trialQuery.eq(Subscription::getUserId, userId)
                     .eq(Subscription::getIsTrial, true);
            
            Subscription existingTrial = subscriptionMapper.selectOne(trialQuery);
            
            if (existingTrial != null) {
                result.put("success", false);
                result.put("message", "您已经使用过试用功能");
                return result;
            }
            
            // 创建试用订阅（等同于PRO套餐）
            Subscription trialSubscription = new Subscription();
            trialSubscription.setUserId(userId);
            trialSubscription.setPlanType("TRIAL");
            trialSubscription.setPrice(BigDecimal.ZERO); // 试用免费
            trialSubscription.setCurrency("CNY"); // 设置货币
            trialSubscription.setStatus("ACTIVE");
            trialSubscription.setStartDate(LocalDateTime.now());
            trialSubscription.setEndDate(LocalDateTime.now().plusDays(7)); // 7天试用
            trialSubscription.setPaymentMethod("TRIAL"); // 试用方式
            trialSubscription.setAutoRenew(false); // 试用不自动续费
            trialSubscription.setIsTrial(true);
            
            // 设置PRO套餐的权限
            trialSubscription.setMaxArticlesPerMonth(300);
            trialSubscription.setMaxWordsPerArticle(5000);
            trialSubscription.setAiFeaturesEnabled(true);
            trialSubscription.setPrioritySupport(false);
            
            subscriptionMapper.insert(trialSubscription);
            
            result.put("success", true);
            result.put("message", "试用已开始，享受7天专业版功能！");
            result.put("trialEndDate", trialSubscription.getEndDate());
            
        } catch (Exception e) {
            log.error("开始试用失败: userId={}, error={}", userId, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "试用启动失败，请稍后重试");
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> checkTrialStatus(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 检查是否有试用记录
            LambdaQueryWrapper<Subscription> trialQuery = new LambdaQueryWrapper<>();
            trialQuery.eq(Subscription::getUserId, userId)
                     .eq(Subscription::getIsTrial, true);
            
            Subscription trialSubscription = subscriptionMapper.selectOne(trialQuery);
            
            boolean hasUsedTrial = trialSubscription != null;
            boolean isTrialActive = false;
            
            if (trialSubscription != null && "ACTIVE".equals(trialSubscription.getStatus())) {
                // 检查是否过期
                isTrialActive = LocalDateTime.now().isBefore(trialSubscription.getEndDate());
            }
            
            result.put("success", true);
            result.put("hasUsedTrial", hasUsedTrial);
            result.put("isTrialActive", isTrialActive);
            
            if (trialSubscription != null) {
                result.put("trialEndDate", trialSubscription.getEndDate());
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查试用状态失败");
        }
        
        return result;
    }
}