package com.xreadup.admin.controller;

import com.xreadup.admin.client.ArticleServiceClient;
import com.xreadup.admin.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 文章管理控制器
 * 提供后台管理系统对文章的管理功能
 */
@RestController
@RequestMapping("/api/admin/articles")
@Tag(name = "文章管理", description = "文章相关的后台管理API")
public class ArticleController {

    private static final Logger logger = LoggerFactory.getLogger(ArticleController.class);

    @Autowired
    private ArticleServiceClient articleServiceClient;

    /**
     * 获取文章列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param title 文章标题搜索关键字
     * @param category 文章分类
     * @param status 文章状态
     * @param authorId 作者ID
     * @return 文章列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取文章列表", description = "获取文章列表，支持分页和多条件筛选")
    public ApiResponse<List<ArticleServiceClient.ArticleDTO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty) {
        try {
            logger.info("获取文章列表，page: {}, pageSize: {}, title: {}, category: {}, difficulty: {}",
                    page, pageSize, title, category, difficulty);
            
            // 直接调用article-service的explore接口
            logger.info("开始调用article-service的explore接口...");
            var response = articleServiceClient.exploreArticles(page, pageSize, title, category, difficulty);
            logger.info("article-service响应: {}", response);
            
            if (response != null && response.isSuccess() && response.getData() != null) {
                logger.info("成功获取文章数据，文章数量: {}", response.getData().getList().size());
                return ApiResponse.success(response.getData().getList());
            }
            
            logger.warn("article-service返回空数据或失败");
            // 返回空文章列表
            return ApiResponse.success(java.util.Collections.emptyList());
        } catch (Exception e) {
            logger.error("处理文章列表请求异常", e);
            // 最后的保障，返回空文章列表
            return ApiResponse.success(java.util.Collections.emptyList());
        }
    }

    /**
     * 获取文章详情
     * @param articleId 文章ID
     * @return 文章详情
     */
    @GetMapping("/{articleId}")
    @Operation(summary = "获取文章详情", description = "根据文章ID获取文章的详细信息")
    public ApiResponse<ArticleServiceClient.ArticleDTO> getArticleDetail(
            @PathVariable @NotNull(message = "文章ID不能为空") Long articleId) {
        try {
            logger.info("获取文章详情，articleId: {}", articleId);
            return articleServiceClient.getArticleDetail(articleId);
        } catch (Exception e) {
            logger.error("获取文章详情失败", e);
            return ApiResponse.fail(500, "获取文章详情失败");
        }
    }

    /**
     * 审核文章
     * @param articleId 文章ID
     * @param status 审核状态
     * @param reason 审核原因（可选）
     * @return 审核结果
     */
    @PutMapping("/{articleId}/review")
    @Operation(summary = "审核文章", description = "审核文章并设置审核状态")
    public ApiResponse<Boolean> reviewArticle(
            @PathVariable @NotNull(message = "文章ID不能为空") Long articleId,
            @RequestParam @NotNull(message = "审核状态不能为空") String status,
            @RequestParam(required = false) String reason) {
        try {
            logger.info("审核文章，articleId: {}, status: {}, reason: {}", articleId, status, reason);
            return articleServiceClient.auditArticle(articleId, status, reason);
        } catch (Exception e) {
            logger.error("审核文章失败", e);
            return ApiResponse.fail(500, "审核文章失败");
        }
    }

    /**
     * 更新文章分类
     * @param articleId 文章ID
     * @param category 新的分类
     * @return 更新结果
     */
    @PutMapping("/{articleId}/category")
    @Operation(summary = "更新文章分类", description = "更新文章的分类信息")
    public ApiResponse<Boolean> updateArticleCategory(
            @PathVariable @NotNull(message = "文章ID不能为空") Long articleId,
            @RequestParam @NotNull(message = "分类不能为空") String category) {
        try {
            logger.info("更新文章分类，articleId: {}, category: {}", articleId, category);
            return articleServiceClient.updateArticleCategory(articleId, category);
        } catch (Exception e) {
            logger.error("更新文章分类失败", e);
            return ApiResponse.fail(500, "更新文章分类失败");
        }
    }

    /**
     * 更新文章难度等级
     * @param articleId 文章ID
     * @param difficulty 难度等级
     * @return 更新结果
     */
    @PutMapping("/{articleId}/difficulty")
    @Operation(summary = "更新文章难度等级", description = "更新文章的难度等级")
    public ApiResponse<Boolean> updateArticleDifficulty(
            @PathVariable @NotNull(message = "文章ID不能为空") Long articleId,
            @RequestParam @NotNull(message = "难度等级不能为空") String difficulty) {
        try {
            logger.info("更新文章难度等级，articleId: {}, difficulty: {}", articleId, difficulty);
            return articleServiceClient.updateArticleDifficulty(articleId, difficulty);
        } catch (Exception e) {
            logger.error("更新文章难度等级失败", e);
            return ApiResponse.fail(500, "更新文章难度等级失败");
        }
    }

    /**
     * 标记/取消标记精选文章
     * @param articleId 文章ID
     * @param featured 是否精选
     * @return 操作结果
     */
    @PutMapping("/{articleId}/featured")
    @Operation(summary = "标记精选文章", description = "标记或取消标记文章为精选")
    public ApiResponse<Boolean> markArticleFeatured(
            @PathVariable @NotNull(message = "文章ID不能为空") Long articleId,
            @RequestParam Boolean featured) {
        try {
            logger.info("标记/取消标记精选文章，articleId: {}, featured: {}", articleId, featured);
            return articleServiceClient.markArticleAsFeatured(articleId, featured);
        } catch (Exception e) {
            logger.error("标记/取消标记精选文章失败", e);
            return ApiResponse.fail(500, "标记/取消标记精选文章失败");
        }
    }

    /**
     * 删除文章
     * @param articleId 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{articleId}")
    @Operation(summary = "删除文章", description = "删除指定的文章")
    public ApiResponse<Boolean> deleteArticle(
            @PathVariable @NotNull(message = "文章ID不能为空") Long articleId) {
        try {
            logger.info("删除文章，articleId: {}", articleId);
            // 调用文章服务删除文章
            return articleServiceClient.deleteArticle(articleId);
        } catch (Exception e) {
            logger.error("删除文章失败", e);
            return ApiResponse.fail(500, "删除文章失败");
        }
    }

    /**
     * 发布文章
     * @param articleId 文章ID
     * @return 发布结果
     */
    @PutMapping("/{articleId}/publish")
    @Operation(summary = "发布文章", description = "发布指定的文章")
    public ApiResponse<Boolean> publishArticle(
            @PathVariable @NotNull(message = "文章ID不能为空") Long articleId) {
        try {
            logger.info("发布文章，articleId: {}", articleId);
            // 调用文章服务发布文章
            return articleServiceClient.publishArticle(articleId);
        } catch (Exception e) {
            logger.error("发布文章失败", e);
            return ApiResponse.fail(500, "发布文章失败");
        }
    }

    /**
     * 获取文章分类列表
     * @return 分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取文章分类列表", description = "获取所有文章分类")
    public ApiResponse<List<String>> getArticleCategories() {
        try {
            logger.info("获取文章分类列表");
            return articleServiceClient.getArticleCategories();
        } catch (Exception e) {
            logger.error("获取文章分类列表失败", e);
            return ApiResponse.fail(500, "获取文章分类列表失败");
        }
    }

    // ========== 内容过滤管理相关接口 ==========

    /**
     * 获取文章的内容过滤记录
     * @param articleId 文章ID
     * @return 过滤记录列表
     */
    @GetMapping("/{articleId}/filter-logs")
    @Operation(summary = "获取文章过滤记录", description = "获取指定文章的内容过滤记录")
    public ApiResponse<List<Object>> getArticleFilterLogs(
            @PathVariable @NotNull(message = "文章ID不能为空") Long articleId) {
        try {
            logger.info("获取文章过滤记录，articleId: {}", articleId);
            return articleServiceClient.getArticleFilterLogs(articleId);
        } catch (Exception e) {
            logger.error("获取文章过滤记录失败", e);
            return ApiResponse.fail(500, "获取文章过滤记录失败");
        }
    }

    /**
     * 获取内容过滤记录分页列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param filterType 过滤类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/filter-logs")
    @Operation(summary = "获取过滤记录列表", description = "获取内容过滤记录的分页列表")
    public ApiResponse<Object> getFilterLogsPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            logger.info("获取过滤记录列表，page: {}, pageSize: {}, filterType: {}, status: {}", 
                    page, pageSize, filterType, status);
            return articleServiceClient.getFilterLogsPage(page, pageSize, filterType, status, startDate, endDate);
        } catch (Exception e) {
            logger.error("获取过滤记录列表失败", e);
            return ApiResponse.fail(500, "获取过滤记录列表失败");
        }
    }

    /**
     * 更新过滤记录状态
     * @param logId 记录ID
     * @param status 新状态
     * @param adminId 管理员ID
     * @return 更新结果
     */
    @PutMapping("/filter-logs/{logId}/status")
    @Operation(summary = "更新过滤记录状态", description = "更新内容过滤记录的状态")
    public ApiResponse<Boolean> updateFilterLogStatus(
            @PathVariable @NotNull(message = "记录ID不能为空") Long logId,
            @RequestParam @NotNull(message = "状态不能为空") String status,
            @RequestParam(required = false) Long adminId) {
        try {
            logger.info("更新过滤记录状态，logId: {}, status: {}, adminId: {}", logId, status, adminId);
            return articleServiceClient.updateFilterLogStatus(logId, status, adminId);
        } catch (Exception e) {
            logger.error("更新过滤记录状态失败", e);
            return ApiResponse.fail(500, "更新过滤记录状态失败");
        }
    }

    /**
     * 删除过滤记录
     * @param logId 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/filter-logs/{logId}")
    @Operation(summary = "删除过滤记录", description = "删除指定的内容过滤记录")
    public ApiResponse<Boolean> deleteFilterLog(
            @PathVariable @NotNull(message = "记录ID不能为空") Long logId) {
        try {
            logger.info("删除过滤记录，logId: {}", logId);
            return articleServiceClient.deleteFilterLog(logId);
        } catch (Exception e) {
            logger.error("删除过滤记录失败", e);
            return ApiResponse.fail(500, "删除过滤记录失败");
        }
    }

    /**
     * 获取过滤统计信息
     * @return 统计信息
     */
    @GetMapping("/filter-logs/statistics")
    @Operation(summary = "获取过滤统计信息", description = "获取内容过滤的统计信息")
    public ApiResponse<Object> getFilterStatistics() {
        try {
            logger.info("获取过滤统计信息");
            return articleServiceClient.getFilterStatistics();
        } catch (Exception e) {
            logger.error("获取过滤统计信息失败", e);
            return ApiResponse.fail(500, "获取过滤统计信息失败");
        }
    }

    /**
     * 记录内容过滤日志
     * @param articleId 文章ID
     * @param filterType 过滤类型
     * @param matchedContent 匹配到的内容
     * @param filterReason 过滤原因
     * @param severityLevel 严重程度
     * @param actionTaken 采取的行动
     * @param adminId 管理员ID
     * @return 记录结果
     */
    @PostMapping("/filter-logs")
    @Operation(summary = "记录内容过滤日志", description = "记录内容过滤操作日志")
    public ApiResponse<Boolean> logContentFilter(
            @RequestParam @NotNull(message = "文章ID不能为空") Long articleId,
            @RequestParam @NotNull(message = "过滤类型不能为空") String filterType,
            @RequestParam @NotNull(message = "匹配内容不能为空") String matchedContent,
            @RequestParam(required = false) String filterReason,
            @RequestParam(defaultValue = "medium") String severityLevel,
            @RequestParam(defaultValue = "blocked") String actionTaken,
            @RequestParam(required = false) Long adminId) {
        try {
            logger.info("记录内容过滤日志，articleId: {}, filterType: {}, matchedContent: {}", 
                    articleId, filterType, matchedContent);
            return articleServiceClient.logContentFilter(articleId, filterType, matchedContent, 
                    filterReason, severityLevel, actionTaken, adminId);
        } catch (Exception e) {
            logger.error("记录内容过滤日志失败", e);
            return ApiResponse.fail(500, "记录内容过滤日志失败");
        }
    }
}