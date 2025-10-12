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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 仪表盘控制器
 * 提供管理后台的仪表盘数据统计功能
 */
@RestController
@RequestMapping("/api/admin")
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

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * 将数据库日期对象转换为字符串
     */
    private String formatDate(Object dateObj) {
        if (dateObj instanceof java.sql.Date) {
            return ((java.sql.Date) dateObj).toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else if (dateObj instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) dateObj).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } else {
            return dateObj.toString();
        }
    }

    /**
     * 获取管理员统计数据
     * @return 统计数据
     */
    @GetMapping("/stats")
    @Operation(summary = "获取管理员统计数据", description = "获取管理后台的统计数据")
    public ApiResponse<Map<String, Object>> getAdminStats() {
        try {
            logger.info("获取仪表盘概览数据");
            Map<String, Object> overviewData = new HashMap<>();

            // 获取用户统计数据 - 通过微服务调用
            try {
                logger.info("=== 开始获取用户统计数据 ===");
                logger.info("尝试调用用户服务获取统计数据");
                
                var userStatsResponse = userServiceClient.getUserStats();
                logger.info("用户服务响应: {}", userStatsResponse);
                
                if (userStatsResponse != null) {
                    logger.info("用户服务响应不为null");
                    logger.info("响应success状态: {}", userStatsResponse.isSuccess());
                    logger.info("响应data: {}", userStatsResponse.getData());
                    
                    if (userStatsResponse.isSuccess() && userStatsResponse.getData() != null) {
                        Object totalUsers = userStatsResponse.getData().get("totalUsers");
                        logger.info("从用户服务获取到用户数: {}", totalUsers);
                        overviewData.put("totalUsers", totalUsers);
                        logger.info("=== 用户数据获取成功 ===");
                    } else {
                        logger.warn("用户服务响应无效，success={}, data={}", 
                                   userStatsResponse.isSuccess(), userStatsResponse.getData());
                        overviewData.put("totalUsers", 0);
                    }
                } else {
                    logger.warn("用户服务响应为null");
                    overviewData.put("totalUsers", 0);
                }
            } catch (Exception e) {
                logger.error("=== 获取用户统计数据失败 ===");
                logger.error("异常类型: {}", e.getClass().getSimpleName());
                logger.error("异常消息: {}", e.getMessage());
                logger.error("异常堆栈: ", e);
                
                // 备用方案：直接查询数据库（仅用于紧急情况）
                try {
                    logger.info("=== 使用备用方案：直接查询数据库 ===");
                    Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user", Integer.class);
                    logger.info("数据库查询结果 - 用户数: {}", userCount);
                    overviewData.put("totalUsers", userCount != null ? userCount : 0);
                } catch (Exception dbException) {
                    logger.error("=== 备用方案也失败 ===");
                    logger.error("数据库异常: {}", dbException.getMessage());
                    overviewData.put("totalUsers", 0);
                }
            }

            // 获取文章统计数据 - 直接查询数据库
            try {
                Integer articleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM article", Integer.class);
                overviewData.put("totalArticles", articleCount != null ? articleCount : 0);
            } catch (Exception e) {
                logger.warn("获取文章统计数据失败", e);
                overviewData.put("totalArticles", 0);
            }

            // 获取订阅统计数据 - 直接查询数据库
            try {
                Integer subscriptionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM subscription", Integer.class);
                overviewData.put("totalSubscriptions", subscriptionCount != null ? subscriptionCount : 0);
            } catch (Exception e) {
                logger.warn("获取订阅统计数据失败", e);
                overviewData.put("totalSubscriptions", 0);
            }

            // 获取AI分析统计数据 - 直接查询数据库
            try {
                Integer aiAnalysisCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ai_analysis", Integer.class);
                overviewData.put("totalAIAnalyses", aiAnalysisCount != null ? aiAnalysisCount : 0);
            } catch (Exception e) {
                logger.warn("获取AI分析统计数据失败", e);
                overviewData.put("totalAIAnalyses", 0);
            }

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
     * 获取文章分类分布
     * @return 文章分类分布数据
     */
    @GetMapping("/dashboard/article-categories")
    @Operation(summary = "获取文章分类分布", description = "获取文章按分类的分布统计")
    public ApiResponse<Map<String, Object>> getArticleCategories() {
        try {
            logger.info("获取文章分类分布");
            
            String sql = "SELECT " +
                        "COALESCE(manual_category, category, '未分类') as category, " +
                        "COUNT(*) as count " +
                        "FROM article " +
                        "WHERE deleted = 0 " +
                        "GROUP BY COALESCE(manual_category, category, '未分类')";
            
            List<Map<String, Object>> categories = jdbcTemplate.queryForList(sql);
            
            Map<String, Object> result = new HashMap<>();
            result.put("categories", categories);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取文章分类分布失败", e);
            return ApiResponse.fail(500, "获取文章分类分布失败");
        }
    }

    /**
     * 获取用户增长趋势
     * @param days 统计天数
     * @return 用户增长趋势数据
     */
    @GetMapping("/dashboard/user-growth")
    @Operation(summary = "获取用户增长趋势", description = "获取指定天数内的用户增长趋势")
    public ApiResponse<Map<String, Object>> getUserGrowth(
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            logger.info("获取用户增长趋势，days: {}", days);
            
            String sql = "SELECT " +
                        "DATE(created_at) as date, " +
                        "COUNT(*) as count " +
                        "FROM user " +
                        "WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                        "GROUP BY DATE(created_at) " +
                        "ORDER BY date";
            
            List<Map<String, Object>> growth = jdbcTemplate.queryForList(sql, days);
            
            Map<String, Object> result = new HashMap<>();
            result.put("growth", growth);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取用户增长趋势失败", e);
            return ApiResponse.fail(500, "获取用户增长趋势失败");
        }
    }

    /**
     * 获取学习活动统计
     * @param days 统计天数
     * @return 学习活动统计数据
     */
    @GetMapping("/dashboard/learning-activities")
    @Operation(summary = "获取学习活动统计", description = "获取用户学习活动统计数据")
    public ApiResponse<Map<String, Object>> getLearningActivities(
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            logger.info("获取学习活动统计，days: {}", days);
            
            // 生成日期范围
            List<String> dateRange = new ArrayList<>();
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                dateRange.add(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            }
            
            // 统计阅读活动
            String readingSql = "SELECT " +
                               "DATE(finished_at) as date, " +
                               "COUNT(*) as count " +
                               "FROM reading_log " +
                               "WHERE finished_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                               "AND finished_at IS NOT NULL " +
                               "GROUP BY DATE(finished_at) " +
                               "ORDER BY date";
            
            List<Map<String, Object>> readingActivities = jdbcTemplate.queryForList(readingSql, days);
            Map<String, Integer> readingMap = new HashMap<>();
            for (Map<String, Object> item : readingActivities) {
                String dateStr = formatDate(item.get("date"));
                readingMap.put(dateStr, ((Number) item.get("count")).intValue());
            }
            
            // 统计新增单词数
            String wordSql = "SELECT " +
                            "DATE(added_at) as date, " +
                            "COUNT(*) as count " +
                            "FROM word " +
                            "WHERE added_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                            "GROUP BY DATE(added_at) " +
                            "ORDER BY date";
            
            List<Map<String, Object>> wordActivities = jdbcTemplate.queryForList(wordSql, days);
            Map<String, Integer> wordMap = new HashMap<>();
            for (Map<String, Object> item : wordActivities) {
                String dateStr = formatDate(item.get("date"));
                wordMap.put(dateStr, ((Number) item.get("count")).intValue());
            }
            
            // 统计AI分析活动（基于ai_analysis表）
            String aiSql = "SELECT " +
                          "DATE(created_at) as date, " +
                          "COUNT(*) as count " +
                          "FROM ai_analysis " +
                          "WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                          "GROUP BY DATE(created_at) " +
                          "ORDER BY date";
            
            List<Map<String, Object>> aiActivities = jdbcTemplate.queryForList(aiSql, days);
            Map<String, Integer> aiMap = new HashMap<>();
            for (Map<String, Object> item : aiActivities) {
                String dateStr = formatDate(item.get("date"));
                aiMap.put(dateStr, ((Number) item.get("count")).intValue());
            }
            
            // 统计订阅购买活动（基于subscription表）
            String subscriptionSql = "SELECT " +
                                   "DATE(created_at) as date, " +
                                   "COUNT(*) as count " +
                                   "FROM subscription " +
                                   "WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? DAY) " +
                                   "AND status = 'ACTIVE' " +
                                   "GROUP BY DATE(created_at) " +
                                   "ORDER BY date";
            
            List<Map<String, Object>> subscriptionActivities = jdbcTemplate.queryForList(subscriptionSql, days);
            Map<String, Integer> subscriptionMap = new HashMap<>();
            for (Map<String, Object> item : subscriptionActivities) {
                String dateStr = formatDate(item.get("date"));
                subscriptionMap.put(dateStr, ((Number) item.get("count")).intValue());
            }
            
            // 构建前端需要的数据格式
            List<Map<String, Object>> activities = new ArrayList<>();
            for (String date : dateRange) {
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", date);
                
                Map<String, Object> activitiesData = new HashMap<>();
                activitiesData.put("阅读文章", readingMap.getOrDefault(date, 0));
                activitiesData.put("AI分析", aiMap.getOrDefault(date, 0));
                activitiesData.put("词汇学习", wordMap.getOrDefault(date, 0));
                activitiesData.put("订阅购买", subscriptionMap.getOrDefault(date, 0));
                
                dayData.put("activities", activitiesData);
                activities.add(dayData);
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("activities", activities);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取学习活动统计失败", e);
            return ApiResponse.fail(500, "获取学习活动统计失败");
        }
    }

    /**
     * 获取最近活动数据
     * @param limit 获取的活动数量限制
     * @return 最近活动列表
     */
    @GetMapping("/dashboard/recent-activities")
    @Operation(summary = "获取最近活动数据", description = "获取系统中的最近操作活动记录")
    public ApiResponse<Map<String, Object>> getRecentActivities(
            @RequestParam(defaultValue = "20") Integer limit) {
        try {
            logger.info("获取最近活动数据，limit: {}", limit);
            
            List<Map<String, Object>> activities = new ArrayList<>();
            logger.info("开始查询最近活动数据...");
            
            // 获取最近注册的用户
            String userSql = "SELECT " +
                            "id as entityId, " +
                            "CONCAT('新用户 ', username, ' 注册了') as title, " +
                            "'用户注册' as type, " +
                            "created_at as time " +
                            "FROM user " +
                            "ORDER BY created_at DESC " +
                            "LIMIT ?";
            
            List<Map<String, Object>> userActivities = jdbcTemplate.queryForList(userSql, limit / 4);
            logger.info("查询到用户注册活动数量: {}", userActivities.size());
            activities.addAll(userActivities);
            
            // 获取最近的阅读记录
            String readingSql = "SELECT " +
                                "rl.id as entityId, " +
                                "CONCAT(u.username, ' 阅读了文章「', COALESCE(a.title, '未知文章'), '」') as title, " +
                                "'阅读记录' as type, " +
                                "rl.finished_at as time " +
                                "FROM reading_log rl " +
                                "LEFT JOIN user u ON rl.user_id = u.id " +
                                "LEFT JOIN article a ON rl.article_id = a.id " +
                                "WHERE rl.finished_at IS NOT NULL " +
                                "ORDER BY rl.finished_at DESC " +
                                "LIMIT ?";
            
            List<Map<String, Object>> readingActivities = jdbcTemplate.queryForList(readingSql, limit / 4);
            logger.info("查询到阅读记录活动数量: {}", readingActivities.size());
            activities.addAll(readingActivities);
            
            // 获取最近的词汇学习记录
            // 根据数据库结构，word表可能有user_id字段或userIds字符串字段
            String wordSql = "SELECT " +
                            "w.id as entityId, " +
                            "CASE " +
                            "  WHEN w.user_id IS NOT NULL THEN CONCAT(u.username, ' 学习了单词「', w.word, '」') " +
                            "  ELSE CONCAT('用户学习了单词「', w.word, '」') " +
                            "END as title, " +
                            "'词汇学习' as type, " +
                            "COALESCE(w.added_at, w.created_at) as time " +
                            "FROM word w " +
                            "LEFT JOIN user u ON w.user_id = u.id " +
                            "WHERE (w.added_at IS NOT NULL OR w.created_at IS NOT NULL) " +
                            "ORDER BY COALESCE(w.added_at, w.created_at) DESC " +
                            "LIMIT ?";
            
            try {
                List<Map<String, Object>> wordActivities = jdbcTemplate.queryForList(wordSql, limit / 4);
                logger.info("查询到词汇学习活动数量: {}", wordActivities.size());
                activities.addAll(wordActivities);
            } catch (Exception e) {
                logger.warn("查询词汇学习记录失败，可能表结构不匹配: {}", e.getMessage());
            }
            
            // 获取最近的订阅记录
            String subscriptionSql = "SELECT " +
                                   "s.id as entityId, " +
                                   "CONCAT(u.username, ' 订阅了 ', s.plan_type, ' 套餐') as title, " +
                                   "'订阅购买' as type, " +
                                   "s.created_at as time " +
                                   "FROM subscription s " +
                                   "LEFT JOIN user u ON s.user_id = u.id " +
                                   "WHERE s.status = 'ACTIVE' " +
                                   "ORDER BY s.created_at DESC " +
                                   "LIMIT ?";
            
            try {
                List<Map<String, Object>> subscriptionActivities = jdbcTemplate.queryForList(subscriptionSql, limit / 4);
                logger.info("查询到订阅购买活动数量: {}", subscriptionActivities.size());
                activities.addAll(subscriptionActivities);
            } catch (Exception e) {
                logger.warn("查询订阅记录失败: {}", e.getMessage());
            }
            
            // 格式化时间并排序
            for (Map<String, Object> activity : activities) {
                Object timeObj = activity.get("time");
                if (timeObj != null) {
                    activity.put("time", formatDate(timeObj));
                }
            }
            
            // 按时间排序
            activities.sort((a, b) -> {
                String timeA = (String) a.get("time");
                String timeB = (String) b.get("time");
                if (timeA == null || timeB == null) return 0;
                return timeB.compareTo(timeA);
            });
            
            // 限制返回数量
            if (activities.size() > limit) {
                activities = activities.subList(0, limit);
            }
            
            logger.info("最终返回活动数量: {}", activities.size());
            
            Map<String, Object> result = new HashMap<>();
            result.put("activities", activities);
            
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
    @GetMapping("/dashboard/trends")
    @Operation(summary = "获取数据统计趋势", description = "获取指定类型的数据统计趋势")
    public ApiResponse<Map<String, Object>> getDataTrends(
            @RequestParam String type,
            @RequestParam(defaultValue = "7") Integer days) {
        try {
            logger.info("获取数据统计趋势，type: {}, days: {}", type, days);
            Map<String, Object> result = new HashMap<>();
            
            if ("user".equals(type)) {
                // 调用用户增长趋势接口
                ApiResponse<Map<String, Object>> userGrowthResponse = getUserGrowth(days);
                if (userGrowthResponse.isSuccess() && userGrowthResponse.getData() != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> growth = (List<Map<String, Object>>) userGrowthResponse.getData().get("growth");
                    // 转换为前端需要的格式
                    List<Map<String, Object>> trends = new ArrayList<>();
                    for (Map<String, Object> item : growth) {
                        Map<String, Object> trendItem = new HashMap<>();
                        trendItem.put("date", item.get("date"));
                        trendItem.put("users", item.get("count"));
                        trends.add(trendItem);
                    }
                    result.put("trends", trends);
                } else {
                    result.put("trends", java.util.Collections.emptyList());
                }
            } else if ("activity".equals(type)) {
                // 调用学习活动统计接口
                ApiResponse<Map<String, Object>> activityResponse = getLearningActivities(days);
                if (activityResponse.isSuccess() && activityResponse.getData() != null) {
                    result.put("trends", activityResponse.getData().get("activities"));
                } else {
                    result.put("trends", java.util.Collections.emptyList());
                }
            } else {
                result.put("trends", java.util.Collections.emptyList());
            }
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取数据统计趋势失败", e);
            return ApiResponse.fail(500, "获取数据统计趋势失败");
        }
    }
}