package com.xreadup.ai.articleservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.model.entity.Article;
import com.xreadup.ai.articleservice.model.vo.ArticleDetailVO;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;
import com.xreadup.ai.articleservice.model.vo.ArticleListVO;

public interface ArticleService {
    
    /**
     * 分页查询文章列表（不包含内容，提升性能）
     */
    IPage<ArticleListVO> getArticlePage(ArticleQueryDTO query);
    
    /**
     * 获取文章详情（包含完整内容）
     */
    ArticleVO getArticleDetail(Long id);
    
    /**
     * 获取文章详情及AI分析结果
     */
    ArticleDetailVO getArticleDetailWithAiAnalysis(Long id);
    
    /**
     * 对文章进行AI分析
     */
    ArticleDetailVO analyzeArticleWithAI(Long id);
    
    /**
     * 更新文章手动难度
     */
    boolean updateManualDifficulty(ManualDifficultyDTO dto);
    
    /**
     * 从gnews.io刷新文章
     */
    int refreshArticles(String category, Integer count);
    
    /**
     * 从gnews.io获取头条新闻
     */
    int refreshTopHeadlines(Integer count);
    
    /**
     * 增加文章阅读量
     */
    void incrementReadCount(Long articleId);
}