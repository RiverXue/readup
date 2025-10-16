/**
 * GNews API 配置
 * 支持的语言、国家和排序选项
 * 
 * 重要说明（根据GNews API官方文档）：
 * - 语言(lang): 控制新闻的语言，如英语、中文、法语等
 * - 国家(country): 控制新闻来源的国家，如美国、英国、德国等
 * - 建议：优先使用语言参数，国家参数作为可选项
 * - 注意：同时使用语言和国家可能导致结果较少或无结果
 */

// 支持的语言配置
export const SUPPORTED_LANGUAGES = [
  { value: 'en', label: 'English', flag: '🇺🇸', description: '英语新闻' },
  { value: 'zh', label: '中文', flag: '🇨🇳', description: '中文新闻' },
  { value: 'es', label: 'Español', flag: '🇪🇸', description: '西班牙语新闻' },
  { value: 'fr', label: 'Français', flag: '🇫🇷', description: '法语新闻' },
  { value: 'de', label: 'Deutsch', flag: '🇩🇪', description: '德语新闻' },
  { value: 'it', label: 'Italiano', flag: '🇮🇹', description: '意大利语新闻' },
  { value: 'pt', label: 'Português', flag: '🇵🇹', description: '葡萄牙语新闻' },
  { value: 'ru', label: 'Русский', flag: '🇷🇺', description: '俄语新闻' },
  { value: 'ja', label: '日本語', flag: '🇯🇵', description: '日语新闻' },
  { value: 'ko', label: '한국어', flag: '🇰🇷', description: '韩语新闻' },
  { value: 'ar', label: 'العربية', flag: '🇸🇦', description: '阿拉伯语新闻' },
  { value: 'hi', label: 'हिन्दी', flag: '🇮🇳', description: '印地语新闻' }
]

// 支持的国家配置
export const SUPPORTED_COUNTRIES = [
  { value: 'us', label: 'United States', flag: '🇺🇸', description: '美国新闻源' },
  { value: 'gb', label: 'United Kingdom', flag: '🇬🇧', description: '英国新闻源' },
  { value: 'ca', label: 'Canada', flag: '🇨🇦', description: '加拿大新闻源' },
  { value: 'au', label: 'Australia', flag: '🇦🇺', description: '澳大利亚新闻源' },
  { value: 'in', label: 'India', flag: '🇮🇳', description: '印度新闻源' },
  { value: 'de', label: 'Germany', flag: '🇩🇪', description: '德国新闻源' },
  { value: 'fr', label: 'France', flag: '🇫🇷', description: '法国新闻源' },
  { value: 'it', label: 'Italy', flag: '🇮🇹', description: '意大利新闻源' },
  { value: 'es', label: 'Spain', flag: '🇪🇸', description: '西班牙新闻源' },
  { value: 'br', label: 'Brazil', flag: '🇧🇷', description: '巴西新闻源' },
  { value: 'mx', label: 'Mexico', flag: '🇲🇽', description: '墨西哥新闻源' },
  { value: 'jp', label: 'Japan', flag: '🇯🇵', description: '日本新闻源' },
  { value: 'kr', label: 'South Korea', flag: '🇰🇷', description: '韩国新闻源' },
  { value: 'cn', label: 'China', flag: '🇨🇳', description: '中国新闻源' },
  { value: 'ru', label: 'Russia', flag: '🇷🇺', description: '俄罗斯新闻源' }
]

// 支持的排序选项
export const SORT_OPTIONS = [
  { value: 'publishedAt', label: '按时间排序' },
  { value: 'relevance', label: '按相关性排序' }
]

// 获取语言选项
export const getLanguageOptions = () => SUPPORTED_LANGUAGES

// 获取国家选项
export const getCountryOptions = () => SUPPORTED_COUNTRIES

// 获取排序选项
export const getSortOptions = () => SORT_OPTIONS

// 获取语言标签（不带国旗，避免显示问题）
export const getLanguageLabel = (value: string): string => {
  const option = SUPPORTED_LANGUAGES.find(opt => opt.value === value)
  return option ? option.label : value
}

// 获取语言描述
export const getLanguageDescription = (value: string): string => {
  const option = SUPPORTED_LANGUAGES.find(opt => opt.value === value)
  return option ? option.description : value
}

// 获取国家标签（不带国旗，避免显示问题）
export const getCountryLabel = (value: string): string => {
  const option = SUPPORTED_COUNTRIES.find(opt => opt.value === value)
  return option ? option.label : value
}

// 获取国家描述
export const getCountryDescription = (value: string): string => {
  const option = SUPPORTED_COUNTRIES.find(opt => opt.value === value)
  return option ? option.description : value
}

// 获取排序标签
export const getSortLabel = (value: string): string => {
  const option = SORT_OPTIONS.find(opt => opt.value === value)
  return option ? option.label : value
}

// 验证语言是否支持
export const isValidLanguage = (language: string): boolean => {
  return SUPPORTED_LANGUAGES.some(opt => opt.value === language)
}

// 验证国家是否支持
export const isValidCountry = (country: string): boolean => {
  return SUPPORTED_COUNTRIES.some(opt => opt.value === country)
}

// 验证排序选项是否支持
export const isValidSortOption = (sortBy: string): boolean => {
  return SORT_OPTIONS.some(opt => opt.value === sortBy)
}
