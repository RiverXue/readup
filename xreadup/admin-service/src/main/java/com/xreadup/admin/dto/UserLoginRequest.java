package com.xreadup.admin.dto;

import lombok.Data;

/**
 * 用户登录请求DTO
 * 用于管理员服务调用用户服务的登录接口
 */
@Data
public class UserLoginRequest {
    private String username;
    private String password;
}
