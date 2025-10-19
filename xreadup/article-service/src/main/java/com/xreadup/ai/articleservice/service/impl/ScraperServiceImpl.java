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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    
                    // 清理文章结尾的无关信息
                    cleanedContent = cleanArticleSuffix(cleanedContent);
                    
                    // 对文章内容进行智能分段处理
                    String segmentedContent = segmentArticleContent(cleanedContent);
                    
                    // 增强内容验证，确保提取的是真正的文章内容
                    if (!isValidArticleContent(segmentedContent)) {
                        log.warn("内容验证失败，不是有效的文章内容: {}", url);
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
     * 支持多种常见的新闻文章开头格式
     */
    private String cleanArticlePrefix(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        String cleanedContent = content;
        
        // 1. 清理时间戳和来源信息前缀
        String[] prefixPatterns = {
            // AP新闻格式：Updated hh:mm AM/PM timezone, Month Day, Year WASHINGTON (AP) —
            "^Updated\\s+\\d{1,2}:\\d{2}\\s+[AP]M\\s+[A-Z]+,\\s+[A-Za-z]+\\s+\\d{1,2},\\s+\\d{4}\\s+[A-Z\\s()]+\\s*—\\s+",
            // 其他AP格式：Month Day, Year WASHINGTON (AP) —
            "^[A-Za-z]+\\s+\\d{1,2},\\s+\\d{4}\\s+[A-Z\\s()]+\\s*—\\s+",
            // 时间戳格式：January 15, 2024 at 2:30 PM EST
            "^[A-Za-z]+\\s+\\d{1,2},\\s+\\d{4}\\s+at\\s+\\d{1,2}:\\d{2}\\s+[AP]M\\s+[A-Z]+\\s*—?\\s*",
            // 作者信息：By John Smith, Staff Writer | January 15, 2024
            "^By\\s+[A-Za-z\\s]+,\\s*[A-Za-z\\s]+\\s*\\|\\s*[A-Za-z]+\\s+\\d{1,2},\\s+\\d{4}\\s*—?\\s*",
            // 简单作者格式：By John Smith
            "^By\\s+[A-Za-z\\s]+\\s*—?\\s*",
            // 网站品牌：CNN Breaking News | BBC News | Reuters
            "^(CNN|BBC|Reuters|AP|AFP|Reuters|Associated Press|Breaking News)\\s*—?\\s*",
            // 社交媒体分享：Share on Facebook | Tweet this
            "^(Share on|Tweet this|Follow us|Like us)\\s+[A-Za-z\\s|]+\\s*—?\\s*",
            // 广告标识：Advertisement | Sponsored Content
            "^(Advertisement|Sponsored|Promoted|Ad)\\s*[A-Za-z\\s]*\\s*—?\\s*",
            // 网站导航：Home | News | Sports | Entertainment
            "^(Home|News|Sports|Entertainment|Business|Technology)\\s*\\|\\s*[A-Za-z\\s|]+\\s*—?\\s*"
        };
        
        for (String pattern : prefixPatterns) {
            Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = compiledPattern.matcher(cleanedContent);
            if (matcher.find()) {
                log.debug("清理文章前缀: {}", matcher.group());
                cleanedContent = cleanedContent.substring(matcher.end());
                break; // 找到第一个匹配就停止
            }
        }
        
        // 2. 清理开头可能的多余空行和标点
        cleanedContent = cleanedContent.replaceAll("^\\s*[—\\-\\|]+\\s*", ""); // 清理开头的破折号、竖线等
        cleanedContent = cleanedContent.replaceAll("^\\s+", ""); // 清理开头的空白字符
        
        // 3. 如果清理后内容过短，可能清理过度了，返回原始内容
        if (cleanedContent.trim().length() < 50) {
            log.warn("清理后内容过短，可能清理过度，返回原始内容");
            return content;
        }
        
        return cleanedContent;
    }
    
    /**
     * 清理文章内容结尾的无关信息
     * 包括版权声明、相关文章推荐、社交媒体链接等
     */
    private String cleanArticleSuffix(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        String cleanedContent = content;
        
        // 1. 清理结尾的版权声明和网站信息
        String[] suffixPatterns = {
            // 版权声明：© 2024 CNN. All rights reserved.
            "\\s*©\\s*\\d{4}\\s+[A-Za-z\\s\\.]+\\s+All rights reserved\\..*$",
            // 版权声明变体：Copyright © 2024
            "\\s*Copyright\\s+©\\s*\\d{4}\\s+[A-Za-z\\s\\.]+.*$",
            // 相关文章推荐：Related Articles | More from this author
            "\\s*(Related|More from|You might also like|Recommended|Also read)\\s+[A-Za-z\\s|]+.*$",
            // 社交媒体链接：Follow us on Twitter | Like us on Facebook
            "\\s*(Follow us|Like us|Share this|Tweet this)\\s+[A-Za-z\\s|]+.*$",
            // 评论区：Comments | Leave a comment | Join the discussion
            "\\s*(Comments|Leave a comment|Join the discussion|Add your comment)\\s*.*$",
            // 广告内容：Advertisement | Sponsored by | Promoted content
            "\\s*(Advertisement|Sponsored by|Promoted|Ad)\\s+[A-Za-z\\s]+.*$",
            // 网站导航：About us | Contact | Privacy Policy
            "\\s*(About us|Contact|Privacy Policy|Terms of Service|Disclaimer)\\s*.*$",
            // 作者信息：About the author | Author bio
            "\\s*(About the author|Author bio|Writer)\\s+[A-Za-z\\s]+.*$",
            // 新闻来源：Source: | Via: | Originally published
            "\\s*(Source|Via|Originally published|First published)\\s*:?\\s*[A-Za-z\\s]+.*$",
            // 更新时间：Updated | Last updated | Modified
            "\\s*(Updated|Last updated|Modified)\\s*:?\\s*[A-Za-z\\s\\d,]+.*$",
            // 标签和分类：Tags: | Categories: | Filed under
            "\\s*(Tags|Categories|Filed under)\\s*:?\\s*[A-Za-z\\s,]+.*$",
            // 分享按钮：Share this article | Print this article
            "\\s*(Share this|Print this|Email this)\\s+[A-Za-z\\s]+.*$",
            // 订阅信息：Subscribe | Newsletter | Get updates
            "\\s*(Subscribe|Newsletter|Get updates|Stay informed)\\s+[A-Za-z\\s]+.*$"
        };
        
        // 从后往前匹配，找到第一个匹配的模式就截断
        for (String pattern : suffixPatterns) {
            Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            Matcher matcher = compiledPattern.matcher(cleanedContent);
            if (matcher.find()) {
                log.debug("清理文章后缀: {}", matcher.group().trim());
                cleanedContent = cleanedContent.substring(0, matcher.start());
                break; // 找到第一个匹配就停止
            }
        }
        
        // 2. 清理结尾可能的多余空行和标点
        cleanedContent = cleanedContent.replaceAll("\\s*[—\\-\\|]+\\s*$", ""); // 清理结尾的破折号、竖线等
        cleanedContent = cleanedContent.replaceAll("\\s+$", ""); // 清理结尾的空白字符
        
        // 3. 如果清理后内容过短，可能清理过度了，返回原始内容
        if (cleanedContent.trim().length() < 50) {
            log.warn("清理后内容过短，可能清理过度，返回原始内容");
            return content;
        }
        
        return cleanedContent;
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
        
        // 检查文本中是否已经包含双换行符（段落）
        if (normalizedContent.contains("\n\n") || normalizedContent.contains("\r\n\r\n")) {
            // 文本已经有自然段落划分，先保留原始分段结构
            String[] paragraphs;
            if (normalizedContent.contains("\r\n\r\n")) {
                paragraphs = normalizedContent.split("\\r\\n\\r\\n");
            } else {
                paragraphs = normalizedContent.split("\\n\\n");
            }
            
            // 过滤空段落并合并短段落
            List<String> filteredParagraphs = new ArrayList<>();
            for (int i = 0; i < paragraphs.length; i++) {
                String paragraph = paragraphs[i].trim();
                if (paragraph.isEmpty()) {
                    continue;
                }
                
                // 计算段落单词数
                int wordCount = countWords(paragraph);
                
                // 如果当前段落太短（少于15个单词）且不是第一段，则合并到前一段
                if (wordCount < 15 && !filteredParagraphs.isEmpty()) {
                    String lastParagraph = filteredParagraphs.remove(filteredParagraphs.size() - 1);
                    filteredParagraphs.add(lastParagraph + " " + paragraph);
                } else {
                    filteredParagraphs.add(paragraph);
                }
            }
            
            // 重新组合为带有双换行符的完整内容
            return String.join("\n\n", filteredParagraphs);
        }
        
        // 定义分段标记
        String paragraphSeparator = "\n\n";
        
        // 优化的正则表达式分段逻辑
        // 1. 优先匹配句号后两个空格+大写字母，这通常是新段落开始的强信号
        // 2. 其次匹配问号/感叹号后两个空格+大写字母
        // 3. 最后才考虑句号后一个空格+大写字母的情况，但设置更高的长度阈值
        String regex = "([.])\\s{2,}([A-Z])|([!?])\\s{2,}([A-Z])|([.])\\s+([A-Z])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(normalizedContent);
        
        StringBuilder segmentedContent = new StringBuilder();
        int lastEnd = 0;
        boolean firstSegment = true;
        
        // 寻找句子边界并添加分段标记
        while (matcher.find()) {
            // 计算当前潜在段落的长度
            int currentMatchEnd = 0;
            String nextChar = "";
            
            if (matcher.group(1) != null) {
                // 匹配到句号后两个以上空格+大写字母的模式
                currentMatchEnd = matcher.end(1);
                nextChar = matcher.group(2);
                
                // 增加语义连贯性检查：即使匹配到句号后两个空格，如果前面内容太短也不分割
                int candidateLength = currentMatchEnd - lastEnd;
                if (firstSegment || candidateLength >= 100) {  // 提高阈值到100字符
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(1)));
                    segmentedContent.append(paragraphSeparator);
                    firstSegment = false;
                } else {
                    // 段落太短，继续添加到当前段落
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(1)));
                    segmentedContent.append(" ");
                }
                lastEnd = matcher.end(2);
            } else if (matcher.group(3) != null) {
                // 匹配到问号/感叹号后两个以上空格+大写字母的模式
                currentMatchEnd = matcher.end(3);
                nextChar = matcher.group(4);
                
                // 问号和感叹号通常表示更强的语义结束，适当降低阈值
                int candidateLength = currentMatchEnd - lastEnd;
                if (firstSegment || candidateLength >= 80) {
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(3)));
                    segmentedContent.append(paragraphSeparator);
                    firstSegment = false;
                } else {
                    // 段落太短，继续添加到当前段落
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(3)));
                    segmentedContent.append(" ");
                }
                lastEnd = matcher.end(4);
            } else if (matcher.group(5) != null) {
                // 匹配到句号后一个空格+大写字母的模式
                currentMatchEnd = matcher.end(5);
                nextChar = matcher.group(6);
                
                // 句号后一个空格通常只是句子结束，不一定是段落结束
                // 大幅提高阈值，只有当段落非常长时才考虑分段
                int candidateLength = currentMatchEnd - lastEnd;
                if (candidateLength >= 150) {  // 大幅提高阈值到150字符
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(5)));
                    segmentedContent.append(paragraphSeparator);
                } else {
                    // 段落较短，仅添加空格
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(5)));
                    segmentedContent.append(" ");
                }
                lastEnd = matcher.end(6);
            }
            
            // 添加下一个字符
            segmentedContent.append(nextChar);
        }
        
        // 添加剩余内容
        if (lastEnd < normalizedContent.length()) {
            segmentedContent.append(normalizedContent.substring(lastEnd));
        }
        
        // 最终检查：合并非常短的段落（少于15个单词）和优化长句单独成段的问题
        String result = segmentedContent.toString();
        if (result.contains(paragraphSeparator)) {
            String[] paragraphs = result.split(paragraphSeparator);
            List<String> mergedParagraphs = new ArrayList<>();
            
            for (int i = 0; i < paragraphs.length; i++) {
                String paragraph = paragraphs[i].trim();
                if (paragraph.isEmpty()) {
                    continue;
                }
                
                int wordCount = countWords(paragraph);
                
                // 策略1：合并短段落（少于15个单词）
                if (wordCount < 15) {
                    if (!mergedParagraphs.isEmpty() && i < paragraphs.length - 1) {
                        // 如果前后都有段落，选择较短的一边合并
                        int prevWordCount = countWords(mergedParagraphs.get(mergedParagraphs.size() - 1));
                        int nextWordCount = countWords(paragraphs[i + 1].trim());
                        
                        if (prevWordCount <= nextWordCount) {
                            // 合并到前一段
                            String lastParagraph = mergedParagraphs.remove(mergedParagraphs.size() - 1);
                            mergedParagraphs.add(lastParagraph + " " + paragraph);
                        } else {
                            // 暂时保留，后面合并到下一段
                            mergedParagraphs.add(paragraph);
                        }
                    } else if (!mergedParagraphs.isEmpty()) {
                        // 只有前一段，合并到前一段
                        String lastParagraph = mergedParagraphs.remove(mergedParagraphs.size() - 1);
                        mergedParagraphs.add(lastParagraph + " " + paragraph);
                    } else if (i < paragraphs.length - 1) {
                        // 只有后一段，暂时保留，后面合并到下一段
                        mergedParagraphs.add(paragraph);
                    } else {
                        // 只有一段，无法合并
                        mergedParagraphs.add(paragraph);
                    }
                } 
                // 策略2：处理可能的长句单独成段问题
                else if (isPotentialSingleSentenceParagraph(paragraph)) {
                    // 如果当前段落可能只是一个长句，尝试与前后段落合并
                    if (!mergedParagraphs.isEmpty()) {
                        // 先检查前一段是否也是长句
                        String lastParagraph = mergedParagraphs.get(mergedParagraphs.size() - 1);
                        if (isPotentialSingleSentenceParagraph(lastParagraph)) {
                            // 前一段也是长句，合并到前一段
                            mergedParagraphs.remove(mergedParagraphs.size() - 1);
                            mergedParagraphs.add(lastParagraph + " " + paragraph);
                        } else {
                            mergedParagraphs.add(paragraph);
                        }
                    } else {
                        mergedParagraphs.add(paragraph);
                    }
                } else {
                    // 检查是否有之前保留的短段落需要合并到当前段落
                    if (!mergedParagraphs.isEmpty()) {
                        String lastParagraph = mergedParagraphs.get(mergedParagraphs.size() - 1);
                        if (countWords(lastParagraph) < 15) {
                            mergedParagraphs.remove(mergedParagraphs.size() - 1);
                            mergedParagraphs.add(lastParagraph + " " + paragraph);
                        } else {
                            mergedParagraphs.add(paragraph);
                        }
                    } else {
                        mergedParagraphs.add(paragraph);
                    }
                }
            }
            
            // 重新组合段落
            result = String.join(paragraphSeparator, mergedParagraphs);
        }
        
        return result;
    }
    
    /**
     * 判断一个段落是否可能只是一个长句
     * 基于标点符号数量、段落长度、句子数量等多维度分析
     */
    private boolean isPotentialSingleSentenceParagraph(String paragraph) {
        if (paragraph == null || paragraph.isEmpty()) {
            return false;
        }
        
        // 计算段落中的句子结束符数量
        int sentenceEndersCount = 0;
        for (char c : paragraph.toCharArray()) {
            if (c == '.' || c == '?' || c == '!') {
                sentenceEndersCount++;
            }
        }
        
        // 计算段落长度（字符数）
        int length = paragraph.length();
        
        // 计算段落中的单词数
        int wordCount = countWords(paragraph);
        
        // 计算平均句子长度（单词数）
        double avgSentenceLength = sentenceEndersCount > 0 ? (double) wordCount / sentenceEndersCount : wordCount;
        
        // 多维度判断：
        // 1. 如果段落中只有一个或没有句子结束符，且长度不是特别长
        // 2. 句子结束符与段落长度的比例很低（每个句子平均长度过长）
        // 3. 单词数适中但句子数很少（表明句子很长）
        boolean hasFewSentences = sentenceEndersCount <= 2;
        boolean highAvgSentenceLength = avgSentenceLength > 30; // 平均句子超过30个单词
        boolean highWordToSentenceRatio = sentenceEndersCount > 0 && wordCount / sentenceEndersCount > 25;
        boolean isLongParagraphButFewSentences = length > 200 && sentenceEndersCount <= 2;
        
        return (sentenceEndersCount <= 1 && length < 300) || 
               (sentenceEndersCount > 0 && length / sentenceEndersCount > 150) ||
               (hasFewSentences && highAvgSentenceLength) ||
               highWordToSentenceRatio ||
               isLongParagraphButFewSentences;
    }
    
    /**
     * 计算文本中的单词数
     */
    private int countWords(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        
        // 使用正则表达式分割文本为单词
        Pattern pattern = Pattern.compile("\\b\\w+\\b");
        Matcher matcher = pattern.matcher(text);
        
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        return count;
    }
    
    /**
     * 验证提取的文章内容是否有效
     * 通过多个维度检查内容质量，确保提取的是真正的文章内容
     * 
     * @param content 待验证的内容
     * @return true表示内容有效，false表示内容无效
     */
    private boolean isValidArticleContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            log.debug("内容为空或null");
            return false;
        }
        
        String trimmedContent = content.trim();
        
        // 1. 长度验证：至少100个字符
        if (trimmedContent.length() < 100) {
            log.debug("内容太短: {} 字符", trimmedContent.length());
            return false;
        }
        
        // 2. 单词数验证：至少20个单词
        int wordCount = countWords(trimmedContent);
        if (wordCount < 20) {
            log.debug("单词数太少: {} 个单词", wordCount);
            return false;
        }
        
        // 3. 句子数验证：至少2个句子
        String[] sentences = trimmedContent.split("[.!?]+");
        long sentenceCount = Arrays.stream(sentences)
                .filter(sentence -> sentence.trim().length() > 0)
                .count();
        
        if (sentenceCount < 2) {
            log.debug("句子数太少: {} 个句子", sentenceCount);
            return false;
        }
        
        // 4. 检查是否包含大量无意义内容（噪音词汇）
        String[] noisePatterns = {
            "click here", "read more", "subscribe", "newsletter",
            "advertisement", "sponsored", "cookie", "privacy policy",
            "terms of service", "all rights reserved", "copyright",
            "follow us", "like us", "share this", "tweet this",
            "join the discussion", "leave a comment", "add your comment",
            "about us", "contact us", "disclaimer", "legal notice"
        };
        
        String lowerContent = trimmedContent.toLowerCase();
        int noiseCount = 0;
        for (String pattern : noisePatterns) {
            if (lowerContent.contains(pattern)) {
                noiseCount++;
            }
        }
        
        // 如果包含太多噪音词汇，可能不是有效内容
        if (noiseCount > 3) {
            log.debug("包含太多噪音词汇: {} 个", noiseCount);
            return false;
        }
        
        // 5. 检查内容密度：有效词汇与总字符的比例
        int validWordCount = countWords(trimmedContent);
        double contentDensity = (double) validWordCount / trimmedContent.length();
        
        // 内容密度应该至少为0.1（10%的字符是有效单词）
        if (contentDensity < 0.1) {
            log.debug("内容密度太低: {:.2f}", contentDensity);
            return false;
        }
        
        // 6. 检查是否包含足够的实质性信息
        // 计算平均句子长度，过短可能表示内容质量不高
        double avgSentenceLength = (double) validWordCount / sentenceCount;
        if (avgSentenceLength < 5) {
            log.debug("平均句子长度太短: {:.1f} 个单词", avgSentenceLength);
            return false;
        }
        
        // 7. 检查是否包含重复内容（可能是错误提取）
        String[] words = trimmedContent.toLowerCase().split("\\s+");
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        for (String word : words) {
            if (word.length() > 3) { // 只统计长度大于3的单词
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        
        // 检查是否有单词重复率过高
        int totalWords = words.length;
        long highFrequencyWords = wordFrequency.values().stream()
                .filter(count -> count > totalWords * 0.1) // 重复率超过10%
                .count();
        
        if (highFrequencyWords > totalWords * 0.2) { // 如果超过20%的单词重复率过高
            log.debug("内容重复率过高: {} 个高频词", highFrequencyWords);
            return false;
        }
        
        // 8. 检查是否包含文章特征词汇
        String[] articleIndicators = {
            "said", "according", "reported", "announced", "stated",
            "however", "therefore", "furthermore", "moreover",
            "first", "second", "finally", "conclusion", "summary"
        };
        
        int indicatorCount = 0;
        for (String indicator : articleIndicators) {
            if (lowerContent.contains(indicator)) {
                indicatorCount++;
            }
        }
        
        // 如果包含一些文章特征词汇，增加可信度
        if (indicatorCount > 0) {
            log.debug("检测到文章特征词汇: {} 个", indicatorCount);
        }
        
        log.debug("内容验证通过: {} 字符, {} 单词, {} 句子, 密度: {:.2f}", 
                trimmedContent.length(), validWordCount, sentenceCount, contentDensity);
        
        return true;
    }
}