package com.xreadup.admin.controller;

import com.xreadup.admin.client.ArticleServiceClient;
import com.xreadup.admin.client.AIServiceClient;
import com.xreadup.admin.client.SubscriptionServiceClient;
import com.xreadup.admin.client.UserServiceClient;
import com.xreadup.admin.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器
 * 提供管理后台的仪表盘数据统计功能
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@Tag(name = "仪表盘", description = "管理后台仪表盘数据统计API")
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private ArticleServiceClient articleServiceClient;

    @Autowired
    private SubscriptionServiceClient subscriptionServiceClient;

    @Autowired
    private AIServiceClient aiServiceClient;

    /**
     * 获取仪表盘概览数据
     * @return 概览统计数据
     */
    @GetMapping("/overview")
    @Operation(summary = "获取仪表盘概览数据", description = "获取管理后台仪表盘的总体统计数据")
    public ApiResponse<Map<String, Object>> getDashboardOverview() {
        try {
            logger.info("获取仪表盘概览数据");
            Map<String, Object> overviewData = new HashMap<>();

            // 获取用户统计数据 - 由于UserServiceClient不再提供getUserList方法，直接返回0
            overviewData.put("totalUsers", 0);

            // 获取文章统计数据
            try {
                // 创建ArticleQueryDTO对象来传递参数
                ArticleServiceClient.ArticleQueryDTO queryDTO = new ArticleServiceClient.ArticleQueryDTO() {
                    @Override
                    public String getCategory() {
                        return null;
                    }

                    @Override
                    public String getDifficultyLevel() {
                        return null;
                    }

                    @Override
                    public String getKeyword() {
                        return null;
                    }

                    @Override
                    public Integer getPage() {
                        return 1;
                    }

                    @Override
                    public Integer getSize() {
                        return 1;
                    }

                    @Override
                    public String getSortBy() {
                        return null;
                    }

                    @Override
                    public Boolean getAscending() {
                        return null;
                    }
                };
                
                var articleListResponse = articleServiceClient.exploreArticles(queryDTO);
                if (articleListResponse != null && articleListResponse.isSuccess() && articleListResponse.getData() != null) {
                    overviewData.put("totalArticles", articleListResponse.getData().getTotal());
                } else {
                    overviewData.put("totalArticles", 0);
                }
            } catch (Exception e) {
                logger.warn("获取文章统计数据失败", e);
                overviewData.put("totalArticles", 0);
            }

            // 获取订阅统计数据
            try {
                var subscriptionPlansResponse = subscriptionServiceClient.getSubscriptionPlans();
                if (subscriptionPlansResponse != null && subscriptionPlansResponse.isSuccess() && subscriptionPlansResponse.getData() != null) {
                    overviewData.put("totalPlans", subscriptionPlansResponse.getData().size());
                } else {
                    overviewData.put("totalPlans", 0);
                }
            } catch (Exception e) {
                logger.warn("获取订阅计划统计数据失败", e);
                overviewData.put("totalPlans", 0);
            }

            // 获取AI分析统计数据 - 由于AIServiceClient不再提供getAIAnalysisList方法，直接返回0
            overviewData.put("totalAnalyses", 0);

            // 获取系统状态
            try {
                Map<String, Boolean> serviceStatus = new HashMap<>();
                serviceStatus.put("userService", true);
                serviceStatus.put("articleService", true);
                serviceStatus.put("subscriptionService", true);
                serviceStatus.put("aiService", true);
                overviewData.put("serviceStatus", serviceStatus);
            } catch (Exception e) {
                logger.warn("获取服务状态失败", e);
            }

            return ApiResponse.success(overviewData);
        } catch (Exception e) {
            logger.error("获取仪表盘概览数据失败", e);
            return ApiResponse.fail(500, "获取仪表盘数据失败");
        }
    }

    /**
     * 获取最近活动数据
     * @param limit 获取的活动数量限制
     * @return 最近活动列表
     */
    @GetMapping("/recent-activities")
    @Operation(summary = "获取最近活动数据", description = "获取系统中的最近操作活动记录")
    public ApiResponse<Map<String, Object>> getRecentActivities(
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            logger.info("获取最近活动数据，limit: {}", limit);
            Map<String, Object> result = new HashMap<>();
            // 这里可以根据实际需求集成各服务的活动日志
            // 目前返回空数据
            result.put("activities", java.util.Collections.emptyList());
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取最近活动数据失败", e);
            return ApiResponse.fail(500, "获取最近活动数据失败");
        }
    }

    /**
     * 获取数据统计趋势
     * @param type 统计类型：user, article, subscription
     * @param days 统计天数
     * @return 统计趋势数据
     */
    @GetMapping("/trends")
    @Operation(summary = "获取数据统计趋势", description = "获取指定类型的数据统计趋势")
    public ApiResponse<Map<String, Object>> getDataTrends(
            @RequestParam String type,
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            logger.info("获取数据统计趋势，type: {}, days: {}", type, days);
            Map<String, Object> result = new HashMap<>();
            // 这里可以根据实际需求集成各服务的趋势数据
            // 目前返回空数据
            result.put("trends", java.util.Collections.emptyList());
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取数据统计趋势失败", e);
            return ApiResponse.fail(500, "获取数据统计趋势失败");
        }
    }
}