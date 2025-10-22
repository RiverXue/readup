// 统一的数据缓存服务
class DataCacheService {
  private cache = new Map<string, { data: any; timestamp: number }>()
  private readonly CACHE_EXPIRY = 5 * 60 * 1000 // 5分钟过期

  /**
   * 获取缓存数据
   * @param key 缓存键
   * @returns 缓存的数据或null
   */
  get(key: string) {
    const cached = this.cache.get(key)
    if (cached && Date.now() - cached.timestamp < this.CACHE_EXPIRY) {
      console.log(`缓存命中: ${key}`)
      return cached.data
    }
    if (cached) {
      console.log(`缓存过期: ${key}`)
      this.cache.delete(key)
    }
    return null
  }

  /**
   * 设置缓存数据
   * @param key 缓存键
   * @param data 要缓存的数据
   */
  set(key: string, data: any) {
    this.cache.set(key, {
      data,
      timestamp: Date.now()
    })
    console.log(`数据已缓存: ${key}`)
  }

  /**
   * 清除指定缓存
   * @param key 缓存键
   */
  delete(key: string) {
    this.cache.delete(key)
    console.log(`缓存已清除: ${key}`)
  }

  /**
   * 清除所有缓存
   */
  clear() {
    this.cache.clear()
    console.log('所有缓存已清除')
  }

  /**
   * 清除过期缓存
   */
  clearExpired() {
    const now = Date.now()
    for (const [key, cached] of this.cache.entries()) {
      if (now - cached.timestamp >= this.CACHE_EXPIRY) {
        this.cache.delete(key)
        console.log(`过期缓存已清除: ${key}`)
      }
    }
  }

  /**
   * 获取缓存统计信息
   */
  getStats() {
    return {
      size: this.cache.size,
      keys: Array.from(this.cache.keys()),
      expiry: this.CACHE_EXPIRY
    }
  }

  /**
   * 设置缓存过期时间
   * @param expiry 过期时间（毫秒）
   */
  setExpiry(expiry: number) {
    this.CACHE_EXPIRY = expiry
  }
}

// 创建全局缓存实例
export const dataCache = new DataCacheService()

// 导出类型
export interface CacheStats {
  size: number
  keys: string[]
  expiry: number
}
