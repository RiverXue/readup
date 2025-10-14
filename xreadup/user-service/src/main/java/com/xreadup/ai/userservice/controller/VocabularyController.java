package com.xreadup.ai.userservice.controller;

import com.xreadup.ai.userservice.dto.AddWordRequest;
import com.xreadup.ai.userservice.entity.Word;
import com.xreadup.ai.userservice.service.VocabularyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 二级词库控制器
 * 提供智能词汇查询和管理功能
 */
@RestController
@RequestMapping("/api/vocabulary")
@Tag(name = "二级词库服务", description = "智能词汇查询和管理接口")
public class VocabularyController {

    @Autowired
    private VocabularyService vocabularyService;

    /**
     * 智能查询单词
     */
    @PostMapping("/lookup")
    @Operation(summary = "智能查询单词", description = "二级词库策略：本地缓存优先，AI兜底生成")
    public ResponseEntity<?> lookupWord(@RequestBody LookupRequest request) {
        try {
            Word word = vocabularyService.lookupWord(
                request.getWord(), 
                request.getContext(), 
                request.getUserId(), 
                request.getArticleId()
            );
            return ResponseEntity.ok(new ApiResponse(200, "查询成功", word));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 批量查询单词
     */
    @PostMapping("/batch-lookup")
    @Operation(summary = "批量查询单词", description = "批量智能查询多个单词")
    public ResponseEntity<?> batchLookup(@RequestBody BatchLookupRequest request) {
        try {
            List<Word> words = vocabularyService.lookupWords(
                request.getWords(),
                request.getContext(),
                request.getUserId(),
                request.getArticleId()
            );
            return ResponseEntity.ok(new ApiResponse(200, "批量查询成功", words));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 获取用户词库统计
     */
    @GetMapping("/stats/{userId}")
    @Operation(summary = "获取词库统计", description = "获取用户的词汇学习统计信息")
    public ResponseEntity<?> getVocabularyStats(
            @Parameter(description = "用户ID", required = true) 
            @PathVariable Long userId) {
        try {
            Map<String, Object> stats = vocabularyService.getUserVocabularyStats(userId);
            return ResponseEntity.ok(new ApiResponse(200, "获取统计成功", stats));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 清理重复词汇
     */
    @PostMapping("/cleanup/{userId}")
    @Operation(summary = "清理重复词汇", description = "清理用户词库中的重复词汇")
    public ResponseEntity<?> cleanupDuplicates(
            @Parameter(description = "用户ID", required = true) 
            @PathVariable Long userId) {
        try {
            int cleaned = vocabularyService.cleanupDuplicateWords(userId);
            return ResponseEntity.ok(new ApiResponse(200, "清理完成", Map.of("cleanedCount", cleaned)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 智能添加单词（兼容原有接口）
     */
    @PostMapping("/add")
    @Operation(summary = "智能添加单词", description = "智能添加单词到生词本，支持上下文")
    public ResponseEntity<?> addWord(@RequestBody AddWordRequest request) {
        try {
            Word word = vocabularyService.lookupWord(
                request.getWord(),
                request.getContext(),
                request.getUserId(),
                request.getSourceArticleId()
            );
            return ResponseEntity.ok(new ApiResponse(200, "添加成功", word));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }
    
    /**
     * 复习单词 - 重定向到 report-service
     * @deprecated 此接口已重定向到 report-service，请使用 /api/report/review/{wordId} 接口
     */
    @PostMapping("/review")
    @Operation(summary = "复习单词", description = "更新单词的复习状态（已重定向到report-service）")
    @Deprecated
    public ResponseEntity<?> reviewWord(@RequestBody ReviewWordRequest request) {
        // 重定向到 report-service
        // 将 reviewStatus 转换为 isRemembered 布尔值
        boolean isRemembered = "mastered".equals(request.getReviewStatus());
        
        // 返回重定向信息，建议前端使用新的API
        return ResponseEntity.ok(new ApiResponse(301, 
            "此接口已重定向到 report-service，请使用 /api/report/review/" + request.getWordId() + 
            "?userId=" + request.getUserId() + "&remembered=" + isRemembered, 
            Map.of("redirectUrl", "/api/report/review/" + request.getWordId() + 
                   "?userId=" + request.getUserId() + "&remembered=" + isRemembered)));
    }
    
    /**
     * 删除单词
     */
    @DeleteMapping("/{wordId}")
    @Operation(summary = "删除单词", description = "从用户词库中删除单词")
    public ResponseEntity<?> deleteWord(
            @Parameter(description = "单词ID", required = true)
            @PathVariable Long wordId,
            
            @Parameter(description = "用户ID", required = true)
            @RequestParam Long userId) {
        try {
            boolean success = vocabularyService.deleteWord(wordId, userId);
            if (success) {
                return ResponseEntity.ok(new ApiResponse(200, "删除成功", null));
            } else {
                return ResponseEntity.ok(new ApiResponse(404, "单词不存在或不属于当前用户", null));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 查询请求DTO
     */
    public static class LookupRequest {
        private String word;
        private String context;
        private Long userId;
        private Long articleId;

        // Getters and Setters
        public String getWord() { return word; }
        public void setWord(String word) { this.word = word; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getArticleId() { return articleId; }
        public void setArticleId(Long articleId) { this.articleId = articleId; }
    }
    
    /**
     * 复习单词请求DTO
     */
    public static class ReviewWordRequest {
        private Long wordId;
        private Long userId;
        private String reviewStatus; // new, learning, mastered

        // Getters and Setters
        public Long getWordId() { return wordId; }
        public void setWordId(Long wordId) { this.wordId = wordId; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getReviewStatus() { return reviewStatus; }
        public void setReviewStatus(String reviewStatus) { this.reviewStatus = reviewStatus; }
    }

    /**
     * 批量查询请求DTO
     */
    public static class BatchLookupRequest {
        private List<String> words;
        private String context;
        private Long userId;
        private Long articleId;

        // Getters and Setters
        public List<String> getWords() { return words; }
        public void setWords(List<String> words) { this.words = words; }
        public String getContext() { return context; }
        public void setContext(String context) { this.context = context; }
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getArticleId() { return articleId; }
        public void setArticleId(Long articleId) { this.articleId = articleId; }
    }

    /**
     * 统一的API响应格式
     */
    public static class ApiResponse {
        private int code;
        private String message;
        private Object data;

        public ApiResponse(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Object getData() { return data; }
        public void setData(Object data) { this.data = data; }
    }
}