package com.xreadup.admin.service.impl;

import com.xreadup.admin.client.UserServiceClient;
import com.xreadup.admin.dto.AdminLoginRequest;
import com.xreadup.admin.dto.AdminLoginResponse;
import com.xreadup.admin.dto.AdminUserDTO;
import com.xreadup.admin.dto.UserLoginRequest;
import com.xreadup.admin.mapper.AdminUserMapper;
import com.xreadup.admin.model.AdminUser;
import com.xreadup.admin.exception.BusinessException;
import com.xreadup.admin.service.AdminUserService;
import com.xreadup.admin.util.AdminTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理员用户服务实现类
 * 实现AdminUserService接口中定义的业务方法
 */
@Service
public class AdminUserServiceImpl implements AdminUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminUserServiceImpl.class);
    
    @Autowired
    private AdminUserMapper adminUserMapper;
    
    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private AdminTokenManager adminTokenManager;
    
    @Override
    public boolean isAdmin(Long userId) {
        AdminUser adminUser = adminUserMapper.selectByUserId(userId);
        return adminUser != null;
    }
    
    @Override
    public boolean isSuperAdmin(Long userId) {
        AdminUser adminUser = adminUserMapper.selectByUserId(userId);
        return adminUser != null && "SUPER_ADMIN".equals(adminUser.getRole());
    }
    
    @Override
    public AdminUserDTO getAdminUserByUserId(Long userId) {
        AdminUser adminUser = adminUserMapper.selectByUserId(userId);
        if (adminUser == null) {
            return null;
        }
        
        // 构建AdminUserDTO
        AdminUserDTO adminUserDTO = new AdminUserDTO();
        BeanUtils.copyProperties(adminUser, adminUserDTO);
        
        // 获取用户详情信息
        try {
            var userDetailResponse = userServiceClient.getUserDetail(userId);
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (userDetailResponse != null && userDetailResponse.getCode() == 200 && userDetailResponse.getData() != null) {
                var userDetail = userDetailResponse.getData();
                adminUserDTO.setUsername(userDetail.getUsername());
                adminUserDTO.setPhone(userDetail.getPhone());
                // 注意：user表中没有email字段，所以不设置email属性
            }
        } catch (Exception e) {
            logger.error("获取用户详情失败", e);
        }
        
        return adminUserDTO;
    }
    
    @Override
    public List<AdminUserDTO> getAdminUsers(Integer page, Integer pageSize, String keyword) {
        List<AdminUser> adminUsers = adminUserMapper.selectAdminUsers(page, pageSize, keyword);
        
        // 转换为AdminUserDTO列表
        return adminUsers.stream().map(adminUser -> {
            AdminUserDTO dto = new AdminUserDTO();
            BeanUtils.copyProperties(adminUser, dto);
            
            // 获取用户名等信息
            try {
                var userDetailResponse = userServiceClient.getUserDetail(adminUser.getUserId());
                // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
                if (userDetailResponse != null && userDetailResponse.getCode() == 200 && userDetailResponse.getData() != null) {
                    var userDetail = userDetailResponse.getData();
                    dto.setUsername(userDetail.getUsername());
                    dto.setPhone(userDetail.getPhone());
                    // 注意：user表中没有email字段，所以不设置email属性
                }
            } catch (Exception e) {
                logger.error("获取用户详情失败", e);
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void addAdminUser(Long userId, String role) {
        logger.info("开始添加管理员用户: userId={}, role={}", userId, role);

        // 检查用户是否已存在
        AdminUser existingAdmin = adminUserMapper.selectByUserId(userId);
        if (existingAdmin != null) {
            logger.warn("添加管理员失败: 用户已经是管理员, userId={}", userId);
            throw new BusinessException("该用户已经是管理员");
        }

        // 检查用户是否存在
        try {
            var userDetailResponse = userServiceClient.getUserDetail(userId);
            logger.debug("用户服务响应: code={}, data={}", userDetailResponse != null ? userDetailResponse.getCode() : "null", userDetailResponse != null ? userDetailResponse.getData() : "null");
            
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (userDetailResponse == null || userDetailResponse.getCode() != 200 || userDetailResponse.getData() == null) {
                String errorMsg = "用户不存在或用户服务响应异常"; 
                logger.error(errorMsg + ": userId={}, responseCode={}", userId, userDetailResponse != null ? userDetailResponse.getCode() : "null");
                throw new BusinessException(errorMsg);
            }
            
            // 获取用户状态并验证
            var userDetail = userDetailResponse.getData();
            // 注意：UserDTO中没有status字段，这里假设用户都是活跃的
            // 如果需要验证状态，需要在UserDTO中添加status字段
            
            logger.debug("用户验证成功: userId={}, username={}", userId, userDetail.getUsername());
        } catch (BusinessException e) {
            // 捕获已知业务异常并重新抛出
            throw e;
        } catch (Exception e) {
            logger.error("验证用户信息失败: userId={}, 错误信息={}", userId, e.getMessage(), e);
            throw new BusinessException("验证用户信息失败：" + e.getMessage());
        }

        // 创建新的管理员用户
        AdminUser adminUser = new AdminUser();
        adminUser.setUserId(userId);
        adminUser.setRole(role);
        adminUser.setCreatedAt(LocalDateTime.now());

        adminUserMapper.insert(adminUser);
        logger.info("添加管理员用户成功: userId={}, role={}", userId, role);
    }
    
    @Override
    @Transactional
    public void updateAdminUser(Long userId, String role, String phone) {
        // 检查管理员用户是否存在
        AdminUser existingAdmin = adminUserMapper.selectByUserId(userId);
        if (existingAdmin == null) {
            throw new BusinessException("管理员用户不存在");
        }
        
        // 更新角色
        existingAdmin.setRole(role);
        adminUserMapper.updateById(existingAdmin);
        
        // 如果提供了手机号，更新用户信息
        if (phone != null && !phone.trim().isEmpty()) {
            try {
                UserServiceClient.UserUpdateDTO userUpdateDTO = new UserServiceClient.UserUpdateDTO();
                userUpdateDTO.setPhone(phone);
                
                var response = userServiceClient.updateUser(userId, userUpdateDTO);
                if (response == null || response.getCode() != 200) {
                    logger.warn("更新用户手机号失败，userId: {}, phone: {}", userId, phone);
                } else {
                    logger.info("更新用户手机号成功，userId: {}, phone: {}", userId, phone);
                }
            } catch (Exception e) {
                logger.error("更新用户手机号异常，userId: {}, phone: {}", userId, phone, e);
            }
        }
        
        logger.info("更新管理员用户成功: userId={}, role={}, phone={}", userId, role, phone);
    }
    
    @Override
    @Transactional
    public void deleteAdminUser(Long userId) {
        // 不能删除自己
        // 实际应用中需要获取当前登录用户ID进行判断
        // if (currentUserId != null && currentUserId.equals(userId)) {
        //     throw new RuntimeException("不能删除自己");
        // }
        
        // 检查管理员用户是否存在
        AdminUser existingAdmin = adminUserMapper.selectByUserId(userId);
        if (existingAdmin == null) {
            throw new BusinessException("管理员用户不存在");
        }
        
        // 删除管理员用户
        adminUserMapper.deleteByUserId(userId);
        logger.info("删除管理员用户成功: userId={}", userId);
    }
    
    @Override
    public AdminLoginResponse adminLogin(AdminLoginRequest loginRequest) {
        logger.info("管理员登录请求处理开始: username={}", loginRequest.getUsername());
        
        // 注意：此方法为向后兼容保留的接口，无IP和User-Agent参数
        // 在实际生产环境中，应使用带IP和User-Agent参数的重载方法
        return adminLogin(loginRequest, "unknown", "unknown");
    }
    
    /**
     * 管理员登录方法 - 带IP和User-Agent参数的增强版本
     * @param loginRequest 登录请求
     * @param clientIp 客户端IP地址
     * @param userAgent 客户端User-Agent
     * @return 登录响应
     */
    public AdminLoginResponse adminLogin(AdminLoginRequest loginRequest, String clientIp, String userAgent) {
        logger.info("管理员登录请求处理开始: username={}, IP={}", loginRequest.getUsername(), clientIp);
        
        // 1. 验证登录请求参数
        if (loginRequest == null || loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            logger.warn("登录参数不完整");
            throw new BusinessException("用户名或密码不能为空");
        }
        
        try {
            // 2. 检查是否超过最大登录尝试次数
            if (adminTokenManager.isAccountLocked(loginRequest.getUsername())) {
                long remainingTime = adminTokenManager.getLockedTimeRemaining(loginRequest.getUsername());
                logger.warn("账户已被锁定: username={}, 剩余锁定时间={}秒", 
                          loginRequest.getUsername(), remainingTime);
                throw new BusinessException("账户已被锁定，请" + remainingTime + "秒后再试");
            }
            
            // 3. 调用用户服务验证用户凭证
            logger.debug("调用用户服务验证凭证: username={}", loginRequest.getUsername());
            UserLoginRequest userLoginRequest = new UserLoginRequest();
            userLoginRequest.setUsername(loginRequest.getUsername());
            userLoginRequest.setPassword(loginRequest.getPassword());
            var userLoginResponse = userServiceClient.login(userLoginRequest);
            
            // 4. 检查登录响应
            if (userLoginResponse == null || userLoginResponse.getCode() != 200 || userLoginResponse.getData() == null) {
                // 记录失败登录尝试
                adminTokenManager.recordFailedLoginAttempt(loginRequest.getUsername(), clientIp);
                
                String errorMsg = "登录失败，请检查用户名和密码";
                logger.warn("{}. 响应码: {}", errorMsg, userLoginResponse != null ? userLoginResponse.getCode() : "null");
                throw new BusinessException(errorMsg);
            }
            
            // 5. 从登录响应中获取用户信息
            @SuppressWarnings("unchecked")
            Map<String, Object> userData = (Map<String, Object>) userLoginResponse.getData();
            @SuppressWarnings("unchecked")
            Map<String, Object> userInfo = (Map<String, Object>) userData.get("userInfo");
            Long userId = ((Number) userInfo.get("id")).longValue();
            String username = (String) userInfo.get("username");
            
            // 6. 检查用户是否为管理员
            logger.debug("验证用户是否为管理员: userId={}", userId);
            if (!isAdmin(userId)) {
                // 记录失败登录尝试（非管理员账户）
                adminTokenManager.recordFailedLoginAttempt(loginRequest.getUsername(), clientIp);
                
                logger.warn("用户不是管理员，拒绝登录: userId={}", userId);
                throw new BusinessException("您没有管理员权限");
            }
            
            // 7. 获取管理员详细信息
            AdminUser adminUser = adminUserMapper.selectByUserId(userId);
            if (adminUser == null) {
                logger.warn("管理员信息不存在: userId={}", userId);
                throw new BusinessException("管理员信息不存在");
            }
            
            // 8. 生成安全的管理员token
            String adminToken = adminTokenManager.generateSecureToken(userId.toString(), clientIp, userAgent);
            
            // 9. 设置过期时间（默认24小时）
            LocalDateTime expireTime = LocalDateTime.now().plus(24, ChronoUnit.HOURS);
            
            // 10. 存储token信息，包含环境验证
            boolean isSuperAdmin = "SUPER_ADMIN".equals(adminUser.getRole());
            adminTokenManager.storeTokenWithEnvironment(adminToken, userId, username, adminUser.getRole(), 
                                                      isSuperAdmin, expireTime, clientIp, userAgent);
            
            // 11. 重置失败登录尝试计数
            adminTokenManager.resetFailedLoginAttempts(loginRequest.getUsername());
            
            // 12. 构建并返回登录响应
            AdminLoginResponse response = new AdminLoginResponse();
            response.setToken(adminToken);
            // 将LocalDateTime转换为毫秒时间戳
            response.setTokenExpireTime(expireTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            response.setUserId(userId);
            response.setUsername(username);
            response.setRole(adminUser.getRole());
            response.setSuperAdmin(isSuperAdmin);
            
            // TODO: 这里可以根据实际业务需求添加权限列表
            response.setPermissions(Collections.emptyList());
            
            logger.info("管理员登录成功: userId={}, username={}, role={}, IP={}", 
                       userId, username, adminUser.getRole(), clientIp);
            
            return response;
            
        } catch (BusinessException e) {
            // 捕获已知业务异常并重新抛出
            throw e;
        } catch (Exception e) {
            logger.error("管理员登录过程中发生异常: username={}", loginRequest.getUsername(), e);
            throw new BusinessException("登录失败，请稍后重试");
        }
    }

    @Override
    public List<AdminUserDTO> getAvailableUsers(Integer pageSize, String keyword) {
        try {
            // 注意：由于user-service没有提供获取用户列表的API
            // 这里返回空列表作为临时解决方案
            logger.info("当前user-service未提供用户列表API，返回空列表");
        } catch (Exception e) {
            logger.error("获取用户列表失败", e);
        }
        return Collections.emptyList();
    }
    
    @Override
    public int countAdminUsers(String keyword) {
        return adminUserMapper.countAdminUsers(keyword);
    }
}