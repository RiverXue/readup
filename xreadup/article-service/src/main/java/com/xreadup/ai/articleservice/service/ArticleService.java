package com.xreadup.ai.articleservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.model.common.PageResult;
import com.xreadup.ai.articleservice.model.vo.ArticleDetailVO;
import com.xreadup.ai.articleservice.model.vo.ArticleListVO;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;
import com.xreadup.ai.articleservice.model.common.ApiResponse;

import java.util.List;

/**
 * 文章服务接口
 */
public interface ArticleService {
    
    /**
     * 探索文章列表
     */
    ApiResponse<PageResult<ArticleListVO>> exploreArticles(ArticleQueryDTO query);
    
    /**
     * 开始阅读文章
     */
    ApiResponse<ArticleDetailVO> readArticle(Long id);
    
    /**
     * 深度分析文章
     */
    ApiResponse<ArticleDetailVO> deepDiveAnalysis(Long id);
    
    /**
     * 更新文章手动难度
     */
    ApiResponse<Boolean> updateManualDifficulty(ManualDifficultyDTO dto);
    
    /**
     * 获取文章分页列表（保留兼容方法）
     */
    IPage<ArticleVO> getArticlePage(ArticleQueryDTO query);
    
    /**
     * 获取文章详情（保留兼容方法）
     */
    ArticleVO getArticleDetail(Long id);
    
    /**
     * 刷新分类文章
     */
    List<ArticleVO> refreshArticles(String category, int limit);
    
    /**
     * 刷新热点文章
     */
    List<ArticleVO> refreshTopHeadlines(int limit);
    
    /**
     * 增加阅读次数
     */
    void incrementReadCount(Long id);
    
    /**
     * 测试API连接是否正常
     * @return 是否连接成功
     */
    boolean testApiConnection();
    
    /**
     * 从GNews获取文章并保存到数据库，使用Readability4J提取全文
     * @param category 文章分类
     * @param limit 获取数量
     * @return 保存的文章数量
     */
    int fetchAndSaveArticles(String category, int limit);
    
    /**
     * 获取文章总数
     */
    long getTotalArticleCount();
    
    /**
     * 获取已发布文章数
     */
    long getPublishedArticleCount();
    
    /**
     * 获取今日新增文章数
     */
    long getNewArticlesTodayCount();
    
    /**
     * 根据英文内容更新文章的中文翻译内容
     * @param contentEn 文章的英文内容
     * @param contentCn 文章的中文翻译内容
     * @return 更新是否成功
     */
    ApiResponse<Boolean> updateContentCn(String contentEn, String contentCn);
}