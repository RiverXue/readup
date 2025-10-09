package com.xreadup.admin.client;

import com.xreadup.admin.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 文章服务Feign客户端
 * 用于调用article-service提供的API接口
 */
@FeignClient(name = "article-service")
public interface ArticleServiceClient {

    /**
     * 获取文章列表
     * @param query 查询条件
     * @return 文章列表
     */
    @GetMapping("/api/article/explore")
    ApiResponse<PageResult> exploreArticles(@SpringQueryMap ArticleQueryDTO query);

    /**
     * 获取文章详情
     * @param articleId 文章ID
     * @return 文章详情
     */
    @GetMapping("/api/article/read/{articleId}")
    ApiResponse<ArticleDTO> getArticleDetail(@PathVariable("articleId") Long articleId);

    /**
     * 审核文章
     * @param articleId 文章ID
     * @param status 审核状态
     * @param reason 审核原因
     * @return 操作结果
     */
    @PutMapping("/api/article/audit/{articleId}")
    ApiResponse<Boolean> auditArticle(
            @PathVariable("articleId") Long articleId,
            @RequestParam String status,
            @RequestParam(required = false) String reason);

    /**
     * 更新文章分类
     * @param articleId 文章ID
     * @param category 分类
     * @return 操作结果
     */
    @PutMapping("/api/article/category/{articleId}")
    ApiResponse<Boolean> updateArticleCategory(
            @PathVariable("articleId") Long articleId,
            @RequestParam String category);

    /**
     * 更新文章难度
     * @param articleId 文章ID
     * @param difficulty 难度
     * @return 操作结果
     */
    @PutMapping("/api/article/difficulty/{articleId}")
    ApiResponse<Boolean> updateArticleDifficulty(
            @PathVariable("articleId") Long articleId,
            @RequestParam String difficulty);

    /**
     * 标记文章为精选
     * @param articleId 文章ID
     * @param isFeatured 是否精选
     * @return 操作结果
     */
    @PutMapping("/api/article/featured/{articleId}")
    ApiResponse<Boolean> markArticleAsFeatured(
            @PathVariable("articleId") Long articleId,
            @RequestParam Boolean isFeatured);

    /**
     * 获取文章分类列表
     * @return 分类列表
     */
    @GetMapping("/api/article/categories")
    ApiResponse<List<String>> getArticleCategories();

    /**
     * 获取文章难度列表
     * @return 难度列表
     */
    @GetMapping("/api/article/difficulties")
    ApiResponse<List<String>> getArticleDifficulties();

    /**
     * 文章查询DTO接口定义
     */
    interface ArticleQueryDTO {
        String getCategory();
        String getDifficultyLevel();
        String getKeyword();
        Integer getPage();
        Integer getSize();
        String getSortBy();
        Boolean getAscending();
    }

    /**
     * 分页结果接口定义
     */
    interface PageResult {
        List<ArticleDTO> getList();
        int getTotal();
        int getPage();
        int getSize();
    }

    /**
     * 文章DTO接口定义
     */
    interface ArticleDTO {
        Long getId();
        String getTitle();
        String getContentEn();
        String getContentCn();
        String getCategory();
        String getDifficultyLevel();
        String getSource();
        String getUrl();
        String getPublishedAt();
        Integer getReadCount();
        Integer getFavoriteCount();
        Integer getCommentCount();
        Boolean getIsFeatured();
        Boolean getIsAudited();
        Integer getWordCount();
        Double getReadabilityScore();
    }
}