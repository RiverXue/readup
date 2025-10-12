package com.xreadup.admin.controller;

import com.xreadup.admin.client.ArticleServiceClient;
import com.xreadup.admin.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
            
            // 创建查询DTO
            ArticleServiceClient.ArticleQueryDTO queryDTO = new ArticleServiceClient.ArticleQueryDTO() {
                @Override
                public String getCategory() {
                    return category;
                }

                @Override
                public String getDifficultyLevel() {
                    return difficulty;
                }

                @Override
                public String getKeyword() {
                    return title;
                }

                @Override
                public Integer getPage() {
                    return page;
                }

                @Override
                public Integer getSize() {
                    return pageSize;
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

            // 调用文章服务获取文章列表
            var response = articleServiceClient.exploreArticles(queryDTO);
            if (response != null && response.isSuccess() && response.getData() != null) {
                return ApiResponse.success(response.getData().getList());
            }
            
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
}