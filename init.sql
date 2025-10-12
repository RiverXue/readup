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
    `created_at`          DATETIME DEFAULT CURRENT_TIMESTAMP,
    `identity_tag`        VARCHAR(50) COMMENT '身份标签：考研/四六级/职场/留学',
    `learning_goal_words` INT      DEFAULT 0 COMMENT '目标词汇量',
    `target_reading_time` INT      DEFAULT 0 COMMENT '每日目标阅读时长（分钟）',
    `status`              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- 管理员表（新增）
CREATE TABLE `admin_user`
(
    `user_id` BIGINT NOT NULL PRIMARY KEY COMMENT '用户ID，与user表关联',
    `role` VARCHAR(20) NOT NULL COMMENT '管理员角色，ADMIN或SUPER_ADMIN',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_role` (`role`),
    KEY `idx_created_at` (`created_at`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员用户表';

-- 插入测试管理员用户
-- 注意：这里需要先有用户数据，然后才能插入管理员数据
-- 假设用户ID为1的用户是超级管理员
INSERT INTO `admin_user` (`user_id`, `role`) VALUES (1, 'SUPER_ADMIN');


CREATE TABLE `article`
(
    `id`                BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title`             VARCHAR(200) NOT NULL COMMENT '标题',
    `content_en`        LONGTEXT     NOT NULL COMMENT '英文原文',
    `content_cn`        LONGTEXT     NOT NULL COMMENT '中文翻译',
    `difficulty`        VARCHAR(10) COMMENT '难度等级',
    `category`          VARCHAR(50) COMMENT 'AI自动分类',
    `read_count`        INT      DEFAULT 0 COMMENT '阅读次数',
    `created_at`        DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `manual_difficulty` VARCHAR(10) COMMENT '手动标注难度：A2/B1/B2/C1',
    `manual_category`   VARCHAR(50) COMMENT '手动标注分类：科技/商业/文化',
    `description`       VARCHAR(500) COMMENT '文章描述',
    `url`               VARCHAR(500) COMMENT '原文链接',
    `image`             VARCHAR(500) COMMENT '文章图片',
    `published_at`      DATETIME COMMENT '发布时间',
    `source`            VARCHAR(100) COMMENT '文章来源',
    `difficulty_level`  VARCHAR(10) COMMENT 'AI自动难度等级：A1/A2/B1/B2/C1/C2',
    `word_count`        INT      DEFAULT 0 COMMENT '单词数量',
    `like_count`        INT      DEFAULT 0 COMMENT '点赞次数',
    `is_featured`       TINYINT  DEFAULT 0 COMMENT '是否精选：0否 1是',
    `status`            VARCHAR(20) DEFAULT 'normal' COMMENT '文章状态',
    `keywords`          TEXT COMMENT '关键词',
    `summary`           TEXT COMMENT '文章摘要',
    `key_phrases`       TEXT COMMENT '关键短语',
    `readability_score` DECIMAL(5,2) COMMENT '可读性评分',
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
    `title`              VARCHAR(500) COMMENT '文章标题',
    `difficulty_level`   VARCHAR(10) COMMENT '难度等级：A1/A2/B1/B2/C1/C2',
    `keywords`           TEXT COMMENT '关键词列表（JSON格式）',
    `summary`            TEXT COMMENT '文章摘要',
    `chinese_translation` LONGTEXT COMMENT '中文翻译',
    `simplified_content` LONGTEXT COMMENT '简化内容',
    `key_phrases`        TEXT COMMENT '关键短语（JSON格式）',
    `readability_score`  DOUBLE COMMENT '可读性评分',
    `word_translations`  LONGTEXT COMMENT '选词翻译结果（JSON格式存储多个翻译结果）',
    `sentence_parse_results` LONGTEXT COMMENT '句子解析结果（JSON格式存储）',
    `quiz_questions`     LONGTEXT COMMENT '测验题列表（JSON格式存储）',
    `learning_tips`      TEXT COMMENT '个性化学习建议',
    `analysis_metadata`  TEXT COMMENT '分析元数据（JSON格式存储）',
    `last_analysis_type` VARCHAR(50) COMMENT '最后一次分析类型',
    `analysis_category`  ENUM('article', 'sentence') DEFAULT 'article' COMMENT '分析类别：article=文章分析，sentence=句子分析',
    `source_article_id`  BIGINT NULL COMMENT '来源文章ID（句子解析时使用）',
    `sentence_content`   TEXT NULL COMMENT '句子内容（句子解析时使用）',
    `created_at`         TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_article_id` (`article_id`),
    INDEX `idx_difficulty_level` (`difficulty_level`),
    INDEX `idx_analysis_category` (`analysis_category`),
    INDEX `idx_source_article_id` (`source_article_id`),
    INDEX `idx_sentence_content` (`sentence_content`(100))
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT ='AI文章分析结果表';

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

-- 验证表结构
DESCRIBE ai_analysis;

use readup_ai;
-- 创建订阅表
CREATE TABLE subscription (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '订阅ID',
                              user_id BIGINT NOT NULL COMMENT '用户ID',
                              plan_type VARCHAR(20) NOT NULL COMMENT '套餐类型: BASIC, PRO, ENTERPRISE',
                              price DECIMAL(10,2) NOT NULL COMMENT '价格',
                              currency VARCHAR(3) NOT NULL DEFAULT 'USD' COMMENT '货币',
                              status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE, CANCELLED, EXPIRED',
                              start_date DATETIME NOT NULL COMMENT '开始日期',
                              end_date DATETIME NOT NULL COMMENT '结束日期',
                              payment_method VARCHAR(20) COMMENT '支付方式: ALIPAY, WECHAT, CREDIT_CARD',
                              transaction_id VARCHAR(100) COMMENT '支付平台交易号',
                              auto_renew BOOLEAN DEFAULT TRUE COMMENT '是否自动续费',
                              max_articles_per_month INT COMMENT '每月最大文章数',
                              max_words_per_article INT COMMENT '每篇文章最大字数',
                              ai_features_enabled BOOLEAN DEFAULT FALSE COMMENT '是否启用AI功能',
                              priority_support BOOLEAN DEFAULT FALSE COMMENT '是否优先支持',
                              created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                              deleted INT DEFAULT 0 COMMENT '逻辑删除标记',
                              INDEX idx_user_id (user_id),
                              INDEX idx_status (status),
                              INDEX idx_end_date (end_date),
                              FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT='用户订阅表';

-- 插入初始超级管理员用户
-- 注意：实际部署时，请替换为真实的用户ID
INSERT INTO admin_user (user_id, role) VALUES (17, 'SUPER_ADMIN');

-- 用户词汇统计视图（如果存在）
-- CREATE VIEW user_vocabulary_stats AS 
-- SELECT user_id, COUNT(*) as total_words, 
--        COUNT(CASE WHEN review_status = 'mastered' THEN 1 END) as mastered_words
-- FROM word GROUP BY user_id;

-- 管理员用户备份表（如果存在）
-- CREATE TABLE `admin_user_backup` (
--     `user_id` BIGINT NOT NULL,
--     `role` VARCHAR(20) NOT NULL,
--     `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统配置表
CREATE TABLE `system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `config_key` VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` ENUM('STRING', 'NUMBER', 'BOOLEAN', 'JSON') NOT NULL DEFAULT 'STRING' COMMENT '配置类型',
    `description` VARCHAR(255) COMMENT '配置描述',
    `category` VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT '配置分类',
    `is_system` BOOLEAN DEFAULT FALSE COMMENT '是否为系统配置（不可删除）',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_category` (`category`),
    INDEX `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 插入默认系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_type`, `description`, `category`, `is_system`) VALUES
-- 系统维护设置
('maintenance.enabled', 'false', 'BOOLEAN', '系统维护模式', 'MAINTENANCE', TRUE),
('maintenance.message', '系统正在维护中，请稍后再试', 'STRING', '维护提示信息', 'MAINTENANCE', FALSE),

-- 功能开关设置
('features.ai_translation', 'true', 'BOOLEAN', 'AI翻译功能', 'FEATURES', TRUE),
('features.vocabulary', 'true', 'BOOLEAN', '词汇功能', 'FEATURES', TRUE),
('features.subscription', 'true', 'BOOLEAN', '订阅功能', 'FEATURES', TRUE),

-- 业务限制设置
('limits.max_articles_per_user', '1000', 'NUMBER', '用户最大文章数', 'LIMITS', TRUE),
('limits.max_words_per_article', '10000', 'NUMBER', '单篇文章最大字数', 'LIMITS', TRUE),
('limits.max_vocabulary_per_user', '5000', 'NUMBER', '用户最大词汇量', 'LIMITS', TRUE);
