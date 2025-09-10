package com.xreadup.ai.articleservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.model.entity.Article;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;

public interface ArticleService {
    
    /**
     * 分页查询文章列表
     */
    IPage<ArticleVO> getArticlePage(ArticleQueryDTO query);
    
    /**
     * 获取文章详情
     */
    ArticleVO getArticleDetail(Long id);
    
    /**
     * 更新文章手动难度
     */
    boolean updateManualDifficulty(ManualDifficultyDTO dto);
    
    /**
     * 从gnews.io刷新文章
     */
    int refreshArticles(String category, Integer count);
    
    /**
     * 增加文章阅读量
     */
    void incrementReadCount(Long articleId);
}