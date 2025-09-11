package com.xreadup.ai.articleservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.dto.ManualDifficultyDTO;
import com.xreadup.ai.articleservice.model.entity.Article;
import com.xreadup.ai.articleservice.model.vo.ArticleDetailVO;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;
import com.xreadup.ai.articleservice.model.vo.ArticleListVO;

import java.util.List;

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
     * 精读分析：AI深度理解文章
     * 智能Token策略：根据文章长度自动优化
     */
    ArticleDetailVO deepDiveAnalysis(Long id);
    
    /**
     * 全文翻译：英文→中文逐句精译
     */
    String translate(Long id);
    
    /**
     * 智能速读：30秒生成100字摘要
     */
    String quickRead(Long id);
    
    /**
     * 核心要点：5秒提取关键词
     */
    List<String> keyPoints(Long id);
    
    /**
     * 短文精学：800字内深度学习
     */
    ArticleDetailVO microStudy(Long id);
    
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