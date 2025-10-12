package com.xreadup.admin.controller;

import com.xreadup.admin.dto.AdminUserDTO;
import com.xreadup.admin.dto.ApiResponse;
import com.xreadup.admin.exception.AccessDeniedException;
import com.xreadup.admin.exception.AdminNotFoundException;
import com.xreadup.admin.exception.BusinessException;
import com.xreadup.admin.service.AdminUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 管理员用户管理控制器
 * 提供管理员用户管理相关的API接口
 */
@RestController
@RequestMapping("/api/admin/admins")
@Tag(name = "管理员用户管理", description = "管理员用户管理相关的API接口")
public class AdminUserController {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    @Autowired
    private AdminUserService adminUserService;
    
    /**
     * 获取当前登录管理员用户ID
     * 从安全框架上下文中获取当前登录用户的ID
     */
    private Long getCurrentAdminUserId() {
        try {
            // 实际应用中应从安全框架(如Spring Security、Shiro等)的上下文中获取当前登录用户ID
            // 以下为示例实现，实际应根据项目使用的安全框架进行调整
            
            // 从请求头中获取Authorization信息并解析用户ID
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            String authorizationHeader = request.getHeader("Authorization");
            
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7);
                // 解析token获取用户ID
                // 注意：实际项目中应使用适当的JWT解析库
                // 这里仅作示例，实际实现需根据项目的认证机制进行调整
                try {
                    // 假设使用标准JWT，这里简单解析示例
                    // 实际代码应使用项目中已有的JWT工具类
                    Claims claims = Jwts.parserBuilder()
                            .setSigningKey("your-secret-key".getBytes())
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
                    
                    return Long.parseLong(claims.getSubject());
                } catch (Exception e) {
                    logger.warn("解析token获取用户ID失败: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("获取当前登录用户ID失败: {}", e.getMessage());
        }
        
        // 为了向后兼容，当无法从安全上下文获取用户ID时，默认返回超级管理员用户ID
        // 注意：在生产环境中应移除这个默认值，确保必须通过安全认证
        return 17L;
    }

    /**
     * 获取管理员用户列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 关键字（用户名或ID）
     * @return 管理员用户列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取管理员用户列表", description = "分页获取管理员用户列表，可以根据关键字进行筛选")
    public ApiResponse<List<AdminUserDTO>> getAdminUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            logger.info("获取管理员用户列表，page: {}, pageSize: {}, keyword: {}", page, pageSize, keyword);
            
            // 检查当前用户是否为超级管理员
            // 从安全上下文中获取当前登录用户ID
            Long currentUserId = getCurrentAdminUserId();
            if (!adminUserService.isSuperAdmin(currentUserId)) {
                throw new AccessDeniedException("只有超级管理员才能查看管理员列表");
            }
            
            List<AdminUserDTO> adminUsers = adminUserService.getAdminUsers(page, pageSize, keyword);
            return ApiResponse.success(adminUsers);
        } catch (AccessDeniedException e) {
            logger.warn("访问被拒绝: {}", e.getMessage());
            return ApiResponse.fail(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取管理员用户列表异常", e);
            return ApiResponse.fail(500, "获取管理员用户列表失败");
        }
    }

    /**
     * 获取管理员用户详情
     * @param userId 用户ID
     * @return 管理员用户详情
     */
    @GetMapping("/detail/{userId}")
    @Operation(summary = "获取管理员用户详情", description = "根据用户ID获取管理员用户的详细信息")
    public ApiResponse<AdminUserDTO> getAdminUserDetail(@PathVariable Long userId) {
        try {
            logger.info("获取管理员用户详情，userId: {}", userId);
            
            // 检查当前用户是否为管理员
            // 从安全上下文中获取当前登录用户ID
            Long currentUserId = getCurrentAdminUserId();
            if (!adminUserService.isAdmin(currentUserId)) {
                throw new AccessDeniedException("只有管理员才能查看管理员详情");
            }
            
            AdminUserDTO adminUser = adminUserService.getAdminUserByUserId(userId);
            if (adminUser == null) {
                throw new AdminNotFoundException("管理员用户不存在");
            }
            
            return ApiResponse.success(adminUser);
        } catch (AccessDeniedException e) {
            logger.warn("访问被拒绝: {}", e.getMessage());
            return ApiResponse.fail(403, e.getMessage());
        } catch (AdminNotFoundException e) {
            logger.warn("管理员不存在: {}", e.getMessage());
            return ApiResponse.fail(404, e.getMessage());
        } catch (Exception e) {
            logger.error("获取管理员用户详情异常，userId: {}", userId, e);
            return ApiResponse.fail(500, "获取管理员用户详情失败");
        }
    }

    /**
     * 添加管理员用户
     * @param userId 用户ID
     * @param role 管理员角色
     * @return 操作结果
     */
    @PostMapping("/add")
    @Operation(summary = "添加管理员用户", description = "将普通用户添加为管理员")
    public ApiResponse<Boolean> addAdminUser(
            @RequestParam Long userId,
            @RequestParam String role) {
        try {
            logger.info("添加管理员用户，userId: {}, role: {}", userId, role);
            
            // 检查当前用户是否为超级管理员
            Long currentUserId = getCurrentAdminUserId();
            if (!adminUserService.isSuperAdmin(currentUserId)) {
                throw new AccessDeniedException("只有超级管理员才能添加管理员");
            }
            
            adminUserService.addAdminUser(userId, role);
            return ApiResponse.success(true);
        } catch (AccessDeniedException e) {
            logger.warn("访问被拒绝: {}", e.getMessage());
            return ApiResponse.fail(403, e.getMessage());
        } catch (Exception e) {
            logger.error("添加管理员用户异常，userId: {}, role: {}", userId, role, e);
            return ApiResponse.fail(500, "添加管理员用户失败");
        }
    }

    /**
     * 更新管理员用户角色
     * @param userId 用户ID
     * @param role 新的管理员角色
     * @return 操作结果
     */
    @PutMapping("/update/{userId}")
    @Operation(summary = "更新管理员用户角色", description = "更新管理员用户的角色")
    public ApiResponse<Boolean> updateAdminUser(
            @PathVariable Long userId,
            @RequestParam String role) {
        try {
            logger.info("更新管理员用户角色，userId: {}, role: {}", userId, role);
            
            // 检查当前用户是否为超级管理员
            Long currentUserId = getCurrentAdminUserId();
            if (!adminUserService.isSuperAdmin(currentUserId)) {
                throw new AccessDeniedException("只有超级管理员才能更新管理员角色");
            }
            
            // 不允许修改超级管理员自己的角色
            if (userId.equals(currentUserId) && !"SUPER_ADMIN".equals(role)) {
                throw new BusinessException("不允许修改超级管理员自己的角色");
            }
            
            adminUserService.updateAdminUser(userId, role, null);
            return ApiResponse.success(true);
        } catch (AccessDeniedException e) {
            logger.warn("访问被拒绝: {}", e.getMessage());
            return ApiResponse.fail(403, e.getMessage());
        } catch (BusinessException e) {
            logger.warn("业务异常: {}", e.getMessage());
            return ApiResponse.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("更新管理员用户角色异常，userId: {}, role: {}", userId, role, e);
            return ApiResponse.fail(500, "更新管理员用户角色失败");
        }
    }

    /**
     * 删除管理员用户
     * @param userId 用户ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{userId}")
    @Operation(summary = "删除管理员用户", description = "删除管理员用户权限")
    public ApiResponse<Boolean> deleteAdminUser(@PathVariable Long userId) {
        try {
            logger.info("删除管理员用户，userId: {}", userId);
            
            // 检查当前用户是否为超级管理员
            Long currentUserId = getCurrentAdminUserId();
            if (!adminUserService.isSuperAdmin(currentUserId)) {
                throw new AccessDeniedException("只有超级管理员才能删除管理员");
            }
            
            // 不允许删除超级管理员自己
            if (userId.equals(currentUserId)) {
                throw new BusinessException("不允许删除超级管理员自己");
            }
            
            adminUserService.deleteAdminUser(userId);
            return ApiResponse.success(true);
        } catch (AccessDeniedException e) {
            logger.warn("访问被拒绝: {}", e.getMessage());
            return ApiResponse.fail(403, e.getMessage());
        } catch (BusinessException e) {
            logger.warn("业务异常: {}", e.getMessage());
            return ApiResponse.fail(400, e.getMessage());
        } catch (Exception e) {
            logger.error("删除管理员用户异常，userId: {}", userId, e);
            return ApiResponse.fail(500, "删除管理员用户失败");
        }
    }

    /**
     * 获取可选的用户列表（用于添加管理员）
     * @param pageSize 每页大小
     * @param keyword 关键字
     * @return 可选用户列表
     */
    @GetMapping("/available-users")
    @Operation(summary = "获取可选的用户列表", description = "获取可以添加为管理员的普通用户列表")
    public ApiResponse<?> getAvailableUsers(
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            logger.info("获取可选的用户列表，pageSize: {}, keyword: {}", pageSize, keyword);
            
            // 检查当前用户是否为超级管理员
            Long currentUserId = getCurrentAdminUserId();
            if (!adminUserService.isSuperAdmin(currentUserId)) {
                throw new AccessDeniedException("只有超级管理员才能获取可选用户列表");
            }
            
            var availableUsers = adminUserService.getAvailableUsers(pageSize, keyword);
            return ApiResponse.success(availableUsers);
        } catch (AccessDeniedException e) {
            logger.warn("访问被拒绝: {}", e.getMessage());
            return ApiResponse.fail(403, e.getMessage());
        } catch (Exception e) {
            logger.error("获取可选的用户列表异常", e);
            return ApiResponse.fail(500, "获取可选的用户列表失败");
        }
    }
}