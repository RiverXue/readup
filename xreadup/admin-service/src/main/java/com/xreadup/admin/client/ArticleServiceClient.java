package com.xreadup.admin.client;

import com.xreadup.admin.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文章服务Feign客户端
 * 用于调用article-service提供的API接口
 */
@FeignClient(name = "article-service", path = "/api/article")
public interface ArticleServiceClient {

    /**
     * 获取文章列表
     * @param query 查询条件
     * @return 文章列表
     */
    @GetMapping("/explore")
    ApiResponse<PageResult> exploreArticles(@SpringQueryMap ArticleQueryDTO query);

    /**
     * 获取文章列表 - 直接参数方式
     * @param page 页码
     * @param pageSize 每页大小
     * @param title 标题关键词
     * @param category 分类
     * @param difficulty 难度
     * @return 文章列表
     */
    @GetMapping("/explore")
    ApiResponse<PageResult> exploreArticles(
            @RequestParam(name = "page", defaultValue = "1") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer pageSize,
            @RequestParam(name = "keyword", required = false) String title,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "difficultyLevel", required = false) String difficulty);

    /**
     * 获取文章详情
     * @param articleId 文章ID
     * @return 文章详情
     */
    @GetMapping("/{articleId}")
    ApiResponse<ArticleDTO> getArticleDetail(@PathVariable("articleId") Long articleId);

    /**
     * 审核文章
     * @param articleId 文章ID
     * @param status 审核状态
     * @param reason 审核原因
     * @return 操作结果
     */
    @PutMapping("/audit/{articleId}")
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
    @PutMapping("/category/{articleId}")
    ApiResponse<Boolean> updateArticleCategory(
            @PathVariable("articleId") Long articleId,
            @RequestParam String category);

    /**
     * 更新文章难度
     * @param articleId 文章ID
     * @param difficulty 难度
     * @return 操作结果
     */
    @PutMapping("/difficulty/{articleId}")
    ApiResponse<Boolean> updateArticleDifficulty(
            @PathVariable("articleId") Long articleId,
            @RequestParam String difficulty);

    /**
     * 标记文章为精选
     * @param articleId 文章ID
     * @param isFeatured 是否精选
     * @return 操作结果
     */
    @PutMapping("/featured/{articleId}")
    ApiResponse<Boolean> markArticleAsFeatured(
            @PathVariable("articleId") Long articleId,
            @RequestParam Boolean isFeatured);

    /**
     * 获取文章分类列表
     * @return 分类列表
     */
    @GetMapping("/categories")
    ApiResponse<List<String>> getArticleCategories();

    /**
     * 获取文章难度列表
     * @return 难度列表
     */
    @GetMapping("/difficulties")
    ApiResponse<List<String>> getArticleDifficulties();

    /**
     * 删除文章
     * @param articleId 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/{articleId}")
    ApiResponse<Boolean> deleteArticle(@PathVariable("articleId") Long articleId);

    /**
     * 发布文章
     * @param articleId 文章ID
     * @return 发布结果
     */
    @PutMapping("/{articleId}/publish")
    ApiResponse<Boolean> publishArticle(@PathVariable("articleId") Long articleId);

    // ========== 内容过滤管理相关接口 ==========

    /**
     * 获取文章的内容过滤记录
     * @param articleId 文章ID
     * @return 过滤记录列表
     */
    @GetMapping("/filter-logs/{articleId}")
    ApiResponse<List<Object>> getArticleFilterLogs(@PathVariable("articleId") Long articleId);

    /**
     * 获取内容过滤记录分页列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param filterType 过滤类型
     * @param status 状态
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 分页结果
     */
    @GetMapping("/filter-logs")
    ApiResponse<Object> getFilterLogsPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String filterType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate);

    /**
     * 更新过滤记录状态
     * @param logId 记录ID
     * @param status 新状态
     * @param adminId 管理员ID
     * @return 更新结果
     */
    @PutMapping("/filter-logs/{logId}/status")
    ApiResponse<Boolean> updateFilterLogStatus(
            @PathVariable("logId") Long logId,
            @RequestParam String status,
            @RequestParam(required = false) Long adminId);

    /**
     * 删除过滤记录
     * @param logId 记录ID
     * @return 删除结果
     */
    @DeleteMapping("/filter-logs/{logId}")
    ApiResponse<Boolean> deleteFilterLog(@PathVariable("logId") Long logId);

    /**
     * 获取过滤统计信息
     * @return 统计信息
     */
    @GetMapping("/filter-logs/statistics")
    ApiResponse<Object> getFilterStatistics();

    /**
     * 记录内容过滤日志
     * @param articleId 文章ID
     * @param filterType 过滤类型
     * @param matchedContent 匹配到的内容
     * @param filterReason 过滤原因
     * @param severityLevel 严重程度
     * @param actionTaken 采取的行动
     * @param adminId 管理员ID
     * @return 记录结果
     */
    @PostMapping("/filter-logs")
    ApiResponse<Boolean> logContentFilter(
            @RequestParam Long articleId,
            @RequestParam String filterType,
            @RequestParam String matchedContent,
            @RequestParam(required = false) String filterReason,
            @RequestParam(defaultValue = "medium") String severityLevel,
            @RequestParam(defaultValue = "blocked") String actionTaken,
            @RequestParam(required = false) Long adminId);

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
     * 分页结果类定义
     */
    class PageResult {
        private List<ArticleDTO> list;
        private int total;
        private int page;
        private int size;

        public PageResult() {}

        public PageResult(List<ArticleDTO> list, int total, int page, int size) {
            this.list = list;
            this.total = total;
            this.page = page;
            this.size = size;
        }

        public List<ArticleDTO> getList() { return list; }
        public void setList(List<ArticleDTO> list) { this.list = list; }
        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }

    /**
     * 文章DTO类定义 - 与init.sql中的article表字段完全一致
     */
    class ArticleDTO {
        private Long id;
        private String title;
        private String contentEn;
        private String contentCn;
        private String description;
        private String url;
        private String image;
        private String publishedAt;
        private String source;
        private String category;
        private String difficultyLevel;
        private String manualDifficulty;
        private String manualCategory;
        private Integer wordCount;
        private Integer readCount;
        private Integer likeCount;
        private Boolean isFeatured;
        private String createTime;
        private String updateTime;
        private Integer deleted;

        public ArticleDTO() {}

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getContentEn() { return contentEn; }
        public void setContentEn(String contentEn) { this.contentEn = contentEn; }
        public String getContentCn() { return contentCn; }
        public void setContentCn(String contentCn) { this.contentCn = contentCn; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
        public String getPublishedAt() { return publishedAt; }
        public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDifficultyLevel() { return difficultyLevel; }
        public void setDifficultyLevel(String difficultyLevel) { this.difficultyLevel = difficultyLevel; }
        public String getManualDifficulty() { return manualDifficulty; }
        public void setManualDifficulty(String manualDifficulty) { this.manualDifficulty = manualDifficulty; }
        public String getManualCategory() { return manualCategory; }
        public void setManualCategory(String manualCategory) { this.manualCategory = manualCategory; }
        public Integer getWordCount() { return wordCount; }
        public void setWordCount(Integer wordCount) { this.wordCount = wordCount; }
        public Integer getReadCount() { return readCount; }
        public void setReadCount(Integer readCount) { this.readCount = readCount; }
        public Integer getLikeCount() { return likeCount; }
        public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
        public Boolean getIsFeatured() { return isFeatured; }
        public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
        public String getCreateTime() { return createTime; }
        public void setCreateTime(String createTime) { this.createTime = createTime; }
        public String getUpdateTime() { return updateTime; }
        public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
        public Integer getDeleted() { return deleted; }
        public void setDeleted(Integer deleted) { this.deleted = deleted; }
    }
}