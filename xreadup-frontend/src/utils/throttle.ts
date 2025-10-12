/**
 * 请求节流工具
 * 防止用户频繁点击导致的重复请求
 */

// 请求状态管理
const requestStates = new Map<string, boolean>()

/**
 * 创建请求节流函数
 * @param key 请求唯一标识
 * @param fn 要执行的函数
 * @param delay 延迟时间（毫秒）
 * @returns 节流后的函数
 */
export function createThrottledRequest<T extends any[]>(
  key: string,
  fn: (...args: T) => Promise<any>,
  delay: number = 1000
) {
  return async (...args: T) => {
    // 检查是否已有相同请求在进行
    if (requestStates.get(key)) {
      console.log(`请求 ${key} 正在进行中，跳过重复请求`)
      return
    }

    try {
      requestStates.set(key, true)
      const result = await fn(...args)
      return result
    } finally {
      // 延迟后清除状态，防止过快重复请求
      setTimeout(() => {
        requestStates.delete(key)
      }, delay)
    }
  }
}

/**
 * 用户状态变更节流
 */
export const createUserStatusThrottle = (userId: number) => {
  return createThrottledRequest(
    `user-status-${userId}`,
    async (statusChangeFn: () => Promise<any>) => {
      return await statusChangeFn()
    },
    2000 // 2秒内不允许重复请求
  )
}

/**
 * 管理员状态变更节流
 */
export const createAdminStatusThrottle = (userId: number) => {
  return createThrottledRequest(
    `admin-status-${userId}`,
    async (statusChangeFn: () => Promise<any>) => {
      return await statusChangeFn()
    },
    2000 // 2秒内不允许重复请求
  )
}

/**
 * 清除所有请求状态
 */
export const clearAllRequestStates = () => {
  requestStates.clear()
}
