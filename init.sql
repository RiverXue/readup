CREATE DATABASE IF NOT EXISTS readup_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE readup_ai;

-- 用户表（新增身份标签+学习目标）
CREATE TABLE `user`
(
    `id`                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username`            VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    `password`            VARCHAR(100) NOT NULL COMMENT '密码（BCrypt加密）',
    `phone`               VARCHAR(20) COMMENT '手机号',
    `interest_tag`        VARCHAR(50) COMMENT '兴趣标签：tech/business/culture',
    `identity_tag`        VARCHAR(50) COMMENT '身份标签：考研/四六级/职场/留学',
    `learning_goal_words` INT      DEFAULT 0 COMMENT '目标词汇量',
    `target_reading_time` INT      DEFAULT 0 COMMENT '每日目标阅读时长（分钟）',
    `created_at`          DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `article`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title`             VARCHAR(200) NOT NULL COMMENT '标题',
    `content_en`        LONGTEXT     NOT NULL COMMENT '英文原文',
    `content_cn`        LONGTEXT     NOT NULL COMMENT '中文翻译',
    `description`       VARCHAR(500) COMMENT '文章描述',
    `url`               VARCHAR(500) COMMENT '原文链接',
    `image`             VARCHAR(500) COMMENT '文章图片',
    `published_at`    DATETIME COMMENT '发布时间',
    `source`            VARCHAR(100) COMMENT '文章来源',
    `category`          VARCHAR(50) COMMENT 'AI自动分类',
    `difficulty_level`  VARCHAR(10) COMMENT 'AI自动难度等级：A1/A2/B1/B2/C1/C2',
    `manual_difficulty` VARCHAR(10) COMMENT '手动标注难度：A2/B1/B2/C1',
    `manual_category`   VARCHAR(50) COMMENT '手动标注分类：科技/商业/文化',
    `word_count`        INT      DEFAULT 0 COMMENT '单词数量',
    `read_count`        INT      DEFAULT 0 COMMENT '阅读次数',
    `like_count`        INT      DEFAULT 0 COMMENT '点赞次数',
    `is_featured`       TINYINT  DEFAULT 0 COMMENT '是否精选：0否 1是',
    `create_time`       DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           TINYINT  DEFAULT 0 COMMENT '是否删除：0否 1是'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- AI分析表（新增）
CREATE TABLE `ai_analysis`
(
    `id`                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    `article_id`         BIGINT       NOT NULL COMMENT '文章ID',
    `title`              VARCHAR(200) COMMENT '文章标题',
    `difficulty_level`   VARCHAR(10) COMMENT '难度等级：A1/A2/B1/B2/C1/C2',
    `keywords`           JSON COMMENT '关键词列表（JSON格式）',
    `summary`            TEXT COMMENT '文章摘要',
    `chinese_translation` LONGTEXT COMMENT '中文翻译',
    `simplified_content` LONGTEXT COMMENT '简化内容',
    `key_phrases`        JSON COMMENT '关键短语（JSON格式）',
    `readability_score`  DECIMAL(5,2) COMMENT '可读性评分',
    `word_translations`  LONGTEXT COMMENT '选词翻译结果（JSON格式存储多个翻译结果）',
    `created_at`         DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY `uk_article_id` (`article_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='AI文章分析结果表';

CREATE TABLE `word`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `word`              VARCHAR(100) NOT NULL COMMENT '单词',
    `meaning`           VARCHAR(500) COMMENT '释义',
    `example`           TEXT COMMENT '例句',
    `context`           VARCHAR(100) COMMENT '上下文（如：金融/科技/地理）',
    `source`            VARCHAR(50) COMMENT '来源：local/ai',
    `source_article_id` BIGINT COMMENT '来源文章ID',
    `review_status`     VARCHAR(20) DEFAULT 'new' COMMENT '复习状态：new/learning/mastered',
    `last_reviewed_at`  DATETIME     NULL COMMENT '上次复习时间',
    `next_review_at`    DATETIME     NULL COMMENT '下次复习时间',
    `added_at`          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    `phonetic`          VARCHAR(50) COMMENT '音标',
    `difficulty`        VARCHAR(10) COMMENT '难度等级：A1/A2/B1/B2/C1/C2',
    UNIQUE KEY `uk_user_word_context` (`user_id`, `word`, `context`) -- 同一单词不同上下文可存多条
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `reading_log`
(
    `id`            BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`       BIGINT NOT NULL,
    `article_id`    BIGINT NOT NULL,
    `read_time_sec` INT COMMENT '阅读时长（秒）',
    `finished_at`   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `ai_cache`
(
    `id`          BIGINT PRIMARY KEY AUTO_INCREMENT,
    `input_text`  TEXT COMMENT '输入原文',
    `ai_type`     VARCHAR(50) COMMENT '类型：summary/parse/question',
    `output_text` TEXT COMMENT 'AI输出结果',
    `created_at`  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `review_schedule`
(
    `id`               BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`          BIGINT   NOT NULL,
    `word_id`          BIGINT   NOT NULL,
    `next_review_time` DATETIME NOT NULL COMMENT '下次复习时间',
    `review_stage`     INT      DEFAULT 1 COMMENT '复习阶段：1/2/4/7/15（天）',
    `created_at`       DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_next_review` (`user_id`, `next_review_time`),
    UNIQUE KEY `uk_user_word` (`user_id`, `word_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户单词复习计划表';


CREATE TABLE `reading_streak`
(
    `id`             BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`        BIGINT NOT NULL,
    `streak_days`    INT      DEFAULT 0 COMMENT '连续阅读天数',
    `last_read_date` DATE COMMENT '最后阅读日期',
    `updated_at`     DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户阅读打卡记录';

-- 添加句子解析结果字段
ALTER TABLE ai_analysis ADD COLUMN sentence_parse_results LONGTEXT COMMENT '句子解析结果（JSON格式存储）';

-- 添加测验题字段
ALTER TABLE ai_analysis ADD COLUMN quiz_questions LONGTEXT COMMENT '测验题列表（JSON格式存储）';

-- 添加学习建议字段
ALTER TABLE ai_analysis ADD COLUMN learning_tips TEXT COMMENT '个性化学习建议';

-- 添加分析元数据字段
ALTER TABLE ai_analysis ADD COLUMN analysis_metadata TEXT COMMENT '分析元数据（JSON格式存储）';

-- 添加最后分析类型字段
ALTER TABLE ai_analysis ADD COLUMN last_analysis_type VARCHAR(50) COMMENT '最后一次分析类型';

-- 验证表结构
DESCRIBE ai_analysis;