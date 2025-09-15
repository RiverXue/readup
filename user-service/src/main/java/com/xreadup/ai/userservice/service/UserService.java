package com.xreadup.ai.userservice.service;

import com.xreadup.ai.userservice.dto.*;
import com.xreadup.ai.userservice.entity.User;
import com.xreadup.ai.userservice.entity.Word;

import java.util.List;

/**
 * 用户服务接口 - 新增邮箱验证功能
 */
public interface UserService {
    /**
     * 用户注册（待验证状态）
     */
    User register(UserRegisterRequest request);
    
    /**
     * 激活用户邮箱
     */
    void activateUser(String email);
    
    /**
     * 检查邮箱是否已存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据邮箱查找用户
     */
    User findByEmail(String email);
    
    /**
     * 用户登录
     */
    LoginResponse login(UserLoginRequest request);
    
    /**
     * 添加生词
     */
    Word addWord(AddWordRequest request);
    
    /**
     * 获取用户生词本
     */
    List<Word> getWordList(Long userId);
    
    /**
     * 更新阅读打卡
     */
    int updateStreak(Long userId);
}