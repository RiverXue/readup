package com.xreadup.ai.userservice.config;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 订阅套餐配置属性类
 * 硬编码的配置值，不再依赖Nacos配置中心
 */
@Data
@Component
public class SubscriptionPlanProperties {
    
    /**
     * 所有套餐配置
     */
    private Map<String, PlanConfig> plans = initPlans();
    
    /**
     * 默认货币类型
     */
    private String defaultCurrency = "CNY";
    
    /**
     * 初始化所有套餐配置
     */
    private Map<String, PlanConfig> initPlans() {
        Map<String, PlanConfig> plansMap = new HashMap<>();
        
        // 免费套餐
        PlanConfig freePlan = new PlanConfig();
        freePlan.setName("免费版");
        freePlan.setDescription("基础阅读体验");
        freePlan.setPriceCny(new BigDecimal("0"));
        freePlan.setMaxArticlesPerMonth(30);
        freePlan.setMaxWordsPerArticle(1500);
        freePlan.setAiFeaturesEnabled(false);
        freePlan.setPrioritySupport(false);
        plansMap.put("free", freePlan);
        
        // 基础套餐
        PlanConfig basicPlan = new PlanConfig();
        basicPlan.setName("基础版");
        basicPlan.setDescription("适合个人用户的基础功能");
        basicPlan.setPriceCny(new BigDecimal("7"));
        basicPlan.setMaxArticlesPerMonth(100);
        basicPlan.setMaxWordsPerArticle(3000);
        basicPlan.setAiFeaturesEnabled(false);
        basicPlan.setPrioritySupport(false);
        plansMap.put("basic", basicPlan);
        
        // 专业套餐
        PlanConfig proPlan = new PlanConfig();
        proPlan.setName("专业版");
        proPlan.setDescription("适合深度阅读用户");
        proPlan.setPriceCny(new BigDecimal("17"));
        proPlan.setMaxArticlesPerMonth(300);
        proPlan.setMaxWordsPerArticle(5000);
        proPlan.setAiFeaturesEnabled(true);
        proPlan.setPrioritySupport(false);
        plansMap.put("pro", proPlan);
        
        // 企业套餐
        PlanConfig enterprisePlan = new PlanConfig();
        enterprisePlan.setName("企业版");
        enterprisePlan.setDescription("适合团队和企业用户");
        enterprisePlan.setPriceCny(new BigDecimal("37"));
        enterprisePlan.setMaxArticlesPerMonth(1000);
        enterprisePlan.setMaxWordsPerArticle(20000);
        enterprisePlan.setAiFeaturesEnabled(true);
        enterprisePlan.setPrioritySupport(true);
        plansMap.put("enterprise", enterprisePlan);
        
        return plansMap;
    }
    
    /**
     * 套餐详细配置
     */
    @Data
    public static class PlanConfig {
        private String name;
        private String description;
        private BigDecimal priceCny;
        private Integer maxArticlesPerMonth;
        private Integer maxWordsPerArticle;
        private Boolean aiFeaturesEnabled;
        private Boolean prioritySupport;
    }
}