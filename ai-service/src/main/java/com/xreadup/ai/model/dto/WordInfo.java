package com.xreadup.ai.model.dto;

import lombok.Data;
import java.util.List;

/**
 * 单词详细信息DTO
 * 用于AI阅读助手的工具调用返回
 */
@Data
public class WordInfo {
    private String word;
    private String phonetic;
    private List<String> meanings;
    private List<String> examples;
    private List<String> synonyms;
    private String difficulty;
    private String usage;
    private String etymology;
}