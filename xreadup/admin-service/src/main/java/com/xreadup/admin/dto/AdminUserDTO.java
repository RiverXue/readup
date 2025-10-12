package com.xreadup.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 管理员用户DTO - 与init.sql中的admin_user表字段完全一致
 * 用于在服务层和控制器层之间传递管理员用户信息
 */
@Data
public class AdminUserDTO {
    
    /**
     * 用户ID - 对应admin_user表的user_id字段
     */
    private Long userId;
    
    /**
     * 管理员角色：ADMIN/SUPER_ADMIN - 对应admin_user表的role字段
     */
    private String role;
    
    /**
     * 创建时间 - 对应admin_user表的created_at字段
     * 使用LocalDateTime存储，但提供格式化的getter方法
     */
    private LocalDateTime createdAt;
    
    /**
     * 用户名 - 从user表关联获取
     */
    private String username;
    
    /**
     * 用户手机号 - 从user表关联获取
     */
    private String phone;
    
    // 手动添加getter和setter方法，确保编译通过
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
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
    
    /**
     * 获取格式化的创建时间字符串
     * @return 格式化的时间字符串 (yyyy-MM-dd HH:mm:ss)
     */
    public String getCreatedAtFormatted() {
        if (createdAt == null) {
            return null;
        }
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
}