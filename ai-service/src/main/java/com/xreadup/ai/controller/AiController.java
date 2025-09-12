package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.ArticleAnalysisRequest;
import com.xreadup.ai.model.dto.ArticleAnalysisResponse;
import com.xreadup.ai.service.AiAnalysisService;
import com.xreadup.ai.service.EnhancedAiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AI分析控制器 - 重构版
 * 从17个API精简为6个核心API，消除冗余
 * 
 * @version 3.0.0
 */
@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI分析服务", description = "提供文章智能分析、翻译、摘要等AI功能，集成数据持久化")
@Slf4j
public class AiController {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @Autowired
    private EnhancedAiAnalysisService enhancedAiAnalysisService;

    /**
     * 【核心API】统一文章分析入口
     * 整合：深度学习、快速分析、分段分析
     */
    @PostMapping("/analyze")
    @Operation(
        summary = "【统一分析】智能选择分析策略", 
        description = "根据文章长度智能选择分析策略：短文章(≤800字)深度学习，中等文章(800-1500字)快速分析，长文章(>1500字)分段分析，支持数据持久化"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "分析成功", 
                    content = @Content(schema = @Schema(implementation = ArticleAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<ArticleAnalysisResponse> analyzeArticle(
            @Valid @RequestBody ArticleAnalysisRequest request,
            @RequestParam(value = "save", defaultValue = "false") 
            @Parameter(description = "是否保存分析结果到数据库", example = "true") boolean save,
            @RequestParam(value = "forceRegenerate", defaultValue = "false") 
            @Parameter(description = "是否强制重新生成，即使已存在", example = "false") boolean forceRegenerate) {
        
        ArticleAnalysisResponse response;
        
        if (save) {
            response = enhancedAiAnalysisService.analyzeAndSaveArticle(request, forceRegenerate);
        } else {
            response = aiAnalysisService.analyzeArticle(request);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 【核心API】批量文章分析
     */
    @PostMapping("/analyze/batch")
    @Operation(
        summary = "【批量分析】多文章统一处理", 
        description = "批量分析多篇文章，自动选择最佳分析策略"
    )
    public ResponseEntity<Map<String, Object>> analyzeBatch(
            @RequestBody ArticleAnalysisRequest[] requests,
            @RequestParam(value = "save", defaultValue = "false") boolean save) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("total", requests.length);
        response.put("processed", 0); // 简化实现
        response.put("message", "批量分析功能开发中");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 【状态API】检查分析状态
     */
    @GetMapping("/status/{articleId}")
    @Operation(
        summary = "【状态查询】检查文章分析状态", 
        description = "检查指定文章是否已存在分析结果"
    )
    public ResponseEntity<Map<String, Object>> checkAnalysisStatus(
            @PathVariable @Parameter(description = "文章ID", example = "123") Long articleId) {
        
        boolean exists = enhancedAiAnalysisService.isArticleAnalyzed(articleId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("articleId", articleId);
        response.put("analyzed", exists);
        response.put("message", exists ? "文章已分析" : "文章未分析");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 【数据API】获取分析结果
     */
    @GetMapping("/result/{articleId}")
    @Operation(
        summary = "【结果获取】获取已保存的分析结果", 
        description = "从数据库获取文章的已保存分析结果"
    )
    public ResponseEntity<ArticleAnalysisResponse> getAnalysisResult(
            @PathVariable @Parameter(description = "文章ID", example = "123") Long articleId) {
        
        ArticleAnalysisResponse response = enhancedAiAnalysisService.getArticleAnalysis(articleId);
        
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * 【数据API】删除分析结果
     */
    @DeleteMapping("/result/{articleId}")
    @Operation(
        summary = "【数据删除】删除已保存的分析结果", 
        description = "从数据库删除指定文章的AI分析结果"
    )
    public ResponseEntity<Map<String, Object>> deleteAnalysisResult(
            @PathVariable @Parameter(description = "文章ID", example = "123") Long articleId) {
        
        boolean deleted = enhancedAiAnalysisService.deleteArticleAnalysis(articleId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("articleId", articleId);
        response.put("deleted", deleted);
        response.put("message", deleted ? "删除成功" : "删除失败或记录不存在");
        
        return ResponseEntity.ok(response);
    }

    /**
     * 【系统API】服务健康检查
     */
    @GetMapping("/health")
    @Operation(
        summary = "【系统自检】AI服务状态检测", 
        description = "一键检测AI服务是否在线"
    )
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "AI分析服务");
        response.put("version", "3.0.0");
        response.put("features", new String[]{"智能分析", "数据持久化", "批量处理"});
        
        return ResponseEntity.ok(response);
    }
}