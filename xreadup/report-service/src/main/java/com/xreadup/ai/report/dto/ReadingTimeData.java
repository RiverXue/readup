package com.xreadup.ai.report.dto;

import java.util.List;

/**
 * 阅读时长统计数据DTO
 */
public class ReadingTimeData {
    private Integer todayMinutes;
    private Integer weeklyAverageMinutes;
    private Integer totalMinutes;
    private Integer totalArticles;
    private List<DailyReading> dailyReadings;
    private List<DifficultyStats> difficultyStats;

    public ReadingTimeData() {}

    public ReadingTimeData(Integer todayMinutes, Integer weeklyAverageMinutes, Integer totalMinutes, Integer totalArticles) {
        this.todayMinutes = todayMinutes;
        this.weeklyAverageMinutes = weeklyAverageMinutes;
        this.totalMinutes = totalMinutes;
        this.totalArticles = totalArticles;
    }

    // Getters and Setters
    public Integer getTodayMinutes() {
        return todayMinutes;
    }

    public void setTodayMinutes(Integer todayMinutes) {
        this.todayMinutes = todayMinutes;
    }

    public Integer getWeeklyAverageMinutes() {
        return weeklyAverageMinutes;
    }

    public void setWeeklyAverageMinutes(Integer weeklyAverageMinutes) {
        this.weeklyAverageMinutes = weeklyAverageMinutes;
    }

    public Integer getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(Integer totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    public Integer getTotalArticles() {
        return totalArticles;
    }

    public void setTotalArticles(Integer totalArticles) {
        this.totalArticles = totalArticles;
    }

    public List<DailyReading> getDailyReadings() {
        return dailyReadings;
    }

    public void setDailyReadings(List<DailyReading> dailyReadings) {
        this.dailyReadings = dailyReadings;
    }

    public List<DifficultyStats> getDifficultyStats() {
        return difficultyStats;
    }

    public void setDifficultyStats(List<DifficultyStats> difficultyStats) {
        this.difficultyStats = difficultyStats;
    }

    @Override
    public String toString() {
        return "ReadingTimeData{" +
                "todayMinutes=" + todayMinutes +
                ", weeklyAverageMinutes=" + weeklyAverageMinutes +
                ", totalMinutes=" + totalMinutes +
                ", totalArticles=" + totalArticles +
                '}';
    }

    /**
     * 每日阅读数据
     */
    public static class DailyReading {
        private String date;
        private Integer minutes;
        private Integer articles;

        public DailyReading() {}

        public DailyReading(String date, Integer minutes, Integer articles) {
            this.date = date;
            this.minutes = minutes;
            this.articles = articles;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public Integer getMinutes() {
            return minutes;
        }

        public void setMinutes(Integer minutes) {
            this.minutes = minutes;
        }

        public Integer getArticles() {
            return articles;
        }

        public void setArticles(Integer articles) {
            this.articles = articles;
        }
    }

    /**
     * 难度统计
     */
    public static class DifficultyStats {
        private String difficulty;
        private Integer count;
        private Integer totalMinutes;

        public DifficultyStats() {}

        public DifficultyStats(String difficulty, Integer count, Integer totalMinutes) {
            this.difficulty = difficulty;
            this.count = count;
            this.totalMinutes = totalMinutes;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public Integer getCount() {
            return count;
        }

        public void setCount(Integer count) {
            this.count = count;
        }

        public Integer getTotalMinutes() {
            return totalMinutes;
        }

        public void setTotalMinutes(Integer totalMinutes) {
            this.totalMinutes = totalMinutes;
        }
    }
}