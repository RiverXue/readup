import java.util.Optional;

/**
 * 网页内容抓取服务接口
 * 负责从文章URL中提取完整的可读内容
 */
public interface ScraperService {
    
    /**
     * 从给定URL抓取文章内容
     * 
     * @param url 文章URL
     * @return 提取的纯文本内容，如果提取失败则返回空
     */
    Optional<String> scrapeArticleContent(String url);
}