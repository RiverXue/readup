/**
 * 文章分类配置
 * 基于GNews API官方标准分类定义
 * 确保前后端分类定义的一致性
 * 
 * 官方支持的分类：
 * - general: 综合
 * - world: 国际  
 * - nation: 国内
 * - business: 商业
 * - technology: 科技
 * - entertainment: 娱乐
 * - sports: 体育
 * - science: 科学
 * - health: 健康
 */

// 分类中英文映射 - 基于GNews API官方标准
export const CATEGORY_MAP: Record<string, string> = {
  'general': '综合',
  'world': '国际',
  'nation': '国内',
  'business': '商业',
  'technology': '科技',
  'entertainment': '娱乐',
  'sports': '体育',
  'science': '科学',
  'health': '健康'
}

// 创建反向映射（中文到英文）
export const REVERSE_CATEGORY_MAP: Record<string, string> = {}
Object.entries(CATEGORY_MAP).forEach(([en, zh]) => {
  REVERSE_CATEGORY_MAP[zh] = en
})

// 获取所有分类选项（用于下拉框等）
export const getCategoryOptions = () => {
  return Object.entries(CATEGORY_MAP).map(([value, label]) => ({
    value,
    label
  }))
}

// 获取分类的中文名称
export const getCategoryLabel = (category: string): string => {
  return CATEGORY_MAP[category] || category
}

// 获取分类的英文名称
export const getCategoryValue = (label: string): string => {
  return REVERSE_CATEGORY_MAP[label] || label
}

// 验证分类是否有效
export const isValidCategory = (category: string): boolean => {
  return category in CATEGORY_MAP
}

// 获取所有有效的分类值
export const getAllCategoryValues = (): string[] => {
  return Object.keys(CATEGORY_MAP)
}

// 获取所有分类标签
export const getAllCategoryLabels = (): string[] => {
  return Object.values(CATEGORY_MAP)
}
