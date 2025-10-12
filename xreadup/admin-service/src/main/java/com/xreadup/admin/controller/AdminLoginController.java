package com.xreadup.admin.controller;

import com.xreadup.admin.dto.AdminLoginRequest;
import com.xreadup.admin.dto.AdminLoginResponse;
import com.xreadup.admin.dto.ApiResponse;
import com.xreadup.admin.service.AdminUserService;
import com.xreadup.admin.util.AdminTokenManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理员登录控制器
 * 提供独立的管理员登录接口，与普通用户登录分离
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理员登录", description = "管理员登录相关的操作接口")
public class AdminLoginController {

    private static final Logger logger = LoggerFactory.getLogger(AdminLoginController.class);

    @Autowired
    private AdminUserService adminUserService;
    
    @Autowired
    private AdminTokenManager adminTokenManager;

    /**
     * 管理员登录接口
     * 独立的管理员登录认证，与普通用户登录分离
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员专用登录接口，验证用户凭证并检查管理员权限")
    public ResponseEntity<ApiResponse<AdminLoginResponse>> adminLogin(
            @Validated @RequestBody AdminLoginRequest request, HttpServletRequest httpRequest) {
        try {
            // 获取客户端IP和User-Agent
            String clientIp = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");
            
            logger.info("管理员登录请求，用户名: {}, IP: {}", request.getUsername(), clientIp);
            
            // 调用服务层进行管理员登录验证，传递IP和User-Agent
            AdminLoginResponse loginResponse = adminUserService.adminLogin(request, clientIp, userAgent);
            
            logger.info("管理员登录成功，用户名: {}, 角色: {}, IP: {}", 
                    request.getUsername(), loginResponse.getRole(), clientIp);
            
            return ResponseEntity.ok(ApiResponse.success(loginResponse, "登录成功"));
        } catch (RuntimeException e) {
            logger.warn("管理员登录失败: {}", e.getMessage());
            return ResponseEntity.ok(ApiResponse.fail(401, e.getMessage()));
        } catch (Exception e) {
            logger.error("管理员登录异常", e);
            return ResponseEntity.ok(ApiResponse.fail(500, "登录失败，请稍后重试"));
        }
    }
    /**
     * 管理员会话检查接口
     * 用于验证管理员token是否有效，以及获取管理员基本信息
     */
    @GetMapping("/session/check")
    @Operation(summary = "管理员会话检查", description = "验证管理员token是否有效，以及获取管理员基本信息")
    public ApiResponse<Map<String, Object>> check(HttpServletRequest request) {
        try {
            // 从请求头或参数中获取token
            String token = getTokenFromRequest(request);
            if (token == null || token.isEmpty()) {
                return ApiResponse.<Map<String, Object>>badRequest("未提供管理员token");
            }
            
            // 获取客户端IP和User-Agent
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 验证token是否有效，同时检查环境一致性
            boolean isValid = adminTokenManager.validateTokenWithEnvironment(token, clientIp, userAgent);
            if (!isValid) {
                logger.warn("管理员token验证失败，IP或User-Agent不匹配: IP={}", clientIp);
                return ApiResponse.<Map<String, Object>>unauthorized("管理员token无效或已过期");
            }
            
            // 获取管理员信息
            Long userId = adminTokenManager.getUserIdByToken(token);
            String role = adminTokenManager.getRoleByToken(token);
            boolean isSuperAdmin = adminTokenManager.isSuperAdminByToken(token);
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("isValid", true);
            data.put("userId", userId);
            data.put("role", role);
            data.put("isSuperAdmin", isSuperAdmin);
            
            logger.debug("管理员会话检查成功: userId={}, IP={}", userId, clientIp);
            return ApiResponse.<Map<String, Object>>success(data);
        } catch (Exception e) {
            logger.error("管理员会话检查失败: {}", e.getMessage());
            return ApiResponse.fail(500, "会话检查失败: " + e.getMessage());
        }
    }
    
    /**
     * 延长管理员会话有效期
     * 用于前端自动延长管理员会话
     */
    @PostMapping("/session/extend")
    @Operation(summary = "延长管理员会话", description = "延长管理员token的有效期")
    public ApiResponse<Map<String, Object>> extendSession(HttpServletRequest request) {
        try {
            // 从请求头或参数中获取token
            String token = getTokenFromRequest(request);
            if (token == null || token.isEmpty()) {
                return ApiResponse.<Map<String, Object>>badRequest("未提供管理员token");
            }
            
            // 获取客户端IP和User-Agent
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 验证token是否有效，同时检查环境一致性
            boolean isValid = adminTokenManager.validateTokenWithEnvironment(token, clientIp, userAgent);
            if (!isValid) {
                logger.warn("管理员token验证失败，IP或User-Agent不匹配: IP={}", clientIp);
                return ApiResponse.<Map<String, Object>>unauthorized("管理员token无效或已过期");
            }
            
            // 获取管理员信息
            Long userId = adminTokenManager.getUserIdByToken(token);
            String role = adminTokenManager.getRoleByToken(token);
            boolean isSuperAdmin = adminTokenManager.isSuperAdminByToken(token);
            
            // 使用安全的方法生成新token并延长有效期
            String newToken = adminTokenManager.generateSecureToken(userId.toString(), clientIp, userAgent);
            LocalDateTime newExpireTime = LocalDateTime.now().plus(24, ChronoUnit.HOURS);
            
            // 存储新token并移除旧token
            adminTokenManager.removeToken(token);
            adminTokenManager.storeTokenWithEnvironment(newToken, userId, "admin_" + userId, role, isSuperAdmin, 
                    newExpireTime, clientIp, userAgent);
            
            // 构建响应数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);
            data.put("expireTime", newExpireTime);
            data.put("userId", userId);
            data.put("role", role);
            
            logger.debug("管理员会话延长成功: userId={}, IP={}", userId, clientIp);
            return ApiResponse.<Map<String, Object>>success(data);
        } catch (Exception e) {
            logger.error("管理员会话延长失败: {}", e.getMessage());
            return ApiResponse.fail(500, "会话延长失败: " + e.getMessage());
        }
    }
    
    /**
     * 管理员登出接口
     */
    @PostMapping("/session/logout")
    @Operation(summary = "管理员登出", description = "管理员退出登录，清除token")
    public ApiResponse<String> logout(HttpServletRequest request) {
        try {
            // 从请求头或参数中获取token
            String token = getTokenFromRequest(request);
            if (token != null && !token.isEmpty()) {
                adminTokenManager.removeToken(token);
                logger.info("管理员登出成功");
            }
            return ApiResponse.success("登出成功");
        } catch (Exception e) {
            logger.error("管理员登出失败: {}", e.getMessage());
            return ApiResponse.fail(500, "登出失败: " + e.getMessage());
        }
    }
    
    /**
     * 从请求中获取token
     * 优先从Authorization头中获取，其次从请求参数中获取
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从Authorization头中获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 从请求参数中获取
        return request.getParameter("token");
    }
    
    /**
     * 获取客户端真实IP地址
     * 处理代理情况下的IP获取
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 多级代理情况下，第一个IP为真实IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
}