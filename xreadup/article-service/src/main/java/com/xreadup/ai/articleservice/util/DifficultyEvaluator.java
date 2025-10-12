package com.xreadup.ai.articleservice.util;

import org.springframework.stereotype.Component;

@Component
public class DifficultyEvaluator {
    
    private static final String[] A1_WORDS = {
        "a", "an", "the", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", 
        "be", "am", "is", "are", "was", "were", "have", "has", "had", "do", "does", "did", 
        "will", "would", "can", "could", "shall", "should", "may", "might", "must"
    };
    
    private static final String[] A2_WORDS = {
        "good", "bad", "big", "small", "happy", "sad", "old", "new", "right", "wrong", 
        "love", "like", "hate", "want", "need", "use", "work", "play", "live", "make"
    };
    
    private static final String[] B1_WORDS = {
        "important", "necessary", "possible", "different", "similar", "available", "responsible", 
        "experience", "situation", "opportunity", "relationship", "development", "information"
    };
    
    private static final String[] B2_WORDS = {
        "significant", "substantial", "considerable", "comprehensive", "sophisticated", 
        "implementation", "perspective", "consequence", "implication", "interpretation"
    };
    
    private static final String[] C1_WORDS = {
        "utilize", "demonstrate", "establish", "indicate", "represent", "substantiate", 
        "methodology", "paradigm", "hypothesis", "phenomenon", "substantiation", "correlation"
    };
    
    public String evaluateDifficulty(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "B1"; // 默认难度
        }
        
        String lowerText = text.toLowerCase();
        String[] words = lowerText.split("\\s+");
        
        int totalWords = words.length;
        if (totalWords == 0) return "B1";
        
        int c1Count = countWordsInList(words, C1_WORDS);
        int c2Count = countAdvancedWords(words);
        int b2Count = countWordsInList(words, B2_WORDS);
        int b1Count = countWordsInList(words, B1_WORDS);
        int a2Count = countWordsInList(words, A2_WORDS);
        int a1Count = countWordsInList(words, A1_WORDS);
        
        // 计算复杂度得分
        double complexityScore = (c2Count * 5.0 + c1Count * 4.0 + b2Count * 3.0 + 
                               b1Count * 2.0 + a2Count * 1.0) / totalWords;
        
        // 根据句子长度调整
        double avgSentenceLength = calculateAvgSentenceLength(text);
        complexityScore += (avgSentenceLength - 15) * 0.1;
        
        // 返回对应难度等级
        if (complexityScore >= 3.5) return "C2";
        if (complexityScore >= 2.5) return "C1";
        if (complexityScore >= 1.8) return "B2";
        if (complexityScore >= 1.2) return "B1";
        if (complexityScore >= 0.8) return "A2";
        return "A1";
    }
    
    private int countWordsInList(String[] words, String[] targetWords) {
        int count = 0;
        for (String word : words) {
            for (String target : targetWords) {
                if (word.equals(target)) {
                    count++;
                    break;
                }
            }
        }
        return count;
    }
    
    private int countAdvancedWords(String[] words) {
        // 简单实现：统计长单词和复杂单词
        int count = 0;
        for (String word : words) {
            if (word.length() >= 8) {
                count++;
            }
        }
        return count;
    }
    
    private double calculateAvgSentenceLength(String text) {
        String[] sentences = text.split("[.!?]+");
        if (sentences.length == 0) return 15;
        
        int totalWords = 0;
        for (String sentence : sentences) {
            totalWords += sentence.trim().split("\\s+").length;
        }
        
        return (double) totalWords / sentences.length;
    }
}