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
import java.util.List;
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
        int currentParagraphLength = 0;
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
                    currentParagraphLength = 0;
                    firstSegment = false;
                } else {
                    // 段落太短，继续添加到当前段落
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(1)));
                    segmentedContent.append(" ");
                    currentParagraphLength += candidateLength + 1;
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
                    currentParagraphLength = 0;
                    firstSegment = false;
                } else {
                    // 段落太短，继续添加到当前段落
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(3)));
                    segmentedContent.append(" ");
                    currentParagraphLength += candidateLength + 1;
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
                    currentParagraphLength = 0;
                } else {
                    // 段落较短，仅添加空格
                    segmentedContent.append(normalizedContent.substring(lastEnd, matcher.end(5)));
                    segmentedContent.append(" ");
                    currentParagraphLength += candidateLength + 1;
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
}