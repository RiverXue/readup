import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import type { AdminRole, AdminPermission, AdminSession, AdminApiResponse } from '@/types/admin'
import { adminApi } from '@/api/admin/adminApi'
import { useUserStore } from '@/stores/user'

// 管理员角色权限映射 - 与backbuilding.md文档保持一致
// 这个配置定义了不同管理员角色所拥有的具体权限
// Record<AdminRole, AdminPermission[]> 表示这是一个映射对象，键为AdminRole类型，值为AdminPermission数组
const ROLE_PERMISSIONS: Record<AdminRole, AdminPermission[]> = {
  // 超级管理员拥有所有权限，包括管理其他管理员的权限
  SUPER_ADMIN: ['USER_MANAGE', 'ARTICLE_MANAGE', 'SUBSCRIPTION_MANAGE', 'AI_RESULT_MANAGE', 'ADMIN_MANAGE'],
  // 普通管理员拥有除管理其他管理员外的所有权限
  ADMIN: ['USER_MANAGE', 'ARTICLE_MANAGE', 'SUBSCRIPTION_MANAGE', 'AI_RESULT_MANAGE']
}

// 修复checkAdmin响应类型检查的辅助函数 - 兼容多种响应格式
const isSuccessResponse = <T>(response: any): response is AdminApiResponse<T> => {
  return response && typeof response === 'object' && 
         (('success' in response && response.success === true) || 
          ('code' in response && (response.code === 200 || response.code === 0)));
}

// 修复类型定义，确保我们能安全地访问data属性
const getResponseData = <T>(response: any): T | undefined => {
  if (response && typeof response === 'object') {
    // 直接返回data，不严格依赖isSuccessResponse
    return response.data;
  }
  return undefined;
}

export const useAdminStore = defineStore('admin', () => {
  // 状态
  const isAdminUser = ref(false)
  const isSuperAdminUser = ref(false)
  const role = ref<AdminRole | null>(null)
  const permissions = ref<AdminPermission[]>([])
  const sessionExpiredAt = ref('')
  const lastLoginTime = ref('')
  const loading = ref(false)
  const lastVerifiedTime = ref<number | null>(null)
  
  // 获取用户store实例
  const userStore = useUserStore()

  // 计算属性
  const hasAllPermissions = computed(() => isSuperAdminUser.value)
  const isSessionExpired = computed(() => {
    if (!sessionExpiredAt.value) return true
    return new Date(sessionExpiredAt.value).getTime() < Date.now()
  })

  // 检查是否需要重新验证 - 修改为1小时
  const shouldReverify = computed(() => {
    if (!lastVerifiedTime.value) return true
    const now = Date.now()
    const diffMinutes = (now - lastVerifiedTime.value) / (1000 * 60)
    return diffMinutes > 60 // 修改为每60分钟重新验证一次
  })

  // 初始化管理员状态
  const initializeAdminStatus = async () => {
    try {
      // 1. 检查用户是否已登录
      if (!userStore.isLoggedIn) {
        clearAdminState()
        return false
      }

      // 2. 检查本地存储的管理员会话
      const savedAdminSession = localStorage.getItem('adminSession')

      if (!savedAdminSession) {
        clearAdminState()
        return false
      }

      // 3. 解析会话数据
      const sessionData: AdminSession = JSON.parse(savedAdminSession)

      // 4. 检查会话是否过期
      if (isSessionExpired.value) {
        clearAdminState()
        return false
      }

      // 5. 验证管理员身份
      try {
        // 使用/api/admin/check接口验证管理员身份，并传递userId参数
        const checkResponse = await adminApi.checkAdmin(userStore.user?.id?.toString())
        const data = getResponseData<{ isAdmin: boolean, admin: boolean, role: string }>(checkResponse)

        if (data && (data.isAdmin || data.admin)) {
          // 6. 更新管理员状态
          updateAdminStateFromCheckResponse(data)
          
          // 7. 延长会话时间并保存
          extendSession()
          saveAdminSession()

          return true
        }
      } catch (error) {
        console.error('管理员身份验证失败:', error)
      }

      // 验证失败，清除管理员信息
      clearAdminState()
      return false
    } catch (error) {
      console.error('初始化管理员状态失败:', error)
      clearAdminState()
      return false
    }
  }

  // 从check接口响应中更新管理员状态
  const updateAdminStateFromCheckResponse = (data: { isAdmin?: boolean, admin?: boolean, role: string }) => {
    isAdminUser.value = data.isAdmin !== undefined ? data.isAdmin : data.admin || false
    isSuperAdminUser.value = data.role === 'SUPER_ADMIN'
    role.value = data.role as AdminRole
    permissions.value = ROLE_PERMISSIONS[role.value] || []
    lastVerifiedTime.value = Date.now()
  }

  // 延长会话时间 - 修改为4小时有效期
  const extendSession = () => {
    const expiryTime = new Date()
    expiryTime.setHours(expiryTime.getHours() + 4) // 修改为4小时有效期
    sessionExpiredAt.value = expiryTime.toISOString()
  }

  // 管理员登录方法 - 已废弃，使用独立的管理员登录API
  // 注意：此方法已废弃，管理员登录应直接使用 adminApi.login()
  // 保留此方法仅用于向后兼容，实际登录流程已移至 AdminLogin.vue 组件
  interface LoginParams {
    username?: string
    password?: string
  }

  const login = async (params: LoginParams) => {
    console.warn('adminStore.login() 已废弃，请使用独立的管理员登录API (adminApi.login())')
    ElMessage.warning('请使用管理员专用登录页面进行登录')
    return false
  }

  // 保存已登录用户的管理员信息
  interface SaveAdminInfoParams {
    admin: any
    token: string
    permissions?: string[]
  }

  const saveAdminInfo = (params: SaveAdminInfoParams) => {
    try {
      // 检查用户是否已登录
      if (!userStore.isLoggedIn) {
        console.error('用户未登录，无法保存管理员信息')
        return false
      }

      // 获取当前登录用户的ID
      const userId = userStore.user?.id
      if (!userId) {
        console.error('无法获取用户ID')
        return false
      }

      // 设置管理员状态
      setAdminState({
        isAdmin: true,
        isSuperAdmin: params.permissions?.includes('SUPER_ADMIN') || false,
        role: params.permissions?.includes('SUPER_ADMIN') ? 'SUPER_ADMIN' : 'ADMIN',
        token: params.token,
        userId: userId.toString()
      }, params.permissions)

      console.log('管理员信息保存成功')
      return true
    } catch (error: any) {
      console.error('保存管理员信息失败:', error)
      return false
    }
  }

  // 验证用户是否为管理员
  const verifyUserAsAdmin = async () => {
    try {
      loading.value = true
      
      // 获取当前登录用户的ID
      const userId = userStore.user?.id
      if (!userId) {
        ElMessage.error('用户未登录')
        return false
      }
      
      // 使用/api/admin/check接口验证管理员身份，并传递必需的userId参数
      const checkResponse = await adminApi.checkAdmin(userId.toString())
      const data = getResponseData<{ isAdmin: boolean, admin: boolean, role: string }>(checkResponse)

      // 更灵活地判断管理员权限 - 优先使用isAdmin，如果没有则使用admin
      const hasAdminPermission = data && (data.isAdmin || data.admin === true)
      
      if (hasAdminPermission) {
        // 设置管理员状态
        setAdminState({
          isAdmin: true,
          isSuperAdmin: data.role === 'SUPER_ADMIN',
          role: data.role as AdminRole || 'ADMIN',
          token: localStorage.getItem('token') || '',
          userId: userId.toString()
        })

        ElMessage.success(`欢迎回来，${data.role === 'SUPER_ADMIN' ? '超级管理员' : '管理员'}`)
        return true
      } else {
        ElMessage.error('该账号没有管理员权限')
        return false
      }
    } catch (error) {
      console.error('验证管理员身份失败:', error)
      ElMessage.error('验证管理员身份失败')
      return false
    } finally {
      loading.value = false
    }
  }

  // 设置管理员状态（提取复用逻辑）
  interface SetAdminStateParams {
    isAdmin: boolean
    isSuperAdmin: boolean
    role: AdminRole
    token: string
    userId: string
  }

  const setAdminState = (params: SetAdminStateParams, customPermissions?: string[]) => {
    // 设置管理员状态
    isAdminUser.value = params.isAdmin
    isSuperAdminUser.value = params.isSuperAdmin
    role.value = params.role
    permissions.value = customPermissions?.length ? 
      customPermissions as AdminPermission[] : 
      ROLE_PERMISSIONS[params.role] || []
    lastLoginTime.value = new Date().toISOString()
    lastVerifiedTime.value = Date.now()

    // 设置会话过期时间（4小时）
    const expiryTime = new Date()
    expiryTime.setHours(expiryTime.getHours() + 4)
    sessionExpiredAt.value = expiryTime.toISOString()

    // 创建会话数据
    const sessionData: AdminSession = {
      isAdmin: params.isAdmin,
      role: params.role,
      permissions: permissions.value,
      lastLoginTime: lastLoginTime.value,
      sessionExpiredAt: sessionExpiredAt.value,
      userId: params.userId
    }

    // 保存会话信息
    localStorage.setItem('adminSession', JSON.stringify(sessionData))
  }

  // 重新验证管理员身份
  const revalidateAdmin = async () => {
    try {
      loading.value = true

      // 检查是否已登录
      if (!userStore.isLoggedIn) {
        clearAdminState()
        return false
      }

      // 使用/api/admin/check接口验证，并传递userId参数
      const response = await adminApi.checkAdmin(userStore.user?.id?.toString())
      const data = getResponseData<{ isAdmin: boolean, admin: boolean, role: string }>(response)

      if (data && (data.isAdmin || data.admin)) {
        // 更新管理员状态
        updateAdminStateFromCheckResponse(data)
        
        // 延长会话时间
        extendSession()

        // 保存会话信息
        saveAdminSession()

        return true
      }

      clearAdminState()
      return false
    } catch (error) {
      console.error('验证管理员身份失败:', error)
      clearAdminState()
      return false
    } finally {
      loading.value = false
    }
  }

  // 检查管理员权限
  const hasPermission = (permission: AdminPermission): boolean => {
    if (!isAdminUser.value || isSessionExpired.value) return false
    if (hasAllPermissions.value) return true
    return permissions.value.includes(permission)
  }

  // 清除管理员信息
  const clearAdminState = () => {
    isAdminUser.value = false
    isSuperAdminUser.value = false
    role.value = null
    permissions.value = []
    sessionExpiredAt.value = ''
    lastLoginTime.value = ''
    lastVerifiedTime.value = null

    // 清除localStorage中的管理员信息
    localStorage.removeItem('adminSession')
  }

  // 保存管理员会话信息
  const saveAdminSession = () => {
    const sessionData: AdminSession = {
      isAdmin: isAdminUser.value,
      role: role.value,
      permissions: permissions.value,
      lastLoginTime: lastLoginTime.value,
      sessionExpiredAt: sessionExpiredAt.value,
      userId: userStore.user?.id?.toString() || ''
    }
    localStorage.setItem('adminSession', JSON.stringify(sessionData))
  }

  // 登出方法
  const logout = () => {
    clearAdminState()
    // 调用用户系统的登出方法
    userStore.logout()
  }

  // 获取当前管理员信息
  const getAdminUserInfo = () => {
    if (!isAdminUser.value || isSessionExpired.value) {
      return null;
    }
    return {
      id: userStore.user?.id?.toString() || '',
      role: role.value,
      isSuperAdmin: isSuperAdminUser.value,
      permissions: permissions.value,
      lastLoginTime: lastLoginTime.value
    };
  }

  // 添加兼容层，支持initializeAdmin方法调用
  const initializeAdmin = async () => {
    return initializeAdminStatus()
  }

  return {
    isAdminUser,
    isSuperAdminUser,
    role,
    permissions,
    sessionExpiredAt,
    lastLoginTime,
    loading,
    hasAllPermissions,
    isSessionExpired,
    shouldReverify,
    initializeAdminStatus,
    initializeAdmin, // 添加这个方法以兼容adminGuard.ts
    login,
    saveAdminInfo,
    verifyUserAsAdmin,
    setAdminState, // 添加setAdminState方法
    revalidateAdmin,
    hasPermission,
    clearAdminState,
    saveAdminSession,
    logout,
    getAdminUserInfo // 添加这个方法以兼容AdminUsersManagement.vue
  }
})
