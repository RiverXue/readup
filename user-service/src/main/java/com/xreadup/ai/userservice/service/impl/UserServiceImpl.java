package com.xreadup.ai.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xreadup.ai.userservice.dto.*;
import com.xreadup.ai.userservice.entity.ReadingStreak;
import com.xreadup.ai.userservice.entity.User;
import com.xreadup.ai.userservice.entity.Word;
import com.xreadup.ai.userservice.mapper.ReadingStreakMapper;
import com.xreadup.ai.userservice.mapper.UserMapper;
import com.xreadup.ai.userservice.mapper.WordMapper;
import com.xreadup.ai.userservice.service.UserService;
import com.xreadup.ai.userservice.service.VocabularyService;
import com.xreadup.ai.userservice.util.JwtUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private WordMapper wordMapper;
    
    @Autowired
    private VocabularyService vocabularyService;
    
    @Autowired
    private ReadingStreakMapper readingStreakMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(UserRegisterRequest request) {
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("用户名已存在");
        }
        
        User user = new User();
        BeanUtils.copyProperties(request, user);
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        
        userMapper.insert(user);
        return user;
    }

    @Override
    public LoginResponse login(UserLoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtil.generateToken(user.getId().toString()));
        
        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        response.setUserInfo(userInfo);
        
        return response;
    }

    @Override
    public Word addWord(AddWordRequest request) {
        // 使用二级词库策略
        return vocabularyService.lookupWord(
            request.getWord(), 
            request.getContext(), 
            request.getUserId(), 
            request.getSourceArticleId()
        );
    }

    @Override
    public List<Word> getWordList(Long userId) {
        return wordMapper.findByUserId(userId);
    }

    @Override
    public int updateStreak(Long userId) {
        LambdaQueryWrapper<ReadingStreak> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReadingStreak::getUserId, userId);
        ReadingStreak streak = readingStreakMapper.selectOne(wrapper);
        
        LocalDate today = LocalDate.now();
        
        if (streak == null) {
            streak = new ReadingStreak();
            streak.setUserId(userId);
            streak.setStreakDays(1);
            streak.setLastReadDate(today);
            readingStreakMapper.insert(streak);
            return 1;
        }
        
        // 检查是否是连续的一天
        if (streak.getLastReadDate().equals(today.minusDays(1))) {
            // 连续打卡：+1天
            streak.setStreakDays(streak.getStreakDays() + 1);
        } else if (!streak.getLastReadDate().equals(today)) {
            // 中断打卡：扣1天，最低不低于1天
            int newStreakDays = Math.max(1, streak.getStreakDays() - 1);
            streak.setStreakDays(newStreakDays);
        }
        
        streak.setLastReadDate(today);
        readingStreakMapper.updateById(streak);
        
        return streak.getStreakDays();
    }
}