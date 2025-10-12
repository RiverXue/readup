package com.xreadup.ai.userservice.controller;

import com.xreadup.ai.userservice.entity.Subscription;
import com.xreadup.ai.userservice.mapper.SubscriptionMapper;
import com.xreadup.ai.userservice.service.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 订阅管理控制器 - 商业化功能
 */
@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "订阅管理", description = "用户订阅和付费相关接口")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionMapper subscriptionMapper;

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

    /**
     * 获取订阅计划列表（管理员API）
     */
    @GetMapping("/plans")
    @Operation(summary = "获取订阅计划列表", description = "获取所有订阅计划（管理员接口）")
    public ResponseEntity<?> getSubscriptionPlans() {
        try {
            Map<String, Map<String, Object>> planPrices = subscriptionService.getPlanPrices();
            
            // 转换为List格式，符合admin-service期望的数据结构
            List<Map<String, Object>> planList = new ArrayList<>();
            long id = 1L;
            
            for (Map.Entry<String, Map<String, Object>> entry : planPrices.entrySet()) {
                Map<String, Object> planData = entry.getValue();
                Map<String, Object> plan = new HashMap<>();
                
                plan.put("id", id++);
                plan.put("planType", entry.getKey());
                plan.put("price", planData.get("price"));
                plan.put("currency", planData.get("currency"));
                plan.put("maxArticlesPerMonth", planData.get("maxArticlesPerMonth"));
                plan.put("maxWordsPerArticle", planData.get("maxWordsPerArticle"));
                plan.put("aiFeaturesEnabled", planData.get("aiFeaturesEnabled"));
                plan.put("prioritySupport", planData.get("prioritySupport"));
                plan.put("createTime", "2025-09-15 00:00:00");
                plan.put("updateTime", "2025-09-15 00:00:00");
                plan.put("deleted", 0);
                
                planList.add(plan);
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "获取订阅计划列表成功",
                "data", planList
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", 500,
                "message", e.getMessage() != null ? e.getMessage() : "获取订阅计划列表失败"
            ));
        }
    }

    /**
     * 创建订阅计划（管理员API）
     */
    @PostMapping("/plans")
    @Operation(summary = "创建订阅计划", description = "创建新的订阅计划（管理员接口）")
    public ResponseEntity<?> createSubscriptionPlan(@RequestBody Map<String, Object> planData) {
        try {
            // 这里可以添加创建订阅计划的逻辑
            // 目前返回成功响应，实际实现需要根据业务需求
            return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "创建订阅计划成功",
                "data", planData
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", 500,
                "message", e.getMessage() != null ? e.getMessage() : "创建订阅计划失败"
            ));
        }
    }

    /**
     * 更新订阅计划（管理员API）
     */
    @PutMapping("/plans/{planId}")
    @Operation(summary = "更新订阅计划", description = "更新现有订阅计划（管理员接口）")
    public ResponseEntity<?> updateSubscriptionPlan(
            @PathVariable Long planId,
            @RequestBody Map<String, Object> planData) {
        try {
            // 这里可以添加更新订阅计划的逻辑
            return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "更新订阅计划成功",
                "data", Map.of("planId", planId, "updatedData", planData)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", 500,
                "message", e.getMessage() != null ? e.getMessage() : "更新订阅计划失败"
            ));
        }
    }

    /**
     * 删除订阅计划（管理员API）
     */
    @DeleteMapping("/plans/{planId}")
    @Operation(summary = "删除订阅计划", description = "删除指定的订阅计划（管理员接口）")
    public ResponseEntity<?> deleteSubscriptionPlan(@PathVariable Long planId) {
        try {
            // 这里可以添加删除订阅计划的逻辑
            return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "删除订阅计划成功",
                "data", Map.of("planId", planId)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", 500,
                "message", e.getMessage() != null ? e.getMessage() : "删除订阅计划失败"
            ));
        }
    }

    /**
     * 获取订阅列表（管理员API）
     */
    @GetMapping("/list")
    @Operation(summary = "获取订阅列表", description = "获取所有用户订阅的分页列表（管理员接口）")
    public ResponseEntity<?> getSubscriptionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            // 这里可以添加获取订阅列表的逻辑
            // 目前返回空列表，实际实现需要查询数据库
            return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "获取订阅列表成功",
                "data", Map.of(
                    "list", List.of(),
                    "total", 0,
                    "page", page,
                    "pageSize", pageSize
                )
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", 500,
                "message", e.getMessage() != null ? e.getMessage() : "获取订阅列表失败"
            ));
        }
    }

    /**
     * 获取用户订阅列表（管理员API）
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户订阅列表", description = "获取指定用户的所有订阅信息（管理员接口）")
    public ResponseEntity<?> getUserSubscriptions(@PathVariable Long userId) {
        try {
            // 这里可以添加获取用户订阅列表的逻辑
            return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "获取用户订阅列表成功",
                "data", List.of()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", 500,
                "message", e.getMessage() != null ? e.getMessage() : "获取用户订阅列表失败"
            ));
        }
    }

    /**
     * 创建用户订阅（管理员API）
     */
    @PostMapping("/user")
    @Operation(summary = "创建用户订阅", description = "为用户创建新的订阅（管理员接口）")
    public ResponseEntity<?> createUserSubscription(@RequestBody Map<String, Object> subscriptionData) {
        try {
            // 这里可以添加创建用户订阅的逻辑
            return ResponseEntity.ok(Map.of(
                "success", true,
                "code", 200,
                "message", "创建用户订阅成功",
                "data", subscriptionData
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", 500,
                "message", e.getMessage() != null ? e.getMessage() : "创建用户订阅失败"
            ));
        }
    }

    /**
     * 更新用户订阅状态（管理员API）
     */
    @PutMapping("/user/{subscriptionId}/status")
    @Operation(summary = "更新用户订阅状态", description = "更新用户订阅的状态（管理员接口）")
    public ResponseEntity<?> updateUserSubscriptionStatus(
            @PathVariable Long subscriptionId,
            @RequestParam String status) {
        try {
            // 验证状态值
            if (!Arrays.asList("ACTIVE", "CANCELLED", "EXPIRED").contains(status)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", 400,
                    "message", "无效的状态值，支持的状态：ACTIVE, CANCELLED, EXPIRED"
                ));
            }
            
            // 查找订阅记录
            Subscription subscription = subscriptionMapper.selectById(subscriptionId);
            if (subscription == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", 404,
                    "message", "订阅记录不存在"
                ));
            }
            
            // 更新状态
            subscription.setStatus(status);
            subscription.setUpdatedAt(LocalDateTime.now());
            
            // 如果状态为CANCELLED，关闭自动续费
            if ("CANCELLED".equals(status)) {
                subscription.setAutoRenew(false);
            }
            
            // 保存更新
            int updateResult = subscriptionMapper.updateById(subscription);
            
            if (updateResult > 0) {
                log.info("更新用户订阅状态成功，subscriptionId: {}, 新状态: {}", subscriptionId, status);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "code", 200,
                    "message", "更新用户订阅状态成功",
                    "data", Map.of("subscriptionId", subscriptionId, "status", status)
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "code", 500,
                    "message", "更新失败，请重试"
                ));
            }
        } catch (Exception e) {
            log.error("更新用户订阅状态失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "code", 500,
                "message", e.getMessage() != null ? e.getMessage() : "更新用户订阅状态失败"
            ));
        }
    }
}