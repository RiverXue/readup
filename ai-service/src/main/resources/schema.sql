-- AI分析结果表
CREATE TABLE IF NOT EXISTS ai_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    title VARCHAR(500),
    difficulty_level VARCHAR(10),
    keywords TEXT,
    summary TEXT,
    chinese_translation LONGTEXT,
    simplified_content LONGTEXT,
    key_phrases TEXT,
    readability_score DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_article_id (article_id),
    INDEX idx_difficulty_level (difficulty_level)
);