-- 文章服务数据库表结构
CREATE DATABASE IF NOT EXISTS xreadup_article DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE xreadup_article;

-- 文章表
CREATE TABLE IF NOT EXISTS `article` (
    `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `title` varchar(500) NOT NULL COMMENT '文章标题',
    `content_en` longtext COMMENT '英文原文',
    `content_cn` longtext COMMENT '中文译文',
    `description` text COMMENT '文章描述',
    `url` varchar(1000) COMMENT '原文链接',
    `image` varchar(1000) COMMENT '文章图片',
    `published_at` datetime COMMENT '发布时间',
    `source` varchar(200) COMMENT '文章来源',
    `category` varchar(100) COMMENT '文章分类',
    `difficulty_level` varchar(10) COMMENT '系统评估难度(A1,A2,B1,B2,C1,C2)',
    `manual_difficulty` varchar(10) COMMENT '用户手动标注难度',
    `word_count` int DEFAULT 0 COMMENT '文章字数',
    `read_count` int DEFAULT 0 COMMENT '阅读次数',
    `like_count` int DEFAULT 0 COMMENT '点赞次数',
    `is_featured` tinyint(1) DEFAULT 0 COMMENT '是否推荐',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_category` (`category`),
    INDEX `idx_difficulty` (`difficulty_level`),
    INDEX `idx_published_at` (`published_at`),
    INDEX `idx_source` (`source`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 插入示例数据
INSERT INTO `article` (`title`, `content_en`, `content_cn`, `description`, `url`, `image`, `published_at`, `source`, `category`, `difficulty_level`, `word_count`, `read_count`, `like_count`, `is_featured`) VALUES
('The Future of AI in Education', 'Artificial intelligence is revolutionizing the way we learn and teach...', '人工智能正在彻底改变我们的学习和教学方式...', '探索AI如何改变教育领域的深度文章', 'https://example.com/ai-education', 'https://example.com/ai-education.jpg', '2024-01-15 10:00:00', 'TechNews', 'technology', 'B2', 1200, 156, 23, 1),
('Climate Change and Global Economy', 'The impact of climate change on global economic systems...', '气候变化对全球经济体系的影响...', '分析气候变化对全球经济影响的权威报道', 'https://example.com/climate-economy', 'https://example.com/climate.jpg', '2024-01-14 15:30:00', 'BBC News', 'environment', 'C1', 1800, 89, 15, 0),
('Space Exploration Breakthrough', 'Scientists have made a groundbreaking discovery in space exploration...', '科学家在太空探索方面取得了突破性发现...', '太空探索领域的最新突破性进展', 'https://example.com/space-discovery', 'https://example.com/space.jpg', '2024-01-13 09:15:00', 'NASA News', 'science', 'B1', 950, 234, 45, 1),
('The Rise of Remote Work Culture', 'Remote work has become the new normal for many professionals...', '远程工作已成为许多专业人士的新常态...', '远程工作文化的兴起及其对未来工作的影响', 'https://example.com/remote-work', 'https://example.com/remote.jpg', '2024-01-12 14:20:00', 'Forbes', 'business', 'B2', 1100, 178, 31, 0);