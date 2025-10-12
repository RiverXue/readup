import { adminApi } from '@/api/admin/adminApi'
import type { AdminPermission } from '@/types/admin'

/**
 * 管理员工具函数
 */

/**
 * 批量检查多个用户的管理员状态
 * @param userIds 用户ID列表
 * @returns 管理员状态映射
 */
const checkUsersAdminStatus = async (userIds: string[]): Promise<Record<string, boolean>> => {
  const result: Record<string, boolean> = {}
  
  try {
    // 使用Promise.all并发检查多个用户的管理员状态
    const checkPromises = userIds.map(async (userId) => {
      try {
        const response = await adminApi.checkAdmin(userId)
        // 添加类型检查以安全地访问success和data属性
        result[userId] = response && 'success' in response && response.success && 'data' in response && response.data && response.data.isAdmin || false
      } catch (error) {
        console.error(`检查用户 ${userId} 管理员状态失败:`, error)
        result[userId] = false
      }
    })
    
    // 等待所有检查完成
    await Promise.all(checkPromises)
  } catch (error) {
    console.error('批量检查用户管理员状态失败:', error)
    // 出错时，为所有用户设置默认值
    userIds.forEach(id => {
      result[id] = false
    })
  }
  
  return result
}

/**
 * 批量检查多个权限
 * @param permissions 需要的权限列表
 * @param userPermissions 用户拥有的权限列表
 * @param allRequired 是否需要所有权限
 * @returns 是否满足权限要求
 */
const hasAnyOfPermissions = (permissions: AdminPermission[], userPermissions: AdminPermission[], allRequired: boolean = true): boolean => {
  if (!userPermissions || userPermissions.length === 0) {
    return false
  }
  
  if (allRequired) {
    return permissions.every(permission => userPermissions.includes(permission))
  } else {
    return permissions.some(permission => userPermissions.includes(permission))
  }
}

export const adminUtils = {
  checkUsersAdminStatus,
  hasAnyOfPermissions
}