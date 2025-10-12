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
     * 删除文章
     * @param articleId 文章ID
     * @return 删除结果
     */
    @DeleteMapping("/api/article/{articleId}")
    ApiResponse<Boolean> deleteArticle(@PathVariable("articleId") Long articleId);

    /**
     * 发布文章
     * @param articleId 文章ID
     * @return 发布结果
     */
    @PutMapping("/api/article/{articleId}/publish")
    ApiResponse<Boolean> publishArticle(@PathVariable("articleId") Long articleId);

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