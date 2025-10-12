import { type NavigationGuardNext, type RouteLocationNormalized } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'

/**
 * 管理员路由守卫
 * 用于检查用户是否具有管理员权限访问特定页面
 * 使用独立的管理员会话验证逻辑，避免与普通用户会话混淆
 */
export const adminGuard = async (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): Promise<void> => {
  // 检查是否需要管理员权限
  if (to.meta.requiresAdmin) {
    // 记录当前页面，便于登录后返回
    const targetPath = to.path !== '/admin/login' ? to.fullPath : ''
    if (targetPath) {
      sessionStorage.setItem('targetPath', targetPath)
    }

    const adminStore = useAdminStore()
    
    // 检查是否已经通过了管理员验证并且会话有效
    if (adminStore.isAdminUser && !adminStore.isSessionExpired) {
      // 如果会话即将过期（距离过期时间不足15分钟），自动延长会话有效期
      if (adminStore.shouldReverify) {
        try {
          // 使用管理员专属接口进行会话刷新
          await adminStore.extendSession()
        } catch (error) {
          console.error('会话延长失败:', error)
          // 会话延长失败，不阻止用户访问，但提示可能需要重新登录
          ElMessage.warning('会话验证失败，可能需要重新登录')
        }
      }
      next()
      return
    }

    // 检查本地是否有管理员Token（admin_token）
    const adminToken = localStorage.getItem('admin_token')
    const adminTokenExpiry = localStorage.getItem('admin_token_expiry')
    
    // 如果有管理员Token，但尚未初始化管理员状态，尝试初始化
    if (adminToken && adminTokenExpiry && !adminStore.isAdminUser) {
      
      // 检查Token是否已过期
      const now = Date.now()
      if (parseInt(adminTokenExpiry, 10) < now) {
        // Token已过期，清除本地存储并跳转到登录页面
        adminStore.clearAdminState()
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_token_expiry')
        localStorage.removeItem('admin_user_info')
        localStorage.removeItem('admin_role')
        
        ElMessage.error('管理员会话已过期，请重新登录')
        next('/admin/login')
        return
      }
      
      // 设置API请求头为管理员Token
      api.defaults.headers.common['Authorization'] = `Bearer ${adminToken}`
      
      try {
        // 验证管理员身份并初始化状态
        await adminStore.initializeAdminStatus()
        
        if (adminStore.isAdminUser && !adminStore.isSessionExpired) {
          next()
          return
        }
      } catch (error: any) {
        console.error('管理员身份验证失败:', error)
        // 验证失败，清除本地存储的管理员信息
        adminStore.clearAdminState()
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_token_expiry')
        localStorage.removeItem('admin_user_info')
        localStorage.removeItem('admin_role')
      }
    }

    // 清除普通用户Token，避免混淆
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    localStorage.removeItem('tokenExpiry')
    
    // 如果没有有效的管理员会话，跳转到管理员登录页面
    next('/admin/login')
  } else {
    // 不需要管理员权限
    next()
  }
}

/**
 * 管理员登录页面守卫
 * 防止已登录的管理员重复登录
 * 使用独立的管理员会话验证逻辑
 */
export const adminLoginGuard = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void => {
  const adminStore = useAdminStore()
  
  // 检查管理员登录状态
  const adminToken = localStorage.getItem('admin_token')
  const adminTokenExpiry = localStorage.getItem('admin_token_expiry')
  
  // 清除普通用户会话，避免混淆
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  localStorage.removeItem('tokenExpiry')
  
  if (adminToken && adminTokenExpiry) {
    // 如果有管理员Token，尝试验证会话是否有效
    const now = Date.now()
    if (parseInt(adminTokenExpiry, 10) < now) {
      // Token已过期，清除本地存储
      adminStore.clearAdminState()
      localStorage.removeItem('admin_token')
      localStorage.removeItem('admin_token_expiry')
      localStorage.removeItem('admin_user_info')
      localStorage.removeItem('admin_role')
      next()
      return
    }
    
    // 设置API请求头为管理员Token
    api.defaults.headers.common['Authorization'] = `Bearer ${adminToken}`
    
    // 尝试初始化管理员状态
    adminStore.initializeAdminStatus().then(() => {
      if (adminStore.isAdminUser && !adminStore.isSessionExpired) {
        // 已经是管理员并且会话有效，重定向到仪表盘
        ElMessage.success('您已登录，正在跳转到管理后台')
        next('/admin/dashboard')
      } else {
        // 会话无效，继续登录
        next()
      }
    }).catch(() => {
      next()
    })
  } else {
    // 无管理员会话，允许登录
    next()
  }
}