package com.xreadup.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.ai.model.entity.AiAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI分析结果Mapper接口
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Mapper
public interface AiAnalysisMapper extends BaseMapper<AiAnalysis> {
    
    /**
     * 根据文章ID查询分析结果
     * 
     * @param articleId 文章ID
     * @return AI分析结果
     */
    AiAnalysis selectByArticleId(Long articleId);
    
    /**
     * 根据文章ID删除分析结果
     * 
     * @param articleId 文章ID
     * @return 影响行数
     */
    int deleteByArticleId(Long articleId);
    
    /**
     * 检查文章是否已存在分析结果
     * 
     * @param articleId 文章ID
     * @return 是否存在
     */
    boolean existsByArticleId(Long articleId);
}