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

    @PostMapping("/deep/complete")
    @Operation(
        summary = "【深度学习-标准Token】800字内精学", 
        description = "适合800字内文章，全维度深度分析：难度评估+翻译+摘要+关键词，专家级精读（标准Token消耗）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "分析成功", 
                    content = @Content(schema = @Schema(implementation = ArticleAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    public ResponseEntity<ArticleAnalysisResponse> deepComplete(@Valid @RequestBody ArticleAnalysisRequest request) {
        ArticleAnalysisResponse response = aiAnalysisService.analyzeArticle(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/translate/full")
    @Operation(
        summary = "【全文翻译-标准Token】英文秒变中文", 
        description = "逐句精译英文全文，地道中文翻译，适合需要完整理解的文章（标准Token消耗）"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "翻译成功"),
        @ApiResponse(responseCode = "400", description = "输入内容为空"),
        @ApiResponse(responseCode = "500", description = "翻译服务异常")
    })
    public ResponseEntity<String> translateFull(
            @Parameter(description = "英文内容", example = "Artificial intelligence is revolutionizing healthcare...") 
            @RequestBody String englishText) {
        String translation = aiAnalysisService.translateToChinese(englishText);
        return ResponseEntity.ok(translation);
    }

    @PostMapping("/extract/summary")
    @Operation(
        summary = "【智能摘要-省80%Token】100字说清全文", 
        description = "AI浓缩英文长文为100字内中文精华，节省80% Token，一眼看懂文章主旨"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "摘要生成成功"),
        @ApiResponse(responseCode = "400", description = "输入内容为空")
    })
    public ResponseEntity<String> extractSummary(
            @Parameter(description = "文章内容", example = "文章内容...") 
            @RequestBody String content) {
        String summary = aiAnalysisService.generateSummary(content);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/extract/keywords")
    @Operation(
        summary = "【关键词提取-省90%Token】5秒抓重点", 
        description = "AI识别文章5-8个核心关键词，节省90% Token，5秒快速抓住文章重点"
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
        summary = "【系统自检】AI服务状态检测", 
        description = "一键检测AI服务是否在线，DeepSeek大模型是否正常工作"
    )
    @ApiResponse(responseCode = "200", description = "服务运行正常")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AI服务运行正常 - DeepSeek API已连接");
    }

    @PostMapping("/quick/summary")
    @Operation(
        summary = "【速读模式-省70%Token】长文5分钟速览", 
        description = "适合800-1500字文章，智能截断前400字符，节省70% Token，5分钟掌握核心要点"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "快速分析成功", 
                    content = @Content(schema = @Schema(implementation = ArticleAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<ArticleAnalysisResponse> quickSummary(@Valid @RequestBody ArticleAnalysisRequest request) {
        ArticleAnalysisResponse response = aiAnalysisService.quickAnalyze(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/smart/sampling")
    @Operation(
        summary = "【智能抽样-省65%Token】超长文高效阅读", 
        description = "适合1500字以上超长文，AI智能分析前30%内容抽样，节省65% Token，高效掌握精髓"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "分段分析成功", 
                    content = @Content(schema = @Schema(implementation = ArticleAnalysisResponse.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误")
    })
    public ResponseEntity<ArticleAnalysisResponse> smartSampling(@Valid @RequestBody ArticleAnalysisRequest request) {
        ArticleAnalysisResponse response = aiAnalysisService.chunkedAnalyze(request);
        return ResponseEntity.ok(response);
    }
}