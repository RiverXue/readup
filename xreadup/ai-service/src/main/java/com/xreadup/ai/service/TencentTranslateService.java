package com.xreadup.ai.service;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tmt.v20180321.TmtClient;
import com.tencentcloudapi.tmt.v20180321.models.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 腾讯云翻译服务 - 基础翻译引擎
 * 严格按照官方文档集成：https://cloud.tencent.com/document/product/551/15612
 * 
 * 作为双引擎策略中的基础翻译引擎，提供快速、稳定的翻译服务
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Slf4j
@Service
public class TencentTranslateService {

    @Autowired
    private TmtClient tmtClient;

    private static final int MAX_TEXT_LENGTH = 5000; // 腾讯云API限制6000字符，留出一些余量
    
    /**
     * 文本翻译 - 通用翻译
     * 支持自动语言检测和长文本分块翻译
     * 
     * @param text 待翻译文本
     * @param sourceLang 源语言，如"en"、"zh"，auto表示自动检测
     * @param targetLang 目标语言，如"zh"、"en"
     * @return 翻译结果
     */
    public String translateText(String text, String sourceLang, String targetLang) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // 如果文本长度超过限制，使用分块翻译
        if (text.length() > MAX_TEXT_LENGTH) {
            log.info("文本长度 {} 超过单条翻译限制，使用分块翻译", text.length());
            return translateLongText(text, sourceLang, targetLang);
        }
        
        try {
            TextTranslateRequest request = new TextTranslateRequest();
            request.setSourceText(text);
            request.setSource(sourceLang);
            request.setTarget(targetLang);
            request.setProjectId(0L); // 使用默认项目
            
            log.info("调用腾讯云翻译API: {} -> {}, 文本长度: {}", sourceLang, targetLang, text.length());
            
            TextTranslateResponse response = tmtClient.TextTranslate(request);
            
            if (response != null && response.getTargetText() != null) {
                log.info("翻译成功: {} -> {}, 结果长度: {}", sourceLang, targetLang, response.getTargetText().length());
                return response.getTargetText();
            }
            
            log.warn("翻译返回空结果");
            return text;
            
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云翻译API调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("翻译服务异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 长文本分块翻译
     * 当文本长度超过API限制时，将文本分块翻译后合并结果
     * 
     * @param text 待翻译的长文本
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @return 完整的翻译结果
     */
    private String translateLongText(String text, String sourceLang, String targetLang) {
        StringBuilder resultBuilder = new StringBuilder();
        int totalLength = text.length();
        int startIndex = 0;
        int chunkCount = 0;
        
        try {
            // 循环分块翻译
            while (startIndex < totalLength) {
                // 计算当前块的结束位置
                int endIndex = Math.min(startIndex + MAX_TEXT_LENGTH, totalLength);
                
                // 尽量在句子结束处分割，避免截断句子影响翻译质量
                if (endIndex < totalLength) {
                    // 查找最近的句子结束符
                    int lastPunctuationIndex = findLastPunctuation(text, startIndex, endIndex);
                    if (lastPunctuationIndex > startIndex + MAX_TEXT_LENGTH / 2) { // 确保至少翻译一半内容
                        endIndex = lastPunctuationIndex + 1;
                    }
                }
                
                // 提取当前块
                String chunk = text.substring(startIndex, endIndex);
                chunkCount++;
                
                log.info("分块翻译第 {} 块: 开始位置={}, 结束位置={}, 块长度={}", 
                        chunkCount, startIndex, endIndex, chunk.length());
                
                // 翻译当前块
                String translatedChunk = translateTextInternal(chunk, sourceLang, targetLang);
                
                // 添加到结果中
                resultBuilder.append(translatedChunk);
                
                // 更新起始位置
                startIndex = endIndex;
            }
            
            log.info("长文本翻译完成: 总共 {} 块, 原文长度={}, 译文长度={}", 
                    chunkCount, totalLength, resultBuilder.length());
            
            return resultBuilder.toString();
            
        } catch (Exception e) {
            log.error("长文本分块翻译失败: {}", e.getMessage(), e);
            throw new RuntimeException("长文本翻译服务异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 内部翻译方法，直接调用API
     * 供分块翻译内部使用，避免递归调用
     */
    private String translateTextInternal(String text, String sourceLang, String targetLang) {
        try {
            TextTranslateRequest request = new TextTranslateRequest();
            request.setSourceText(text);
            request.setSource(sourceLang);
            request.setTarget(targetLang);
            request.setProjectId(0L);
            
            TextTranslateResponse response = tmtClient.TextTranslate(request);
            
            if (response != null && response.getTargetText() != null) {
                return response.getTargetText();
            }
            
            log.warn("分块翻译返回空结果");
            return text;
            
        } catch (TencentCloudSDKException e) {
            log.error("分块翻译API调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("分块翻译服务异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 查找文本中最后一个标点符号的位置
     * 用于在分块时尽量保持句子完整性
     */
    private int findLastPunctuation(String text, int start, int end) {
        // 常见的英文标点符号
        String[] punctuations = {".", "?", "!", ";", "\"", "'", ","};
        
        int lastIndex = -1;
        
        for (String punctuation : punctuations) {
            int index = text.lastIndexOf(punctuation, end - 1);
            if (index > lastIndex && index >= start) {
                lastIndex = index;
            }
        }
        
        // 如果没有找到标点符号，返回原始结束位置
        return lastIndex > 0 ? lastIndex : end;
    }

    /**
     * 文本翻译 - 英文到中文
     * 专门处理英文文章翻译
     * 
     * @param englishText 英文文本
     * @return 中文翻译结果
     */
    public String translateEnglishToChinese(String englishText) {
        return translateText(englishText, "en", "zh");
    }

    /**
     * 文本翻译 - 中文到英文
     * 专门处理中文内容翻译
     * 
     * @param chineseText 中文文本
     * @return 英文翻译结果
     */
    public String translateChineseToEnglish(String chineseText) {
        return translateText(chineseText, "zh", "en");
    }

    /**
     * 批量文本翻译
     * 用于翻译多个文本片段
     * 
     * @param texts 文本列表
     * @param sourceLang 源语言
     * @param targetLang 目标语言
     * @return 翻译结果列表
     */
    public String translateBatch(String[] texts, String sourceLang, String targetLang) {
        try {
            TextTranslateBatchRequest request = new TextTranslateBatchRequest();
            request.setSource(sourceLang);
            request.setTarget(targetLang);
            request.setSourceTextList(texts);
            request.setProjectId(0L);
            
            log.info("批量翻译: {} -> {}, 文本数量: {}", sourceLang, targetLang, texts.length);
            
            TextTranslateBatchResponse response = tmtClient.TextTranslateBatch(request);
            
            if (response != null && response.getTargetTextList() != null && response.getTargetTextList().length > 0) {
                log.info("批量翻译成功: 翻译结果数量: {}", response.getTargetTextList().length);
                return String.join("\n", response.getTargetTextList());
            }
            
            log.warn("批量翻译返回空结果");
            return String.join("\n", texts);
            
        } catch (TencentCloudSDKException e) {
            log.error("腾讯云批量翻译API调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("批量翻译服务异常: " + e.getMessage(), e);
        }
    }

    /**
     * 获取支持的语言列表
     * 用于获取腾讯云支持的所有语言
     * 
     * @return 支持的语言列表
     */
    public String getSupportedLanguages() {
        return "[\"en\", \"zh\", \"ja\", \"ko\", \"fr\", \"de\", \"es\", \"ru\", \"it\", \"pt\"]";
    }
}