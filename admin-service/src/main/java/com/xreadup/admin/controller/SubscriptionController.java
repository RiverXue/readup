package com.xreadup.admin.controller;

import com.xreadup.admin.client.SubscriptionServiceClient;
import com.xreadup.admin.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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

    /**
     * 获取订阅计划列表
     * @return 订阅计划列表
     */
    @GetMapping("/plans")
    @Operation(summary = "获取订阅计划列表", description = "获取所有订阅计划")
    public ApiResponse<List<SubscriptionServiceClient.SubscriptionPlanDTO>> getSubscriptionPlans() {
        try {
            logger.info("获取订阅计划列表");
            return subscriptionServiceClient.getSubscriptionPlans();
        } catch (Exception e) {
            logger.error("获取订阅计划列表失败", e);
            return ApiResponse.fail(500, "获取订阅计划列表失败");
        }
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
     * 获取用户订阅列表
     * @param userId 用户ID
     * @return 用户订阅列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户订阅列表", description = "获取指定用户的所有订阅信息")
    public ApiResponse<List<SubscriptionServiceClient.UserSubscriptionDTO>> getUserSubscriptions(
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
            logger.info("创建用户订阅，userId: {}, planId: {}", 
                    userSubscriptionCreateDTO.getUserId(), userSubscriptionCreateDTO.getPlanId());
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