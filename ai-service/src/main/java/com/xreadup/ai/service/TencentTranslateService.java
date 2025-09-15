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

    /**
     * 文本翻译 - 通用翻译
     * 支持自动语言检测
     * 
     * @param text 待翻译文本
     * @param sourceLang 源语言，如"en"、"zh"，auto表示自动检测
     * @param targetLang 目标语言，如"zh"、"en"
     * @return 翻译结果
     */
    public String translateText(String text, String sourceLang, String targetLang) {
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