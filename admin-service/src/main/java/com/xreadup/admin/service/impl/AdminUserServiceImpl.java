package com.xreadup.admin.service.impl;

import com.xreadup.admin.client.UserServiceClient;
import com.xreadup.admin.dto.AdminUserDTO;
import com.xreadup.admin.mapper.AdminUserMapper;
import com.xreadup.admin.model.AdminUser;
import com.xreadup.admin.exception.BusinessException;
import com.xreadup.admin.service.AdminUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
                adminUserDTO.setUsername((String) userDetail.get("username"));
                adminUserDTO.setPhone((String) userDetail.get("phone"));
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
                    dto.setUsername((String) userDetail.get("username"));
                    dto.setPhone((String) userDetail.get("phone"));
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
        // 检查用户是否已存在
        AdminUser existingAdmin = adminUserMapper.selectByUserId(userId);
        if (existingAdmin != null) {
            throw new BusinessException("该用户已经是管理员");
        }
        
        // 检查用户是否存在
        try {
            var userDetailResponse = userServiceClient.getUserDetail(userId);
            // 增强响应判断逻辑：检查响应是否成功，同时考虑code为200的情况
            if (userDetailResponse == null || userDetailResponse.getCode() != 200 || userDetailResponse.getData() == null) {
                throw new BusinessException("用户不存在");
            }
        } catch (Exception e) {
            throw new BusinessException("验证用户信息失败", e);
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
    public void updateAdminUser(Long userId, String role) {
        // 检查管理员用户是否存在
        AdminUser existingAdmin = adminUserMapper.selectByUserId(userId);
        if (existingAdmin == null) {
            throw new BusinessException("管理员用户不存在");
        }
        
        // 更新角色
        existingAdmin.setRole(role);
        adminUserMapper.updateById(existingAdmin);
        logger.info("更新管理员用户成功: userId={}, role={}", userId, role);
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