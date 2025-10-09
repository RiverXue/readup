package com.xreadup.ai.userservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xreadup.ai.userservice.dto.*;
import com.xreadup.ai.userservice.entity.User;
import com.xreadup.ai.userservice.entity.Word;

import java.util.List;
import java.util.Map;

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
    
    /**
     * 根据用户ID获取用户信息
     */
    User getUserById(Long userId);
    
    /**
     * 更新用户信息
     */
    void updateUser(User user);
    
    /**
     * 分页查询用户列表
     */
    Page<User> getUserList(int page, int pageSize, Map<String, Object> params);
}