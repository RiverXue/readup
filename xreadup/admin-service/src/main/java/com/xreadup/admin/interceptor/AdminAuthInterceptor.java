package com.xreadup.admin.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xreadup.admin.dto.ApiResponse;
import com.xreadup.admin.util.AdminTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * 管理员认证拦截器
 * 用于拦截所有管理员API请求，验证管理员身份
 * 
 * 主要功能：
 * 1. 验证管理员token是否有效
 * 2. 排除公开接口（如登录、检查等）
 * 3. 对未授权请求进行拦截并返回错误信息
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAuthInterceptor.class);
    
    @Autowired
    private AdminTokenManager adminTokenManager;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    // 不需要进行认证的公开接口
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
        "/api/admin/login",
        "/api/admin/check",
        "/api/admin/logout",
        "/api/admin/extend-session",
        "/api/admin/system-config/internal" // 内部接口不需要认证
    );
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        logger.debug("拦截管理员请求: {}", requestUri);
        
        // 检查是否为公开接口
        for (String excludePath : EXCLUDE_PATHS) {
            if (requestUri.contains(excludePath)) {
                logger.debug("跳过公开接口认证: {}", requestUri);
                return true;
            }
        }
        
        // 从请求中获取token
        String token = getTokenFromRequest(request);
        if (token == null || token.isEmpty()) {
            logger.warn("未提供管理员token: {}", requestUri);
            sendUnauthorizedResponse(response, "未提供管理员token");
            return false;
        }
        
        // 获取请求的IP地址和User-Agent
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        
        // 验证token是否有效，同时检查环境一致性
        boolean isValid = adminTokenManager.validateTokenWithEnvironment(token, clientIp, userAgent);
        if (!isValid) {
            logger.warn("管理员token无效或已过期: {}, IP={}, User-Agent={}", requestUri, clientIp, userAgent);
            sendUnauthorizedResponse(response, "管理员token无效或已过期");
            return false;
        }
        
        // token有效，将用户信息存入请求属性中，方便后续接口使用
        Long userId = adminTokenManager.getUserIdByToken(token);
        String role = adminTokenManager.getRoleByToken(token);
        boolean isSuperAdmin = adminTokenManager.isSuperAdminByToken(token);
        
        request.setAttribute("adminUserId", userId);
        request.setAttribute("adminRole", role);
        request.setAttribute("adminIsSuperAdmin", isSuperAdmin);
        request.setAttribute("adminClientIp", clientIp);
        request.setAttribute("adminUserAgent", userAgent);
        
        logger.debug("管理员认证通过: userId={}, uri={}, IP={}", userId, requestUri, clientIp);
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, 
                          ModelAndView modelAndView) throws Exception {
        // 处理请求之后的逻辑，可以在这里添加统计信息等
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, 
                               Exception ex) throws Exception {
        // 请求完成后的清理工作
    }
    
    /**
     * 从请求中获取token
     * 优先从Authorization头中获取，其次从请求参数中获取，最后从cookie中获取
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 从Authorization头中获取
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        // 从请求参数中获取
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            return tokenParam;
        }
        
        // 从cookie中获取
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (jakarta.servlet.http.Cookie cookie : cookies) {
                if ("admin_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        
        return null;
    }
    
    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        ApiResponse<String> apiResponse = ApiResponse.fail(401, message);
        String jsonResponse = objectMapper.writeValueAsString(apiResponse);
        
        try (PrintWriter writer = response.getWriter()) {
            writer.write(jsonResponse);
            writer.flush();
        }
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