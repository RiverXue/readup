package com.xreadup.ai.userservice.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    private String token;
    private UserInfo userInfo;
    
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String phone;
        private String interestTag;
        private String identityTag;
        private Integer learningGoalWords;
        private Integer targetReadingTime;
    }
}