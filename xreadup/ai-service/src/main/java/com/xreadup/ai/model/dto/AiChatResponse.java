package com.xreadup.ai.model.dto;

import lombok.Data;
import java.util.List;

/**
 * AI对话响应DTO
 */
@Data
public class AiChatResponse {
    private String answer;
    private List<WordInfo> referencedWords;
    private String followUpQuestion;
    private String difficulty;
}