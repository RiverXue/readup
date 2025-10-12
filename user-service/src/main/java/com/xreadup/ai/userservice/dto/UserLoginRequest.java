package com.xreadup.ai.userservice.dto;

import lombok.Data;

/**
 * 用户登录请求DTO
 */
@Data
public class UserLoginRequest {
    private String username;
    private String password;
}