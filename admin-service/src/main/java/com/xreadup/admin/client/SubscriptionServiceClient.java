package com.xreadup.admin.client;

import com.xreadup.admin.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订阅服务Feign客户端
 * 用于调用订阅服务提供的API接口
 */
@FeignClient(name = "subscription-service")
public interface SubscriptionServiceClient {

    /**
     * 获取订阅计划列表
     * @return 订阅计划列表
     */
    @GetMapping("/api/subscription/plans")
    ApiResponse<List<SubscriptionPlanDTO>> getSubscriptionPlans();

    /**
     * 创建订阅计划
     * @param subscriptionPlanCreateDTO 创建订阅计划的数据
     * @return 创建的订阅计划
     */
    @PostMapping("/api/subscription/plans")
    ApiResponse<SubscriptionPlanDTO> createSubscriptionPlan(@RequestBody SubscriptionPlanCreateDTO subscriptionPlanCreateDTO);

    /**
     * 更新订阅计划
     * @param planId 订阅计划ID
     * @param subscriptionPlanUpdateDTO 更新订阅计划的数据
     * @return 更新结果
     */
    @PutMapping("/api/subscription/plans/{planId}")
    ApiResponse<Boolean> updateSubscriptionPlan(
            @PathVariable("planId") Long planId,
            @RequestBody SubscriptionPlanUpdateDTO subscriptionPlanUpdateDTO);

    /**
     * 删除订阅计划
     * @param planId 订阅计划ID
     * @return 删除结果
     */
    @DeleteMapping("/api/subscription/plans/{planId}")
    ApiResponse<Boolean> deleteSubscriptionPlan(@PathVariable("planId") Long planId);

    /**
     * 获取用户订阅列表
     * @param userId 用户ID
     * @return 用户订阅列表
     */
    @GetMapping("/api/subscription/user/{userId}")
    ApiResponse<List<UserSubscriptionDTO>> getUserSubscriptions(@PathVariable("userId") Long userId);

    /**
     * 手动创建用户订阅
     * @param userSubscriptionCreateDTO 创建用户订阅的数据
     * @return 创建的用户订阅
     */
    @PostMapping("/api/subscription/user")
    ApiResponse<UserSubscriptionDTO> createUserSubscription(@RequestBody UserSubscriptionCreateDTO userSubscriptionCreateDTO);

    /**
     * 更新用户订阅状态
     * @param subscriptionId 用户订阅ID
     * @param status 订阅状态
     * @return 更新结果
     */
    @PutMapping("/api/subscription/user/{subscriptionId}/status")
    ApiResponse<Boolean> updateUserSubscriptionStatus(
            @PathVariable("subscriptionId") Long subscriptionId,
            @RequestParam String status);

    /**
     * 订阅计划DTO接口定义
     */
    interface SubscriptionPlanDTO {
        Long getId();
        String getPlanType();
        Double getPrice();
        String getCurrency();
        Integer getMaxArticlesPerMonth();
        Integer getMaxWordsPerArticle();
        Boolean getAiFeaturesEnabled();
        Boolean getPrioritySupport();
        String getCreateTime();
        String getUpdateTime();
        Integer getDeleted();
    }

    /**
     * 创建订阅计划DTO
     */
    interface SubscriptionPlanCreateDTO {
        String getPlanType();
        Double getPrice();
        String getCurrency();
        Integer getMaxArticlesPerMonth();
        Integer getMaxWordsPerArticle();
        Boolean getAiFeaturesEnabled();
        Boolean getPrioritySupport();
    }

    /**
     * 更新订阅计划DTO
     */
    interface SubscriptionPlanUpdateDTO {
        String getPlanType();
        Double getPrice();
        String getCurrency();
        Integer getMaxArticlesPerMonth();
        Integer getMaxWordsPerArticle();
        Boolean getAiFeaturesEnabled();
        Boolean getPrioritySupport();
    }

    /**
     * 用户订阅DTO接口定义
     */
    interface UserSubscriptionDTO {
        Long getId();
        Long getUserId();
        String getPlanType();
        Double getPrice();
        String getCurrency();
        String getStatus();
        String getStartDate();
        String getEndDate();
        Boolean getAutoRenew();
        String getCreateTime();
        String getUpdateTime();
        Integer getDeleted();
    }

    /**
     * 创建用户订阅DTO
     */
    interface UserSubscriptionCreateDTO {
        Long getUserId();
        Long getPlanId();
        String getStatus();
        String getStartDate();
        String getEndDate();
        Boolean getAutoRenew();
    }
}