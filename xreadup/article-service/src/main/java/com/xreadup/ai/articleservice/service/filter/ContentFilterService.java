package com.xreadup.ai.articleservice.service.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * 文章服务内容过滤服务
 * 独立于其他服务，符合微服务架构原则
 */
@Service
@Slf4j
public class ContentFilterService {

    // 英文敏感词库 - 针对英文文章（记录但不拦截，因为新闻中常见）
    private static final Set<String> ENGLISH_SENSITIVE_WORDS = Set.of(
            "terrorism", "bomb", "explosion", "massacre", "genocide",
            "violence", "murder", "kill", "death", "suicide", "gun", "weapon",
            "porn", "pornography", "sex", "sexual", "nude", "naked",
            "drug", "cocaine", "heroin", "marijuana", "addiction",
            "gambling", "casino", "bet", "poker", "lottery",
            "hate", "racism", "discrimination", "abuse", "torture"
    );

    // 高风险词汇 - 直接拦截（仅限明显宣传或极端内容）
    private static final Set<String> HIGH_RISK_WORDS = Set.of(
            "nazi", "hitler", "fascism", "extremism",
            "法轮功", "六四", "天安门", "达赖", "台独", "港独", "疆独"
    );

    /**
     * 过滤英文文章内容
     * 针对新闻内容优化，允许正常的新闻报道通过
     */
    public boolean isArticleSafe(String content) {
        if (content == null || content.trim().isEmpty()) {
            log.debug("📝 文章内容为空，跳过过滤检查");
            return true;
        }

        String lowerContent = content.toLowerCase();
        log.debug("🔍 开始检查文章内容，长度: {} 字符", content.length());

        // 检查高风险词汇 - 直接拦截（仅限明显宣传或极端内容）
        for (String word : HIGH_RISK_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                log.warn("🚨 文章包含高风险违禁词: '{}' - 直接拦截", word);
                log.warn("📄 违禁词上下文: {}", getWordContext(content, word));
                return false;
            }
        }

        // 检查一般敏感词 - 记录但不拦截（新闻中常见，允许通过）
        int sensitiveWordCount = 0;
        for (String word : ENGLISH_SENSITIVE_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                sensitiveWordCount++;
                log.info("⚠️ 文章包含敏感词汇: '{}' (新闻内容，已记录，允许通过)", word);
                log.debug("📄 敏感词上下文: {}", getWordContext(content, word));
            }
        }

        // 特殊处理：如果文章包含大量敏感词汇，可能是极端内容
        if (sensitiveWordCount > 5) {
            log.warn("⚠️ 文章包含过多敏感词汇 ({}个)，可能是极端内容，但仍允许通过", sensitiveWordCount);
        }

        if (sensitiveWordCount > 0) {
            log.info("📊 文章包含 {} 个敏感词汇，已记录但允许通过", sensitiveWordCount);
        } else {
            log.debug("✅ 文章内容检查通过，未发现违禁词汇");
        }

        return true;
    }

    /**
     * 获取词汇在内容中的上下文
     */
    private String getWordContext(String content, String word) {
        try {
            String lowerContent = content.toLowerCase();
            String lowerWord = word.toLowerCase();
            int index = lowerContent.indexOf(lowerWord);

            if (index == -1) {
                return "未找到上下文";
            }

            int start = Math.max(0, index - 50);
            int end = Math.min(content.length(), index + word.length() + 50);
            String context = content.substring(start, end);

            // 高亮显示关键词
            return context.replaceAll("(?i)" + word, "【" + word + "】");
        } catch (Exception e) {
            return "获取上下文失败: " + e.getMessage();
        }
    }
}
