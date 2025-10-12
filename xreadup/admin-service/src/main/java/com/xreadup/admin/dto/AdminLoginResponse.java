package com.xreadup.admin.dto;

import java.util.List;

/**
 * 管理员登录响应数据
 */
public class AdminLoginResponse {

    // JWT Token
    private String token;

    // Token过期时间（毫秒）
    private long tokenExpireTime;

    // 用户ID
    private Long userId;

    // 用户名
    private String username;

    // 管理员角色（SUPER_ADMIN 或 ADMIN）
    private String role;

    // 是否为超级管理员
    private boolean isSuperAdmin;

    // 权限列表
    private List<String> permissions;

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(long tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
        this.isSuperAdmin = "SUPER_ADMIN".equals(role);
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}