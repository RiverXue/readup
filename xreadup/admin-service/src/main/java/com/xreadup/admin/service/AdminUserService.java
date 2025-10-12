package com.xreadup.admin.service;

import com.xreadup.admin.dto.AdminLoginRequest;
import com.xreadup.admin.dto.AdminLoginResponse;
import com.xreadup.admin.dto.AdminUserDTO;

import java.util.List;

/**
 * 管理员用户服务接口
 * 定义管理员用户相关的业务方法
 */
public interface AdminUserService {
    
    /**
     * 管理员登录
     * 验证用户凭证并检查管理员权限
     * @param request 登录请求参数
     * @return 登录响应数据
     */
    AdminLoginResponse adminLogin(AdminLoginRequest request);
    
    /**
     * 判断用户是否为管理员
     * @param userId 用户ID
     * @return 是否为管理员
     */
    boolean isAdmin(Long userId);
    
    /**
     * 判断用户是否为超级管理员
     * @param userId 用户ID
     * @return 是否为超级管理员
     */
    boolean isSuperAdmin(Long userId);
    
    /**
     * 根据用户ID获取管理员用户信息
     * @param userId 用户ID
     * @return 管理员用户DTO
     */
    AdminUserDTO getAdminUserByUserId(Long userId);
    
    /**
     * 获取管理员用户列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 关键字（用户名或ID）
     * @return 管理员用户DTO列表
     */
    List<AdminUserDTO> getAdminUsers(Integer page, Integer pageSize, String keyword);
    
    /**
     * 添加管理员用户
     * @param userId 用户ID
     * @param role 管理员角色
     */
    void addAdminUser(Long userId, String role);
    
    /**
     * 更新管理员用户
     * @param userId 用户ID
     * @param role 管理员角色
     * @param phone 手机号（可选）
     */
    void updateAdminUser(Long userId, String role, String phone);
    
    /**
     * 删除管理员用户
     * @param userId 用户ID
     */
    void deleteAdminUser(Long userId);
    
    /**
     * 获取可添加为管理员的用户列表
     * @param pageSize 每页大小
     * @param keyword 关键字（用户名）
     * @return 用户DTO列表
     */
    List<AdminUserDTO> getAvailableUsers(Integer pageSize, String keyword);
    
    /**
     * 统计管理员用户数量
     * @param keyword 关键字（可选）
     * @return 管理员用户数量
     */
    int countAdminUsers(String keyword);

    /**
     * 管理员登录验证（带环境信息）
     * @param request 登录请求参数
     * @param clientIp 客户端IP地址
     * @param userAgent 客户端User-Agent
     * @return 登录响应结果
     */
    com.xreadup.admin.dto.AdminLoginResponse adminLogin(com.xreadup.admin.dto.AdminLoginRequest request, String clientIp, String userAgent);
}