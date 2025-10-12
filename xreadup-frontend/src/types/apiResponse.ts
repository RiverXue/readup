// 通用API响应类型定义
export interface ApiResponse<T> {
  success: boolean
  message?: string
  data?: T
  [key: string]: any // 允许其他属性
}