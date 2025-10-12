package com.xreadup.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xreadup.admin.model.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AdminUserMapper接口
 * MyBatis Mapper，用于操作admin_user表
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    
    /**
     * 根据用户ID查询管理员用户
     * @param userId 用户ID
     * @return 管理员用户对象
     */
    AdminUser selectByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID删除管理员用户
     * @param userId 用户ID
     * @return 影响的行数
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 查询管理员用户列表
     * @param page 页码
     * @param pageSize 每页大小
     * @param keyword 关键字（用户名或ID）
     * @return 管理员用户列表
     */
    List<AdminUser> selectAdminUsers(@Param("page") Integer page,
                                    @Param("pageSize") Integer pageSize,
                                    @Param("keyword") String keyword);
    
    /**
     * 统计管理员用户数量
     * @param keyword 关键字（可选）
     * @return 管理员用户数量
     */
    int countAdminUsers(@Param("keyword") String keyword);
}