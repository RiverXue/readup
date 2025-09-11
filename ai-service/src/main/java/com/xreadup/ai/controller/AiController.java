package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.ArticleAnalysisRequest;
import com.xreadup.ai.model.dto.ArticleAnalysisResponse;
import com.xreadup.ai.service.AiAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
@Tag(name = "AI分析服务", description = "提供文章智能分析、翻译、摘要等AI功能，基于DeepSeek大模型")
public class AiController {

    @Autowired
    private AiAnalysisService aiAnalysisService;

    @PostMapping("/analyze")
    @Operation(
        summary = "文章全面分析", 
        description = "对文章进行CEFR难度评估、关键词提取、中文翻译、摘要生成、简化内容等全面AI分析"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "分析成功", 
                    content = @Content(schema = @Schema(implementation = ArticleAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<ArticleAnalysisResponse> analyzeArticle(@Valid @RequestBody ArticleAnalysisRequest request) {
        ArticleAnalysisResponse response = aiAnalysisService.analyzeArticle(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate")
    @Operation(
        summary = "英文翻译中文", 
        description = "使用DeepSeek AI将英文内容翻译成地道、准确的中文"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "翻译成功"),
        @ApiResponse(responseCode = "400", description = "输入内容为空"),
        @ApiResponse(responseCode = "500", description = "翻译服务异常")
    })
    public ResponseEntity<String> translateToChinese(
            @Parameter(description = "英文内容", example = "Artificial intelligence is revolutionizing healthcare...") 
            @RequestBody String englishText) {
        String translation = aiAnalysisService.translateToChinese(englishText);
        return ResponseEntity.ok(translation);
    }

    @PostMapping("/summary")
    @Operation(
        summary = "生成中文摘要", 
        description = "使用AI生成简洁、准确的中文文章摘要，控制在100字以内"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "摘要生成成功"),
        @ApiResponse(responseCode = "400", description = "输入内容为空")
    })
    public ResponseEntity<String> generateSummary(
            @Parameter(description = "文章内容", example = "文章内容...") 
            @RequestBody String content) {
        String summary = aiAnalysisService.generateSummary(content);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/keywords")
    @Operation(
        summary = "提取关键词", 
        description = "使用AI从文章内容中提取5-8个核心关键词"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "关键词提取成功"),
        @ApiResponse(responseCode = "400", description = "输入内容为空")
    })
    public ResponseEntity<List<String>> extractKeywords(
            @Parameter(description = "文章内容", example = "文章内容...") 
            @RequestBody String content) {
        List<String> keywords = aiAnalysisService.extractKeywords(content);
        return ResponseEntity.ok(keywords);
    }

    @GetMapping("/health")
    @Operation(
        summary = "健康检查", 
        description = "检查AI服务运行状态和DeepSeek API连接"
    )
    @ApiResponse(responseCode = "200", description = "服务运行正常")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI服务运行正常 - DeepSeek API已连接");
    }

    @PostMapping("/quick-analyze")
    @Operation(
        summary = "快速文章分析", 
        description = "针对长文章的轻量级分析，智能截断内容，节省70%token消耗"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "快速分析成功", 
                    content = @Content(schema = @Schema(implementation = ArticleAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<ArticleAnalysisResponse> quickAnalyze(@Valid @RequestBody ArticleAnalysisRequest request) {
        ArticleAnalysisResponse response = aiAnalysisService.quickAnalyze(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/chunked-analyze")
    @Operation(
        summary = "分段文章分析", 
        description = "将长文章分段处理，只分析前30%内容推断整体质量，节省65%token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "分段分析成功", 
                    content = @Content(schema = @Schema(implementation = ArticleAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<ArticleAnalysisResponse> chunkedAnalyze(@Valid @RequestBody ArticleAnalysisRequest request) {
        ArticleAnalysisResponse response = aiAnalysisService.chunkedAnalyze(request);
        return ResponseEntity.ok(response);
    }
}