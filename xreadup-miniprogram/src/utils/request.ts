// utils/request.ts - HTTP请求封装
interface RequestConfig {
  url: string
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  data?: any
  header?: Record<string, string>
  timeout?: number
}

interface ApiResponse<T = any> {
  code: number
  message: string
  success: boolean
  data: T
}

class Request {
  private baseURL: string
  private timeout: number
  
  constructor() {
    // 根据环境设置不同的API地址
    // #ifdef MP-WEIXIN
    this.baseURL = 'https://api.readup.com'
    // #endif
    
    // #ifdef H5
    this.baseURL = '/api'
    // #endif
    
    this.timeout = 10000
  }
  
  // 请求拦截器
  private interceptRequest(config: RequestConfig): RequestConfig {
    // 添加认证头
    const token = uni.getStorageSync('token')
    if (token) {
      config.header = {
        ...config.header,
        'Authorization': `Bearer ${token}`
      }
    }
    
    // 添加通用头
    config.header = {
      'Content-Type': 'application/json',
      ...config.header
    }
    
    return config
  }
  
  // 响应拦截器
  private interceptResponse<T>(response: any): ApiResponse<T> {
    const { statusCode, data } = response
    
    if (statusCode === 200) {
      return data
    } else if (statusCode === 401) {
      // Token过期，清除登录状态
      uni.removeStorageSync('token')
      uni.removeStorageSync('user')
      
      // 跳转到登录页
      uni.reLaunch({
        url: '/pages/login/login'
      })
      
      throw new Error('登录已过期')
    } else {
      throw new Error(data.message || '请求失败')
    }
  }
  
  // 通用请求方法
  async request<T = any>(config: RequestConfig): Promise<ApiResponse<T>> {
    const finalConfig = this.interceptRequest(config)
    
    return new Promise((resolve, reject) => {
      uni.request({
        url: `${this.baseURL}${finalConfig.url}`,
        method: finalConfig.method || 'GET',
        data: finalConfig.data,
        header: finalConfig.header,
        timeout: finalConfig.timeout || this.timeout,
        success: (response) => {
          try {
            const result = this.interceptResponse<T>(response)
            resolve(result)
          } catch (error) {
            reject(error)
          }
        },
        fail: (error) => {
          console.error('请求失败:', error)
          reject(new Error('网络请求失败'))
        }
      })
    })
  }
  
  // GET请求
  async get<T = any>(url: string, params?: any): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'GET',
      data: params
    })
  }
  
  // POST请求
  async post<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'POST',
      data
    })
  }
  
  // PUT请求
  async put<T = any>(url: string, data?: any): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'PUT',
      data
    })
  }
  
  // DELETE请求
  async delete<T = any>(url: string): Promise<ApiResponse<T>> {
    return this.request<T>({
      url,
      method: 'DELETE'
    })
  }
}

export default new Request()
