package com.xreadup.ai.service.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * AI服务内容过滤服务
 * 独立于其他服务，符合微服务架构原则
 */
@Service
@Slf4j
public class ContentFilterService {

    // 英文违禁词库 - 针对英文文章
    private static final Set<String> ENGLISH_BAD_WORDS = Set.of(
        "terrorism", "bomb", "explosion", "massacre", "genocide",
        "violence", "murder", "kill", "death", "suicide", "gun", "weapon",
        "porn", "pornography", "sex", "sexual", "nude", "naked",
        "drug", "cocaine", "heroin", "marijuana", "addiction",
        "gambling", "casino", "bet", "poker", "lottery",
        "hate", "racism", "discrimination", "abuse", "torture",
        "nazi", "hitler", "fascism", "extremism"
    );

    // 中文违禁词库 - 针对AI对话
    private static final Set<String> CHINESE_BAD_WORDS = Set.of(
        "法轮功", "六四", "天安门", "达赖", "台独", "港独", "疆独",
        "恐怖主义", "爆炸", "枪击", "暴力", "色情", "成人", "性爱",
        "赌博", "毒品", "自杀", "邪教", "传销", "诈骗"
    );

    // 高风险词汇 - 直接拦截
    private static final Set<String> HIGH_RISK_WORDS = Set.of(
        "terrorism", "bomb", "explosion", "massacre", "genocide",
        "nazi", "hitler", "fascism", "extremism",
        "法轮功", "六四", "天安门", "达赖", "台独", "港独", "疆独"
    );

    /**
     * 过滤AI对话内容
     */
    public boolean isChatSafe(String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }

        String lowerContent = content.toLowerCase();

        // 检查高风险词汇 - 直接拦截
        for (String word : HIGH_RISK_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                log.warn("AI对话包含高风险违禁词: {}", word);
                return false;
            }
        }

        // 检查一般违禁词 - 记录但不拦截
        for (String word : CHINESE_BAD_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                log.info("AI对话包含敏感词汇: {} (已记录)", word);
                // 可以选择是否拦截，这里选择记录但不拦截
            }
        }

        return true;
    }
}
