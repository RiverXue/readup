package com.xreadup.ai.controller;

import com.xreadup.ai.model.dto.*;
import com.xreadup.ai.service.EnhancedAiAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI分析服务控制器
 * 
 * 提供文章AI分析相关API接口
 * 所有接口都保存分析结果到数据库
 * 
 * @author xreadup
 * @version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final EnhancedAiAnalysisService enhancedAiAnalysisService;

    // ===== 以下方法已删除（未使用） =====
    // - analyzeArticle() - 未使用
    // - batchAnalyzeArticles() - 未使用
    // - translateFullText() - 未使用
    // - translateWord() - 未使用

    /**
     * 获取AI分析结果列表
     * 为admin管理后台提供分析结果查询功能
     * 
     * @param page 页码
     * @param pageSize 每页大小
     * @param articleTitle 文章标题搜索关键字
     * @param analysisType 分析类型
     * @param status 分析状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return AI分析结果列表
     */
    @GetMapping("/analysis")
    public ApiResponse<Map<String, Object>> getAIAnalysisList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String articleTitle,
            @RequestParam(required = false) String analysisType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            log.info("获取AI分析结果列表，page: {}, pageSize: {}, articleTitle: {}, analysisType: {}, status: {}, startDate: {}, endDate: {}",
                    page, pageSize, articleTitle, analysisType, status, startDate, endDate);
            
            Map<String, Object> result = enhancedAiAnalysisService.getAIAnalysisList(
                    page, pageSize, articleTitle, analysisType, status, startDate, endDate);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取AI分析结果列表失败", e);
            return ApiResponse.error("获取AI分析结果列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取AI分析详情
     * 为admin管理后台提供分析详情查询功能
     * 
     * @param analysisId 分析ID
     * @return AI分析详情
     */
    @GetMapping("/analysis/{analysisId}")
    public ApiResponse<Map<String, Object>> getAIAnalysisDetail(@PathVariable Long analysisId) {
        try {
            log.info("获取AI分析详情，analysisId: {}", analysisId);
            
            Map<String, Object> result = enhancedAiAnalysisService.getAIAnalysisDetail(analysisId);
            
            if (result == null) {
                return ApiResponse.error("分析结果不存在");
            }
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取AI分析详情失败", e);
            return ApiResponse.error("获取AI分析详情失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查
     * 检查AI服务状态
     * 
     * @return 服务状态
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("AI服务运行正常");
    }
}