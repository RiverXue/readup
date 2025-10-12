package com.xreadup.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 管理员用户DTO
 * 用于在服务层和控制器层之间传递管理员用户信息
 */
@Data
public class AdminUserDTO {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 管理员角色：ADMIN/SUPER_ADMIN
     */
    private String role;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 用户手机号
     */
    private String phone;
}