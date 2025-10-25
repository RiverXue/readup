-- 数据库迁移脚本：添加试用功能支持
-- 执行时间：2024年

USE readup_ai;

-- 为subscription表添加isTrial字段
ALTER TABLE subscription ADD COLUMN is_trial BOOLEAN DEFAULT FALSE COMMENT '是否为试用订阅';

-- 添加索引以提高查询性能
CREATE INDEX idx_is_trial ON subscription(is_trial);
CREATE INDEX idx_user_trial ON subscription(user_id, is_trial);

-- 验证字段添加成功
DESCRIBE subscription;
