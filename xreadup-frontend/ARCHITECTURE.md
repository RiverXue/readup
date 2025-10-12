# XReadUp Frontend 架构设计文档

<div align="center">

![Architecture](https://img.shields.io/badge/Architecture-Dual_System-blue.svg)
![Pattern](https://img.shields.io/badge/Pattern-MVC_+_MVVM-green.svg)
![Design](https://img.shields.io/badge/Design-Component_Based-orange.svg)

**现代化双系统前端架构设计**

</div>

## 📋 架构概述

XReadUp Frontend 采用现代化的单页应用(SPA)架构，基于 Vue 3 + TypeScript 构建，结合微服务后端架构，旨在提供高性能、易扩展、用户体验良好的英语学习平台。采用**双系统架构设计**，分别服务于用户端学习功能和管理员端管理功能。

## 🏗 整体架构设计

### 系统架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                    XReadUp Frontend 架构                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │   用户端系统     │    │   管理员端系统   │                    │
│  │  User System    │    │  Admin System   │                    │
│  │                 │    │                 │                    │
│  │  ┌─────────────┐│    │  ┌─────────────┐│                    │
│  │  │App.vue      ││    │  │AdminLayout  ││                    │
│  │  │├─Header.vue ││    │  │├─Sidebar    ││                    │
│  │  │├─router-view││    │  │├─Header     ││                    │
│  │  │└─Footer.vue ││    │  │└─router-view││                    │
│  │  └─────────────┘│    │  └─────────────┘│                    │
│  └─────────────────┘    └─────────────────┘                    │
│           │                       │                             │
│           └───────────┬───────────┘                             │
│                       │                                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    路由层 (Router Layer)                    │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │User Routes  │ │Admin Routes │ │Route Guards │           │ │
│  │  │17个路由     │ │7个路由      │ │双守卫系统   │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                       │                                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    状态管理层 (State Layer)                 │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │User Store   │ │Admin Store  │ │Pinia Core   │           │ │
│  │  │用户状态     │ │管理员状态   │ │状态管理     │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                       │                                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    API 通信层 (API Layer)                   │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │User APIs    │ │Admin APIs   │ │HTTP Client  │           │ │
│  │  │用户接口     │ │管理员接口   │ │Axios封装    │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                       │                                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    组件层 (Component Layer)                │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │Layout Cmp   │ │Business Cmp │ │Common Cmp   │           │ │
│  │  │布局组件     │ │业务组件     │ │通用组件     │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 🎯 核心设计原则

### 1. 双系统隔离原则
- **用户端系统**：专注于学习功能，提供沉浸式学习体验
- **管理员端系统**：专注于管理功能，提供高效的管理工具
- **系统隔离**：两套独立的认证、路由、状态管理机制

### 2. 组件化设计原则
- **单一职责**：每个组件只负责一个功能模块
- **可复用性**：通用组件可在不同场景下复用
- **可维护性**：组件结构清晰，易于维护和扩展

### 3. 状态管理原则
- **集中管理**：全局状态统一管理
- **类型安全**：TypeScript 类型约束
- **持久化**：关键状态本地存储

### 4. 性能优化原则
- **懒加载**：路由和组件按需加载
- **缓存策略**：合理使用浏览器缓存
- **代码分割**：减少初始加载时间

## 🚀 技术栈架构

### 核心技术栈

| 层级 | 技术 | 版本 | 职责 |
|------|------|------|------|
| **框架层** | Vue 3 | 3.5.18 | 核心UI框架 |
| **语言层** | TypeScript | 5.8.0 | 静态类型检查 |
| **构建层** | Vite | 7.0.6 | 构建工具和开发服务器 |
| **UI层** | Element Plus | 2.11.2 | UI组件库 |
| **状态层** | Pinia | 3.0.3 | 状态管理 |
| **路由层** | Vue Router | 4.5.1 | 路由管理 |
| **网络层** | Axios | 1.12.2 | HTTP客户端 |
| **图表层** | ECharts | 5.4.3 | 数据可视化 |

### 开发工具链

| 工具 | 版本 | 用途 |
|------|------|------|
| **ESLint** | 9.31.0 | 代码质量检查 |
| **Vue DevTools** | 8.0.0 | 开发调试工具 |
| **Sass** | 1.93.0 | CSS预处理器 |

## 📁 项目结构设计

### 目录结构

```
src/
├── App.vue                 # 应用根组件
├── main.ts                 # 应用入口文件
├── assets/                 # 静态资源
│   ├── base.css           # 基础样式
│   ├── main.css           # 全局样式
│   ├── logo.svg           # SVG Logo
│   └── styles/            # 样式模块
│       └── mobile.css     # 移动端样式
├── api/                    # API接口模块
│   ├── admin/             # 管理员API
│   │   └── adminApi.ts    # 管理员接口封装
│   └── user/              # 用户API
├── components/            # 公共组件
│   ├── admin/             # 管理员组件
│   │   └── AdminLayout.vue # 管理员布局
│   ├── business/          # 业务组件
│   ├── common/            # 通用组件
│   │   ├── DictationModal.vue # 听写模态框
│   │   └── TTSControl.vue # TTS朗读控制
│   ├── layout/            # 布局组件
│   │   ├── Footer/        # 页脚组件
│   │   ├── Header/        # 导航栏组件
│   │   │   └── UserActions/ # 用户操作组件
│   │   └── Sidebar/       # 侧边栏组件
│   ├── ArticleCard.vue    # 文章卡片组件
│   ├── ArticleDiscovery.vue # 文章发现组件
│   ├── Footer.vue         # 全局页脚
│   ├── Header.vue         # 全局导航栏
│   ├── QuizComponent.vue  # 测验组件
│   └── icons/             # 图标组件
├── composables/           # 组合式函数
├── layouts/               # 布局模板
├── plugins/               # 插件配置
├── router/                # 路由配置
│   ├── guards/            # 路由守卫
│   │   ├── adminGuard.ts  # 管理员路由守卫
│   │   └── userGuard.ts   # 用户路由守卫
│   └── index.ts           # 路由定义与守卫
├── stores/                # Pinia 状态管理
│   ├── admin.ts           # 管理员状态管理
│   └── user.ts            # 用户状态管理
├── types/                 # TypeScript 类型定义
│   ├── admin.ts           # 管理员相关类型
│   ├── ai.ts              # AI 功能类型
│   ├── apiResponse.ts     # API 响应类型
│   ├── article.ts         # 文章相关类型
│   ├── report.ts          # 报告相关类型
│   ├── subscription.ts    # 订阅相关类型
│   ├── user.ts            # 用户相关类型
│   ├── vocabulary.ts      # 词汇相关类型
│   └── word.ts            # 单词相关类型
├── utils/                 # 工具函数
│   ├── admin.ts           # 管理员工具函数
│   ├── api.ts             # API 接口封装
│   ├── throttle.ts        # 节流函数
│   └── tts.ts             # TTS工具函数
└── views/                 # 页面组件
    ├── About/             # 关于页面组件
    ├── admin/             # 管理员页面
    │   ├── AdminDashboard.vue      # 管理员仪表盘
    │   ├── AdminLogin.vue          # 管理员登录
    │   ├── AdminUsersManagement.vue # 管理员用户管理
    │   ├── AiAnalysisView.vue      # AI分析视图
    │   ├── ArticleManagement.vue   # 文章管理
    │   ├── SubscriptionManagement.vue # 订阅管理
    │   ├── SystemSettings.vue     # 系统设置
    │   └── UserManagement.vue     # 用户管理
    ├── ArticleDiscovery.vue # 文章发现页面
    ├── ArticleListView.vue  # 文章列表页面
    ├── ArticleReader/       # 文章阅读器组件
    │   ├── Dialogs/        # 对话框组件
    │   ├── FloatingElements/ # 浮动元素
    │   ├── MainContent/    # 主要内容
    │   └── Sidebar/        # 侧边栏
    ├── ArticleReader.vue   # 文章阅读器
    ├── Auth/               # 认证相关页面
    │   └── components/     # 认证组件
    ├── Home/               # 首页组件
    │   └── components/     # 首页子组件
    ├── HomeView.vue        # 首页
    ├── LoginPage.vue       # 登录页面
    ├── QuizTest.vue        # 测验页面
    ├── RegisterView.vue    # 注册页面
    ├── Report/             # 报告页面组件
    │   └── components/     # 报告组件
    ├── ReportPage.vue      # 学习报告
    ├── Subscription/       # 订阅页面组件
    │   └── components/     # 订阅组件
    ├── SubscriptionPage.vue # 订阅管理页面
    ├── Vocabulary/         # 词汇页面组件
    │   └── components/     # 词汇组件
    └── VocabularyPage.vue  # 生词本
```

### 模块化设计

#### 1. API 模块化
```typescript
// 用户端API模块
export const articleApi = { ... }      // 文章相关接口
export const vocabularyApi = { ... }    // 词汇管理接口
export const aiApi = { ... }           // AI 功能接口
export const reportApi = { ... }       // 学习报告接口
export const learningApi = { ... }     // 学习记录接口
export const subscriptionApi = { ... } // 订阅管理接口

// 管理员端API模块
export const adminApi = { ... }        // 管理员接口
```

#### 2. 组件模块化
```typescript
// 布局组件
├── Header.vue              # 全局导航栏
├── Footer.vue              # 全局页脚
└── AdminLayout.vue         # 管理员布局

// 业务组件
├── ArticleCard.vue          # 文章卡片
├── ArticleDiscovery.vue    # 文章发现
├── QuizComponent.vue        # 测验组件
└── TTSControl.vue          # TTS控制

// 通用组件
├── DictationModal.vue       # 听写模态框
└── icons/                   # 图标组件
```

#### 3. 状态管理模块化
```typescript
// 用户状态管理
export const useUserStore = defineStore('user', () => {
  // 用户认证、订阅、AI权限等状态
})

// 管理员状态管理
export const useAdminStore = defineStore('admin', () => {
  // 管理员认证、权限、会话等状态
})
```

## 🎨 双系统架构设计

### 用户端系统架构

```
用户端系统 (User System)
├── 应用入口 (App.vue)
│   ├── 全局导航栏 (Header.vue)
│   │   ├── Logo 和品牌
│   │   ├── 导航菜单
│   │   ├── 用户状态显示
│   │   └── 每日打卡功能
│   ├── 页面内容区 (router-view)
│   │   ├── 首页 (HomeView.vue)
│   │   ├── 文章阅读器 (ArticleReader.vue)
│   │   ├── 生词本 (VocabularyPage.vue)
│   │   ├── 学习报告 (ReportPage.vue)
│   │   ├── 订阅管理 (SubscriptionPage.vue)
│   │   └── 认证页面 (LoginPage.vue, RegisterView.vue)
│   └── 全局页脚 (Footer.vue)
├── 状态管理 (User Store)
│   ├── 用户认证状态
│   ├── 订阅信息状态
│   ├── AI功能权限状态
│   └── 学习数据状态
└── 路由守卫 (User Guard)
    ├── 认证检查
    ├── 权限验证
    └── 自动重定向
```

### 管理员端系统架构

```
管理员端系统 (Admin System)
├── 管理员布局 (AdminLayout.vue)
│   ├── 管理员侧边栏 (AdminSidebar.vue)
│   │   ├── 导航菜单
│   │   ├── 权限控制
│   │   └── 用户信息
│   ├── 管理员导航栏 (AdminHeader.vue)
│   │   ├── 系统状态
│   │   ├── 快速操作
│   │   └── 退出登录
│   └── 页面内容区 (router-view)
│       ├── 仪表盘 (AdminDashboard.vue)
│       ├── 用户管理 (UserManagement.vue)
│       ├── 文章管理 (ArticleManagement.vue)
│       ├── 订阅管理 (SubscriptionManagement.vue)
│       ├── 系统设置 (SystemSettings.vue)
│       ├── AI分析 (AiAnalysisView.vue)
│       └── 管理员管理 (AdminUsersManagement.vue)
├── 状态管理 (Admin Store)
│   ├── 管理员认证状态
│   ├── 角色权限状态
│   ├── 会话管理状态
│   └── 系统配置状态
└── 路由守卫 (Admin Guard)
    ├── 管理员权限检查
    ├── 会话有效性验证
    └── 自动重验证机制
```

## 🔄 状态管理架构

### 双Store架构设计

#### 1. User Store 设计

```typescript
interface UserState {
  // 用户基本信息
  user: User | null
  token: string
  loading: boolean
  
  // 用户等级和权限
  tier: 'free' | 'basic' | 'pro' | 'enterprise'
  aiCalls: number
  
  // 订阅信息
  subscription: Subscription | null
}

// 核心功能模块
const useUserStore = defineStore('user', () => {
  // 认证模块
  const login = async (username: string, password: string) => { ... }
  const register = async (username: string, password: string, nickname?: string) => { ... }
  const logout = () => { ... }
  
  // 状态管理模块
  const initializeUser = async () => { ... }
  const fetchUserProfile = async () => { ... }
  const updateProfile = async (data: Partial<User>) => { ... }
  
  // 订阅管理模块
  const fetchSubscription = async () => { ... }
  const checkAiQuota = async () => { ... }
  
  return {
    // State
    user, token, loading, tier, aiCalls, subscription,
    // Computed
    isLoggedIn, userInfo, userStage, userTier, hasAIFeatures,
    // Actions
    login, register, logout, initializeUser, fetchUserProfile, 
    updateProfile, fetchSubscription, checkAiQuota
  }
})
```

#### 2. Admin Store 设计

```typescript
interface AdminState {
  // 管理员身份
  isAdminUser: boolean
  isSuperAdminUser: boolean
  role: AdminRole | null
  
  // 权限管理
  permissions: AdminPermission[]
  
  // 会话管理
  sessionExpiredAt: string
  lastLoginTime: string
  lastVerifiedTime: number | null
  
  // 状态控制
  loading: boolean
}

// 核心功能模块
const useAdminStore = defineStore('admin', () => {
  // 认证模块
  const login = async (params: LoginParams) => { ... }
  const verifyUserAsAdmin = async () => { ... }
  const logout = () => { ... }
  
  // 权限管理模块
  const setAdminState = (params: SetAdminStateParams) => { ... }
  const hasPermission = (permission: AdminPermission): boolean => { ... }
  const revalidateAdmin = async () => { ... }
  
  // 会话管理模块
  const extendSession = () => { ... }
  const saveAdminSession = () => { ... }
  const clearAdminState = () => { ... }
  
  return {
    // State
    isAdminUser, isSuperAdminUser, role, permissions, sessionExpiredAt,
    lastLoginTime, loading, lastVerifiedTime,
    // Computed
    hasAllPermissions, isSessionExpired, shouldReverify,
    // Actions
    login, verifyUserAsAdmin, setAdminState, hasPermission, 
    revalidateAdmin, extendSession, saveAdminSession, clearAdminState, logout
  }
})
```

### 状态管理特性

#### 1. 双系统隔离
- **独立状态**：用户状态和管理员状态完全独立
- **独立认证**：不同的认证机制和会话管理
- **独立权限**：基于不同角色的权限控制

#### 2. 智能状态恢复
- **自动初始化**：页面刷新后自动恢复用户状态
- **会话管理**：管理员会话自动验证和延长
- **Token管理**：Token过期自动清理和重定向

#### 3. 权限控制
- **用户权限**：基于订阅等级的AI功能权限
- **管理员权限**：基于角色的功能访问权限
- **动态权限**：实时权限验证和更新

#### 4. 状态同步
- **本地同步**：本地状态与服务器状态同步
- **实时更新**：订阅信息实时更新
- **权限验证**：管理员权限实时验证

## 🛣 路由架构设计

### 双路由守卫系统

#### 1. 用户路由守卫 (userGuard.ts)

```typescript
export const userGuard = async (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): Promise<void> => {
  if (to.meta.requiresAuth) {
    const userStore = useUserStore()
    const token = localStorage.getItem('token')
    
    if (token && token.trim() !== '') {
      try {
        // 初始化用户状态
        await userStore.initializeUser()
        
        // 验证用户身份
        if (userStore.isLoggedIn) {
          next()
        } else {
          // 验证失败，清除状态并重定向
          userStore.logout()
          next('/login')
        }
      } catch (error) {
        userStore.logout()
        next('/login')
      }
    } else {
      next('/login')
    }
  } else {
    next()
  }
}

// 登录页面重定向守卫
export const loginGuard = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void => {
  const token = localStorage.getItem('token')
  if (token) {
    next('/') // 已登录用户重定向到首页
  } else {
    next()
  }
}
```

#### 2. 管理员路由守卫 (adminGuard.ts)

```typescript
export const adminGuard = async (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): Promise<void> => {
  if (to.meta.requiresAdmin) {
    const adminStore = useAdminStore()
    
    // 检查是否已经通过了管理员验证并且会话有效
    if (adminStore.isAdminUser && !adminStore.isSessionExpired) {
      // 如果会话即将过期，自动延长会话有效期
      if (adminStore.shouldReverify) {
        try {
          await adminStore.revalidateAdmin()
        } catch (error) {
          console.error('管理员权限重新验证失败:', error)
        }
      }
      next()
    } else {
      // 未通过管理员验证，重定向到管理员登录页
      next('/admin/login')
    }
  } else {
    next()
  }
}

// 管理员登录页面重定向守卫
export const adminLoginGuard = (
  to: RouteLocationNormalized,
  from: RouteLocationNormalized,
  next: NavigationGuardNext
): void => {
  const adminStore = useAdminStore()
  if (adminStore.isAdminUser && !adminStore.isSessionExpired) {
    next('/admin/dashboard') // 已登录管理员重定向到仪表盘
  } else {
    next()
  }
}
```

### 路由配置结构

#### 用户端路由 (10个路由)

```typescript
const userRoutes = [
  // 公开路由
  { path: '/', name: 'home', component: HomeView },
  { path: '/article/:id', name: 'article', component: ArticleReader },
  { path: '/reading-list', name: 'readingList', component: ArticleListView },
  
  // 需要认证的路由
  { path: '/vocabulary', name: 'vocabulary', component: VocabularyPage, meta: { requiresAuth: true } },
  { path: '/report', name: 'report', component: ReportPage, meta: { requiresAuth: true } },
  { path: '/subscription', name: 'subscription', component: SubscriptionPage, meta: { requiresAuth: true } },
  
  // 认证相关路由
  { path: '/login', name: 'login', component: LoginPage, beforeEnter: loginGuard },
  { path: '/register', name: 'register', component: RegisterView, beforeEnter: loginGuard },
]
```

#### 管理员端路由 (7个路由)

```typescript
const adminRoutes = [
  // 管理员登录
  { path: '/admin/login', name: 'adminLogin', component: AdminLogin, beforeEnter: adminLoginGuard },
  
  // 管理员功能页面
  { path: '/admin/dashboard', name: 'adminDashboard', component: AdminDashboard, meta: { requiresAdmin: true } },
  { path: '/admin/users', name: 'adminUsers', component: UserManagement, meta: { requiresAdmin: true } },
  { path: '/admin/articles', name: 'adminArticles', component: ArticleManagement, meta: { requiresAdmin: true } },
  { path: '/admin/subscriptions', name: 'adminSubscriptions', component: SubscriptionManagement, meta: { requiresAdmin: true } },
  { path: '/admin/settings', name: 'adminSettings', component: SystemSettings, meta: { requiresAdmin: true } },
  { path: '/admin/ai-analysis', name: 'adminAiAnalysis', component: AiAnalysisView, meta: { requiresAdmin: true } },
  { path: '/admin/admins', name: 'adminAdmins', component: AdminUsersManagement, meta: { requiresAdmin: true } }
]
```

### 路由守卫执行顺序

```typescript
// 全局路由守卫 - 按优先级执行
router.beforeEach((to, from, next) => {
  // 1. 优先处理管理员路由
  if (to.meta.requiresAdmin) {
    adminGuard(to, from, next)
  } else if (to.meta.requiresAuth) {
    // 2. 处理需要用户认证的路由
    userGuard(to, from, next)
  } else {
    // 3. 公开路由直接放行
    next()
  }
})
```

### 路由特性

#### 1. 双重认证系统
- **用户认证**：基于JWT Token的用户身份验证
- **管理员认证**：独立的管理员权限验证系统

#### 2. 智能重定向
- **已登录用户**：访问登录页自动重定向到首页
- **已登录管理员**：访问登录页自动重定向到仪表盘
- **未授权访问**：自动重定向到对应登录页

#### 3. 会话管理
- **用户会话**：基于Token的会话管理
- **管理员会话**：独立的管理员会话管理（4小时有效期）

#### 4. 权限控制
- **页面级权限**：基于meta字段的权限控制
- **组件级权限**：基于Store状态的组件权限控制
- **功能级权限**：基于用户等级和管理员角色的功能权限

## 🔌 API 架构设计

### 双API系统架构

#### 1. HTTP 客户端封装 (utils/api.ts)

```typescript
// 创建axios实例
const api = axios.create({
  baseURL: '/', // 使用相对路径，利用Vite代理
  timeout: 600000, // 增加timeout至60秒以适应热点文章获取等耗时操作
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 智能Token管理
api.interceptors.request.use(
  (config) => {
    // 判断是否是管理员API请求
    const isAdminApi = config.url?.startsWith('/api/admin/') || 
                      config.url?.startsWith('/api/user/login') && config.data?.isAdminLogin
    
    // 检查是否需要跳过token验证
    const shouldSkipToken = config.url === '/api/admin/check'
    
    if (isAdminApi && !shouldSkipToken) {
      // 管理员API：使用管理员token
      const adminToken = localStorage.getItem('admin_token')
      if (adminToken) {
        config.headers.Authorization = `Bearer ${adminToken}`
      }
    } else {
      // 普通用户API：使用普通token
      const token = localStorage.getItem('token')
      const tokenExpiry = localStorage.getItem('tokenExpiry')
      const currentTime = Date.now()

      // 检查token是否已过期
      if (token && tokenExpiry && parseInt(tokenExpiry) < currentTime) {
        console.warn('Token已过期，将自动清除登录状态')
        localStorage.removeItem('token')
        localStorage.removeItem('tokenExpiry')
        localStorage.removeItem('user')
      } else if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }

    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 智能错误处理
api.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    // 处理401未授权错误
    if (error.response?.status === 401) {
      // 判断是管理员API还是普通用户API
      const isAdminApi = error.config?.url?.startsWith('/api/admin/')
      
      if (isAdminApi) {
        // 管理员API：清除管理员登录信息，重定向到管理员登录页
        localStorage.removeItem('adminSession')
        if (window.location.pathname !== '/admin/login') {
          window.location.href = '/admin/login'
        }
      } else {
        // 普通用户API：清除用户登录信息，重定向到用户登录页
        localStorage.removeItem('token')
        localStorage.removeItem('tokenExpiry')
        localStorage.removeItem('user')
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(error)
  }
)
```

#### 2. API 模块化设计

**用户端API模块**
```typescript
// 文章相关接口
export const articleApi = {
  getArticles: (params?: any) => api.get('/api/article/explore', { params }),
  getArticle: (id: string) => api.get(`/api/article/read/${id}`),
  getCategoryArticles: (category: string) => api.get(`/api/article/discover/category/${category}`),
  getTrendingArticles: () => api.get('/api/article/discover/trending')
}

// AI功能接口
export const aiApi = {
  translateText: (text: string, targetLanguage: string = 'zh') => 
    api.post('/api/ai/translate', { text, targetLanguage }),
  translateWord: (word: string) => 
    api.post('/api/ai/translate/word', { word }),
  generateSummary: (articleId: string) => 
    api.post('/api/ai/summary', { articleId }),
  chatWithAI: (question: string, context?: string) => 
    api.post('/api/ai/assistant/chat', { question, context }),
  generateQuiz: (articleId: string) => 
    api.post('/api/ai/quiz/generate', { articleId })
}

// 词汇管理接口
export const vocabularyApi = {
  addWord: (word: string, meaning: string, userId: number) => 
    api.post('/api/user/word/add', { word, meaning, userId }),
  getUserWords: (userId: number) => 
    api.get(`/api/user/word/list/${userId}`),
  batchLookup: (words: string[]) => 
    api.post('/api/vocabulary/batch-lookup', { words })
}

// 学习记录接口
export const learningApi = {
  recordReadingTime: (articleId: string, readingTime: number) => 
    api.post('/api/learning/reading-time', { articleId, readingTime }),
  dailyCheckIn: () => api.post('/api/learning/check-in'),
  getTodaySummary: () => api.get('/api/learning/today/summary')
}

// 报告相关接口
export const reportApi = {
  getDashboard: () => api.get('/api/report/dashboard'),
  getGrowthCurve: () => api.get('/api/report/growth-curve'),
  getReadingTime: () => api.get('/api/report/reading-time'),
  getTodaySummary: () => api.get('/api/report/today/summary')
}

// 订阅管理接口
export const subscriptionApi = {
  getPlans: () => api.get('/api/subscription/plans'),
  getUserSubscription: () => api.get('/api/user/subscription'),
  getUserUsage: () => api.get('/api/user/usage')
}
```

**管理员端API模块 (api/admin/adminApi.ts)**
```typescript
// 管理员认证
export const login = async (params: { username: string, password: string }) => 
  api.post('/api/admin/login', params)

export const checkAdmin = async (userId?: string | number) => 
  api.get('/api/admin/check', { params: { userId } })

// 用户管理
export const getUserList = (params?: { pageNum?: number, pageSize?: number, keyword?: string }) => 
  api.get('/api/admin/users', { params })

export const updateUser = (userId: number, data: any) => 
  api.put(`/api/admin/users/${userId}`, data)

export const disableUser = (userId: number) => 
  api.put(`/api/admin/users/${userId}/disable`)

export const enableUser = (userId: number) => 
  api.put(`/api/admin/users/${userId}/enable`)

// 文章管理
export const getArticleList = (params?: any) => 
  api.get('/api/admin/articles', { params })

export const getArticleDetail = (articleId: string) => 
  api.get(`/api/admin/articles/${articleId}`)

export const deleteArticle = (articleId: string) => 
  api.delete(`/api/admin/articles/${articleId}`)

export const publishArticle = (articleId: string) => 
  api.put(`/api/admin/articles/${articleId}/publish`)

// 订阅管理
export const getSubscriptionPlans = () => 
  api.get('/api/admin/subscription/plans')

export const createSubscriptionPlan = (data: any) => 
  api.post('/api/admin/subscription/plans', data)

export const updateSubscriptionPlan = (planId: string, data: any) => 
  api.put(`/api/admin/subscription/plans/${planId}`, data)

export const getUserSubscriptionList = (params?: any) => 
  api.get('/api/admin/subscription/users', { params })

export const updateUserSubscriptionStatus = (subscriptionId: string, status: string) => 
  api.put(`/api/admin/subscription/users/${subscriptionId}/status`, { status })

// 系统配置
export const getSystemConfigs = () => 
  api.get('/api/admin/system-config/all')

export const updateSystemConfig = (configKey: string, configValue: string) => 
  api.put('/api/admin/system-config', { configKey, configValue })

export const batchUpdateSystemConfigs = (configs: Record<string, string>) => 
  api.put('/api/admin/system-config/batch', { configs })

// AI分析
export const getAIAnalysisList = (params?: any) => 
  api.get('/api/admin/ai-analysis', { params })

export const getAIAnalysisDetail = (analysisId: string) => 
  api.get(`/api/admin/ai-analysis/${analysisId}`)

// 管理员管理
export const getAdminUsersList = (params?: any) => 
  api.get('/api/admin/admins', { params })

export const addAdminUser = (data: any) => 
  api.post('/api/admin/admins', data)

export const updateAdminUserRole = (userId: number, role: AdminRole) => 
  api.put(`/api/admin/admins/${userId}/role`, { role })

export const deleteAdminUser = (userId: number) => 
  api.delete(`/api/admin/admins/${userId}`)

// 系统监控
export const getAdminStats = () => 
  api.get('/api/admin/stats')

export const getDataTrends = () => 
  api.get('/api/admin/trends')

export const getRecentActivities = () => 
  api.get('/api/admin/activities')
```

#### 3. API特性

**1. 智能Token管理**
- **自动识别**：自动识别管理员API和用户API
- **分别认证**：分别使用不同的Token进行认证
- **自动处理**：自动处理Token过期和刷新

**2. 错误处理机制**
- **401错误**：自动重定向到对应登录页
- **管理员API错误**：重定向到管理员登录页
- **用户API错误**：重定向到用户登录页

**3. 请求优化**
- **60秒超时**：适应长时间操作
- **智能重试**：网络错误自动重试
- **请求去重**：防止重复请求

**4. 类型安全**
- **完整类型定义**：TypeScript类型定义
- **API响应类型检查**：响应数据类型验证
- **参数类型验证**：请求参数类型验证

## 🎨 UI/UX 设计架构

### 设计系统

#### 色彩体系
```css
:root {
  /* 主色调 */
  --primary-color: #409eff;
  --primary-light: #79bbff;
  --primary-dark: #337ecc;
  
  /* 功能色 */
  --success-color: #67c23a;
  --warning-color: #e6a23c;
  --danger-color: #f56c6c;
  --info-color: #909399;
  
  /* 中性色 */
  --text-primary: #303133;
  --text-regular: #606266;
  --text-secondary: #909399;
  --text-placeholder: #c0c4cc;
  
  /* 背景色 */
  --bg-primary: #ffffff;
  --bg-secondary: #f5f7fa;
  --bg-tertiary: #fafafa;
  
  /* 边框色 */
  --border-light: #ebeef5;
  --border-base: #dcdfe6;
  --border-dark: #d4d7de;
}
```

#### 响应式断点
```css
/* 移动端优先设计 */
@media (max-width: 480px) { 
  /* 小屏手机 */ 
}

@media (max-width: 768px) { 
  /* 平板 */ 
}

@media (max-width: 1024px) { 
  /* 小屏笔记本 */ 
}

@media (min-width: 1200px) { 
  /* 大屏显示器 */ 
}
```

#### 动画与交互
```css
/* 标准过渡 */
.transition-standard {
  transition: all 0.3s ease;
}

/* 悬停效果 */
.hover-effect {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.hover-effect:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

/* 触摸反馈 */
@media (hover: none) and (pointer: coarse) {
  .touch-feedback:active {
    transform: scale(0.98);
  }
}
```

### 组件设计规范

#### 1. Header 导航组件设计

```vue
<template>
  <header class="header">
    <div class="header-container">
      <!-- Logo 区域 -->
      <div class="logo-container" @click="goToHome">
        <img src="../../public/ReadUpLogonosans.png" class="logo-image" alt="品牌logo"/>
        <div class="logo-text">
          <h1>ReadUp</h1>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <ul>
          <li class="nav-item">
            <router-link to="/" class="nav-link">首页</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/article/1" class="nav-link">文章阅读</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/reading-list" class="nav-link">阅读列表</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <router-link to="/vocabulary" class="nav-link">生词本</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <router-link to="/report" class="nav-link">学习报告</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <router-link to="/subscription" class="nav-link">💎 会员</router-link>
          </li>
        </ul>
      </nav>

      <!-- 用户操作区域 -->
      <div class="user-actions">
        <div v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="btn btn-login">登录</router-link>
          <router-link to="/register" class="btn btn-register">注册</router-link>
        </div>

        <div v-else class="user-info">
          <!-- 每日打卡按钮 -->
          <el-button
            :icon="hasCheckedInToday ? Check : Clock"
            size="small"
            :type="hasCheckedInToday ? 'success' : 'primary'"
            @click="performCheckIn"
            :disabled="hasCheckedInToday"
            class="check-in-button"
          >
            {{ hasCheckedInToday ? '今日已打卡' : '每日打卡' }}
            <span v-if="streakDays > 0"> ({{ streakDays }}天)</span>
          </el-button>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              {{ userStore.userInfo?.username || '用户' }}
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </header>
</template>
```

#### 2. Footer 页脚组件设计

```vue
<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-content">
        <!-- 公司信息 -->
        <div class="footer-section">
          <h3>XReadUp</h3>
          <p>智能英语学习平台</p>
          <p>让英语学习变得更智能、更高效</p>
        </div>
        
        <!-- 快速链接 -->
        <div class="footer-section">
          <h4>快速链接</h4>
          <ul>
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/article/1">文章阅读</router-link></li>
            <li><router-link to="/vocabulary">生词本</router-link></li>
            <li><router-link to="/report">学习报告</router-link></li>
          </ul>
        </div>
        
        <!-- 联系方式 -->
        <div class="footer-section">
          <h4>联系我们</h4>
          <p>邮箱: dev@xreadup.com</p>
          <p>GitHub: github.com/xreadup</p>
        </div>
      </div>
      
      <!-- 版权信息 -->
      <div class="footer-bottom">
        <p>&copy; 2024 XReadUp. All rights reserved.</p>
      </div>
    </div>
  </footer>
</template>
```

#### 3. 表单组件规范

```vue
<template>
  <el-form
    ref="formRef"
    :model="form"
    :rules="rules"
    label-width="100px"
    class="form-container"
  >
    <el-form-item label="用户名" prop="username">
      <el-input
        v-model="form.username"
        placeholder="请输入用户名"
        :disabled="loading"
      />
    </el-form-item>
    
    <el-form-item label="密码" prop="password">
      <el-input
        v-model="form.password"
        type="password"
        placeholder="请输入密码"
        :disabled="loading"
        show-password
      />
    </el-form-item>
    
    <el-form-item>
      <el-button
        type="primary"
        @click="handleSubmit"
        :loading="loading"
        :disabled="loading"
      >
        {{ loading ? '提交中...' : '提交' }}
      </el-button>
    </el-form-item>
  </el-form>
</template>
```

## 🚀 核心功能架构

### 1. 智能阅读器架构

#### 组件结构设计

```
ArticleReader.vue
├── 侧边栏 (Sidebar)
│   ├── 用户状态卡片
│   ├── AI工具区域
│   ├── TTS控制组件
│   └── 生词本快捷操作
├── 主内容区 (MainContent)
│   ├── 文章标题
│   ├── 双语对照内容
│   ├── 单词点击交互
│   └── 阅读进度显示
├── 浮动元素 (FloatingElements)
│   ├── 单词详情弹窗
│   ├── AI功能面板
│   └── 加载状态指示器
└── 对话框 (Dialogs)
    ├── 翻译结果对话框
    ├── AI摘要对话框
    └── 测验生成对话框
```

#### 核心功能实现

```typescript
// 智能查词功能
const onWordClick = async (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const word = target.textContent?.trim()
  
  if (word && /^[a-zA-Z]+$/.test(word)) {
    try {
      // 显示加载状态
      setLoading(true)
      
      // 调用词汇查询API
      const result = await vocabularyApi.batchLookup([word])
      
      if (result.data && result.data.length > 0) {
        // 显示单词详情
        showWordDetail(result.data[0])
      }
    } catch (error) {
      console.error('查词失败:', error)
      ElMessage.error('查词失败，请稍后重试')
    } finally {
      setLoading(false)
    }
  }
}

// 双击朗读功能
const onWordDoubleClick = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const word = target.textContent?.trim()
  
  if (word && /^[a-zA-Z]+$/.test(word) && word.length <= 20) {
    // 调用TTS组件进行朗读
    ttsControlRef.value?.handleWordClick(event)
    
    // 添加视觉反馈
    target.style.backgroundColor = '#e3f2fd'
    setTimeout(() => {
      target.style.backgroundColor = ''
    }, 1000)
  }
}

// AI功能集成
const handleAITranslation = async () => {
  try {
    const result = await aiApi.translateText(articleContent.value, 'zh')
    showTranslationDialog(result.data.translatedText)
  } catch (error) {
    ElMessage.error('翻译失败，请稍后重试')
  }
}

const handleAISummary = async () => {
  try {
    const result = await aiApi.generateSummary(articleId.value)
    showSummaryDialog(result.data.summary)
  } catch (error) {
    ElMessage.error('摘要生成失败，请稍后重试')
  }
}
```

### 2. TTS语音朗读系统架构

#### TTS控制组件设计

```vue
<template>
  <div class="read-control-sidebar" v-if="showReadControlSidebar">
    <div class="read-control-header">
      <h3>朗读控制</h3>
      <el-button
        type="text"
        @click="handleStopSpeaking"
        class="close-control"
        title="关闭朗读控制"
      >
        <el-icon><CircleClose /></el-icon>
      </el-button>
    </div>

    <div class="read-control-content">
      <!-- 控制按钮组 -->
      <div class="control-buttons">
        <el-button
          @click="handlePauseResumeSpeaking"
          type="primary"
          size="large"
        >
          {{ isPaused ? '继续' : '暂停' }}
        </el-button>
        <el-button
          @click="handleStopSpeaking"
          type="danger"
          size="large"
        >
          停止
        </el-button>
      </div>

      <!-- 语速调节 -->
      <div class="speed-control">
        <label for="speed-slider">语速: {{ readingSpeed.toFixed(1) }}</label>
        <el-slider
          id="speed-slider"
          v-model="readingSpeed"
          :min="0.5"
          :max="2.0"
          :step="0.1"
          @change="handleSpeedChange"
        />
        <div class="speed-marks">
          <span>慢</span>
          <span>标准</span>
          <span>快</span>
        </div>
      </div>

      <!-- 语音选择 -->
      <div class="voice-selection">
        <label>语音选择</label>
        <el-select v-model="selectedVoice" @change="handleVoiceChange">
          <el-option
            v-for="voice in availableVoices"
            :key="voice.name"
            :label="voice.name"
            :value="voice.name"
          />
        </el-select>
      </div>
    </div>
  </div>
</template>
```

#### TTS功能实现

```typescript
// TTS工具函数 (utils/tts.ts)
export class TTSController {
  private synthesis: SpeechSynthesis
  private utterance: SpeechSynthesisUtterance | null = null
  private isPaused = false
  private currentText = ''
  
  constructor() {
    this.synthesis = window.speechSynthesis
  }

  // 朗读单词
  speakWord(word: string, speed: number = 1.0): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.synthesis) {
        reject(new Error('浏览器不支持语音合成'))
        return
      }

      // 停止当前朗读
      this.stopSpeaking()

      // 创建新的语音合成实例
      this.utterance = new SpeechSynthesisUtterance(word)
      this.utterance.rate = speed
      this.utterance.lang = 'en-US'
      this.utterance.volume = 1.0

      // 设置事件监听
      this.utterance.onend = () => resolve()
      this.utterance.onerror = (error) => reject(error)

      // 开始朗读
      this.synthesis.speak(this.utterance)
    })
  }

  // 暂停/继续朗读
  pauseResumeSpeaking(): void {
    if (this.isPaused) {
      this.synthesis.resume()
      this.isPaused = false
    } else {
      this.synthesis.pause()
      this.isPaused = true
    }
  }

  // 停止朗读
  stopSpeaking(): void {
    this.synthesis.cancel()
    this.isPaused = false
    this.utterance = null
  }

  // 获取可用语音列表
  getAvailableVoices(): SpeechSynthesisVoice[] {
    return this.synthesis.getVoices()
  }
}
```

### 3. 管理员系统架构

#### 管理员布局设计

```vue
<template>
  <div class="admin-layout">
    <!-- 管理员侧边栏 -->
    <AdminSidebar 
      :collapsed="sidebarCollapsed"
      @toggle="toggleSidebar"
    />
    
    <!-- 主内容区域 -->
    <div class="admin-main" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <!-- 管理员导航栏 -->
      <AdminHeader 
        :user-info="adminUserInfo"
        @logout="handleLogout"
        @toggle-sidebar="toggleSidebar"
      />
      
      <!-- 页面内容 -->
      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>
```

#### 管理员功能模块

```typescript
// 用户管理功能
const handleUserStatusToggle = async (userId: number, isActive: boolean) => {
  try {
    if (isActive) {
      await adminApi.enableUser(userId)
      ElMessage.success('用户已启用')
    } else {
      await adminApi.disableUser(userId)
      ElMessage.success('用户已禁用')
    }
    
    // 刷新用户列表
    await loadUserList()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 系统配置管理
const updateSystemConfig = async (configKey: string, configValue: string) => {
  try {
    await adminApi.updateSystemConfig(configKey, configValue)
    ElMessage.success('配置更新成功')
    
    // 刷新配置列表
    await loadSystemConfigs()
  } catch (error) {
    ElMessage.error('配置更新失败')
  }
}

// 批量更新配置
const batchUpdateConfigs = async (configs: Record<string, string>) => {
  try {
    await adminApi.batchUpdateSystemConfigs(configs)
    ElMessage.success('批量更新成功')
  } catch (error) {
    ElMessage.error('批量更新失败')
  }
}
```

## 📱 响应式设计架构

### 移动端适配策略

#### 1. 断点设计

```css
/* 移动端优先设计 */
:root {
  --mobile-breakpoint: 768px;
  --tablet-breakpoint: 1024px;
  --desktop-breakpoint: 1200px;
}

/* 小屏手机 */
@media (max-width: 480px) {
  .container {
    padding: 0 16px;
  }
  
  .nav-menu {
    display: none; /* 隐藏桌面导航 */
  }
  
  .mobile-menu {
    display: block; /* 显示移动端菜单 */
  }
}

/* 平板 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .sidebar.open {
    transform: translateX(0);
  }
}

/* 桌面端 */
@media (min-width: 1024px) {
  .container {
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .grid-layout {
    display: grid;
    grid-template-columns: 1fr 3fr;
    gap: 24px;
  }
}
```

#### 2. 触摸友好设计

```css
/* 触摸目标大小 */
.touch-target {
  min-height: 44px;
  min-width: 44px;
  padding: 12px 16px;
}

/* 触摸反馈 */
@media (hover: none) and (pointer: coarse) {
  .button:active {
    transform: scale(0.98);
    background-color: var(--primary-dark);
  }
  
  .card:active {
    transform: scale(0.99);
  }
}

/* 移动端导航 */
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 1px solid var(--border-light);
  display: flex;
  justify-content: space-around;
  padding: 8px 0;
  z-index: 1000;
}

.mobile-nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  text-decoration: none;
  color: var(--text-secondary);
}

.mobile-nav-item.active {
  color: var(--primary-color);
}
```

#### 3. 性能优化

```typescript
// 图片懒加载
const useLazyLoad = () => {
  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        const img = entry.target as HTMLImageElement
        img.src = img.dataset.src || ''
        observer.unobserve(img)
      }
    })
  })
  
  return { observer }
}

// 虚拟滚动
const useVirtualScroll = (items: any[], itemHeight: number, containerHeight: number) => {
  const visibleCount = Math.ceil(containerHeight / itemHeight)
  const startIndex = ref(0)
  const endIndex = computed(() => Math.min(startIndex.value + visibleCount, items.length))
  
  const visibleItems = computed(() => 
    items.slice(startIndex.value, endIndex.value)
  )
  
  return { visibleItems, startIndex, endIndex }
}
```

## 🧪 测试架构设计

### 测试策略

#### 1. 单元测试架构

```typescript
// 组件测试示例
import { mount } from '@vue/test-utils'
import { describe, it, expect } from 'vitest'
import ArticleReader from '@/views/ArticleReader.vue'

describe('ArticleReader', () => {
  it('should render article content', () => {
    const wrapper = mount(ArticleReader, {
      props: {
        articleId: '1'
      }
    })
    
    expect(wrapper.find('.article-content').exists()).toBe(true)
  })
  
  it('should handle word click', async () => {
    const wrapper = mount(ArticleReader)
    const wordElement = wrapper.find('.word')
    
    await wordElement.trigger('click')
    
    expect(wrapper.emitted('word-click')).toBeTruthy()
  })
})
```

#### 2. 集成测试架构

```typescript
// API集成测试
import { describe, it, expect } from 'vitest'
import { articleApi } from '@/utils/api'

describe('Article API', () => {
  it('should fetch article list', async () => {
    const result = await articleApi.getArticles()
    
    expect(result).toHaveProperty('data')
    expect(Array.isArray(result.data)).toBe(true)
  })
  
  it('should fetch article detail', async () => {
    const result = await articleApi.getArticle('1')
    
    expect(result).toHaveProperty('data')
    expect(result.data).toHaveProperty('id')
  })
})
```

#### 3. E2E测试架构

```typescript
// Cypress E2E测试
describe('User Journey', () => {
  it('should complete article reading flow', () => {
    cy.visit('/')
    cy.get('[data-testid="article-card"]').first().click()
    cy.get('[data-testid="article-content"]').should('be.visible')
    cy.get('[data-testid="word"]').first().click()
    cy.get('[data-testid="word-detail"]').should('be.visible')
  })
})
```

## 🚀 部署架构设计

### 构建优化

#### 1. Vite配置优化

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools()
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  build: {
    target: 'es2020',
    outDir: 'dist',
    sourcemap: false,
    minify: 'terser',
    rollupOptions: {
      output: {
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]',
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'ui-vendor': ['element-plus'],
          'chart-vendor': ['echarts']
        }
      }
    }
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      }
    }
  }
})
```

#### 2. 代码分割策略

```typescript
// 路由懒加载
const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue')
  },
  {
    path: '/article/:id',
    name: 'article',
    component: () => import('@/views/ArticleReader.vue')
  },
  {
    path: '/admin/dashboard',
    name: 'adminDashboard',
    component: () => import('@/views/admin/AdminDashboard.vue')
  }
]

// 组件懒加载
const AsyncComponent = defineAsyncComponent(() => 
  import('@/components/HeavyComponent.vue')
)
```

#### 3. 缓存策略

```typescript
// Service Worker缓存策略
const CACHE_NAME = 'xreadup-v1'
const CACHE_URLS = [
  '/',
  '/static/js/app.js',
  '/static/css/app.css'
]

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => cache.addAll(CACHE_URLS))
  )
})

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request)
      .then(response => response || fetch(event.request))
  )
})
```

## 🔒 安全架构设计

### 前端安全策略

#### 1. XSS防护

```typescript
// 内容安全策略
const sanitizeHtml = (html: string): string => {
  const div = document.createElement('div')
  div.textContent = html
  return div.innerHTML
}

// 输入验证
const validateInput = (input: string): boolean => {
  const dangerousPatterns = [
    /<script/i,
    /javascript:/i,
    /on\w+\s*=/i
  ]
  
  return !dangerousPatterns.some(pattern => pattern.test(input))
}
```

#### 2. CSRF防护

```typescript
// CSRF Token管理
const getCSRFToken = (): string => {
  return document.querySelector('meta[name="csrf-token"]')?.getAttribute('content') || ''
}

// 请求头添加CSRF Token
api.interceptors.request.use(config => {
  const token = getCSRFToken()
  if (token) {
    config.headers['X-CSRF-Token'] = token
  }
  return config
})
```

#### 3. 敏感信息保护

```typescript
// Token安全存储
const secureStorage = {
  setItem: (key: string, value: string) => {
    try {
      localStorage.setItem(key, value)
    } catch (error) {
      console.error('存储失败:', error)
    }
  },
  
  getItem: (key: string): string | null => {
    try {
      return localStorage.getItem(key)
    } catch (error) {
      console.error('读取失败:', error)
      return null
    }
  },
  
  removeItem: (key: string) => {
    try {
      localStorage.removeItem(key)
    } catch (error) {
      console.error('删除失败:', error)
    }
  }
}
```

## 📈 性能监控架构

### 性能指标监控

#### 1. Web Vitals监控

```typescript
// 性能监控
const performanceMonitor = {
  // 监控Core Web Vitals
  measureWebVitals: () => {
    // LCP (Largest Contentful Paint)
    new PerformanceObserver((list) => {
      const entries = list.getEntries()
      const lastEntry = entries[entries.length - 1]
      console.log('LCP:', lastEntry.startTime)
    }).observe({ entryTypes: ['largest-contentful-paint'] })
    
    // FID (First Input Delay)
    new PerformanceObserver((list) => {
      const entries = list.getEntries()
      entries.forEach(entry => {
        console.log('FID:', entry.processingStart - entry.startTime)
      })
    }).observe({ entryTypes: ['first-input'] })
    
    // CLS (Cumulative Layout Shift)
    let clsValue = 0
    new PerformanceObserver((list) => {
      const entries = list.getEntries()
      entries.forEach(entry => {
        if (!entry.hadRecentInput) {
          clsValue += entry.value
        }
      })
      console.log('CLS:', clsValue)
    }).observe({ entryTypes: ['layout-shift'] })
  },
  
  // 监控页面加载时间
  measurePageLoad: () => {
    window.addEventListener('load', () => {
      const navigation = performance.getEntriesByType('navigation')[0]
      console.log('页面加载时间:', navigation.loadEventEnd - navigation.fetchStart)
    })
  }
}
```

#### 2. 错误监控

```typescript
// 错误监控
const errorMonitor = {
  // 全局错误捕获
  init: () => {
    window.addEventListener('error', (event) => {
      console.error('JavaScript错误:', event.error)
      // 发送错误报告到服务器
      this.reportError(event.error)
    })
    
    window.addEventListener('unhandledrejection', (event) => {
      console.error('Promise拒绝:', event.reason)
      // 发送错误报告到服务器
      this.reportError(event.reason)
    })
  },
  
  // 错误报告
  reportError: (error: Error) => {
    const errorReport = {
      message: error.message,
      stack: error.stack,
      url: window.location.href,
      timestamp: new Date().toISOString(),
      userAgent: navigator.userAgent
    }
    
    // 发送到错误收集服务
    fetch('/api/errors', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(errorReport)
    }).catch(console.error)
  }
}
```

## 🤝 开发规范架构

### 代码规范

#### 1. 命名约定

```typescript
// 组件命名：PascalCase
const ArticleReader = defineComponent({...})
const UserManagement = defineComponent({...})

// 变量命名：camelCase
const userInfo = ref<UserInfo>()
const isLoading = ref(false)
const articleList = ref<Article[]>([])

// 常量命名：UPPER_SNAKE_CASE
const API_BASE_URL = 'https://api.xreadup.com'
const MAX_RETRY_COUNT = 3
const DEFAULT_PAGE_SIZE = 10

// 文件命名：kebab-case
// article-reader.vue
// user-management.ts
// api-client.ts
```

#### 2. TypeScript规范

```typescript
// 接口定义
interface UserInfo {
  id: number
  username: string
  email?: string
  avatar?: string
  createdAt: string
  updatedAt: string
}

// 类型声明
type LoadingState = 'idle' | 'loading' | 'success' | 'error'
type UserRole = 'user' | 'admin' | 'super_admin'

// 泛型使用
interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: string
}

// 函数类型
type EventHandler<T = any> = (event: T) => void
type AsyncFunction<T, R> = (param: T) => Promise<R>
```

#### 3. Vue组件规范

```vue
<template>
  <!-- 使用语义化标签 -->
  <article class="article-container">
    <header class="article-header">
      <h1>{{ title }}</h1>
      <time :datetime="publishDate">{{ formatDate(publishDate) }}</time>
    </header>
    <main class="article-content">
      <div v-html="sanitizedContent"></div>
    </main>
    <footer class="article-footer">
      <div class="article-meta">
        <span>阅读时长: {{ readingTime }}分钟</span>
        <span>字数: {{ wordCount }}</span>
      </div>
    </footer>
  </article>
</template>

<script setup lang="ts">
// 导入顺序：Vue -> 第三方库 -> 自定义模块
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

// Props 定义
interface Props {
  title: string
  content: string
  publishDate: string
  readingTime?: number
  wordCount?: number
}

const props = withDefaults(defineProps<Props>(), {
  readingTime: 0,
  wordCount: 0
})

// Emits 定义
interface Emits {
  update: [value: string]
  close: []
  share: [platform: string]
}

const emit = defineEmits<Emits>()

// 响应式数据
const isLoading = ref(false)
const userStore = useUserStore()
const router = useRouter()

// 计算属性
const sanitizedContent = computed(() => {
  return sanitizeHtml(props.content)
})

// 方法
const handleShare = (platform: string) => {
  emit('share', platform)
}

const formatDate = (date: string): string => {
  return new Date(date).toLocaleDateString('zh-CN')
}

// 生命周期
onMounted(() => {
  console.log('ArticleReader mounted')
})

onUnmounted(() => {
  console.log('ArticleReader unmounted')
})
</script>

<style scoped>
/* 使用 BEM 命名规范 */
.article-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.article-header {
  padding: 20px 0;
  border-bottom: 1px solid var(--border-light);
}

.article-content {
  padding: 20px 0;
  line-height: 1.6;
}

.article-footer {
  padding: 20px 0;
  border-top: 1px solid var(--border-light);
}

.article-meta {
  display: flex;
  gap: 20px;
  color: var(--text-secondary);
  font-size: 14px;
}
</style>
```

## 📚 总结

XReadUp Frontend 采用现代化的双系统架构设计，通过以下核心特性实现了高性能、易扩展、用户体验良好的英语学习平台：

### 🎯 核心优势

1. **双系统架构**：用户端和管理员端完全隔离，各自独立发展
2. **组件化设计**：高度模块化的组件结构，易于维护和扩展
3. **类型安全**：完整的TypeScript类型系统，提升代码质量
4. **性能优化**：代码分割、懒加载、缓存策略等多重优化
5. **响应式设计**：完美适配各种设备尺寸
6. **安全防护**：XSS、CSRF等多重安全防护机制

### 🚀 技术特色

- **Vue 3 + TypeScript**：现代化前端技术栈
- **Element Plus**：企业级UI组件库
- **Pinia**：轻量级状态管理
- **Vite**：极速构建工具
- **ECharts**：强大的数据可视化
- **双路由守卫**：智能权限控制
- **TTS集成**：语音朗读功能
- **AI功能**：智能学习辅助

### 📈 扩展性

- **模块化架构**：易于添加新功能模块
- **API抽象**：统一的API调用接口
- **主题系统**：支持主题定制
- **国际化**：支持多语言扩展
- **插件系统**：支持功能插件扩展

这个架构设计为XReadUp提供了坚实的技术基础，支持项目的长期发展和功能扩展。

---

<div align="center">

**🏗️ 构建现代化前端架构，打造智能学习体验**

*设计于 ❤️ 与最佳实践*

</div>