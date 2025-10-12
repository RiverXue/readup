package com.xreadup.ai.userservice.controller;

import com.xreadup.ai.userservice.dto.*;
import com.xreadup.ai.userservice.entity.User;
import com.xreadup.ai.userservice.entity.Word;
import com.xreadup.ai.userservice.mapper.SubscriptionMapper;
import com.xreadup.ai.userservice.service.UserService;
import com.xreadup.ai.userservice.service.SubscriptionService;
import com.xreadup.ai.userservice.util.JwtUtil;
import com.xreadup.ai.userservice.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户服务", description = "用户管理相关接口")
public class UserController {
    
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private SubscriptionMapper subscriptionMapper;
    
    
    

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "新用户注册接口")
    public ResponseEntity<?> register(@RequestBody UserRegisterRequest request) {
        try {
            User user = userService.register(request);


            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "注册成功",
                "data", Map.of(
                    "userId", user.getId(),
                    "username", user.getUsername()
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
     * 获取用户详情 - 供admin-service调用
     */
    @GetMapping("/detail/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public ResponseEntity<?> getUserDetail(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(new ApiResponse(404, "用户不存在", null));
            }
            
            // 构建用户详情响应
            Map<String, Object> userDetail = new HashMap<>();
            userDetail.put("id", user.getId());
            userDetail.put("username", user.getUsername());
            userDetail.put("phone", user.getPhone());
            userDetail.put("interestTag", user.getInterestTag());
            userDetail.put("identityTag", user.getIdentityTag());
            userDetail.put("learningGoalWords", user.getLearningGoalWords());
            userDetail.put("targetReadingTime", user.getTargetReadingTime());
            
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", userDetail));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(500, e.getMessage(), null));
        }
    }
    
    /**
     * 更新用户信息 - 供admin-service调用
     */
    @PutMapping("/update/{userId}")
    @Operation(summary = "更新用户信息", description = "更新用户基本信息")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> userUpdateDTO) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(new ApiResponse(404, "用户不存在", false));
            }
            
            // 更新用户信息
            if (userUpdateDTO.containsKey("phone")) {
                user.setPhone((String) userUpdateDTO.get("phone"));
            }
            if (userUpdateDTO.containsKey("interestTag")) {
                user.setInterestTag((String) userUpdateDTO.get("interestTag"));
            }
            if (userUpdateDTO.containsKey("identityTag")) {
                user.setIdentityTag((String) userUpdateDTO.get("identityTag"));
            }
            if (userUpdateDTO.containsKey("learningGoalWords")) {
                user.setLearningGoalWords((Integer) userUpdateDTO.get("learningGoalWords"));
            }
            if (userUpdateDTO.containsKey("targetReadingTime")) {
                user.setTargetReadingTime((Integer) userUpdateDTO.get("targetReadingTime"));
            }
            
            userService.updateUser(user);
            return ResponseEntity.ok(new ApiResponse(200, "更新成功", true));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(500, e.getMessage(), false));
        }
    }
    
    /**
     * 禁用用户 - 供admin-service调用
     */
    @PutMapping("/delete/{userId}")
    @Operation(summary = "禁用用户", description = "禁用指定用户账号")
    public ResponseEntity<?> disableUser(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(new ApiResponse(404, "用户不存在", false));
            }
            
            // 禁用用户
            user.setStatus("DISABLED");
            userService.updateUser(user);
            return ResponseEntity.ok(new ApiResponse(200, "禁用成功", true));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(500, e.getMessage(), false));
        }
    }
    
    /**
     * 启用用户 - 供admin-service调用
     */
    @PutMapping("/enable/{userId}")
    @Operation(summary = "启用用户", description = "启用指定用户账号")
    public ResponseEntity<?> enableUser(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(new ApiResponse(404, "用户不存在", false));
            }
            
            // 启用用户
            user.setStatus("ACTIVE");
            userService.updateUser(user);
            return ResponseEntity.ok(new ApiResponse(200, "启用成功", true));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(500, e.getMessage(), false));
        }
    }
    
    /**
     * 查询用户列表 - 供admin-service调用
     */
    @GetMapping("/list")
    @Operation(summary = "查询用户列表", description = "分页查询用户列表，支持多条件筛选")
    public ResponseEntity<?> getUserList(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String interestTag,
            @RequestParam(required = false) String identityTag,
            @RequestParam(required = false) String status) {
        try {
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            if (username != null) {
                params.put("username", username);
            }
            if (phone != null) {
                params.put("phone", phone);
            }
            if (interestTag != null) {
                params.put("interestTag", interestTag);
            }
            if (identityTag != null) {
                params.put("identityTag", identityTag);
            }
            if (status != null) {
                params.put("status", status);
            }
            
            // 查询用户列表
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> userPage = userService.getUserList(page, pageSize, params);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", userPage.getTotal());
            result.put("pages", userPage.getPages());
            result.put("current", userPage.getCurrent());
            result.put("size", userPage.getSize());
            result.put("records", userPage.getRecords());
            
            return ResponseEntity.ok(new ApiResponse(200, "查询成功", result));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(500, e.getMessage(), null));
        }
    }
    
    /**
     * 获取用户订阅列表 - 供admin-service调用
     */
    @GetMapping("/subscription/list")
    @Operation(summary = "获取用户订阅列表", description = "分页获取用户订阅信息列表，支持筛选")
    public ResponseEntity<?> getUserSubscriptionList(
            @RequestParam Integer page, 
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String planType,
            @RequestParam(required = false) String status) {
        try {
            // 使用MyBatis-Plus的Page对象
            com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.xreadup.ai.userservice.entity.Subscription> pageParam = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
            
            // 构建查询条件
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.xreadup.ai.userservice.entity.Subscription> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            
            // 添加筛选条件
            if (userId != null && !userId.trim().isEmpty()) {
                try {
                    Long userIdLong = Long.parseLong(userId.trim());
                    queryWrapper.eq("user_id", userIdLong);
                } catch (NumberFormatException e) {
                    log.warn("无效的用户ID: {}", userId);
                }
            }
            
            if (planType != null && !planType.trim().isEmpty()) {
                queryWrapper.eq("plan_type", planType.trim());
            }
            
            if (status != null && !status.trim().isEmpty()) {
                queryWrapper.eq("status", status.trim());
            }
            
            queryWrapper.orderByDesc("created_at");
            
            // 调用mapper进行分页查询
            subscriptionMapper.selectPage(pageParam, queryWrapper);
            
            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("total", pageParam.getTotal());
            result.put("pages", pageParam.getPages());
            result.put("current", pageParam.getCurrent());
            result.put("size", pageParam.getSize());
            result.put("records", pageParam.getRecords());
            
            log.info("获取用户订阅列表成功，page: {}, pageSize: {}, userId: {}, planType: {}, status: {}, total: {}", 
                    page, pageSize, userId, planType, status, pageParam.getTotal());
            
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", result));
        } catch (Exception e) {
            log.error("获取用户订阅列表失败: {}", e.getMessage());
            return ResponseEntity.ok(new ApiResponse(500, "获取用户订阅列表失败: " + e.getMessage(), new java.util.ArrayList<>()));
        }
    }

    /**
     * 获取用户统计信息 - 供admin-service调用
     */
    @GetMapping("/stats")
    @Operation(summary = "获取用户统计信息", description = "获取用户总数等统计信息")
    public ResponseEntity<?> getUserStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getTotalUserCount());
            stats.put("activeUsers", userService.getActiveUserCount());
            stats.put("newUsersToday", userService.getNewUsersTodayCount());
            return ResponseEntity.ok(com.xreadup.ai.userservice.common.ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.ok(com.xreadup.ai.userservice.common.ApiResponse.error("获取统计信息失败"));
        }
    }

    /**
     * 获取用户学习进度统计 - 供admin-service调用
     */
    @GetMapping("/progress/{userId}")
    @Operation(summary = "获取用户学习进度统计", description = "根据用户ID获取用户的学习进度统计信息")
    public ResponseEntity<?> getUserLearningProgress(@PathVariable Long userId) {
        try {
            // 验证用户是否存在
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.ok(new ApiResponse(404, "用户不存在", null));
            }
            
            // 构建用户学习进度统计数据
            Map<String, Object> progressData = new HashMap<>();
            progressData.put("userId", userId);
            
            // 获取单词学习数量（从生词本获取）
            List<Word> wordList = userService.getWordList(userId);
            progressData.put("vocabularyCount", wordList != null ? wordList.size() : 0);
            
            // 获取阅读连续天数
            // 注意：实际项目中应该有专门的方法获取阅读连续天数
            progressData.put("readingStreakDays", 0);
            
            // 获取总学习时长（分钟）
            // 注意：实际项目中应该从数据库查询真实数据
            progressData.put("totalLearningMinutes", 0);
            
            // 获取完成的文章数量
            // 注意：实际项目中应该从数据库查询真实数据
            progressData.put("completedArticles", 0);
            
            // 获取学习目标完成度
            // 注意：实际项目中应该根据用户设置的目标计算完成度
            progressData.put("goalCompletionRate", 0.0);
            
            return ResponseEntity.ok(new ApiResponse(200, "获取成功", progressData));
        } catch (Exception e) {
            return ResponseEntity.ok(new ApiResponse(500, e.getMessage(), null));
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