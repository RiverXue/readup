package com.xreadup.admin.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 创建管理员用户DTO
 * 用于接收创建管理员用户的请求参数
 */
@Data
public class AdminUserCreateDTO {
    
    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    /**
     * 管理员角色
     * 只能是ADMIN或SUPER_ADMIN
     */
    @NotNull(message = "管理员角色不能为空")
    @Pattern(regexp = "^(ADMIN|SUPER_ADMIN)$", message = "角色只能是ADMIN或SUPER_ADMIN")
    private String role;
}