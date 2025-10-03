package com.xreadup.ai.report.dto;

/**
 * 待复习单词DTO
 */
public class ReviewWordDto {
    private Long wordId;
    private String word;
    private String meaning;
    private String example;
    private String difficulty;
    private String dueDate;
    private String nextReviewTime; // 下次复习时间
    private int progress; // 复习进度百分比
    private String formattedReviewTime; // 格式化的复习时间

    public ReviewWordDto() {}

    public ReviewWordDto(Long wordId, String word, String meaning, String example, String difficulty, String dueDate) {
        this.wordId = wordId;
        this.word = word;
        this.meaning = meaning;
        this.example = example;
        this.difficulty = difficulty;
        this.dueDate = dueDate;
    }

    // Getters and Setters
    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
    
    public String getNextReviewTime() {
        return nextReviewTime;
    }
    
    public void setNextReviewTime(String nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }
    
    public int getProgress() {
        return progress;
    }
    
    public void setProgress(int progress) {
        this.progress = progress;
    }
    
    public String getFormattedReviewTime() {
        return formattedReviewTime;
    }
    
    public void setFormattedReviewTime(String formattedReviewTime) {
        this.formattedReviewTime = formattedReviewTime;
    }

    @Override
    public String toString() {
        return "ReviewWordDto{" +
                "wordId=" + wordId +
                ", word='" + word + '\'' +
                ", meaning='" + meaning + '\'' +
                ", example='" + example + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", nextReviewTime='" + nextReviewTime + '\'' +
                ", progress=" + progress +
                ", formattedReviewTime='" + formattedReviewTime + '\'' +
                '}';
    }
}