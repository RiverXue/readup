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
}