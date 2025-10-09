package com.xreadup.ai.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String username;
    
    private String password;

    private String phone;
    
    private String interestTag;
    
    private String identityTag;
    
    private Integer learningGoalWords;
    
    private Integer targetReadingTime;

    private LocalDateTime createdAt;
    
    // 注意：需要确保数据库表中存在status字段
    private String status = "ACTIVE";
}