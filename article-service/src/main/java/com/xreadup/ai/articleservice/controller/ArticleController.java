package com.xreadup.ai.articleservice.controller;

import com.xreadup.ai.articleservice.common.ApiResponse;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.model.vo.ArticleDetailVO;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;
import com.xreadup.ai.articleservice.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
@Tag(name = "文章管理", description = "文章服务接口")
@Validated
public class ArticleController {
    
    private final ArticleService articleService;
    
    @GetMapping("/explore")
    @Operation(summary = "【发现文章】探索阅读世界", description = "发现你的下一篇精彩阅读，智能推荐每日更新")
    public ApiResponse<Object> exploreArticles(@Valid ArticleQueryDTO query) {
        return ApiResponse.success(articleService.getArticlePage(query));
    }
    
    @GetMapping("/read/{id}")
    @Operation(summary = "【开始阅读】沉浸式阅读体验", description = "进入专注阅读模式，享受纯净的阅读时光")
    public ApiResponse<Object> startReading(@PathVariable Long id) {
        ArticleVO article = articleService.getArticleDetail(id);
        if (article == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        
        // 增加阅读量
        articleService.incrementReadCount(id);
        
        return ApiResponse.success(article);
    }

    @GetMapping("/{id}/deep-dive")
    @Operation(summary = "【精读分析】AI深度理解", description = "【智能Token策略】根据文章长度自动选择最优方案：≤1000字标准分析，1001-2000字省70%Token，>2000字省65%Token。适合深度学习和理解文章")
    public ApiResponse<Object> deepDiveAnalysis(@PathVariable Long id, 
                                              @RequestParam(defaultValue = "true") boolean incrementRead) {
        ArticleDetailVO detail = articleService.deepDiveAnalysis(id);
        if (detail == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        
        if (incrementRead) {
            articleService.incrementReadCount(id);
        }
        
        return ApiResponse.success(detail);
    }
    
    @PostMapping("/feedback/difficulty")
    @Operation(summary = "【学习反馈】标记文章难度", description = "告诉AI你的真实感受，让它更懂你")
    public ApiResponse<Object> giveDifficultyFeedback(@Valid @RequestBody ManualDifficultyDTO dto) {
        boolean updated = articleService.updateManualDifficulty(dto);
        if (updated) {
            return ApiResponse.success("难度更新成功");
        } else {
            return ApiResponse.error(400, "更新失败");
        }
    }
    
    @PostMapping("/discover/category")
    @Operation(summary = "【发现新文】按主题探索", description = "发现你感兴趣主题的最新文章")
    public ApiResponse<Object> discoverByCategory(@RequestParam String category) {
        return ApiResponse.success(articleService.refreshArticles(category, 20));
    }
    
    @PostMapping("/discover/trending")
    @Operation(summary = "【热点追踪】今日必看", description = "掌握全球热点，不错过任何重要资讯")
    public ApiResponse<Object> discoverTrending() {
        return ApiResponse.success(articleService.refreshTopHeadlines(20));
    }
    
    @GetMapping("/{id}/translate")
    @Operation(summary = "【全文翻译】英文秒变中文", description = "【标准Token策略】逐句精译英文全文，地道中文翻译，适合精读英文原文")
    public ApiResponse<Object> translate(@PathVariable Long id) {
        String translation = articleService.translate(id);
        if (translation == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        return ApiResponse.success(translation);
    }
    
    @GetMapping("/{id}/quick-read")
    @Operation(summary = "【智能速读】30秒掌握要点", description = "【省80%Token策略】30秒生成100字内中文精华摘要，快速判断文章价值")
    public ApiResponse<Object> quickRead(@PathVariable Long id) {
        String summary = articleService.quickRead(id);
        if (summary == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        return ApiResponse.success(summary);
    }
    
    @GetMapping("/{id}/key-points")
    @Operation(summary = "【核心要点】5秒抓关键词", description = "【省90%Token策略】AI提取5-8个核心关键词，快速把握文章主题和重点")
    public ApiResponse<Object> keyPoints(@PathVariable Long id) {
        List<String> keywords = articleService.keyPoints(id);
        if (keywords == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        return ApiResponse.success(keywords);
    }
    
    @GetMapping("/{id}/micro-study")
    @Operation(summary = "【短文精学】800字内深度学习", description = "【标准Token策略】专为800字内短文设计的全维度精学模式，逐句解析+词汇学习+语法分析")
    public ApiResponse<Object> microStudy(@PathVariable Long id) {
        ArticleDetailVO analysis = articleService.microStudy(id);
        if (analysis == null) {
            return ApiResponse.error(404, "文章不存在");
        }
        return ApiResponse.success(analysis);
    }
}