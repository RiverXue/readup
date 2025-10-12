import { type NavigationGuardNext, type RouteLocationNormalized } from 'vue-router'
import { useUserStore } from '@/stores/user'

/**
 * 用户路由守卫
 * 用于检查用户是否已登录访问需要认证的页面
 */
export const userGuard = async (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): Promise<void> => {
  // 检查是否需要用户认证
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    
    // 检查用户token
    const token = localStorage.getItem('token')
    
    if (token && token.trim() !== '') {
      try {
        // 初始化用户状态
        await userStore.initializeUser()
        
        // 验证用户身份
        if (userStore.isLoggedIn) {
          next()
        } else {
          // 验证失败
          userStore.logout()
          next('/login')
        }
      } catch (error) {
        console.error('用户状态初始化失败:', error)
        userStore.logout()
        next('/login')
      }
    } else {
      // 无用户token
      userStore.logout()
      next('/login')
    }
  } else {
    // 不需要用户认证
    next()
  }
}

/**
 * 登录页面守卫
 * 防止已登录的用户重复登录
 */
export const loginGuard = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void => {
  const userStore = useUserStore()
  
  // 检查用户登录状态
  const token = localStorage.getItem('token')
  
  if (token && token.trim() !== '') {
    // 如果有token，尝试初始化用户状态
    userStore.initializeUser().then(() => {
      if (userStore.isLoggedIn) {
        // 已经登录，重定向到首页
        next('/')
      } else {
        // 会话无效，继续登录
        next()
      }
    }).catch(() => {
      next()
    })
  } else {
    // 无会话，允许登录
    next()
  }
}