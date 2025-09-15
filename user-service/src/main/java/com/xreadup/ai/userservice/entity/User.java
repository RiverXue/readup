package com.xreadup.ai.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类 - 新增邮箱验证功能
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;
    
    private String email;  // 新增邮箱字段
    
    private String phone;
    
    private String interestTag;
    
    private String identityTag;
    
    private Integer learningGoalWords;
    
    private Integer targetReadingTime;
    
    private Boolean emailVerified = false;  // 邮箱验证状态
    
    private LocalDateTime createdAt;
}