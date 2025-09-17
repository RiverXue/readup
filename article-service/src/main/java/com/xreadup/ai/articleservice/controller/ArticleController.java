package com.xreadup.ai.articleservice.controller;

import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.service.ArticleService;
import com.xreadup.ai.articleservice.service.ScraperService;
import com.xreadup.ai.articleservice.model.common.ApiResponse;
import com.xreadup.ai.articleservice.model.common.PageResult;
import com.xreadup.ai.articleservice.model.vo.ArticleDetailVO;
import com.xreadup.ai.articleservice.model.vo.ArticleListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 文章控制器
 * 提供文章相关的REST API
 */
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@Tag(name = "文章管理", description = "文章相关的API接口")
public class ArticleController {

    private final ArticleService articleService;
    private final ScraperService scraperService;

    @GetMapping("/explore")
    @Operation(summary = "【文章发现】探索文章列表", 
               description = "根据条件筛选和分页获取文章列表")
    public ApiResponse<PageResult<ArticleListVO>> exploreArticles(ArticleQueryDTO query) {
        return articleService.exploreArticles(query);
    }

    @GetMapping("/read/{id}")
    @Operation(summary = "【开始阅读】获取文章详情", 
               description = "获取文章详细信息并增加阅读次数")
    public ApiResponse<ArticleDetailVO> readArticle(@PathVariable Long id) {
        return articleService.readArticle(id);
    }

    @GetMapping("/{id}/deep-dive")
    @Operation(summary = "【深度精读】AI深度分析", 
               description = "对文章进行AI深度分析，包括难度评估、关键词提取、摘要生成等")
    public ApiResponse<ArticleDetailVO> deepDiveAnalysis(@PathVariable Long id) {
        return articleService.deepDiveAnalysis(id);
    }

    @PostMapping("/feedback/difficulty")
    @Operation(summary = "【难度反馈】手动标注文章难度", 
               description = "允许用户手动标注文章难度等级")
    public ApiResponse<Boolean> updateManualDifficulty(@RequestBody ManualDifficultyDTO dto) {
        return articleService.updateManualDifficulty(dto);
    }

    @PostMapping("/discover/category")
    @Operation(summary = "【主题探索】按主题获取文章", 
               description = "根据指定主题获取最新文章")
    public ApiResponse<Object> discoverByCategory(@RequestParam String category,
                                                  @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(articleService.refreshArticles(category, limit));
    }

    @PostMapping("/discover/trending")
    @Operation(summary = "【热点追踪】获取热点文章", 
               description = "获取当前热点新闻文章")
    public ApiResponse<Object> discoverTrending(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.success(articleService.refreshTopHeadlines(limit));
    }
    
    @GetMapping("/extract-content")
    @Operation(summary = "【内容提取】从URL提取可读内容", 
               description = "使用Readability4J从指定URL提取文章的可读内容")
    public ApiResponse<String> extractArticleContent(@RequestParam String url) {
        Optional<String> content = scraperService.scrapeArticleContent(url);
        if (content.isPresent()) {
            return ApiResponse.success(content.get());
        } else {
            return ApiResponse.error("提取内容失败，请检查URL是否有效");
        }
    }
}