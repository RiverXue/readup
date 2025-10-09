package com.xreadup.admin.controller;

import com.xreadup.admin.client.UserServiceClient;
import com.xreadup.admin.dto.ApiResponse;
import com.xreadup.admin.exception.AccessDeniedException;
import com.xreadup.admin.exception.AdminNotFoundException;
import com.xreadup.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户管理控制器
 * 提供用户管理相关的API接口
 */
@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "用户管理", description = "用户管理相关的API接口")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private AdminUserService adminUserService;

    /**
     * 获取用户列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param username 用户名（可选）
     * @param phone 电话（可选）
     * @param interestTag 兴趣标签（可选）
     * @param identityTag 身份标签（可选）
     * @param status 用户状态（可选）
     * @return 用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持多条件筛选")
    public ApiResponse<?> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String interestTag,
            @RequestParam(required = false) String identityTag,
            @RequestParam(required = false) String status) {
        try {
            logger.info("获取用户列表，page: {}, pageSize: {}, username: {}, phone: {}, interestTag: {}, identityTag: {}, status: {}", 
                        page, pageSize, username, phone, interestTag, identityTag, status);
            
            // 调用用户服务获取用户列表
            var response = userServiceClient.getUserList(page, pageSize, username, phone, interestTag, identityTag, status);
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (response != null && (response.isSuccess() || response.getCode() == 200)) {
                // 将UserServiceClient.UserListDTO转换为前端需要的格式
                Map<String, Object> userList = new HashMap<>();
                userList.put("list", response.getData().getRecords());
                userList.put("total", response.getData().getTotal());
                userList.put("page", response.getData().getCurrent());
                userList.put("pageSize", response.getData().getSize());
                userList.put("pages", response.getData().getPages());
                
                return ApiResponse.success(userList);
            } else {
                logger.warn("获取用户列表失败，code: {}, message: {}", 
                           response != null ? response.getCode() : "未知", 
                           response != null ? response.getMessage() : "未知");
                
                // 降级处理：返回空列表
                Map<String, Object> defaultUserList = new HashMap<>();
                defaultUserList.put("list", Collections.emptyList());
                defaultUserList.put("total", 0);
                defaultUserList.put("page", page);
                defaultUserList.put("pageSize", pageSize);
                defaultUserList.put("pages", 0);
                return ApiResponse.success(defaultUserList);
            }
        } catch (Exception e) {
            logger.error("获取用户列表异常", e);
            // 降级处理：返回空列表
            Map<String, Object> defaultUserList = new HashMap<>();
            defaultUserList.put("list", Collections.emptyList());
            defaultUserList.put("total", 0);
            defaultUserList.put("page", page);
            defaultUserList.put("pageSize", pageSize);
            defaultUserList.put("pages", 0);
            return ApiResponse.success(defaultUserList);
        }
    }

    /**
     * 获取用户详情
     * @param userId 用户ID
     * @return 用户详情
     */
    @GetMapping("/detail/{userId}")
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户的详细信息")
    public ApiResponse<?> getUserDetail(@PathVariable Long userId) {
        try {
            logger.info("获取用户详情，userId: {}", userId);
            
            // 调用用户服务获取用户详情
            var response = userServiceClient.getUserDetail(userId);
            if (response != null && response.getCode() == 200) {
                return ApiResponse.success(response.getData());
            } else {
                logger.warn("获取用户详情失败，userId: {}, code: {}, message: {}", 
                           userId, response != null ? response.getCode() : "未知", 
                           response != null ? response.getMessage() : "未知");
                return ApiResponse.fail(500, "获取用户详情失败");
            }
        } catch (Exception e) {
            logger.error("获取用户详情异常，userId: {}", userId, e);
            return ApiResponse.fail(500, "获取用户详情失败");
        }
    }

    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param userUpdateDTO 用户更新信息
     * @return 更新结果
     */
    @PutMapping("/update/{userId}")
    @Operation(summary = "更新用户信息", description = "根据用户ID更新用户的信息")
    public ApiResponse<?> updateUser(
            @PathVariable Long userId,
            @RequestBody UserServiceClient.UserUpdateDTO userUpdateDTO) {
        try {
            logger.info("更新用户信息，userId: {}", userId);
            
            // 调用用户服务更新用户信息
            var response = userServiceClient.updateUser(userId, userUpdateDTO);
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (response != null && response.getCode() == 200) {
                return ApiResponse.success(response.getData());
            } else {
                logger.warn("更新用户信息失败，userId: {}, code: {}, message: {}", 
                           userId, response != null ? response.getCode() : "未知", 
                           response != null ? response.getMessage() : "未知");
                return ApiResponse.fail(500, "更新用户信息失败");
            }
        } catch (Exception e) {
            logger.error("更新用户信息异常，userId: {}", userId, e);
            return ApiResponse.fail(500, "更新用户信息失败");
        }
    }

    /**
     * 禁用/启用用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/disable/{userId}")
    @Operation(summary = "禁用用户", description = "根据用户ID禁用用户")
    public ApiResponse<?> disableUser(@PathVariable Long userId) {
        try {
            logger.info("禁用用户，userId: {}", userId);
            
            // 调用用户服务禁用用户
            var response = userServiceClient.disableUser(userId);
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (response != null && response.getCode() == 200) {
                return ApiResponse.success(response.getData());
            } else {
                logger.warn("禁用用户失败，userId: {}, code: {}, message: {}", 
                           userId, response != null ? response.getCode() : "未知", 
                           response != null ? response.getMessage() : "未知");
                return ApiResponse.fail(500, "禁用用户失败");
            }
        } catch (Exception e) {
            logger.error("禁用用户异常，userId: {}", userId, e);
            return ApiResponse.fail(500, "禁用用户失败");
        }
    }
    
    /**
     * 启用用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @PutMapping("/enable/{userId}")
    @Operation(summary = "启用用户", description = "根据用户ID启用用户")
    public ApiResponse<?> enableUser(@PathVariable Long userId) {
        try {
            logger.info("启用用户，userId: {}", userId);
            
            // 调用用户服务启用用户
            var response = userServiceClient.enableUser(userId);
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (response != null && response.getCode() == 200) {
                return ApiResponse.success(response.getData());
            } else {
                logger.warn("启用用户失败，userId: {}, code: {}, message: {}", 
                           userId, response != null ? response.getCode() : "未知", 
                           response != null ? response.getMessage() : "未知");
                return ApiResponse.fail(500, "启用用户失败");
            }
        } catch (Exception e) {
            logger.error("启用用户异常，userId: {}", userId, e);
            return ApiResponse.fail(500, "启用用户失败");
        }
    }
    
    /**
     * 获取用户订阅列表
     * @param page 页码
     * @param pageSize 每页大小
     * @return 用户订阅列表
     */
    @GetMapping("/subscriptions")
    @Operation(summary = "获取用户订阅列表", description = "分页获取用户的订阅信息")
    public ApiResponse<?> getUserSubscriptionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            logger.info("获取用户订阅列表，page: {}, pageSize: {}", page, pageSize);
            
            // 调用用户服务获取用户订阅列表
            var response = userServiceClient.getUserSubscriptionList(page, pageSize);
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (response != null && response.getCode() == 200) {
                return ApiResponse.success(response.getData());
            } else {
                logger.warn("获取用户订阅列表失败，code: {}, message: {}", 
                           response != null ? response.getCode() : "未知", 
                           response != null ? response.getMessage() : "未知");
                return ApiResponse.fail(500, "获取用户订阅列表失败");
            }
        } catch (Exception e) {
            logger.error("获取用户订阅列表异常", e);
            return ApiResponse.fail(500, "获取用户订阅列表失败");
        }
    }

    /**
     * 获取用户学习进度统计
     * @param userId 用户ID
     * @return 用户学习进度统计
     */
    @GetMapping("/progress/{userId}")
    @Operation(summary = "获取用户学习进度统计", description = "根据用户ID获取用户的学习进度统计信息")
    public ApiResponse<?> getUserLearningProgress(@PathVariable Long userId) {
        try {
            logger.info("获取用户学习进度统计，userId: {}", userId);
            
            // 调用用户服务获取用户学习进度统计
            var response = userServiceClient.getUserLearningProgress(userId);
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (response != null && (response.isSuccess() || response.getCode() == 200)) {
                return ApiResponse.success(response.getData());
            } else {
                logger.warn("获取用户学习进度统计失败，userId: {}, code: {}, message: {}", 
                           userId, response != null ? response.getCode() : "未知", 
                           response != null ? response.getMessage() : "未知");
                return ApiResponse.fail(500, "获取用户学习进度统计失败");
            }
        } catch (Exception e) {
            logger.error("获取用户学习进度统计异常，userId: {}", userId, e);
            return ApiResponse.fail(500, "获取用户学习进度统计失败");
        }
    }
}