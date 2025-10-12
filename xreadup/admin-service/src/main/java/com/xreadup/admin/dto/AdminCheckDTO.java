package com.xreadup.admin.dto;

import lombok.Data;

/**
 * 管理员权限验证响应DTO
 * 用于返回管理员身份验证结果
 */
@Data
public class AdminCheckDTO {
    
    /**
     * 是否为管理员
     */
    private boolean isAdmin;
    
    /**
     * 管理员角色：ADMIN/SUPER_ADMIN
     */
    private String role;
    
    /**
     * 管理员用户ID
     */
    private Long userId;
    
    /**
     * 管理员用户名
     */
    private String username;
    
    // 手动添加getter和setter方法，确保编译通过
    public boolean isAdmin() {
        return isAdmin;
    }
    
    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
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
}