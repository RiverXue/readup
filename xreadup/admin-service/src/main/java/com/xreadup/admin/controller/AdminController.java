package com.xreadup.admin.controller;

import com.xreadup.admin.dto.AdminCheckDTO;
import com.xreadup.admin.dto.AdminUserCreateDTO;
import com.xreadup.admin.dto.AdminUserDTO;
import com.xreadup.admin.dto.ApiResponse;
import com.xreadup.admin.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;

/**
 * 管理员控制器
 * 处理管理员相关的HTTP请求
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员管理", description = "管理员相关的操作接口")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private AdminUserService adminUserService;
    
    /**
     * 检查用户是否为管理员
     * @param userId 用户ID
     * @return 管理员权限检查结果
     */
    @GetMapping("/check")
    @Operation(summary = "检查用户是否为管理员", description = "验证用户的管理员权限状态")
    public ApiResponse<AdminCheckDTO> checkAdmin(@RequestParam("userId") Long userId) {
        try {
            boolean isAdmin = adminUserService.isAdmin(userId);
            boolean isSuperAdmin = adminUserService.isSuperAdmin(userId);
            
            AdminCheckDTO checkDTO = new AdminCheckDTO();
            checkDTO.setAdmin(isAdmin);
            checkDTO.setRole(isSuperAdmin ? "SUPER_ADMIN" : (isAdmin ? "ADMIN" : null));
            checkDTO.setUserId(userId);
            
            return ApiResponse.success(checkDTO);
        } catch (Exception e) {
            logger.error("检查管理员权限失败", e);
            return ApiResponse.fail(500, "检查管理员权限失败");
        }
    }
    
    /**
     * 获取管理员用户详情
     * @param userId 用户ID
     * @return 管理员用户详情
     */
    @GetMapping("/detail")
    @Operation(summary = "获取管理员用户详情", description = "根据用户ID获取管理员用户的详细信息")
    public ApiResponse<Map<String, Object>> getAdminUserDetail(@RequestParam("userId") Long userId) {
        try {
            AdminUserDTO adminUserDTO = adminUserService.getAdminUserByUserId(userId);
            if (adminUserDTO == null) {
                return ApiResponse.fail(404, "管理员用户不存在");
            }
            
            // 格式化时间字段
            Map<String, Object> formattedAdmin = new HashMap<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            formattedAdmin.put("userId", adminUserDTO.getUserId());
            formattedAdmin.put("role", adminUserDTO.getRole());
            formattedAdmin.put("username", adminUserDTO.getUsername());
            formattedAdmin.put("phone", adminUserDTO.getPhone());
            // 格式化时间
            if (adminUserDTO.getCreatedAt() != null) {
                formattedAdmin.put("createdAt", adminUserDTO.getCreatedAt().format(formatter));
            } else {
                formattedAdmin.put("createdAt", null);
            }
            
            return ApiResponse.success(formattedAdmin);
        } catch (Exception e) {
            logger.error("获取管理员用户详情失败", e);
            return ApiResponse.fail(500, "获取管理员用户详情失败");
        }
    }
    
    /**
     * 获取管理员用户列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键字
     * @return 管理员用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取管理员用户列表", description = "分页获取管理员用户列表")
    public ApiResponse<Map<String, Object>> getAdminUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            List<AdminUserDTO> adminUsers = adminUserService.getAdminUsers(page, pageSize, keyword);
            int total = adminUserService.countAdminUsers(keyword);
            
            // 格式化时间字段
            List<Map<String, Object>> formattedAdminUsers = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            
            for (AdminUserDTO admin : adminUsers) {
                Map<String, Object> adminMap = new HashMap<>();
                adminMap.put("userId", admin.getUserId());
                adminMap.put("role", admin.getRole());
                adminMap.put("username", admin.getUsername());
                adminMap.put("phone", admin.getPhone());
                // 格式化时间
                if (admin.getCreatedAt() != null) {
                    adminMap.put("createdAt", admin.getCreatedAt().format(formatter));
                } else {
                    adminMap.put("createdAt", null);
                }
                formattedAdminUsers.add(adminMap);
            }
            
            // 构建与前端期望一致的数据结构
            Map<String, Object> result = new HashMap<>();
            result.put("list", formattedAdminUsers);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);
            result.put("pages", total % pageSize == 0 ? total / pageSize : total / pageSize + 1);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            logger.error("获取管理员用户列表失败", e);
            return ApiResponse.fail(500, "获取管理员用户列表失败");
        }
    }
    
    /**
     * 添加管理员用户
     * @param adminUserCreateDTO 创建管理员用户的请求数据
     * @return 操作结果
     */
    @PostMapping("/add")
    @Operation(summary = "添加管理员用户", description = "将普通用户添加为管理员")
    public ApiResponse<Void> addAdminUser(@Validated @RequestBody AdminUserCreateDTO adminUserCreateDTO) {
        try {
            adminUserService.addAdminUser(adminUserCreateDTO.getUserId(), adminUserCreateDTO.getRole());
            return ApiResponse.success();
        } catch (RuntimeException e) {
            logger.error("添加管理员用户失败", e);
            return ApiResponse.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("添加管理员用户失败", e);
            return ApiResponse.fail(500, "添加管理员用户失败");
        }
    }
    
    /**
     * 更新管理员用户
     * @param userId 用户ID
     * @param role 管理员角色
     * @param phone 手机号（可选）
     * @return 操作结果
     */
    @PutMapping("/update")
    @Operation(summary = "更新管理员用户", description = "更新管理员用户的角色和手机号信息")
    public ApiResponse<Void> updateAdminUser(
            @RequestParam Long userId,
            @RequestParam String role,
            @RequestParam(required = false) String phone) {
        try {
            adminUserService.updateAdminUser(userId, role, phone);
            return ApiResponse.success();
        } catch (RuntimeException e) {
            logger.error("更新管理员用户失败", e);
            return ApiResponse.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("更新管理员用户失败", e);
            return ApiResponse.fail(500, "更新管理员用户失败");
        }
    }
    
    /**
     * 删除管理员用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除管理员用户", description = "移除用户的管理员权限")
    public ApiResponse<Void> deleteAdminUser(@RequestParam Long userId) {
        try {
            adminUserService.deleteAdminUser(userId);
            return ApiResponse.success();
        } catch (RuntimeException e) {
            logger.error("删除管理员用户失败", e);
            return ApiResponse.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("删除管理员用户失败", e);
            return ApiResponse.fail(500, "删除管理员用户失败");
        }
    }
    
    /**
     * 获取可添加为管理员的用户列表
     * @param pageSize 每页大小
     * @param keyword 搜索关键字
     * @return 可添加为管理员的用户列表
     */
    @GetMapping("/available-users")
    @Operation(summary = "获取可添加为管理员的用户列表", description = "获取还不是管理员的普通用户列表")
    public ApiResponse<List<AdminUserDTO>> getAvailableUsers(
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            List<AdminUserDTO> availableUsers = adminUserService.getAvailableUsers(pageSize, keyword);
            return ApiResponse.success(availableUsers);
        } catch (Exception e) {
            logger.error("获取可添加为管理员的用户列表失败", e);
            return ApiResponse.fail(500, "获取可添加为管理员的用户列表失败");
        }
    }
}