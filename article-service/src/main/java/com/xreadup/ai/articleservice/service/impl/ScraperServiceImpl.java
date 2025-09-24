package com.xreadup.ai.articleservice.service.impl;

import com.xreadup.ai.articleservice.service.ScraperService;
import lombok.extern.slf4j.Slf4j;
import net.dankito.readability4j.Article;
import net.dankito.readability4j.Readability4J;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ScraperServiceImpl implements ScraperService {

    @Override
    @Retryable(
            value = {SocketTimeoutException.class, IOException.class}, 
            maxAttempts = 3,
            backoff = @org.springframework.retry.annotation.Backoff(delay = 5000)
    )
    public Optional<String> scrapeArticleContent(String url) {
        try {
            // 1. 使用 Jsoup 获取整个网页的 HTML
            Document doc = Jsoup.connect(url)
                    .timeout(30000) // 从10秒提高到30秒
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.google.com/")
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "en-US,en;q=0.5")
                    .get();

            // 2. 将 HTML 文档传递给 Readability4J
            Readability4J readability = new Readability4J(url, doc.html());

            // 3. 解析并获取文章对象
            Article article = readability.parse();

            if (article != null) {
                // 获取纯文本内容并清理开头格式
                String textContent = article.getTextContent();
                if (textContent != null) {
                    // 清理文章开头常见的时间戳和来源信息格式
                    // 例如：Updated [hour]:[minute] [AMPM] [timezone], [monthFull] [day], [year] WASHINGTON (AP) — 
                    String cleanedContent = cleanArticlePrefix(textContent);
                    
                    // 对文章内容进行智能分段处理
                    String segmentedContent = segmentArticleContent(cleanedContent);
                    
                    // 检查提取内容的长度，过短的内容视为失败
                    if (segmentedContent == null || segmentedContent.trim().length() < 100) {
                        log.warn("提取内容过短，视为失败: {}", url);
                        return Optional.empty();
                    }
                    
                    return Optional.of(segmentedContent);
                }
            } else {
                log.warn("Readability4J 无法解析文章：{}", url);
                return Optional.empty();
            }
        } catch (IOException e) {
            log.error("❌ 抓取失败 | URL: {} | 原因: {}", url, e.getMessage());
            // 不抛出异常，但让@Retryable能够识别到异常以触发重试
            // 由于Spring Retry在方法内部也能捕获异常进行重试，所以直接返回empty
            return Optional.empty();
        } catch (Exception e) {
            log.error("❌ 解析文章内容时发生异常 | URL: {} | 原因: {}", url, e.getMessage());
            return Optional.empty();
        }
        return Optional.empty();
    }
    
    /**
     * 重试失败后的兜底方法
     */
    @Recover
    public Optional<String> recover(Exception e, String url) {
        log.error("⛔ 重试3次后仍失败，放弃抓取: {}", url, e);
        return Optional.empty();
    }

    /**
     * 清理文章内容开头的时间戳和来源信息前缀
     */
    private String cleanArticlePrefix(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // 匹配常见的新闻文章前缀格式，如 "Updated hh:mm AM/PM timezone, Month Day, Year WASHINGTON (AP) — "
        // 此正则表达式尝试匹配多种可能的变体
        String regex = "^Updated\\s+\\d{1,2}:\\d{2}\\s+[AP]M\\s+[A-Z]+,\\s+[A-Za-z]+\\s+\\d{1,2},\\s+\\d{4}\\s+[A-Z\\s()]+\\s*—\\s+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            // 找到匹配的前缀，返回清理后的内容
            log.debug("清理文章前缀: {}", matcher.group());
            return content.substring(matcher.end());
        }
        
        // 也尝试匹配其他可能的AP新闻格式
        String regex2 = "^[A-Za-z]+\\s+\\d{1,2},\\s+\\d{4}\\s+[A-Z\\s()]+\\s*—\\s+";
        Pattern pattern2 = Pattern.compile(regex2);
        Matcher matcher2 = pattern2.matcher(content);
        
        if (matcher2.find()) {
            log.debug("清理文章前缀: {}", matcher2.group());
            return content.substring(matcher2.end());
        }
        
        // 如果没有匹配的前缀模式，返回原始内容
        return content;
    }
    
    /**
     * 智能分段处理
     * 基于标点符号和段落语义对文章内容进行分段，并添加特殊标记
     */
    private String segmentArticleContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // 保留原始文本中的换行符，在此基础上进行智能分段
        String normalizedContent = content.trim();
        
        // 检查文本中是否已经包含换行符（段落）
        if (normalizedContent.contains("\n\n") || normalizedContent.contains("\r\n\r\n")) {
            // 文本已经有自然段落划分，保留原始分段结构
            return normalizedContent;
        }
        
        // 定义分段标记
        String paragraphSeparator = "\n\n";
        
        // 使用正则表达式进行智能分段
        // 匹配句号、问号、感叹号等句子结束符后跟空格和大写字母的模式
        String regex = "([.!?])\\s+([A-Z])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(normalizedContent);
        
        StringBuilder segmentedContent = new StringBuilder();
        int lastEnd = 0;
        
        // 寻找句子边界并添加分段标记
        while (matcher.find()) {
            // 将前一段内容添加到结果中
            segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(1)));
            
            // 如果找到的句子足够长（超过50个字符），则添加分段标记
            if (matcher.start(1) - lastEnd > 50) {
                segmentedContent.append(paragraphSeparator);
            } else {
                segmentedContent.append(" ");
            }
            
            // 保留匹配到的大写字母
            segmentedContent.append(matcher.group(2));
            lastEnd = matcher.end(2);
        }
        
        // 添加剩余内容
        if (lastEnd < normalizedContent.length()) {
            segmentedContent.append(normalizedContent.substring(lastEnd));
        }
        
        return segmentedContent.toString();
    }
}