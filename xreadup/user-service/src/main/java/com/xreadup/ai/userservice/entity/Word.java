package com.xreadup.ai.userservice.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 生词实体类
 */
@Data
@TableName("word")
public class Word {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    // 使用字符串存储多个用户ID，用逗号分隔，实现多用户共享单词
    private String userIds;
    
    private String word;
    
    private String meaning;
    
    private String example; // 例句
    
    private String context; // 上下文（如：金融/科技/地理）
    
    private String source; // 来源：local/ai
    
    private Long sourceArticleId;
    
    private String reviewStatus;
    
    private LocalDateTime lastReviewedAt;
    
    private LocalDateTime nextReviewAt;
    
    private LocalDateTime addedAt;
    
    private String phonetic; // 音标
    
    private String difficulty; // 难度等级 (A1, A2, B1, B2, C1, C2)
    
    // 非数据库字段，用于临时存储当前用户对该单词的状态
    @TableField(exist = false)
    private String userReviewStatus;
    
    // 将用户ID字符串转换为集合
    public Set<Long> getUserIdSet() {
        if (userIds == null || userIds.isEmpty()) {
            return new HashSet<>();
        }
        return java.util.Arrays.stream(userIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }
    
    // 将用户ID集合转换为字符串
    public void setUserIdSet(Set<Long> userIdSet) {
        if (userIdSet == null || userIdSet.isEmpty()) {
            this.userIds = "";
        } else {
            this.userIds = userIdSet.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        }
    }
    
    // 添加用户ID
    public void addUserId(Long userId) {
        Set<Long> userIdSet = getUserIdSet();
        userIdSet.add(userId);
        setUserIdSet(userIdSet);
    }
    
    // 移除用户ID
    public boolean removeUserId(Long userId) {
        Set<Long> userIdSet = getUserIdSet();
        boolean removed = userIdSet.remove(userId);
        if (removed) {
            setUserIdSet(userIdSet);
        }
        return removed;
    }
    
    // 检查是否包含用户ID
    public boolean containsUserId(Long userId) {
        return getUserIdSet().contains(userId);
    }
}