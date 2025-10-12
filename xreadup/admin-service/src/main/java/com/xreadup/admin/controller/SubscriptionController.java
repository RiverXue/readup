package com.xreadup.admin.controller;

import com.xreadup.admin.client.SubscriptionServiceClient;
import com.xreadup.admin.client.UserServiceClient;
import com.xreadup.admin.dto.ApiResponse;
import com.xreadup.admin.util.FeignResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订阅管理控制器
 * 提供后台管理系统对订阅计划和用户订阅的管理功能
 */
@RestController
@RequestMapping("/api/admin/subscriptions")
@Tag(name = "订阅管理", description = "订阅相关的后台管理API")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionServiceClient subscriptionServiceClient;
    
    @Autowired
    private UserServiceClient userServiceClient;

    /**
     * 获取订阅计划列表
     * @return 订阅计划列表
     */
    @GetMapping("/plans")
    @Operation(summary = "获取订阅计划列表", description = "获取所有订阅计划")
    public ApiResponse<List<Map<String, Object>>> getSubscriptionPlans() {
        try {
            logger.info("获取订阅计划列表");
            
            // 通过FeignClient调用订阅服务
            var response = subscriptionServiceClient.getSubscriptionPlans();
            if (response != null && response.isSuccess() && response.getData() != null) {
                logger.info("成功获取订阅计划列表，数量: {}", response.getData().size());
                return ApiResponse.success(response.getData());
            } else {
                logger.warn("获取订阅计划列表失败，code: {}, message: {}", 
                           response != null ? response.getCode() : "未知", 
                           response != null ? response.getMessage() : "未知");
                
                // 降级处理：返回默认的订阅计划配置
                return getDefaultSubscriptionPlans();
            }
        } catch (Exception e) {
            logger.error("获取订阅计划列表失败", e);
            // 降级处理：返回默认的订阅计划配置
            return getDefaultSubscriptionPlans();
        }
    }

    /**
     * 降级处理：返回默认的订阅计划配置
     */
    private ApiResponse<List<Map<String, Object>>> getDefaultSubscriptionPlans() {
        logger.info("使用默认订阅计划配置");
        
        List<Map<String, Object>> defaultPlans = new ArrayList<>();
        
        // 免费套餐
        Map<String, Object> freePlan = new HashMap<>();
        freePlan.put("id", 1L);
        freePlan.put("planType", "FREE");
        freePlan.put("price", 0.0);
        freePlan.put("currency", "CNY");
        freePlan.put("maxArticlesPerMonth", 10);
        freePlan.put("maxWordsPerArticle", 1000);
        freePlan.put("aiFeaturesEnabled", false);
        freePlan.put("prioritySupport", false);
        freePlan.put("createTime", "2025-09-15 00:00:00");
        freePlan.put("updateTime", "2025-09-15 00:00:00");
        freePlan.put("deleted", 0);
        defaultPlans.add(freePlan);
        
        // 基础套餐
        Map<String, Object> basicPlan = new HashMap<>();
        basicPlan.put("id", 2L);
        basicPlan.put("planType", "BASIC");
        basicPlan.put("price", 29.0);
        basicPlan.put("currency", "CNY");
        basicPlan.put("maxArticlesPerMonth", 100);
        basicPlan.put("maxWordsPerArticle", 5000);
        basicPlan.put("aiFeaturesEnabled", true);
        basicPlan.put("prioritySupport", false);
        basicPlan.put("createTime", "2025-09-15 00:00:00");
        basicPlan.put("updateTime", "2025-09-15 00:00:00");
        basicPlan.put("deleted", 0);
        defaultPlans.add(basicPlan);
        
        // 专业套餐
        Map<String, Object> proPlan = new HashMap<>();
        proPlan.put("id", 3L);
        proPlan.put("planType", "PRO");
        proPlan.put("price", 99.0);
        proPlan.put("currency", "CNY");
        proPlan.put("maxArticlesPerMonth", 500);
        proPlan.put("maxWordsPerArticle", 10000);
        proPlan.put("aiFeaturesEnabled", true);
        proPlan.put("prioritySupport", true);
        proPlan.put("createTime", "2025-09-15 00:00:00");
        proPlan.put("updateTime", "2025-09-15 00:00:00");
        proPlan.put("deleted", 0);
        defaultPlans.add(proPlan);
        
        return ApiResponse.success(defaultPlans);
    }

    /**
     * 创建订阅计划
     * @param subscriptionPlanCreateDTO 订阅计划创建信息
     * @return 创建的订阅计划
     */
    @PostMapping("/plans")
    @Operation(summary = "创建订阅计划", description = "创建新的订阅计划")
    public ApiResponse<SubscriptionServiceClient.SubscriptionPlanDTO> createSubscriptionPlan(
            @Valid @RequestBody SubscriptionServiceClient.SubscriptionPlanCreateDTO subscriptionPlanCreateDTO) {
        try {
            logger.info("创建订阅计划: {}", subscriptionPlanCreateDTO.getPlanType());
            return subscriptionServiceClient.createSubscriptionPlan(subscriptionPlanCreateDTO);
        } catch (Exception e) {
            logger.error("创建订阅计划失败", e);
            return ApiResponse.fail(500, "创建订阅计划失败");
        }
    }

    /**
     * 更新订阅计划
     * @param planId 订阅计划ID
     * @param subscriptionPlanUpdateDTO 订阅计划更新信息
     * @return 更新结果
     */
    @PutMapping("/plans/{planId}")
    @Operation(summary = "更新订阅计划", description = "更新现有订阅计划信息")
    public ApiResponse<Boolean> updateSubscriptionPlan(
            @PathVariable @NotNull(message = "订阅计划ID不能为空") Long planId,
            @Valid @RequestBody SubscriptionServiceClient.SubscriptionPlanUpdateDTO subscriptionPlanUpdateDTO) {
        try {
            logger.info("更新订阅计划，planId: {}", planId);
            return subscriptionServiceClient.updateSubscriptionPlan(planId, subscriptionPlanUpdateDTO);
        } catch (Exception e) {
            logger.error("更新订阅计划失败", e);
            return ApiResponse.fail(500, "更新订阅计划失败");
        }
    }

    /**
     * 删除订阅计划
     * @param planId 订阅计划ID
     * @return 删除结果
     */
    @DeleteMapping("/plans/{planId}")
    @Operation(summary = "删除订阅计划", description = "删除指定的订阅计划")
    public ApiResponse<Boolean> deleteSubscriptionPlan(
            @PathVariable @NotNull(message = "订阅计划ID不能为空") Long planId) {
        try {
            logger.info("删除订阅计划，planId: {}", planId);
            return subscriptionServiceClient.deleteSubscriptionPlan(planId);
        } catch (Exception e) {
            logger.error("删除订阅计划失败", e);
            return ApiResponse.fail(500, "删除订阅计划失败");
        }
    }

    /**
     * 获取所有用户订阅列表（分页）
     * @param page 页码
     * @param pageSize 每页大小
     * @return 用户订阅列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取订阅列表", description = "获取所有用户订阅的分页列表")
    public ApiResponse<SubscriptionServiceClient.SubscriptionPageResult> getSubscriptionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            logger.info("获取订阅列表，page: {}, pageSize: {}", page, pageSize);
            return subscriptionServiceClient.getSubscriptionList(page, pageSize);
        } catch (Exception e) {
            logger.error("获取订阅列表失败", e);
            return ApiResponse.fail(500, "获取订阅列表失败");
        }
    }

    /**
     * 获取所有用户订阅列表（分页，支持筛选）
     * @param page 页码
     * @param pageSize 每页大小
     * @param userId 用户ID（可选筛选）
     * @param planType 计划类型（可选筛选）
     * @param status 状态（可选筛选）
     * @return 用户订阅列表
     */
    @GetMapping("/user")
    @Operation(summary = "获取用户订阅列表", description = "获取所有用户订阅的分页列表，支持筛选")
    public ApiResponse<Map<String, Object>> getUserSubscriptions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status) {
        try {
            logger.info("获取用户订阅列表，page: {}, pageSize: {}, userId: {}, planType: {}, status: {}", 
                       page, pageSize, userId, planType, status);
            
            // 调用user-service获取用户订阅列表
            var response = userServiceClient.getUserSubscriptionList(page, pageSize, userId, planType, status);
            if (FeignResponseUtil.isSuccess(response, logger, "获取用户订阅列表")) {
                logger.info("成功获取用户订阅列表，数量: {}", response.getData());
                return ApiResponse.success(response.getData());
            } else {
                
                // 降级处理：返回空列表
                Map<String, Object> emptyResult = new HashMap<>();
                emptyResult.put("list", new ArrayList<>());
                emptyResult.put("total", 0);
                emptyResult.put("page", page);
                emptyResult.put("pageSize", pageSize);
                return ApiResponse.success(emptyResult);
            }
        } catch (Exception e) {
            logger.error("获取用户订阅列表失败", e);
            // 降级处理：返回空列表
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("list", new ArrayList<>());
            emptyResult.put("total", 0);
            emptyResult.put("page", page);
            emptyResult.put("pageSize", pageSize);
            return ApiResponse.success(emptyResult);
        }
    }

    /**
     * 获取指定用户订阅列表
     * @param userId 用户ID
     * @return 用户订阅列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取指定用户订阅列表", description = "获取指定用户的所有订阅信息")
    public ApiResponse<List<SubscriptionServiceClient.UserSubscriptionDTO>> getUserSubscriptionsByUserId(
            @PathVariable @NotNull(message = "用户ID不能为空") Long userId) {
        try {
            logger.info("获取用户订阅列表，userId: {}", userId);
            return subscriptionServiceClient.getUserSubscriptions(userId);
        } catch (Exception e) {
            logger.error("获取用户订阅列表失败", e);
            return ApiResponse.fail(500, "获取用户订阅列表失败");
        }
    }

    /**
     * 创建用户订阅
     * @param userSubscriptionCreateDTO 用户订阅创建信息
     * @return 创建的用户订阅
     */
    @PostMapping("/user")
    @Operation(summary = "创建用户订阅", description = "为用户创建新的订阅")
    public ApiResponse<SubscriptionServiceClient.UserSubscriptionDTO> createUserSubscription(
            @Valid @RequestBody SubscriptionServiceClient.UserSubscriptionCreateDTO userSubscriptionCreateDTO) {
        try {
            logger.info("创建用户订阅，userId: {}, planType: {}", 
                    userSubscriptionCreateDTO.getUserId(), userSubscriptionCreateDTO.getPlanType());
            return subscriptionServiceClient.createUserSubscription(userSubscriptionCreateDTO);
        } catch (Exception e) {
            logger.error("创建用户订阅失败", e);
            return ApiResponse.fail(500, "创建用户订阅失败");
        }
    }

    /**
     * 更新用户订阅状态
     * @param subscriptionId 订阅ID
     * @param status 订阅状态
     * @return 更新结果
     */
    @PutMapping("/user/{subscriptionId}/status")
    @Operation(summary = "更新用户订阅状态", description = "更新用户订阅的状态")
    public ApiResponse<Boolean> updateUserSubscriptionStatus(
            @PathVariable @NotNull(message = "订阅ID不能为空") Long subscriptionId,
            @RequestParam @NotNull(message = "状态不能为空") String status) {
        try {
            logger.info("更新用户订阅状态，subscriptionId: {}, status: {}", subscriptionId, status);
            return subscriptionServiceClient.updateUserSubscriptionStatus(subscriptionId, status);
        } catch (Exception e) {
            logger.error("更新用户订阅状态失败", e);
            return ApiResponse.fail(500, "更新用户订阅状态失败");
        }
    }
}