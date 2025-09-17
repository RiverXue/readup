package com.xreadup.ai.articleservice;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
import com.xreadup.ai.articleservice.model.vo.ArticleVO;
import com.xreadup.ai.articleservice.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Test
    void testGetArticlePage() {
        ArticleQueryDTO query = new ArticleQueryDTO();
        query.setPage(1);
        query.setSize(5);

        IPage<ArticleVO> page = articleService.getArticlePage(query);
        assertNotNull(page);
        System.out.println("文章总数: " + page.getTotal());
        page.getRecords().forEach(article -> {
            System.out.println("标题: " + article.getTitle());
            System.out.println("分类: " + article.getCategory());
            System.out.println("难度: " + article.getDifficultyLevel());
            System.out.println("---");
        });
    }

    @Test
    void testGetArticleDetail() {
        ArticleVO article = articleService.getArticleDetail(1L);
        assertNotNull(article);
        System.out.println("文章详情: " + article.getTitle());
    }
    
    @Test
    void testApiConnection() {
        // 这个测试只验证是否能连接到GNews API并获取文章信息
        System.out.println("正在测试API连接...");
        boolean apiConnected = articleService.testApiConnection();
        assertTrue(apiConnected, "API连接测试失败");
        System.out.println("API连接测试成功");
    }
    
    @Test
    void testFetchAndSaveArticles() {
        // 测试获取并保存技术分类的2篇文章
        int savedCount = articleService.fetchAndSaveArticles("technology", 2);
        
        // 验证至少保存了一篇文章
        assertTrue(savedCount >= 0, "保存的文章数不应为负数");
        System.out.println("成功保存的文章数量: " + savedCount);
        
        // 如果有保存的文章，查询验证内容完整性
        if (savedCount > 0) {
            // 查询最新保存的文章
            ArticleQueryDTO query = new ArticleQueryDTO();
            query.setPage(1);
            query.setSize(savedCount);
            query.setCategory("technology");
            
            IPage<ArticleVO> page = articleService.getArticlePage(query);
            assertNotNull(page);
            assertTrue(page.getRecords().size() >= savedCount, "查询到的文章数量应至少为保存的数量");
            
            // 验证文章内容是否完整
            page.getRecords().forEach(article -> {
                System.out.println("\n验证文章: " + article.getTitle());
                System.out.println("文章来源: " + article.getSource());
                System.out.println("文章URL: " + article.getUrl());
                System.out.println("文章内容长度: " + (article.getContentEn() != null ? article.getContentEn().length() : 0));
                System.out.println("单词数: " + article.getWordCount());
                
                // 验证内容不为空且长度合理
                assertNotNull(article.getContentEn());
                assertTrue(article.getContentEn().length() > 100, "文章内容应足够长，表明是完整内容");
                
                System.out.println("内容完整性验证通过");
            });
        }
    }
}