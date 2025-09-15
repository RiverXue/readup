-- 添加邮箱验证相关字段到用户表
ALTER TABLE t_user 
ADD COLUMN email VARCHAR(255) NOT NULL UNIQUE COMMENT '用户邮箱',
ADD COLUMN email_verified BOOLEAN DEFAULT FALSE COMMENT '邮箱验证状态';

-- 创建订阅表
CREATE TABLE t_subscription (
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
    FOREIGN KEY (user_id) REFERENCES t_user(id) ON DELETE CASCADE
) COMMENT='用户订阅表';

-- 创建邮箱验证日志表（可选，用于跟踪验证状态）
CREATE TABLE t_email_verification_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '验证日志ID',
    email VARCHAR(255) NOT NULL COMMENT '邮箱地址',
    verification_code VARCHAR(6) COMMENT '验证码',
    sent_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    verified_at DATETIME COMMENT '验证时间',
    expired BOOLEAN DEFAULT FALSE COMMENT '是否过期',
    INDEX idx_email (email),
    INDEX idx_sent_at (sent_at)
) COMMENT='邮箱验证日志表';