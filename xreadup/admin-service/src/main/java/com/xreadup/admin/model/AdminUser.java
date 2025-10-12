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
    
    // 手动添加getter方法，确保编译通过
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
}