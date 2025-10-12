package com.xreadup.ai.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.articleservice.model.entity.Article;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleMapper extends BaseMapper<Article> {
    
    IPage<Article> selectArticlePage(Page<Article> page, @Param("query") ArticleQueryDTO query);
}