package com.xreadup.ai.articleservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xreadup.ai.articleservice.mapper.ArticleMapper;
import com.xreadup.ai.articleservice.model.common.ApiResponse;
import com.xreadup.ai.articleservice.model.entity.Article;
import com.xreadup.ai.articleservice.model.entity.ContentFilterLog;
import com.xreadup.ai.articleservice.service.ArticleService;
import com.xreadup.ai.articleservice.service.ContentFilterLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章管理控制器 - 供admin-service调用
 * 提供文章管理相关的API接口
 */
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@Tag(name = "文章管理", description = "文章管理相关的API接口")
public class AdminArticleController {

    private final ArticleService articleService;
    private final ArticleMapper articleMapper;
    private final ContentFilterLogService contentFilterLogService;

    /**
     * 获取文章详情
     * @param articleId 文章ID
     * @return 文章详情
     */
    @GetMapping("/{articleId}")
    @Operation(summary = "获取文章详情", description = "根据文章ID获取文章详细信息")
    public ApiResponse<Article> getArticleDetail(@PathVariable Long articleId) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                return ApiResponse.error("文章不存在");
            }
            return ApiResponse.success(article);
        } catch (Exception e) {
            return ApiResponse.error("获取文章详情失败: " + e.getMessage());
        }
    }

    /**
     * 审核文章
     * @param articleId 文章ID
     * @param status 审核状态
     * @param reason 审核原因（可选）
     * @return 审核结果
     */
    @PutMapping("/audit/{articleId}")
    @Operation(summary = "审核文章", description = "审核文章并设置审核状态")
    public ApiResponse<Boolean> auditArticle(
            @PathVariable Long articleId,
            @RequestParam String status,
            @RequestParam(required = false) String reason) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                return ApiResponse.error("文章不存在");
            }
            
            // 这里可以根据实际业务需求添加审核状态字段
            // 目前先返回成功，实际项目中需要在Article实体中添加审核相关字段
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error("审核文章失败: " + e.getMessage());
        }
    }

    /**
     * 更新文章分类
     * @param articleId 文章ID
     * @param category 新的分类
     * @return 更新结果
     */
    @PutMapping("/category/{articleId}")
    @Operation(summary = "更新文章分类", description = "更新文章的分类信息")
    public ApiResponse<Boolean> updateArticleCategory(
            @PathVariable Long articleId,
            @RequestParam String category) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                return ApiResponse.error("文章不存在");
            }
            
            article.setCategory(category);
            boolean updated = articleMapper.updateById(article) > 0;
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("更新文章分类失败: " + e.getMessage());
        }
    }

    /**
     * 更新文章难度等级
     * @param articleId 文章ID
     * @param difficulty 难度等级
     * @return 更新结果
     */
    @PutMapping("/difficulty/{articleId}")
    @Operation(summary = "更新文章难度等级", description = "更新文章的难度等级")
    public ApiResponse<Boolean> updateArticleDifficulty(
            @PathVariable Long articleId,
            @RequestParam String difficulty) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                return ApiResponse.error("文章不存在");
            }
            
            article.setDifficultyLevel(difficulty);
            article.setManualDifficulty(difficulty);
            boolean updated = articleMapper.updateById(article) > 0;
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("更新文章难度失败: " + e.getMessage());
        }
    }

    /**
     * 标记/取消标记精选文章
     * @param articleId 文章ID
     * @param isFeatured 是否精选
     * @return 操作结果
     */
    @PutMapping("/featured/{articleId}")
    @Operation(summary = "标记精选文章", description = "标记或取消标记文章为精选")
    public ApiResponse<Boolean> markArticleAsFeatured(
            @PathVariable Long articleId,
            @RequestParam Boolean isFeatured) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                return ApiResponse.error("文章不存在");
            }
            
            article.setIsFeatured(isFeatured);
            boolean updated = articleMapper.updateById(article) > 0;
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("标记精选文章失败: " + e.getMessage());
        }
    }

    /**
     * 删除文章
     * @param articleId 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{articleId}")
    @Operation(summary = "删除文章", description = "删除指定的文章")
    public ApiResponse<Boolean> deleteArticle(@PathVariable Long articleId) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                return ApiResponse.error("文章不存在");
            }
            
            // 使用逻辑删除
            boolean deleted = articleMapper.deleteById(articleId) > 0;
            return ApiResponse.success(deleted);
        } catch (Exception e) {
            return ApiResponse.error("删除文章失败: " + e.getMessage());
        }
    }

    /**
     * 发布文章
     * @param articleId 文章ID
     * @return 发布结果
     */
    @PutMapping("/{articleId}/publish")
    @Operation(summary = "发布文章", description = "发布指定的文章")
    public ApiResponse<Boolean> publishArticle(@PathVariable Long articleId) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) {
                return ApiResponse.error("文章不存在");
            }
            
            // 这里可以根据实际业务需求添加发布状态字段
            // 目前先返回成功，实际项目中需要在Article实体中添加发布相关字段
            return ApiResponse.success(true);
        } catch (Exception e) {
            return ApiResponse.error("发布文章失败: " + e.getMessage());
        }
    }

    // ========== 内容过滤管理相关接口 ==========

    /**
     * 获取文章的内容过滤记录
     * @param articleId 文章ID
     * @return 过滤记录列表
     */
    @GetMapping("/filter-logs/{articleId}")
    @Operation(summary = "获取文章过滤记录", description = "获取指定文章的内容过滤记录")
    public ApiResponse<List<ContentFilterLog>> getArticleFilterLogs(@PathVariable Long articleId) {
        try {
            List<ContentFilterLog> logs = contentFilterLogService.getArticleFilterLogs(articleId);
            return ApiResponse.success(logs);
        } catch (Exception e) {
            return ApiResponse.error("获取文章过滤记录失败: " + e.getMessage());
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
    public ApiResponse<IPage<ContentFilterLog>> getFilterLogsPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            LocalDateTime start = null;
            LocalDateTime end = null;
            
            if (startDate != null && !startDate.isEmpty()) {
                start = LocalDateTime.parse(startDate + "T00:00:00");
            }
            if (endDate != null && !endDate.isEmpty()) {
                end = LocalDateTime.parse(endDate + "T23:59:59");
            }
            
            IPage<ContentFilterLog> result = contentFilterLogService.getFilterLogsPage(
                    page, pageSize, filterType, status, start, end);
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取过滤记录列表失败: " + e.getMessage());
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
            @PathVariable Long logId,
            @RequestParam String status,
            @RequestParam(required = false) Long adminId) {
        try {
            boolean updated = contentFilterLogService.updateFilterLogStatus(logId, status, adminId);
            return ApiResponse.success(updated);
        } catch (Exception e) {
            return ApiResponse.error("更新过滤记录状态失败: " + e.getMessage());
        }
    }

    /**
     * 删除过滤记录
     * @param logId 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/filter-logs/{logId}")
    @Operation(summary = "删除过滤记录", description = "删除指定的内容过滤记录")
    public ApiResponse<Boolean> deleteFilterLog(@PathVariable Long logId) {
        try {
            boolean deleted = contentFilterLogService.deleteFilterLog(logId);
            return ApiResponse.success(deleted);
        } catch (Exception e) {
            return ApiResponse.error("删除过滤记录失败: " + e.getMessage());
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
            return contentFilterLogService.getFilterStatistics();
        } catch (Exception e) {
            return ApiResponse.error("获取过滤统计信息失败: " + e.getMessage());
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
            @RequestParam Long articleId,
            @RequestParam String filterType,
            @RequestParam String matchedContent,
            @RequestParam(required = false) String filterReason,
            @RequestParam(defaultValue = "medium") String severityLevel,
            @RequestParam(defaultValue = "blocked") String actionTaken,
            @RequestParam(required = false) Long adminId) {
        try {
            boolean logged = contentFilterLogService.logContentFilter(
                    articleId, filterType, matchedContent, filterReason, 
                    severityLevel, actionTaken, adminId);
            return ApiResponse.success(logged);
        } catch (Exception e) {
            return ApiResponse.error("记录内容过滤日志失败: " + e.getMessage());
        }
    }
}
