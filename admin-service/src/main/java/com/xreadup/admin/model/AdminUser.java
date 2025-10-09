package com.xreadup.admin.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员用户模型
 * 对应数据库中的admin_user表
 */
@Data
@TableName("admin_user")
public class AdminUser {
    
    /**
     * 用户ID，作为主键，与user表关联
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    
    /**
     * 管理员角色：ADMIN/SUPER_ADMIN
     */
    private String role;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
}