//package com.xreadup.ai.articleservice;
//
//import com.baomidou.mybatisplus.core.metadata.IPage;
//import com.xreadup.ai.articleservice.model.dto.ArticleQueryDTO;
//import com.xreadup.ai.articleservice.model.vo.ArticleVO;
//import com.xreadup.ai.articleservice.service.ArticleService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//
//@SpringBootTest
//class ArticleServiceTest {
//
//    @Autowired
//    private ArticleService articleService;
//
//    @Test
//    void testGetArticlePage() {
//        ArticleQueryDTO query = new ArticleQueryDTO();
//        query.setPage(1);
//        query.setSize(5);
//
//        IPage<ArticleVO> page = articleService.getArticlePage(query);
//        assertNotNull(page);
//        System.out.println("文章总数: " + page.getTotal());
//        page.getRecords().forEach(article -> {
//            System.out.println("标题: " + article.getTitle());
//            System.out.println("分类: " + article.getCategory());
//            System.out.println("难度: " + article.getDifficultyLevel());
//            System.out.println("---");
//        });
//    }
//
//    @Test
//    void testGetArticleDetail() {
//        ArticleVO article = articleService.getArticleDetail(1L);
//        assertNotNull(article);
//        System.out.println("文章详情: " + article.getTitle());
//    }
//}