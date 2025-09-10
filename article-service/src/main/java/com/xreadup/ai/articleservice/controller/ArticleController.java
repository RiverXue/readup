package com.xreadup.ai.articleservice.controller;

import com.xreadup.ai.articleservice.common.ApiResponse;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;
import com.xreadup.ai.articleservice.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@Tag(name = "文章管理", description = "文章服务接口")
@Validated
public class ArticleController {
    
    private final ArticleService articleService;
    
    @GetMapping("/list")
    @Operation(summary = "获取文章列表", description = "分页查询文章列表，支持分类、难度、关键词筛选")
    public ApiResponse<Object> getArticleList(@Valid ArticleQueryDTO query) {
        return ApiResponse.success(articleService.getArticlePage(query));
    }
    
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取文章详情", description = "根据ID获取文章详细信息")
    public ApiResponse<Object> getArticleDetail(@PathVariable Long id) {
        ArticleVO article = articleService.getArticleDetail(id);
        if (article == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        
        // 增加阅读量
        articleService.incrementReadCount(id);
        
        return ApiResponse.success(article);
    }
    
    @PostMapping("/update-manual")
    @Operation(summary = "更新手动难度", description = "用户手动标注文章难度等级")
    public ApiResponse<Object> updateManualDifficulty(@Valid @RequestBody ManualDifficultyDTO dto) {
        boolean updated = articleService.updateManualDifficulty(dto);
        if (updated) {
            return ApiResponse.success("难度更新成功");
        } else {
            return ApiResponse.error(400, "更新失败");
        }
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "刷新文章", description = "从gnews.io获取最新文章")
    public ApiResponse<Object> refreshArticles(
            @RequestParam(required = false, defaultValue = "general") String category,
            @RequestParam(required = false, defaultValue = "100") Integer count) {
        int updatedCount = articleService.refreshArticles(category, count);
        return ApiResponse.success("成功更新 " + updatedCount + " 篇文章");
    }
}