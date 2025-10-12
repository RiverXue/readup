package com.xreadup.ai.report.dto;

import java.util.List;

/**
 * 词汇增长曲线数据DTO
 */
public class VocabularyGrowthData {
    private List<String> dates;
    private List<Integer> counts;
    private Integer totalWords;
    private Integer weeklyNewWords;
    private Double dailyAverage;

    public VocabularyGrowthData() {}

    public VocabularyGrowthData(List<String> dates, List<Integer> counts, Integer totalWords, Integer weeklyNewWords, Double dailyAverage) {
        this.dates = dates;
        this.counts = counts;
        this.totalWords = totalWords;
        this.weeklyNewWords = weeklyNewWords;
        this.dailyAverage = dailyAverage;
    }

    // Getters and Setters
    public List<String> getDates() {
        return dates;
    }

    public void setDates(List<String> dates) {
        this.dates = dates;
    }

    public List<Integer> getCounts() {
        return counts;
    }

    public void setCounts(List<Integer> counts) {
        this.counts = counts;
    }

    public Integer getTotalWords() {
        return totalWords;
    }

    public void setTotalWords(Integer totalWords) {
        this.totalWords = totalWords;
    }

    public Integer getWeeklyNewWords() {
        return weeklyNewWords;
    }

    public void setWeeklyNewWords(Integer weeklyNewWords) {
        this.weeklyNewWords = weeklyNewWords;
    }

    public Double getDailyAverage() {
        return dailyAverage;
    }

    public void setDailyAverage(Double dailyAverage) {
        this.dailyAverage = dailyAverage;
    }

    @Override
    public String toString() {
        return "VocabularyGrowthData{" +
                "dates=" + dates +
                ", counts=" + counts +
                ", totalWords=" + totalWords +
                ", weeklyNewWords=" + weeklyNewWords +
                ", dailyAverage=" + dailyAverage +
                '}';
    }
}