// api/article.ts - 文章相关API
import request from '@/utils/request'

export interface Article {
  id: number
  title: string
  summary: string
  contentEn: string
  contentCn: string
  source: string
  publishTime: string
  readTime: number
  wordCount: number
  difficulty: string
  category: string
  imageUrl?: string
  isBookmarked: boolean
}

export interface ArticleListParams {
  page: number
  pageSize: number
  category?: string
  keyword?: string
  difficulty?: string
}

export interface ArticleListResponse {
  list: Article[]
  total: number
  hasMore: boolean
}

export interface ReadingRecord {
  articleId: number
  readingTime: number
  startTime: string
  endTime?: string
}

export const articleApi = {
  // 获取文章列表
  getArticleList: (params: ArticleListParams) => 
    request.get<ArticleListResponse>('/api/article/list', params),
  
  // 获取文章详情
  getArticleDetail: (id: number) => 
    request.get<Article>('/api/article/read/' + id),
  
  // 开始阅读
  startReading: (id: number) => 
    request.post(`/api/article/${id}/start-reading`),
  
  // 结束阅读
  endReading: (id: number, readingTime: number) => 
    request.post(`/api/article/${id}/end-reading`, { readingTime }),
  
  // 收藏/取消收藏
  toggleBookmark: (id: number) => 
    request.post(`/api/article/${id}/bookmark`),
  
  // 搜索文章
  searchArticles: (keyword: string, params?: Partial<ArticleListParams>) => 
    request.get<ArticleListResponse>('/api/article/search', { keyword, ...params }),
  
  // 获取推荐文章
  getRecommendedArticles: (limit: number = 10) => 
    request.get<Article[]>('/api/article/recommended', { limit }),
  
  // 获取分类文章
  getArticlesByCategory: (category: string, params?: Partial<ArticleListParams>) => 
    request.get<ArticleListResponse>('/api/article/category/' + category, params),
  
  // 获取热点文章
  getTopHeadlines: (limit: number = 20) => 
    request.get<Article[]>('/api/article/headlines', { limit })
}
