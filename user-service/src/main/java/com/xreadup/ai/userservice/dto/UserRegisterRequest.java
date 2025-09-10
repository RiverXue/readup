package com.xreadup.ai.userservice.dto;

import lombok.Data;

/**
 * 用户注册请求DTO
 */
@Data
public class UserRegisterRequest {
    private String username;
    private String password;
    private String phone;
    private String interestTag;
    private String identityTag;
    private Integer learningGoalWords;
    private Integer targetReadingTime;
}