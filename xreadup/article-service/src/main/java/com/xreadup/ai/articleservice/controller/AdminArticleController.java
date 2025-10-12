package com.xreadup.ai.articleservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xreadup.ai.articleservice.mapper.ArticleMapper;
import com.xreadup.ai.articleservice.model.common.ApiResponse;
import com.xreadup.ai.articleservice.model.entity.Article;
import com.xreadup.ai.articleservice.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取文章分类列表
     * @return 分类列表
     */
    @GetMapping("/categories")
    @Operation(summary = "获取文章分类列表", description = "获取所有文章分类")
    public ApiResponse<List<String>> getArticleCategories() {
        try {
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Article::getCategory)
                       .isNotNull(Article::getCategory)
                       .ne(Article::getCategory, "")
                       .groupBy(Article::getCategory);
            
            List<Article> articles = articleMapper.selectList(queryWrapper);
            List<String> categories = articles.stream()
                    .map(Article::getCategory)
                    .distinct()
                    .collect(Collectors.toList());
            
            return ApiResponse.success(categories);
        } catch (Exception e) {
            return ApiResponse.error("获取文章分类列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取文章难度列表
     * @return 难度列表
     */
    @GetMapping("/difficulties")
    @Operation(summary = "获取文章难度列表", description = "获取所有文章难度等级")
    public ApiResponse<List<String>> getArticleDifficulties() {
        try {
            LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.select(Article::getDifficultyLevel)
                       .isNotNull(Article::getDifficultyLevel)
                       .ne(Article::getDifficultyLevel, "")
                       .groupBy(Article::getDifficultyLevel);
            
            List<Article> articles = articleMapper.selectList(queryWrapper);
            List<String> difficulties = articles.stream()
                    .map(Article::getDifficultyLevel)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 如果没有数据，返回默认难度等级
            if (difficulties.isEmpty()) {
                difficulties = List.of("A1", "A2", "B1", "B2", "C1", "C2");
            }
            
            return ApiResponse.success(difficulties);
        } catch (Exception e) {
            return ApiResponse.error("获取文章难度列表失败: " + e.getMessage());
        }
    }
}
