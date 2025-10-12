package com.xreadup.ai.report.dto;

import java.util.List;

/**
 * 用户学习仪表盘数据DTO
 */
public class DashboardData {
    private VocabularyGrowthData vocabularyData;
    private ReadingTimeData readingData;
    private List<ReviewWordDto> todayReviews;
    private Integer currentStreak;
    private Integer totalDays;
    private Double reviewSuccessRate;
    private List<String> achievements;

    public DashboardData() {}

    public DashboardData(VocabularyGrowthData vocabularyData, ReadingTimeData readingData, 
                        List<ReviewWordDto> todayReviews, Integer currentStreak, 
                        Integer totalDays, Double reviewSuccessRate) {
        this.vocabularyData = vocabularyData;
        this.readingData = readingData;
        this.todayReviews = todayReviews;
        this.currentStreak = currentStreak;
        this.totalDays = totalDays;
        this.reviewSuccessRate = reviewSuccessRate;
    }

    // Getters and Setters
    public VocabularyGrowthData getVocabularyData() {
        return vocabularyData;
    }

    public void setVocabularyData(VocabularyGrowthData vocabularyData) {
        this.vocabularyData = vocabularyData;
    }

    public ReadingTimeData getReadingData() {
        return readingData;
    }

    public void setReadingData(ReadingTimeData readingData) {
        this.readingData = readingData;
    }

    public List<ReviewWordDto> getTodayReviews() {
        return todayReviews;
    }

    public void setTodayReviews(List<ReviewWordDto> todayReviews) {
        this.todayReviews = todayReviews;
    }

    public Integer getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(Integer currentStreak) {
        this.currentStreak = currentStreak;
    }

    public Integer getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(Integer totalDays) {
        this.totalDays = totalDays;
    }

    public Double getReviewSuccessRate() {
        return reviewSuccessRate;
    }

    public void setReviewSuccessRate(Double reviewSuccessRate) {
        this.reviewSuccessRate = reviewSuccessRate;
    }

    public List<String> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<String> achievements) {
        this.achievements = achievements;
    }

    @Override
    public String toString() {
        return "DashboardData{" +
                "vocabularyData=" + vocabularyData +
                ", readingData=" + readingData +
                ", todayReviews=" + todayReviews +
                ", currentStreak=" + currentStreak +
                ", totalDays=" + totalDays +
                ", reviewSuccessRate=" + reviewSuccessRate +
                ", achievements=" + achievements +
                '}';
    }
}