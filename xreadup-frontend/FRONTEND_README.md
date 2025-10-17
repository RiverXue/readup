# XReadUp Frontend - 智能英语学习平台前端

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.18-4FC08D?logo=vue.js)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.0-3178C6?logo=typescript)
![Element Plus](https://img.shields.io/badge/Element_Plus-2.11.2-409EFF?logo=element)
![Vite](https://img.shields.io/badge/Vite-7.0.6-646CFF?logo=vite)

</div>

## 📖 项目概述

XReadUp Frontend 是一个基于 Vue 3 + TypeScript 构建的现代化智能英语学习平台前端应用。采用 Composition API、Element Plus UI 组件库和 Pinia 状态管理，提供了丰富的英语学习功能，包括智能阅读、生词管理、学习报告、AI 助手和订阅管理等核心模块。

### 🎯 核心特性

- **🤖 AI 驱动学习** - 集成多种 AI 功能：智能翻译、文章摘要、语法解析、个性化问答
- **🎓 AI 学习导师** - Rayda老师专业英语学习导师，提供个性化学习指导
- **📊 智能用户画像** - 多维度学习数据分析，智能识别薄弱环节和优势
- **📚 智能阅读器** - 双语对照阅读，点击查词，双击朗读，上下文理解
- **🔍 三级词库系统** - 本地缓存 + 共享词库 + AI 实时生成
- **📊 数据可视化报告** - ECharts 图表展示学习进度和成就
- **📱 响应式设计** - 完美适配桌面端和移动端
- **🎨 现代化 UI** - Element Plus 组件库，优雅的用户界面
- **⚡ 高性能架构** - Vite 构建，热更新，TypeScript 类型安全
- **💳 灵活订阅管理** - 多种订阅方案，自动配额管理，实时更新
- **📅 每日打卡功能** - 自动和手动打卡，连续打卡统计，状态持久化
- **🎨 设计系统** - 完整的UI设计系统，包含颜色、组件、动画规范
- **📖 单词复习系统** - 智能复习算法，听写练习，掌握程度追踪

## 🛠 技术栈

### 核心框架
- **Vue 3.5.18** - 渐进式前端框架，使用 Composition API
- **TypeScript 5.8.0** - 静态类型检查，提升代码质量
- **Vite 7.0.6** - 极速构建工具，支持热模块替换

### UI 组件与样式
- **Element Plus 2.11.2** - 企业级 Vue 3 组件库
- **Element Plus Icons** - 丰富的图标库
- **自定义 CSS** - 响应式设计，移动端适配

### 状态管理与路由
- **Pinia 3.0.3** - Vue 3 官方状态管理库
- **Vue Router 4.5.1** - 官方路由管理器

### 数据可视化
- **ECharts 5.4.3** - 强大的数据可视化图表库

### 开发工具
- **ESLint** - 代码质量检查
- **Vue DevTools** - 开发调试工具

## 📁 项目架构

```
xreadup-frontend/
├── public/                      # 静态资源
│   ├── favicon.ico             # 网站图标
│   ├── ReadUpLogo.png          # 品牌Logo
│   └── lineReadUpLogonosans.png # 无字tiLogo
├── src/                        # 源代码目录
│   ├── App.vue                 # 根组件
│   ├── main.ts                 # 应用入口
│   ├── assets/                 # 静态资源
│   │   ├── base.css           # 基础样式
│   │   ├── main.css           # 全局样式
│   │   ├── design-system.css  # 设计系统样式
│   │   ├── logo.svg           # SVG Logo
│   │   └── styles/            # 样式模块
│   │       └── mobile.css     # 移动端样式
│   ├── api/                    # API接口模块
│   │   ├── admin/             # 管理员API
│   │   │   └── adminApi.ts    # 管理员接口封装
│   │   └── user/              # 用户API
│   ├── components/            # 公共组件
│   │   ├── admin/             # 管理员组件
│   │   │   └── AdminLayout.vue # 管理员布局
│   │   ├── business/          # 业务组件
│   │   ├── common/            # 通用组件
│   │   │   ├── DictationModal.vue # 听写模态框
│   │   │   ├── SmartLoading.vue   # 智能加载组件
│   │   │   └── TTSControl.vue     # TTS朗读控制
│   │   ├── layout/            # 布局组件
│   │   │   ├── Footer/        # 页脚组件
│   │   │   ├── Header/        # 导航栏组件
│   │   │   │   └── UserActions/ # 用户操作组件
│   │   │   └── Sidebar/       # 侧边栏组件
│   │   ├── ArticleCard.vue    # 文章卡片组件
│   │   ├── ArticleDiscovery.vue # 文章发现组件
│   │   ├── Footer.vue         # 全局页脚
│   │   ├── Header.vue         # 全局导航栏
│   │   ├── QuizComponent.vue  # 测验组件
│   │   └── icons/             # 图标组件
│   ├── composables/           # 组合式函数
│   ├── layouts/               # 布局模板
│   ├── plugins/               # 插件配置
│   ├── router/                # 路由配置
│   │   ├── guards/            # 路由守卫
│   │   │   ├── adminGuard.ts  # 管理员路由守卫
│   │   │   └── userGuard.ts   # 用户路由守卫
│   │   └── index.ts           # 路由定义与守卫
│   ├── stores/                # Pinia 状态管理
│   │   ├── admin.ts           # 管理员状态管理
│   │   └── user.ts            # 用户状态管理
│   ├── types/                 # TypeScript 类型定义
│   │   ├── admin.ts           # 管理员相关类型
│   │   ├── ai.ts              # AI 功能类型
│   │   ├── apiResponse.ts     # API 响应类型
│   │   ├── article.ts         # 文章相关类型
│   │   ├── report.ts          # 报告相关类型
│   │   ├── subscription.ts    # 订阅相关类型
│   │   ├── user.ts            # 用户相关类型
│   │   ├── vocabulary.ts      # 词汇相关类型
│   │   └── word.ts            # 单词相关类型
│   ├── utils/                 # 工具函数
│   │   ├── admin.ts           # 管理员工具函数
│   │   ├── api.ts             # API 接口封装
│   │   ├── throttle.ts        # 节流函数
│   │   └── tts.ts             # TTS工具函数
│   └── views/                 # 页面组件
│       ├── About/             # 关于页面组件
│       ├── admin/             # 管理员页面
│       │   ├── AdminDashboard.vue      # 管理员仪表盘
│       │   ├── AdminLogin.vue          # 管理员登录
│       │   ├── AdminUsersManagement.vue # 管理员用户管理
│       │   ├── AiAnalysisView.vue      # AI分析视图
│       │   ├── ArticleManagement.vue   # 文章管理
│       │   ├── SubscriptionManagement.vue # 订阅管理
│       │   ├── SystemSettings.vue     # 系统设置
│       │   └── UserManagement.vue     # 用户管理
│       ├── ArticleDiscovery.vue # 文章发现页面
│       ├── ArticleListView.vue  # 文章列表页面
│       ├── ArticleReader/       # 文章阅读器组件
│       │   ├── Dialogs/        # 对话框组件
│       │   ├── FloatingElements/ # 浮动元素
│       │   ├── MainContent/    # 主要内容
│       │   └── Sidebar/        # 侧边栏
│       ├── ArticleReader.vue   # 文章阅读器
│       ├── Auth/               # 认证相关页面
│       │   └── components/     # 认证组件
│       ├── Home/               # 首页组件
│       │   └── components/     # 首页子组件
│       ├── HomeView.vue        # 首页
│       ├── LoginPage.vue       # 登录页面
│       ├── QuizTest.vue        # 测验页面
│       ├── RegisterView.vue    # 注册页面
│       ├── Report/             # 报告页面组件
│       │   └── components/     # 报告组件
│       ├── ReportPage.vue      # 学习报告
│       ├── Subscription/       # 订阅页面组件
│       │   └── components/     # 订阅组件
│       ├── SubscriptionPage.vue # 订阅管理页面
│       ├── Vocabulary/         # 词汇页面组件
│       │   └── components/     # 词汇组件
│       ├── VocabularyPage.vue  # 生词本
│       └── UIDemoPage.vue      # UI演示页面
├── dist/                      # 构建输出目录
├── node_modules/              # 依赖包
├── env.d.ts                   # 环境类型声明
├── eslint.config.ts           # ESLint 配置
├── index.html                 # HTML 模板
├── package-lock.json          # 依赖锁定文件
├── package.json               # 项目依赖配置
├── tsconfig.app.json          # TypeScript 应用配置
├── tsconfig.json              # TypeScript 配置
├── tsconfig.node.json         # TypeScript Node 配置
└── vite.config.ts             # Vite 构建配置

```

## 🏗 核心架构设计

### 双系统架构

XReadUp Frontend 采用**双系统架构**，包含用户端和管理员端两个独立的系统：

#### 1. 用户端系统 (User System)
```
App.vue
├── Header.vue (用户导航栏)
├── router-view (用户页面内容区)
└── Footer.vue (全局页脚)
```

#### 2. 管理员端系统 (Admin System)
```
AdminLayout.vue
├── AdminSidebar.vue (管理员侧边栏)
├── AdminHeader.vue (管理员导航栏)
└── router-view (管理员页面内容区)
```

### 页面组件设计

#### 用户端页面组件

**HomeView.vue - 首页**
- 功能展示：智能阅读、生词本、学习报告、AI助手
- 推荐文章列表：动态加载最新文章
- 用户引导：未登录用户的登录提示
- 每日打卡：连续打卡统计和状态显示

**ArticleReader.vue - 文章阅读器**
- 双语对照：英文原文 + 中文翻译
- 智能查词：点击单词即时查询
- 双击朗读：TTS语音朗读功能
- AI 功能集成：翻译、摘要、解析、问答
- 生词管理：一键添加到生词本
- 阅读时长记录：自动记录有效阅读时间

**VocabularyPage.vue - 生词本**
- 词汇统计：总量、今日新增、待复习
- 智能筛选：状态过滤、关键词搜索
- 复习系统：智能复习算法，追踪掌握程度
- 听写功能：单词听写练习
- 系统性学习：今日复习、听写练习

#### 单词复习系统详解

**智能复习算法**
```typescript
// 基于艾宾浩斯遗忘曲线的复习间隔
const reviewIntervals = {
  'NEW': 1,        // 新单词：1天后复习
  'LEARNING': 3,   // 学习中：3天后复习
  'REVIEWING': 7,  // 复习中：7天后复习
  'MASTERED': 30   // 已掌握：30天后复习
}

// 掌握程度评估
const calculateMasteryLevel = (correctCount: number, totalCount: number) => {
  const accuracy = correctCount / totalCount
  if (accuracy >= 0.9) return 'MASTERED'
  if (accuracy >= 0.7) return 'REVIEWING'
  if (accuracy >= 0.5) return 'LEARNING'
  return 'NEW'
}
```

**听写练习功能**
- **TTS语音朗读** - 支持多种语音选择，可调节语速
- **实时反馈** - 即时显示正确/错误状态
- **进度追踪** - 记录听写准确率和完成情况
- **智能提示** - 根据错误类型提供针对性建议

**复习状态管理**
- **NEW** - 新添加的单词，需要首次学习
- **LEARNING** - 正在学习中的单词，需要定期复习
- **REVIEWING** - 复习中的单词，巩固记忆
- **MASTERED** - 已掌握的单词，长期记忆

**学习数据统计**
- 每日学习单词数量
- 复习准确率统计
- 学习进度可视化
- 连续学习天数记录

**ReportPage.vue - 学习报告**
- 数据可视化：ECharts 图表展示
- 学习统计：词汇量、阅读时长、连续打卡
- 成就系统：学习里程碑和进度追踪
- 周报功能：每周学习总结

**AIAssistantView.vue - AI学习导师**
- 专业导师：Rayda老师专业英语学习导师
- 学习画像：多维度学习数据分析展示
- 学习诊断：智能识别薄弱环节和优势
- 个性化指导：基于学习数据的定制建议
- 智能问答：多轮对话，上下文理解
- 问题推荐：基于学习状态的个性化问题
- 学习建议：具体可操作的学习计划

#### AI学习导师系统详解

**Rayda老师身份定位**
- **专业身份**：经验丰富的英语学习导师
- **教学风格**：耐心、专业、鼓励式教学
- **服务范围**：英语阅读、词汇、语法学习指导
- **个性化**：基于用户学习数据的定制化指导

**学习画像系统**
```typescript
interface UserProfile {
  // 基础学习数据
  learningDays: number              // 学习天数
  totalArticlesRead: number         // 已读文章数
  vocabularyCount: number           // 学习词汇总数
  averageReadTime: number           // 平均阅读时长
  totalReadTime: number             // 总阅读时长
  readingStreak: number             // 连续学习天数
  
  // 学习偏好数据
  preferredCategories: string[]     // 偏好分类
  currentLevel: string              // 当前学习水平
  averageDifficulty: string         // 平均难度等级
  
  // 词汇学习数据
  newWords: number                  // 新词数量
  learningWords: number             // 学习中词汇数量
  masteredWords: number             // 已掌握词汇数量
  
  // 学习分析数据
  weakAreas: string[]               // 薄弱环节列表
}
```

**学习诊断算法**
- **学习水平评估**：基于学习天数、文章数、词汇量综合评估
- **薄弱环节识别**：多维度分析学习状态，识别需要提升的方面
- **优势能力分析**：识别用户的学习优势和强项
- **学习建议生成**：基于分析结果生成具体可操作的建议

**个性化问题推荐**
- **8种问题类型**：个性化进度、分类提升、词汇扩展、阅读效率等
- **智能推荐**：基于用户学习数据动态生成问题
- **问题分类**：词汇学习、语法解析、阅读技巧、学习规划等

**学习诊断类型**
- **学习行为类**：学习坚持性、学习习惯、阅读专注力
- **内容消费类**：阅读练习、词汇积累
- **学习效果类**：新词掌握、词汇巩固、复习频率、掌握率等

**SubscriptionPage.vue - 订阅管理**
- 套餐展示：多种订阅方案（免费、基础、专业、企业版）
- 额度管理：阅读篇数、单词量、AI功能使用情况
- 实时数据：完全从后端API获取，避免前端硬编码
- 订阅操作：升级、降级、取消订阅功能

**QuizTest.vue - 测验页面**
- AI生成测验：基于文章内容生成题目
- 多种题型：选择题、填空题、问答题
- 实时评分：即时反馈和答案解析

**UIDemoPage.vue - UI演示页面**
- 设计系统展示：完整的UI组件库演示
- 颜色系统：主色调、语义化颜色、渐变效果
- 组件系统：按钮、卡片、标签、表单等组件
- 加载状态：旋转器、脉冲、波浪、骨架屏
- 动画效果：过渡动画、微交互、响应式效果
- 响应式设计：移动端、平板、桌面端适配

**LoginPage.vue - 登录页面**
- 用户身份验证：账号密码登录
- 错误处理：友好的登录失败提示
- 自动重定向：已登录用户自动跳转

**RegisterView.vue - 注册页面**
- 新用户注册：账号密码设置
- 验证逻辑：表单验证和错误提示

#### 管理员端页面组件

**AdminDashboard.vue - 管理员仪表盘**
- 数据概览：用户数、文章数、订阅数、AI调用数
- 实时统计：图表展示系统运行状态
- 快速操作：常用管理功能入口
- 系统监控：服务状态和性能指标

**AdminLogin.vue - 管理员登录**
- 独立登录：管理员专用登录接口
- 权限验证：角色和权限检查
- 会话管理：管理员会话独立管理

**UserManagement.vue - 用户管理**
- 用户列表：分页展示所有用户
- 用户搜索：按用户名、手机号、标签筛选
- 状态管理：启用/禁用用户账户
- 用户详情：查看用户详细信息和学习数据

**ArticleManagement.vue - 文章管理**
- 文章列表：分页展示所有文章
- 文章搜索：按标题、来源、难度筛选
- 文章操作：发布、删除、审核文章
- 文章详情：查看文章内容和AI分析结果

**SubscriptionManagement.vue - 订阅管理**
- 订阅方案：管理所有订阅套餐
- 用户订阅：查看用户订阅记录
- 订阅统计：订阅数据分析和趋势
- 套餐配置：价格和功能配置

**SystemSettings.vue - 系统设置**
- 功能开关：维护模式、功能开关管理
- 业务限制：阅读限制、AI调用限制
- 系统配置：分类管理配置项
- 实时更新：配置变更即时生效

**AiAnalysisView.vue - AI分析管理**
- AI结果：查看AI分析结果
- 服务监控：AI服务状态检查
- 分析统计：AI使用情况统计

**AdminUsersManagement.vue - 管理员用户管理**
- 管理员列表：查看所有管理员
- 权限管理：分配和修改管理员权限
- 角色管理：超级管理员和普通管理员
- 管理员操作：添加、删除、更新管理员

### 状态管理架构

#### 双Store架构设计

XReadUp Frontend 采用**双Store架构**，分别管理用户端和管理员端的状态：

#### 1. User Store (stores/user.ts) - 用户状态管理

```typescript
interface UserState {
  user: User | null
  token: string
  loading: boolean
  tier: 'free' | 'basic' | 'pro' | 'enterprise'
  aiCalls: number
  subscription: Subscription | null
}

// 核心功能
- 用户认证：login、register、logout
- 状态持久化：localStorage 集成
- 自动初始化：页面刷新后状态恢复
- 用户分级：根据词汇量判断学习阶段
- 订阅管理：获取和管理用户订阅信息
- AI权限：检查AI功能使用权限
- 打卡功能：每日打卡状态管理
```

#### 2. Admin Store (stores/admin.ts) - 管理员状态管理

```typescript
interface AdminState {
  isAdminUser: boolean
  isSuperAdminUser: boolean
  role: AdminRole | null
  permissions: AdminPermission[]
  sessionExpiredAt: string
  lastLoginTime: string
  loading: boolean
  lastVerifiedTime: number | null
}

// 核心功能
- 管理员认证：独立的管理员登录验证
- 权限管理：角色权限和功能权限控制
- 会话管理：管理员会话独立管理（4小时有效期）
- 权限验证：实时验证管理员权限状态
- 状态持久化：管理员会话信息本地存储
- 自动重验证：定期重新验证管理员身份
```

#### 状态管理特性

**1. 双系统隔离**
- 用户状态和管理员状态完全独立
- 不同的认证机制和会话管理
- 独立的权限验证逻辑

**2. 智能状态恢复**
- 页面刷新后自动恢复用户状态
- 管理员会话自动验证和延长
- Token过期自动清理和重定向

**3. 权限控制**
- 用户权限：基于订阅等级的AI功能权限
- 管理员权限：基于角色的功能访问权限
- 动态权限：实时权限验证和更新

**4. 状态同步**
- 本地状态与服务器状态同步
- 订阅信息实时更新
- 管理员权限实时验证

### 路由架构

#### 双路由守卫系统

XReadUp Frontend 采用**双路由守卫系统**，分别处理用户端和管理员端的路由权限：

#### 1. 用户路由守卫 (userGuard.ts)

```typescript
// 用户认证路由守卫
export const userGuard = async (to, from, next) => {
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
export const loginGuard = (to, from, next) => {
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
// 管理员权限路由守卫
export const adminGuard = async (to, from, next) => {
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
export const adminLoginGuard = (to, from, next) => {
  const adminStore = useAdminStore()
  if (adminStore.isAdminUser && !adminStore.isSessionExpired) {
    next('/admin/dashboard') // 已登录管理员重定向到仪表盘
  } else {
    next()
  }
}
```

#### 路由配置结构

**用户端路由 (18个路由)**
```typescript
const routes = [
  // 公开路由
  { path: '/', name: 'home', component: HomeView },
  { path: '/article/:id', name: 'article', component: ArticleReader },
  { path: '/reading-list', name: 'readingList', component: ArticleListView },
  { path: '/ui-demo', name: 'uiDemo', component: UIDemoPage, meta: { requiresAuth: false } },
  
  // 需要认证的路由
  { path: '/vocabulary', name: 'vocabulary', component: VocabularyPage, meta: { requiresAuth: true } },
  { path: '/report', name: 'report', component: ReportPage, meta: { requiresAuth: true } },
  { path: '/subscription', name: 'subscription', component: SubscriptionPage, meta: { requiresAuth: true } },
  
  // 认证相关路由
  { path: '/login', name: 'login', component: LoginPage, beforeEnter: loginGuard },
  { path: '/register', name: 'register', component: RegisterView, beforeEnter: loginGuard },
  
  // 管理员路由
  { path: '/admin/login', name: 'adminLogin', component: AdminLogin, beforeEnter: adminLoginGuard },
  { path: '/admin/dashboard', name: 'adminDashboard', component: AdminDashboard, meta: { requiresAdmin: true } },
  { path: '/admin/users', name: 'adminUsers', component: UserManagement, meta: { requiresAdmin: true } },
  { path: '/admin/articles', name: 'adminArticles', component: ArticleManagement, meta: { requiresAdmin: true } },
  { path: '/admin/subscriptions', name: 'adminSubscriptions', component: SubscriptionManagement, meta: { requiresAdmin: true } },
  { path: '/admin/settings', name: 'adminSettings', component: SystemSettings, meta: { requiresAdmin: true } },
  { path: '/admin/ai-analysis', name: 'adminAiAnalysis', component: AiAnalysisView, meta: { requiresAdmin: true } },
  { path: '/admin/admins', name: 'adminAdmins', component: AdminUsersManagement, meta: { requiresAdmin: true } }
]
```

#### 路由守卫执行顺序

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

#### 路由特性

**1. 双重认证系统**
- 用户认证：基于JWT Token的用户身份验证
- 管理员认证：独立的管理员权限验证系统

**2. 智能重定向**
- 已登录用户访问登录页自动重定向到首页
- 已登录管理员访问登录页自动重定向到仪表盘
- 未授权访问自动重定向到对应登录页

**3. 会话管理**
- 用户会话：基于Token的会话管理
- 管理员会话：独立的管理员会话管理（4小时有效期）

**4. 权限控制**
- 页面级权限：基于meta字段的权限控制
- 组件级权限：基于Store状态的组件权限控制
- 功能级权限：基于用户等级和管理员角色的功能权限

### API 架构设计

#### 双API系统架构

XReadUp Frontend 采用**双API系统架构**，分别处理用户端和管理员端的API调用：

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
- **articleApi**: 文章相关接口
- **vocabularyApi**: 词汇管理接口
- **aiApi**: AI 功能接口
- **reportApi**: 学习报告接口
- **learningApi**: 学习记录接口
- **subscriptionApi**: 订阅管理接口

**管理员端API模块 (api/admin/adminApi.ts)**
- **管理员认证**: login、checkAdmin
- **用户管理**: getUserList、updateUser、disableUser、enableUser
- **文章管理**: getArticleList、getArticleDetail、deleteArticle、publishArticle
- **订阅管理**: getSubscriptionPlans、getUserSubscriptionList、updateUserSubscriptionStatus
- **系统配置**: getSystemConfigs、updateSystemConfig、batchUpdateSystemConfigs
- **AI分析**: analyzeArticle、getAIAnalysisList、getAIAnalysisDetail
- **管理员管理**: getAdminUsersList、addAdminUser、updateAdminUserRole、deleteAdminUser
- **系统监控**: getAdminStats、getDataTrends、getRecentActivities

#### 3. API特性

**1. 智能Token管理**
- 自动识别管理员API和用户API
- 分别使用不同的Token进行认证
- 自动处理Token过期和刷新

**2. 错误处理机制**
- 401错误自动重定向到对应登录页
- 管理员API错误重定向到管理员登录页
- 用户API错误重定向到用户登录页

**3. 请求优化**
- 60秒超时设置适应长时间操作
- 智能重试机制
- 请求去重和缓存

**4. 类型安全**
- 完整的TypeScript类型定义
- API响应类型检查
- 参数类型验证

## 🎨 UI/UX 设计

### 设计系统

XReadUp Frontend 采用现代化的设计系统，融合了 iOS 26 设计语言和 Airbnb 的温暖美学，提供一致且优雅的用户体验。

#### 设计理念
- **Liquid Glass 效果** - 玻璃态背景，模糊效果，现代感十足
- **iOS 26 风格** - 简洁、直观、符合用户习惯
- **Airbnb 温暖美学** - 温馨的色彩搭配和友好的交互
- **响应式设计** - 完美适配各种设备尺寸

#### 色彩体系
- **主色调**: #007AFF (iOS 蓝)
- **成功色**: #34C759 (iOS 绿)
- **警告色**: #FF9500 (iOS 橙)
- **危险色**: #FF3B30 (iOS 红)
- **信息色**: #8E8E93 (iOS 灰)
- **背景色**: #F2F2F7 (iOS 背景灰)
- **文本色**: #000000 (主文本), #6D6D70 (次要文本)

#### 语义化标签系统
```css
/* 分类标签 - 蓝色渐变 */
.capsule-tag--category {
  background: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  box-shadow: 0 4px 12px rgba(0, 122, 255, 0.3);
}

/* 难度标签 - 橙色渐变 */
.capsule-tag--difficulty {
  background: linear-gradient(135deg, #FF9500 0%, #FFCC00 100%);
  box-shadow: 0 4px 12px rgba(255, 149, 0, 0.3);
}
```

#### 智能加载组件系统
- **旋转加载器** - 三色边框旋转，简洁现代
- **脉冲加载器** - 呼吸式动画，柔和自然
- **波浪加载器** - 波浪式动画，生动有趣
- **骨架屏** - 内容预加载，提升用户体验

#### 响应式断点
```css
/* 移动端优先设计 */
@media (max-width: 480px) { /* 小屏手机 */ }
@media (max-width: 768px) { /* 平板 */ }
@media (max-width: 1024px) { /* 小屏笔记本 */ }
@media (min-width: 1200px) { /* 大屏显示器 */ }
```

#### 动画与交互
- **过渡动画**: 0.3s ease 标准过渡
- **悬停效果**: 卡片阴影、按钮变换
- **加载状态**: Element Plus Loading 组件
- **用户反馈**: Message 消息提示

### 组件设计规范

#### Header 导航组件
- 响应式导航菜单
- 用户登录状态展示
- 下拉菜单：个人资料、设置、退出
- Logo 点击返回首页

#### Footer 页脚组件
- 公司信息与快速链接
- 联系方式与版权声明
- 响应式布局适配

#### 表单组件规范
- 统一的验证规则
- 错误信息友好提示
- 加载状态与禁用状态
- 移动端优化布局

## 🚀 核心功能实现

### 1. 管理员系统

#### 管理员认证与权限管理
```typescript
// 管理员登录流程
const handleAdminLogin = async (username: string, password: string) => {
  try {
    const response = await adminApi.login({ username, password })
    
    if (response.success) {
      // 保存管理员信息到Store
      adminStore.setAdminState({
        isAdmin: true,
        isSuperAdmin: response.data.role === 'SUPER_ADMIN',
        role: response.data.role,
        token: response.data.token,
        userId: response.data.userId
      })
      
      // 重定向到管理员仪表盘
      router.push('/admin/dashboard')
    }
  } catch (error) {
    ElMessage.error('管理员登录失败')
  }
}

// 权限检查
const hasPermission = (permission: AdminPermission): boolean => {
  return adminStore.hasPermission(permission)
}
```

#### 系统配置管理
```typescript
// 系统配置更新
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

#### 用户管理功能
```typescript
// 用户状态管理
const toggleUserStatus = async (userId: number, isActive: boolean) => {
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

// 用户订阅管理
const updateUserSubscription = async (subscriptionId: string, status: string) => {
  try {
    await adminApi.updateUserSubscriptionStatus(subscriptionId, status)
    ElMessage.success('订阅状态更新成功')
    
    // 刷新订阅列表
    await loadSubscriptionList()
  } catch (error) {
    ElMessage.error('订阅状态更新失败')
  }
}
```

### 2. TTS语音朗读系统

#### TTS控制组件 (TTSControl.vue)
```vue
<template>
  <div class="read-control-sidebar" v-if="showReadControlSidebar">
    <div class="read-control-header">
      <h3>朗读控制</h3>
      <el-button @click="handleStopSpeaking" type="text">
        <el-icon><CircleClose /></el-icon>
      </el-button>
    </div>

    <div class="read-control-content">
      <!-- 控制按钮组 -->
      <div class="control-buttons">
        <el-button @click="handlePauseResumeSpeaking" type="primary">
          {{ isPaused ? '继续' : '暂停' }}
        </el-button>
        <el-button @click="handleStopSpeaking" type="danger">
          停止
        </el-button>
      </div>

      <!-- 语速调节 -->
      <div class="speed-control">
        <label>语速: {{ readingSpeed.toFixed(1) }}</label>
        <el-slider
          v-model="readingSpeed"
          :min="0.5"
          :max="2.0"
          :step="0.1"
          @change="handleSpeedChange"
        />
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

#### 双击朗读功能集成
```typescript
// ArticleReader.vue 中的双击朗读实现
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
```

### 3. 智能阅读器

#### 双语对照阅读
```vue
<div class="bilingual-content">
  <!-- 英文原文 -->
  <div class="english-section">
    <div class="english-content" @click="onWordClick">
      <p v-for="(paragraph, index) in englishParagraphs" 
         :key="index" class="paragraph">
        {{ paragraph }}
      </p>
    </div>
  </div>
  
  <!-- 中文翻译 -->
  <div class="chinese-section" v-if="showTranslation">
    <div class="chinese-content">
      <p v-for="(paragraph, index) in chineseParagraphs" 
         :key="index" class="paragraph">
        {{ paragraph }}
      </p>
    </div>
  </div>
</div>
```

#### 智能查词功能
- 点击单词触发查询
- 词典 API 集成
- 音标显示
- 例句

#### 双击单词朗读功能
- **交互方式**：双击文章中的英语单词即可触发朗读功能，与单击查词功能独立共存
- **技术实现**：通过Vue 3的事件系统实现，在ArticleReader.vue中绑定`@dblclick="onWordDoubleClick"`事件监听器
- **核心流程**：
  1. 双击事件触发`onWordDoubleClick`函数
  2. 通过`ttsControlRef.value.handleWordClick(event)`调用TTS组件的朗读功能
  3. TTS组件内部检查浏览器的TTS支持情况
  4. 获取用户选择的单词文本并进行格式验证（仅支持20个字符以内的纯英文单词）
  5. 调用`speakWord`方法执行实际的语音朗读
- **用户体验优化**：
  1. 提供实时单词高亮反馈（浅蓝色背景，1秒后自动消失）
  2. 支持多种语音选择和语速调节
  3. 与查词功能完美结合，双击朗读不影响单击查词
  4. 实现了优雅的事件处理，防止双击事件被错误处理为两次单击

#### AI 功能集成
- **全文翻译**: 腾讯云翻译 API
- **AI 摘要**: 文章重点提取
- **语法解析**: 句法分析与语法点讲解
- **智能问答**: 基于文章内容的 Q&A

### 2. 生词本系统

#### 词汇管理
- 自动收集用户查过的单词
- 提供单词记忆练习功能
- 支持按难度分类管理
- 单词卡片式学习界面

### 3. 学习报告可视化

#### ECharts 图表实现
```typescript
// 词汇增长曲线
const growthChartOption = {
  title: { text: '词汇增长趋势' },
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value' },
  series: [{
    data: vocabularyCounts,
    type: 'line',
    smooth: true,
    areaStyle: {}
  }]
}

// 阅读时长统计
const readingChartOption = {
  title: { text: '每日阅读时长' },
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value', name: '分钟' },
  series: [{
    data: readingTimes,
    type: 'bar',
    itemStyle: { color: '#409eff' }
  }]
}
```

#### 学习成就系统
- 进度追踪：百分比进度条
- 里程碑：完成特定目标的徽章
- 数据统计：词汇量、阅读时长、连续天数

### 4. 用户认证系统

#### 登录注册流程
```typescript
// 登录逻辑
const handleLogin = async () => {
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败')
  }
}

// 注册逻辑
const handleRegister = async () => {
  try {
    await userStore.register(form.username, form.password, form.nickname)
    ElMessage.success('注册成功')
  } catch (error) {
    ElMessage.error('注册失败')
  }
}
```

#### 状态管理
- JWT Token 管理
- 自动登录检查
- 登出清理机制

### 5. 数据持久化
- 后端 API 同步
- 本地缓存机制
- 离线数据支持

### 6. 阅读时长记录
- 自动记录用户的有效阅读时间（超过2秒才记录）
- 在页面卸载时自动同步至后端
- 进行用户ID和文章ID有效性验证

### 7. 每日打卡功能

**Header.vue - 打卡功能实现**

```typescript
// 获取打卡状态
const fetchCheckInStatus = async () => {
  const todayKey = getTodayKey()
  
  // 1. 从本地存储加载打卡状态
  const localCheckIn = localStorage.getItem(todayKey)
  if (localCheckIn) {
    const parsed = JSON.parse(localCheckIn)
    hasCheckedInToday.value = parsed.hasCheckedIn
    streakDays.value = parsed.streakDays || 0
  }
  
  try {
    // 2. 获取今日学习统计数据
    const summaryRes = await learningApi.getTodaySummary()
    
    // 3. 根据API返回更新打卡状态
    if (summaryRes.data.hasCheckedInToday !== undefined) {
      hasCheckedInToday.value = summaryRes.data.hasCheckedInToday
    } else {
      // 4. 如果API未返回打卡状态，根据学习活动推断
      hasCheckedInToday.value = summaryRes.data.studyTime > 0 || summaryRes.data.wordsLearned > 0
    }
    
    // 5. 获取连续打卡天数
    const checkInRes = await learningApi.dailyCheckIn()
    if (checkInRes.code === 200) {
      streakDays.value = checkInRes.data.streakDays || 0
      
      // 6. 如果打卡状态未设置，根据连续天数推断
      if (hasCheckedInToday.value === null && streakDays.value > 0) {
        hasCheckedInToday.value = true
      }
    }
  } catch (error) {
    console.error('获取打卡状态失败:', error)
  } finally {
    // 7. 保存打卡状态到本地存储
    localStorage.setItem(todayKey, JSON.stringify({
      hasCheckedIn: hasCheckedInToday.value,
      streakDays: streakDays.value,
      timestamp: Date.now()
    }))
  }
}

// 执行打卡操作
const performCheckIn = async () => {
  try {
    const res = await learningApi.dailyCheckIn()
    if (res.code === 200) {
      hasCheckedInToday.value = true
      streakDays.value = res.data.streakDays || 0
      
      // 保存状态到本地存储
      const todayKey = getTodayKey()
      localStorage.setItem(todayKey, JSON.stringify({
        hasCheckedIn: true,
        streakDays: streakDays.value,
        timestamp: Date.now()
      }))
      
      ElMessage.success(`打卡成功！已连续打卡 ${streakDays.value} 天`)
    }
  } catch (error) {
    console.error('打卡失败:', error)
    ElMessage.error('打卡失败，请稍后重试')
  }
}

// 生成当日标识
const getTodayKey = () => {
  const now = new Date()
  return `checkin_${now.getFullYear()}_${now.getMonth() + 1}_${now.getDate()}`
}
```

#### 打卡功能的主要特性

1. **多渠道打卡状态获取**
   - 本地存储快速显示
   - API返回状态
   - 学习活动推断（阅读时长>0或学习单词数>0）
   - 连续打卡天数推断

2. **自动打卡机制**
   - 用户完成学习活动后自动标记为已打卡
   - 无需手动操作，提升用户体验

3. **手动打卡支持**
   - 提供手动打卡按钮，支持用户主动完成打卡
   - 打卡成功后显示连续天数提示

4. **连续打卡统计**
   - 实时记录并显示用户的连续打卡天数
   - 支持成就系统集成

5. **状态持久化**
   - 将打卡状态保存到本地存储，防止页面刷新丢失
   - 包含时间戳，确保数据准确性

6. **用户状态监听**
   - 监听用户登录状态变化
   - 自动更新打卡状态

7. **错误处理机制**
   - 完善的异常处理，确保打卡功能稳定运行
   - 网络错误时使用本地缓存数据

### 8. 订阅管理功能

**SubscriptionPage.vue - 订阅管理页面实现**

```typescript
// 从后端获取订阅计划数据
const loadBackendPlanPrices = async () => {
  try {
    const result = await api.get('/api/subscription/plans')
    const plans: SubscriptionPlan[] = []
    
    // 处理API返回的套餐数据
    Object.entries(result.data).forEach(([planType, planData]) => {
      if (typeof planData === 'object' && planData !== null) {
        const planKey = planType.toUpperCase()
        
        // 创建完整的套餐对象
        const plan: SubscriptionPlan = {
          type: planKey as 'FREE' | 'BASIC' | 'PRO' | 'ENTERPRISE',
          name: getPlanName(planKey),
          price: planData && ('priceCny' in planData || 'price' in planData) 
            ? parseFloat(String('priceCny' in planData ? planData.priceCny : planData.price)) 
            : 0,
          currency: (planData as { currency?: string })?.currency || 'CNY',
          duration: planKey === 'FREE' ? '永久' : '月',
          maxArticles: planData && 'maxArticlesPerMonth' in planData 
            ? parseInt(String(planData.maxArticlesPerMonth)) 
            : 0,
          maxWords: planData && 'maxWordsPerArticle' in planData 
            ? parseInt(String(planData.maxWordsPerArticle)) 
            : 0,
          aiFeatures: planData && 'aiFeaturesEnabled' in planData 
            ? Boolean(planData.aiFeaturesEnabled) 
            : false,
          prioritySupport: planData && 'prioritySupport' in planData 
            ? Boolean(planData.prioritySupport) 
            : false,
          features: [] // 特性将通过generateFeaturesForPlan生成
        }
        
        plans.push(plan)
      }
    })
    
    // 按正确的顺序排序套餐
    plans.sort((a, b) => {
      const order = { 'FREE': 0, 'BASIC': 1, 'PRO': 2, 'ENTERPRISE': 3 }
      return order[a.type] - order[b.type]
    })
    
    // 为每个套餐生成特性列表
    plans.forEach(plan => {
      plan.features = generateFeaturesForPlan(plan)
    })
    
    subscriptionPlans.value = plans
  } catch (error) {
    console.error('加载订阅计划失败:', error)
    
    // 提供默认的套餐配置作为后备方案
    subscriptionPlans.value = [
      {
        type: 'FREE',
        name: '免费版',
        price: 0,
        currency: 'CNY',
        duration: '永久',
        maxArticles: 30,
        maxWords: 2000,
        aiFeatures: false,
        prioritySupport: false,
        features: ['30篇文章/月', '每篇2000词限制', '基础阅读功能']
      },
      // 其他默认套餐配置...
    ]
  }
}

// 加载用户订阅数据
const loadSubscriptionData = async () => {
  try {
    const result = await api.get('/api/user/subscription')
    userSubscription.value = result.data
    
    // 获取免费套餐配置
    const freePlan = mergedSubscriptionPlans.value.find(plan => plan.type === 'FREE')
    
    // 设置免费用户的阅读额度
    if (userSubscription.value.planType === 'FREE') {
      userSubscription.value.maxArticlesPerMonth = freePlan?.maxArticles || 30
    }
    
    // 获取用户使用统计
    const usageResult = await api.get('/api/user/usage')
    usageQuota.value = usageResult.data
  } catch (error) {
    console.error('加载订阅数据失败:', error)
  }
}

// 生成套餐特性列表
const generateFeaturesForPlan = (plan: SubscriptionPlan): string[] => {
  const features: string[] = []
  
  // 根据maxArticles生成阅读额度特性
  if (plan.maxArticles > 0) {
    features.push(`${plan.maxArticles}篇文章/月`)
  } else if (plan.maxArticles === 0 && plan.type !== 'FREE') {
    features.push('无限文章阅读')
  }
  
  // 添加其他特性
  if (plan.maxWords > 0) {
    features.push(`每篇${plan.maxWords}词限制`)
  } else if (plan.maxWords === 0 && plan.type !== 'FREE') {
    features.push('无单词限制')
  }
  
  if (plan.aiFeatures) {
    features.push('AI翻译与解析')
  }
  
  if (plan.prioritySupport) {
    features.push('优先技术支持')
  }
  
  return features
}```

#### 订阅管理用户界面设计

**SubscriptionPage.vue - 界面设计与用户体验**

```vue
<template>
  <div class="subscription-container">
    <!-- 用户订阅状态卡片 -->
    <div v-if="userSubscription" class="subscription-status-card">
      <div class="subscription-header">
        <h3>当前订阅</h3>
        <Badge :type="getStatusType(userSubscription.status)" :text="getStatusText(userSubscription.status)" />
      </div>
      
      <div class="subscription-details">
        <div class="plan-info">
          <p class="plan-name">{{ getPlanName(userSubscription.planType) }}</p>
          <p class="billing-info" v-if="userSubscription.planType !== 'FREE'">
            ¥{{ userSubscription.price }}/{{ getBillingCycle(userSubscription) }}
          </p>
        </div>
        
        <!-- 阅读额度使用情况 -->
        <div v-if="usageQuota && userSubscription.planType === 'FREE'" class="usage-quota">
          <p>本月阅读额度</p>
          <div class="progress-bar">
            <div 
              class="progress-fill" 
              :style="{ width: `${(usageQuota.articlesQuota.used / usageQuota.articlesQuota.total) * 100}%` }"
            ></div>
          </div>
          <p class="usage-text">{{ usageQuota.articlesQuota.used }}/{{ usageQuota.articlesQuota.total }} 篇文章</p>
          <p class="reset-info">下月重置时间: {{ formatResetDate(usageQuota.articlesQuota.resetDate) }}</p>
        </div>
        
        <!-- 订阅操作按钮 -->
        <div class="subscription-actions">
          <Button v-if="userSubscription.status === 'ACTIVE' && userSubscription.planType !== 'FREE'" type="warning" @click="showCancelDialog">
            取消订阅
          </Button>
          <Button v-else type="primary" @click="scrollToPlans">
            升级订阅
          </Button>
        </div>
      </div>
    </div>
    
    <!-- 订阅计划选择区域 -->
    <div class="plans-section" id="subscription-plans">
      <h2>选择适合您的订阅计划</h2>
      <p class="plans-description">根据您的阅读需求选择最合适的方案，随时可以升级或降级</p>
      
      <!-- 订阅计划卡片网格 -->
      <div class="plans-grid">
        <div 
          v-for="plan in mergedSubscriptionPlans" 
          :key="plan.type" 
          class="plan-card" 
          :class="{ 'recommended': plan.recommended, 'active': userSubscription?.planType === plan.type }"
        >
          <!-- 套餐标识与推荐标签 -->
          <div class="plan-header">
            <h3>{{ plan.name }}</h3>
            <Badge v-if="plan.recommended" type="success" text="推荐" />
            <Badge v-else-if="userSubscription?.planType === plan.type" type="info" text="当前方案" />
          </div>
          
          <!-- 价格信息 -->
          <div class="plan-price">
            <span v-if="plan.price > 0">¥{{ plan.price }}</span>
            <span v-else>免费</span>
            <span v-if="plan.price > 0" class="plan-period">/{{ plan.duration }}</span>
          </div>
          
          <!-- 套餐特性列表 -->
          <ul class="plan-features">
            <li v-for="feature in plan.features" :key="feature" class="feature-item">
              <Check v-if="featureIsIncluded(feature, plan)" class="feature-icon success" />
              <Close v-else class="feature-icon forbidden" />
              <span>{{ feature }}</span>
            </li>
          </ul>
          
          <!-- 订阅按钮 -->
          <div class="plan-actions">
            <Button 
              v-if="userSubscription?.planType !== plan.type" 
              type="primary" 
              :class="{ 'btn-large': plan.recommended }"
              @click="selectPlan(plan)"
              :loading="loading"
            >
              选择{{ plan.name }}
            </Button>
            <Button 
              v-else 
              type="default" 
              disabled
            >
              当前使用
            </Button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 常见问题解答 -->
    <div class="faq-section">
      <h2>常见问题</h2>
      <div class="faq-item" v-for="faq in faqs" :key="faq.question">
        <details>
          <summary>{{ faq.question }}</summary>
          <p>{{ faq.answer }}</p>
        </details>
      </div>
    </div>
  </div>
</template>
```

#### 订阅管理的主要功能

1. **灵活的订阅计划展示**
   - 支持FREE、BASIC、PRO、ENTERPRISE四个等级的订阅方案
   - 根据后端返回数据动态生成套餐特性列表
   - 推荐套餐高亮显示，当前使用套餐明确标识

2. **实时订阅状态管理**
   - 用户当前订阅状态和有效期显示
   - 免费用户阅读额度使用情况实时监控
   - 订阅状态变更（激活、取消、过期）的直观展示

3. **无缝升级/降级流程**
   - 支持用户随时切换不同的订阅计划
   - 订阅取消功能和确认流程
   - 平滑的支付流程集成

4. **数据驱动的订阅体验**
   - 完全从后端API获取订阅计划和价格
   - 自动处理API错误和网络异常情况
   - 提供默认配置作为后备方案，确保用户体验的连续性

#### 数据持久化
- 后端 API 同步
- 本地缓存机制
- 离线数据支持


```typescript
// 词汇增长曲线
const growthChartOption = {
  title: { text: '词汇增长趋势' },
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value' },
  series: [{
    data: vocabularyCounts,
    type: 'line',
    smooth: true,
    areaStyle: {}
  }]
}

// 阅读时长统计
const readingChartOption = {
  title: { text: '每日阅读时长' },
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value', name: '分钟' },
  series: [{
    data: readingTimes,
    type: 'bar',
    itemStyle: { color: '#409eff' }
  }]
}
```

#### 学习成就系统
- 进度追踪：百分比进度条
- 里程碑：完成特定目标的徽章
- 数据统计：词汇量、阅读时长、连续天数



#### 登录注册流程
```typescript
// 登录逻辑
const handleLogin = async () => {
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败')
  }
}

// 注册逻辑
const handleRegister = async () => {
  try {
    await userStore.register(form.username, form.password, form.nickname)
    ElMessage.success('注册成功')
  } catch (error) {
    ElMessage.error('注册失败')
  }
}
```

#### 状态管理
- JWT Token 管理
- 自动登录检查
- 登出清理机制


- 后端 API 同步
- 本地缓存机制
- 离线数据支持

### 6. 阅读时长记录
- 自动记录用户的有效阅读时间（超过2秒才记录）
- 在页面卸载时自动同步至后端
- 进行用户ID和文章ID有效性验证

### 7. 每日打卡功能

#### ECharts 图表实现
```typescript
// 词汇增长曲线
const growthChartOption = {
  title: { text: '词汇增长趋势' },
  tooltip: { trigger: 'axis' },
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value' },
  series: [{
    data: vocabularyCounts,
    type: 'line',
    smooth: true,
    areaStyle: {}
  }]
}

// 阅读时长统计
const readingChartOption = {
  title: { text: '每日阅读时长' },
  xAxis: { type: 'category', data: dates },
  yAxis: { type: 'value', name: '分钟' },
  series: [{
    data: readingTimes,
    type: 'bar',
    itemStyle: { color: '#409eff' }
  }]
}
```

#### 学习成就系统
- 进度追踪：百分比进度条
- 里程碑：完成特定目标的徽章
- 数据统计：词汇量、阅读时长、连续天数

### 4. 用户认证系统

#### 登录注册流程
```typescript
// 登录逻辑
const handleLogin = async () => {
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    ElMessage.error('登录失败')
  }
}

// 注册逻辑
const handleRegister = async () => {
  try {
    await userStore.register(form.username, form.password, form.nickname)
    ElMessage.success('注册成功')
  } catch (error) {
    ElMessage.error('注册失败')
  }
}
```

#### 状态管理
- JWT Token 管理
- 自动登录检查
- 登出清理机制

## 🔧 开发环境配置

### 环境要求
- **Node.js**: >=18.0.0
- **npm**: >=9.0.0
- **现代浏览器**: 支持 ES2020+

### 快速开始

#### 1. 克隆项目
```bash
git clone <repository-url>
cd xreadup-frontend
```

#### 2. 安装依赖
```bash
npm install
```

#### 3. 环境配置
```bash
# 创建环境配置文件
cp .env.example .env

# 配置后端API地址
echo "VITE_API_BASE_URL=http://localhost:8080" > .env
```

#### 4. 启动开发服务器
```bash
npm run dev
```

#### 5. 构建生产版本
```bash
npm run build
```

### 开发脚本说明

```json
{
  "scripts": {
    "dev": "vite",                    // 启动开发服务器
    "build": "vue-tsc && vite build", // 构建生产版本
    "preview": "vite preview",        // 预览构建结果
    "lint": "eslint . --fix"          // 代码检查与修复
  }
}
```

### Vite 配置详解

```typescript
// vite.config.ts
export default defineConfig({
  plugins: [vue(), vueDevTools()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
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

## 🔍 API 集成文档

### 后端服务对接

#### 微服务架构
- **Gateway**: `http://localhost:8080` - API 网关
- **User Service**: 用户认证与管理
- **Article Service**: 文章内容管理
- **AI Service**: AI 功能服务
- **Report Service**: 学习数据分析

#### API 接口示例

**用户认证**
```typescript
// 登录
POST /api/user/login
{
  "username": "string",
  "password": "string"
}

// 注册
POST /api/user/register
{
  "username": "string",
  "password": "string",
  "nickname": "string"
}
```

**文章管理**
```typescript
// 获取文章列表
GET /api/article/list?pageNum=1&pageSize=10

// 获取文章详情
GET /api/article/{id}
```

**词汇管理**
```typescript
// 获取用户词汇
GET /api/vocabulary/user/{userId}

// 添加词汇
POST /api/vocabulary/add
{
  "word": "string",
  "meaning": "string",
  "userId": "number"
}

// 复习词汇
PUT /api/vocabulary/review/{userId}/{wordId}
{
  "mastered": "boolean"
}
```

**AI 功能**
```typescript
// 查词
POST /api/ai/lookup
{
  "word": "string",
  "context": "string"
}

// 翻译
POST /api/ai/translate
{
  "text": "string",
  "targetLanguage": "zh"
}

// AI 问答
POST /api/ai/ask
{
  "question": "string",
  "context": "string"
}
```

### 错误处理机制

#### HTTP 状态码处理
```typescript
// 401 未授权 - 自动跳转登录
if (error.response?.status === 401) {
  userStore.logout()
  router.push('/login')
}

// 403 禁止访问 - 权限不足提示
if (error.response?.status === 403) {
  ElMessage.error('权限不足')
}

// 500 服务器错误 - 通用错误提示
if (error.response?.status === 500) {
  ElMessage.error('服务器错误，请稍后重试')
}
```

## 📱 响应式设计

### 移动端优化

#### 触摸友好设计
```css
/* 触摸目标大小 */
.touch-target {
  min-height: 44px;
  min-width: 44px;
}

/* 触摸反馈 */
@media (hover: none) and (pointer: coarse) {
  .button:active {
    transform: scale(0.98);
  }
}
```

#### 移动端布局适配
- **栅格系统**: 响应式网格布局
- **导航优化**: 移动端友好的菜单
- **表单优化**: 大按钮、清晰的输入框
- **图表适配**: ECharts 移动端优化

#### 性能优化
- **图片懒加载**: 提升页面加载速度
- **代码分割**: 路由级别的代码分割
- **缓存策略**: 合理的缓存配置

### 浏览器兼容性

#### 支持的浏览器
- **Chrome**: >= 87
- **Firefox**: >= 78
- **Safari**: >= 14
- **Edge**: >= 88
- **移动端浏览器**: iOS Safari, Android Chrome

#### Polyfill 支持
- ES2020+ 特性支持
- CSS Grid 布局
- Flexbox 布局

## 🧪 测试策略

### 测试框架
- **单元测试**: Vitest
- **组件测试**: Vue Test Utils
- **E2E 测试**: Cypress
- **类型检查**: TypeScript

### 测试覆盖率目标
- **单元测试**: >= 80%
- **组件测试**: >= 70%
- **E2E 测试**: 核心用户流程 100%

### 测试命令
```bash
# 运行单元测试
npm run test:unit

# 运行组件测试
npm run test:component

# 运行 E2E 测试
npm run test:e2e

# 生成覆盖率报告
npm run test:coverage
```

## 🚀 部署与构建

### 构建优化

#### Vite 构建配置
```typescript
// vite.config.ts
export default defineConfig({
  build: {
    target: 'es2020',
    outDir: 'dist',
    sourcemap: false,
    minify: 'terser',
    rollupOptions: {
      output: {
        chunkFileNames: 'js/[name]-[hash].js',
        entryFileNames: 'js/[name]-[hash].js',
        assetFileNames: '[ext]/[name]-[hash].[ext]'
      }
    }
  }
})
```

#### 代码分割策略
- **路由分割**: 每个页面单独打包
- **第三方库分割**: vendor chunk 独立
- **组件库分割**: Element Plus 单独打包

### 部署方案

#### 1. Nginx 部署
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;

    # 处理 Vue Router 的 History 模式
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

#### 2. Docker 容器化
```dockerfile
# Dockerfile
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

#### 3. CDN 加速
- 静态资源 CDN 分发
- 图片资源优化
- GZIP 压缩配置

### 环境配置

#### 开发环境
```env
VITE_API_BASE_URL=http://localhost:8080
VITE_APP_TITLE=XReadUp Development
NODE_ENV=development
```

#### 生产环境
```env
VITE_API_BASE_URL=https://api.xreadup.com
VITE_APP_TITLE=XReadUp
NODE_ENV=production
```

## 🔒 安全考虑

### 前端安全策略

#### XSS 防护
- **内容安全策略**: CSP 头部配置
- **输入验证**: 严格的表单验证
- **输出编码**: HTML 内容转义

#### CSRF 防护
- **Token 验证**: CSRF Token 机制
- **SameSite Cookie**: 跨站请求限制

#### 敏感信息保护
- **Token 存储**: localStorage 安全考虑
- **API 密钥**: 环境变量管理
- **调试信息**: 生产环境清理

### 权限控制
- **路由守卫**: 页面访问权限
- **组件权限**: 条件渲染控制
- **API 权限**: 接口调用鉴权

## 📈 性能优化

### 加载性能

#### 首屏优化
- **代码分割**: 路由级别的懒加载
- **预加载策略**: 关键资源优先加载
- **骨架屏**: 加载状态优化用户体验
- **图片优化**: WebP 格式、懒加载、响应式图片

#### 运行时性能
- **虚拟滚动**: 大列表性能优化
- **组件缓存**: keep-alive 组件缓存
- **事件优化**: 防抖节流处理
- **内存管理**: 组件卸载时清理定时器和事件监听

#### 构建优化
- **Tree Shaking**: 死代码消除
- **压缩优化**: Terser 压缩 JavaScript
- **资源优化**: 图片压缩、CSS 压缩
- **缓存策略**: 文件哈希命名，长期缓存

### 监控与分析

#### 性能监控
- **Web Vitals**: 核心性能指标追踪
- **错误监控**: 前端错误收集与上报
- **用户行为**: 页面访问统计
- **性能分析**: 加载时间、交互延迟

## 🤝 开发规范

### 代码规范

#### 命名约定
```typescript
// 组件命名：PascalCase
const ArticleReader = defineComponent({...})

// 变量命名：camelCase
const userInfo = ref<UserInfo>()
const isLoading = ref(false)

// 常量命名：UPPER_SNAKE_CASE
const API_BASE_URL = 'https://api.xreadup.com'
const MAX_RETRY_COUNT = 3

// 文件命名：kebab-case
// article-reader.vue
// user-profile.ts
```

#### TypeScript 规范
```typescript
// 接口定义
interface UserInfo {
  id: number
  username: string
  email?: string
  avatar?: string
}

// 类型声明
type LoadingState = 'idle' | 'loading' | 'success' | 'error'

// 泛型使用
interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
```

#### Vue 组件规范
```vue
<template>
  <!-- 使用语义化标签 -->
  <article class="article-container">
    <header class="article-header">
      <h1>{{ title }}</h1>
    </header>
    <main class="article-content">
      <!-- 内容区域 -->
    </main>
  </article>
</template>

<script setup lang="ts">
// 导入顺序：Vue -> 第三方库 -> 自定义模块
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// Props 定义
interface Props {
  title: string
  content?: string
}

const props = defineProps<Props>()

// Emits 定义
interface Emits {
  update: [value: string]
  close: []
}

const emit = defineEmits<Emits>()
</script>

<style scoped>
/* 使用 BEM 命名规范 */
.article-container {
  max-width: 800px;
  margin: 0 auto;
}

.article-header {
  padding: 20px 0;
  border-bottom: 1px solid #eee;
}

.article-content {
  padding: 20px 0;
}
</style>
```

### Git 工作流

#### 分支策略
```bash
# 主分支
main          # 生产环境代码
develop       # 开发环境代码

# 功能分支
feature/login-page        # 新功能开发
feature/vocabulary-system # 生词本功能

# 修复分支
hotfix/fix-login-bug      # 紧急修复
bugfix/article-reader     # 一般修复

# 发布分支
release/v1.0.0           # 版本发布
```

#### 提交规范
```bash
# 提交格式：<type>(<scope>): <description>

# 功能开发
feat(auth): add user login functionality
feat(vocabulary): implement word review system

# 问题修复
fix(reader): resolve article loading issue
fix(ui): correct mobile responsive layout

# 文档更新
docs(readme): update installation guide
docs(api): add vocabulary API documentation

# 样式调整
style(components): improve button hover effects
style(layout): adjust mobile navigation styling

# 重构代码
refactor(store): simplify user state management
refactor(api): extract common request logic

# 性能优化
perf(components): optimize large list rendering
perf(images): implement lazy loading

# 测试相关
test(utils): add API helper function tests
test(components): add vocabulary page tests
```

### 代码审查

#### 审查清单
- [ ] **功能完整性**: 是否满足需求规格
- [ ] **代码质量**: 是否遵循编码规范
- [ ] **性能考虑**: 是否存在性能问题
- [ ] **安全检查**: 是否存在安全漏洞
- [ ] **测试覆盖**: 是否包含必要测试
- [ ] **文档更新**: 是否更新相关文档

## 🛠 故障排除

### 常见问题

#### 1. 开发环境问题

**问题**: `npm install` 失败
```bash
# 解决方案
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

**问题**: Vite 开发服务器启动失败
```bash
# 检查端口占用
lsof -ti:3000

# 更换端口启动
npm run dev -- --port 3001
```

#### 2. 构建问题

**问题**: TypeScript 编译错误
```bash
# 类型检查
npm run type-check

# 修复 ESLint 错误
npm run lint --fix
```

**问题**: 构建产物过大
```bash
# 分析构建产物
npx vite-bundle-analyzer dist

# 检查依赖大小
npx bundlephobia <package-name>
```

#### 3. 运行时问题

**问题**: API 请求失败
```typescript
// 检查网络连接
console.log('API Base URL:', import.meta.env.VITE_API_BASE_URL)

// 检查请求头
console.log('Request Headers:', config.headers)

// 检查响应状态
console.log('Response Status:', error.response?.status)
```

**问题**: 路由跳转异常
```typescript
// 检查路由配置
console.log('Current Route:', router.currentRoute.value)

// 检查路由守卫
console.log('Route Meta:', to.meta)
```

#### 4. 性能问题

**问题**: 页面加载缓慢
```javascript
// 使用 Performance API 分析
const navigation = performance.getEntriesByType('navigation')[0]
console.log('页面加载时间:', navigation.loadEventEnd - navigation.fetchStart)

// 使用 Chrome DevTools
// Network 面板检查资源加载
// Performance 面板分析运行时性能
```

### 调试技巧

#### Vue DevTools
```bash
# 安装 Vue DevTools 浏览器扩展
# 开发环境自动启用

# 生产环境启用（仅调试时）
if (import.meta.env.DEV) {
  app.config.devtools = true
}
```

#### 网络调试
```typescript
// API 请求日志
axios.interceptors.request.use(config => {
  console.log('🚀 API Request:', config.method?.toUpperCase(), config.url)
  return config
})

axios.interceptors.response.use(
  response => {
    console.log('✅ API Response:', response.status, response.config.url)
    return response
  },
  error => {
    console.error('❌ API Error:', error.response?.status, error.config?.url)
    return Promise.reject(error)
  }
)
```

## 📚 相关资源

### 技术文档
- **Vue 3 官方文档**: https://cn.vuejs.org/
- **TypeScript 手册**: https://www.typescriptlang.org/docs/
- **Element Plus 组件库**: https://element-plus.gitee.io/zh-CN/
- **Pinia 状态管理**: https://pinia.vuejs.org/zh/
- **Vue Router 路由**: https://router.vuejs.org/zh/
- **Vite 构建工具**: https://cn.vitejs.dev/
- **ECharts 图表库**: https://echarts.apache.org/zh/index.html

### 设计资源
- **Element Plus 设计语言**: https://element-plus.gitee.io/zh-CN/guide/design.html
- **Vue 3 风格指南**: https://cn.vuejs.org/style-guide/
- **Responsive Web Design**: https://web.dev/responsive-web-design-basics/

### 学习资源
- **Vue 3 Composition API**: https://cn.vuejs.org/guide/introduction.html
- **TypeScript 进阶**: https://github.com/type-challenges/type-challenges
- **前端性能优化**: https://web.dev/performance/
- **Web 安全最佳实践**: https://owasp.org/www-project-web-security-testing-guide/

## 🤝 贡献指南

### 如何贡献

1. **Fork 项目**
   ```bash
   git clone https://github.com/your-username/xreadup-frontend.git
   cd xreadup-frontend
   ```

2. **创建功能分支**
   ```bash
   git checkout -b feature/new-feature
   ```

3. **开发与测试**
   ```bash
   npm install
   npm run dev
   npm run test
   ```

4. **提交代码**
   ```bash
   git add .
   git commit -m "feat: add new feature"
   git push origin feature/new-feature
   ```

5. **创建 Pull Request**
   - 详细描述功能变更
   - 附加测试截图
   - 确保通过 CI 检查

### 贡献类型
- 🐛 **Bug 修复**: 修复应用程序错误
- ✨ **新功能**: 添加新的功能特性
- 📚 **文档改进**: 完善文档内容
- 🎨 **UI/UX 优化**: 改善用户界面和体验
- ⚡ **性能优化**: 提升应用性能
- 🔧 **配置优化**: 改进构建和配置

### 行为准则
- 保持友好和专业的沟通
- 尊重不同的观点和经验水平
- 提供建设性的反馈
- 遵循项目的编码规范

## 📄 许可证

本项目采用 MIT 许可证。详细信息请查看 [LICENSE](LICENSE) 文件。

## 👥 团队

### 核心开发团队
- **前端架构师**: 负责架构设计和技术选型
- **UI/UX 设计师**: 负责用户界面和体验设计
- **前端工程师**: 负责功能开发和维护
- **质量保证**: 负责测试和质量控制

### 联系方式
- **项目地址**: https://github.com/xreadup/frontend
- **问题反馈**: https://github.com/xreadup/frontend/issues
- **邮箱**: dev@xreadup.com
- **微信群**: 扫描二维码加入开发者群

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给我们一个 Star！**

![GitHub stars](https://img.shields.io/github/stars/xreadup/frontend?style=social)
![GitHub forks](https://img.shields.io/github/forks/xreadup/frontend?style=social)
![GitHub issues](https://img.shields.io/github/issues/xreadup/frontend)
![GitHub pull requests](https://img.shields.io/github/issues-pr/xreadup/frontend)

*构建于 ❤️ 与 Vue 3*

</div>