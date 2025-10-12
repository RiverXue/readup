package com.xreadup.ai.model.dto;

import lombok.Data;
import java.util.List;
import com.xreadup.ai.model.dto.Example;

/**
 * 单词详细信息DTO
 * 用于AI阅读助手的工具调用返回
 */
@Data
public class WordInfo {
    private String word;
    private String phonetic;
    private List<String> meanings;
    private List<Example> examples;
    private List<String> synonyms;
    private String difficulty;
    private String usage;
    private String etymology;
    private String context; // 语境类型，如：日常对话/学术/科技/金融/文学等
}