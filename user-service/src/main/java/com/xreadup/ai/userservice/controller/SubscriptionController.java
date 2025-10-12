package com.xreadup.ai.userservice.controller;

import com.xreadup.ai.userservice.entity.Subscription;
import com.xreadup.ai.userservice.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订阅管理控制器 - 商业化功能
 */
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Tag(name = "订阅管理", description = "用户订阅和付费相关接口")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * 创建订阅
     */
    @PostMapping("/create")
    @Operation(summary = "创建订阅", description = "创建新的订阅套餐")
    public ResponseEntity<?> createSubscription(
            @RequestParam Long userId,
            @RequestParam String planType,
            @RequestParam String paymentMethod) {
        try {
            Subscription subscription = subscriptionService.createSubscription(userId, planType, paymentMethod);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "订阅创建成功",
                "data", subscription
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "未知错误"
            ));
        }
    }

    /**
     * 获取当前订阅
     */
    @GetMapping("/current/{userId}")
    @Operation(summary = "获取当前订阅", description = "获取用户当前激活的订阅信息")
    public ResponseEntity<?> getCurrentSubscription(@PathVariable Long userId) {
        try {
            // 添加参数验证
            if (userId == null) {
                return ResponseEntity.badRequest().body(java.util.Map.of(
                    "success", false,
                    "message", "用户ID不能为空"
                ));
            }
            
            Subscription subscription = subscriptionService.getCurrentSubscription(userId);
            // 处理subscription为null的情况，避免NullPointerException
            if (subscription != null) {
                return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "data", subscription
                ));
            } else {
                // 用户没有订阅记录时返回空数据而不是错误
                return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "data", new java.util.HashMap<>()  // 使用HashMap而不是null
                ));
            }
        } catch (Exception e) {
            // 记录详细错误信息
            e.printStackTrace();
            return ResponseEntity.badRequest().body(java.util.Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "未知错误"
            ));
        }
    }

    /**
     * 获取订阅历史
     */
    @GetMapping("/history/{userId}")
    @Operation(summary = "获取订阅历史", description = "获取用户的订阅历史记录")
    public ResponseEntity<?> getSubscriptionHistory(@PathVariable Long userId) {
        try {
            List<Subscription> subscriptions = subscriptionService.getSubscriptionHistory(userId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", subscriptions
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "未知错误"
            ));
        }
    }

    /**
     * 取消订阅
     */
    @PostMapping("/cancel/{subscriptionId}")
    @Operation(summary = "取消订阅", description = "取消用户的订阅")
    public ResponseEntity<?> cancelSubscription(@PathVariable Long subscriptionId) {
        try {
            subscriptionService.cancelSubscription(subscriptionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "订阅已取消"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "未知错误"
            ));
        }
    }

    /**
     * 检查使用限制
     */
    @GetMapping("/check-limit/{userId}")
    @Operation(summary = "检查使用限制", description = "检查用户是否超出使用限制")
    public ResponseEntity<?> checkUsageLimit(
            @PathVariable Long userId,
            @RequestParam int articlesCount,
            @RequestParam int wordsCount) {
        try {
            boolean withinLimit = subscriptionService.checkUsageLimit(userId, articlesCount, wordsCount);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "withinLimit", withinLimit
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "未知错误"
            ));
        }
    }

    /**
     * 获取剩余额度
     */
    @GetMapping("/quota/{userId}")
    @Operation(summary = "获取剩余额度", description = "获取用户剩余的使用额度")
    public ResponseEntity<?> getRemainingQuota(@PathVariable Long userId) {
        try {
            Map<String, Object> quota = subscriptionService.getRemainingQuota(userId);
            // 确保quota不为null
            if (quota != null) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", quota
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", new HashMap<>()
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "未知错误"
            ));
        }
    }
    
    /**
     * 获取所有套餐价格配置
     */
    @GetMapping("/plan-prices")
    @Operation(summary = "获取套餐价格配置", description = "获取所有订阅套餐的价格和功能配置")
    public ResponseEntity<?> getPlanPrices() {
        try {
            Map<String, Map<String, Object>> planPrices = subscriptionService.getPlanPrices();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", planPrices
            ));
        } catch (Exception e) {
            // 记录详细错误信息
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage() != null ? e.getMessage() : "获取套餐价格配置失败"
            ));
        }
    }
}