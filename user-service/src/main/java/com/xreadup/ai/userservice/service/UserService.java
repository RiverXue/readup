package com.xreadup.ai.userservice.service;

import com.xreadup.ai.userservice.dto.*;
import com.xreadup.ai.userservice.entity.User;
import com.xreadup.ai.userservice.entity.Word;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    /**
     * 用户注册
     */
    User register(UserRegisterRequest request);
    
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