package com.xreadup.ai.userservice.controller;

import com.xreadup.ai.userservice.dto.*;
import com.xreadup.ai.userservice.entity.User;
import com.xreadup.ai.userservice.entity.Word;
import com.xreadup.ai.userservice.service.EmailService;
import com.xreadup.ai.userservice.service.UserService;
import com.xreadup.ai.userservice.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
@Tag(name = "用户服务", description = "用户管理相关接口")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private EmailService emailService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册接口，注册后需验证邮箱")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            User user = userService.register(request);
            
            // 发送验证邮件
            emailService.sendVerificationEmail(user.getEmail(), user.getUsername());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "注册成功，请查收验证邮件",
                "data", Map.of(
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录验证接口")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(200, "登录成功", userService.login(request)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 添加生词
     */
    @PostMapping("/vocabulary/add")
    @Operation(summary = "【收藏词汇】加入生词本", description = "一键收藏难词，打造专属词汇库")
    public ResponseEntity<?> addToVocabulary(@RequestBody AddWordRequest request) {
        try {
            return ResponseEntity.ok(new ApiResponse(200, "已加入生词本", userService.addWord(request)));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 获取生词本
     */
    @GetMapping("/vocabulary/my-words")
    @Operation(summary = "【我的词汇】查看生词本", description = "回顾你的词汇收藏，温故而知新")
    public ResponseEntity<?> getMyVocabulary(@Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            List<Word> words = userService.getWordList(userId);
            return ResponseEntity.ok(new ApiResponse(200, "success", words));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 更新阅读打卡
     */
    @PostMapping("/progress/check-in")
    @Operation(summary = "【每日打卡】记录学习足迹", description = "每日阅读打卡，积累学习成就")
    public ResponseEntity<?> dailyCheckIn(@Parameter(description = "用户ID", required = true) @RequestParam Long userId) {
        try {
            int days = userService.updateStreak(userId);
            return ResponseEntity.ok(new ApiResponse(200, "打卡成功", days));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(400, e.getMessage(), null));
        }
    }

    /**
     * 统一的API响应格式
     */
    @lombok.Data
    public static class ApiResponse {
        private int code;
        private String message;
        private Object data;

        public ApiResponse(int code, String message, Object data) {
            this.code = code;
            this.message = message;
            this.data = data;
        }
    }
}