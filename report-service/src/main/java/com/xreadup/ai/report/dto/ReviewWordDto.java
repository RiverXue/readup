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

    @Override
    public String toString() {
        return "ReviewWordDto{" +
                "wordId=" + wordId +
                ", word='" + word + '\'' +
                ", meaning='" + meaning + '\'' +
                ", example='" + example + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", dueDate='" + dueDate + '\'' +
                '}';
    }
}