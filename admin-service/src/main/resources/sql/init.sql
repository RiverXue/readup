-- 创建admin_user表
CREATE TABLE IF NOT EXISTS admin_user (
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role VARCHAR(20) NOT NULL COMMENT '管理员角色，ADMIN或SUPER_ADMIN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_user_id (user_id),
    KEY idx_role (role),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员用户表';

-- 插入初始超级管理员用户
-- 注意：实际部署时，请替换为真实的用户ID
INSERT INTO admin_user (user_id, role) VALUES (17, 'SUPER_ADMIN');

-- 添加备注信息
ALTER TABLE admin_user COMMENT='存储系统管理员用户信息';

