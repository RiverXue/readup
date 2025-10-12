package com.xreadup.ai.userservice.entity;

/**
 * 用户等级 - 毕设简化版（仅用于演示，不限制功能）
 */
public enum UserTier {
    FREE("免费用户", 3, 500, false),
    BASIC("基础会员", 10, 1000, false),
    PRO("专业会员", 50, 5000, true),
    ENTERPRISE("企业会员", 200, 20000, true);

    private final String displayName;
    private final int maxArticlesPerMonth;
    private final int maxWordsPerArticle;
    private final boolean hasAIFeatures;

    UserTier(String displayName, int maxArticlesPerMonth, int maxWordsPerArticle, boolean hasAIFeatures) {
        this.displayName = displayName;
        this.maxArticlesPerMonth = maxArticlesPerMonth;
        this.maxWordsPerArticle = maxWordsPerArticle;
        this.hasAIFeatures = hasAIFeatures;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getMaxArticlesPerMonth() {
        return maxArticlesPerMonth;
    }

    public int getMaxWordsPerArticle() {
        return maxWordsPerArticle;
    }

    public boolean hasAIFeatures() {
        return hasAIFeatures;
    }
}