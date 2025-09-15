package com.xreadup.ai.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 选词翻译响应DTO
 * 
 * @author XReadUp Team
 * @version 1.0.0
 */
@Data
@Schema(description = "选词翻译响应")
public class WordTranslationResponse {
    
    @Schema(description = "英文单词", example = "technology")
    private String word;
    
    @Schema(description = "中文翻译", example = "技术")
    private String chinese;
    
    @Schema(description = "音标", example = "/tekˈnɒlədʒi/")
    private String phonetic;
    
    @Schema(description = "词性", example = "名词")
    private String partOfSpeech;
    
    @Schema(description = "英文释义", example = "the application of scientific knowledge for practical purposes")
    private String definition;
    
    @Schema(description = "中文释义", example = "科学知识在实际目的中的应用")
    private String chineseDefinition;
    
    @Schema(description = "例句", example = "Modern technology has revolutionized communication.")
    private String example;
    
    @Schema(description = "例句中文翻译", example = "现代技术彻底改变了通讯方式。")
    private String exampleChinese;
    
    @Schema(description = "同义词列表", example = "[\"technique\", \"method\", \"system\"]")
    private List<String> synonyms;
    
    @Schema(description = "难度等级", example = "B2")
    private String difficultyLevel;
}