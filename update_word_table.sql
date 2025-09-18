-- 为已有的word表添加phonetic和difficulty字段的SQL语句
USE readup_ai;

-- 添加音标字段
ALTER TABLE `word`
ADD COLUMN `phonetic` VARCHAR(50) COMMENT '音标' AFTER `added_at`;

-- 添加难度等级字段
ALTER TABLE `word`
ADD COLUMN `difficulty` VARCHAR(10) COMMENT '难度等级：A1/A2/B1/B2/C1/C2' AFTER `phonetic`;