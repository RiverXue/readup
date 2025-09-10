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
    `en_content`        LONGTEXT     NOT NULL COMMENT '英文原文',
    `cn_content`        LONGTEXT     NOT NULL COMMENT '中文翻译',
    `difficulty`        VARCHAR(10) COMMENT 'AI自动难度',
    `category`          VARCHAR(50) COMMENT 'AI自动分类',
    `manual_difficulty` VARCHAR(10) COMMENT '手动标注难度：A2/B1/B2/C1',
    `manual_category`   VARCHAR(50) COMMENT '手动标注分类：科技/商业/文化',
    `read_count`        INT      DEFAULT 0 COMMENT '阅读次数',
    `created_at`        DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


CREATE TABLE `word`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `word`              VARCHAR(100) NOT NULL COMMENT '单词',
    `meaning`           VARCHAR(500) COMMENT '释义',
    `source_article_id` BIGINT COMMENT '来源文章ID',
    `review_status`     VARCHAR(20) DEFAULT 'new' COMMENT '复习状态：new/learning/mastered',
    `last_reviewed_at`  DATETIME     NULL COMMENT '上次复习时间',
    `next_review_at`    DATETIME     NULL COMMENT '下次复习时间',
    `added_at`          DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_word` (`user_id`, `word`)
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