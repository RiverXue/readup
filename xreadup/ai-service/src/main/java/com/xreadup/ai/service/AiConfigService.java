package com.xreadup.ai.service;

import com.xreadup.common.client.SystemConfigClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * AI服务配置管理
 * 负责检查AI相关的系统配置
 * 
 * @author XReadUp
 * @since 2025-10-12
 */
@Service
@Slf4j
public class AiConfigService {
    
    @Autowired(required = false)
    private SystemConfigClient systemConfigClient;
    
    /**
     * 检查AI文章分析功能是否启用
     * 
     * @return 是否启用
     */
    public boolean isAiArticleAnalysisEnabled() {
        return checkFeatureEnabled("features.ai_article_analysis", true);
    }
    
    /**
     * 检查AI句子解析功能是否启用
     * 
     * @return 是否启用
     */
    public boolean isAiSentenceParsingEnabled() {
        return checkFeatureEnabled("features.ai_sentence_parsing", true);
    }
    
    /**
     * 检查AI测验生成功能是否启用
     * 
     * @return 是否启用
     */
    public boolean isAiQuizGenerationEnabled() {
        return checkFeatureEnabled("features.ai_quiz_generation", true);
    }
    
    /**
     * 检查AI摘要生成功能是否启用
     * 
     * @return 是否启用
     */
    public boolean isAiSummaryGenerationEnabled() {
        return checkFeatureEnabled("features.ai_summary_generation", true);
    }
    
    /**
     * 检查AI单词翻译功能是否启用
     * 
     * @return 是否启用
     */
    public boolean isAiWordTranslationEnabled() {
        return checkFeatureEnabled("features.ai_word_translation", true);
    }
    
    /**
     * 检查系统是否处于维护模式
     * 
     * @return 是否维护模式
     */
    public boolean isMaintenanceMode() {
        try {
            if (systemConfigClient != null) {
                Object status = systemConfigClient.getMaintenanceStatus();
                if (status instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> statusMap = (java.util.Map<String, Object>) status;
                    Boolean maintenanceMode = (Boolean) statusMap.get("maintenanceMode");
                    return maintenanceMode != null ? maintenanceMode : false;
                }
            }
        } catch (Exception e) {
            log.warn("获取维护模式配置失败，使用默认值: false", e);
        }
        return false; // 默认非维护模式
    }
    
    /**
     * 检查AI服务是否可用
     * 综合检查维护模式和功能开关
     * 
     * @return 是否可用
     */
    public boolean isAiServiceAvailable() {
        if (isMaintenanceMode()) {
            log.warn("系统处于维护模式，AI服务不可用");
            return false;
        }
        
        if (!isAiArticleAnalysisEnabled()) {
            log.warn("AI文章分析功能已禁用");
            return false;
        }
        
        return true;
    }
    
    /**
     * 检查特定功能是否启用
     * 
     * @param featureKey 功能键
     * @param defaultValue 默认值
     * @return 是否启用
     */
    private boolean checkFeatureEnabled(String featureKey, boolean defaultValue) {
        try {
            if (systemConfigClient != null) {
                Boolean enabled = systemConfigClient.isFeatureEnabled(featureKey);
                log.debug("功能 {} 状态: {}", featureKey, enabled);
                return enabled != null ? enabled : defaultValue;
            }
        } catch (Exception e) {
            log.warn("获取功能 {} 配置失败，使用默认值: {}", featureKey, defaultValue, e);
        }
        return defaultValue;
    }
}
