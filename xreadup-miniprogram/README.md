# ReadUp 微信小程序项目

## 🚀 项目概述

ReadUp 微信小程序是基于 Uni-app 框架开发的真实新闻驱动的AI英语学习平台，为用户提供便捷的移动端学习体验。

## 📁 项目结构

```
xreadup-miniprogram/
├── api/                    # API接口层
│   ├── user.ts            # 用户相关API
│   ├── article.ts         # 文章相关API
│   ├── vocabulary.ts      # 生词本API
│   ├── ai.ts              # AI助手API
│   └── subscription.ts    # 订阅管理API
├── components/            # 公共组件
│   ├── article-card/      # 文章卡片组件
│   ├── vocabulary-card/   # 单词卡片组件
│   ├── ai-chat/           # AI对话组件
│   └── loading/           # 加载组件
├── pages/                 # 页面
│   ├── index/             # 首页
│   ├── login/             # 登录页
│   ├── article/           # 文章相关页面
│   ├── vocabulary/        # 生词本页面
│   ├── ai-assistant/      # AI助手页面
│   ├── report/            # 学习报告页面
│   ├── subscription/      # 订阅管理页面
│   └── profile/           # 个人中心页面
├── store/                 # 状态管理
│   ├── user.ts            # 用户状态
│   ├── article.ts         # 文章状态
│   ├── vocabulary.ts      # 生词本状态
│   └── ai.ts              # AI助手状态
├── utils/                 # 工具函数
│   ├── request.ts         # HTTP请求封装
│   ├── auth.ts            # 认证工具
│   ├── storage.ts         # 存储工具
│   └── common.ts          # 通用工具
├── static/                # 静态资源
│   ├── images/            # 图片资源
│   ├── icons/             # 图标资源
│   └── fonts/             # 字体资源
├── App.vue                # 应用根组件
├── main.js                # 应用入口
├── manifest.json          # 应用配置
├── pages.json             # 页面配置
├── uni.scss               # 全局样式
└── package.json           # 依赖配置
```

## 🛠️ 技术栈

- **框架**: Uni-app 3.x + Vue 3 + TypeScript
- **状态管理**: Pinia
- **UI组件**: uni-ui + uView Plus
- **样式**: SCSS
- **构建工具**: Vite
- **代码规范**: ESLint + Prettier

## 📱 核心功能

### 1. 用户认证
- 微信一键登录
- JWT Token认证
- 自动登录状态管理
- 用户信息管理

### 2. 文章阅读
- 文章列表展示
- 文章详情阅读
- 双语对照显示
- 点击查词功能
- 收藏和分享

### 3. 生词本系统
- 生词收集和管理
- 三种学习模式（闪卡复习、单词速刷、批量听写）
- 艾宾浩斯遗忘曲线算法
- 学习进度跟踪

### 4. AI助手
- 智能对话功能
- 文章摘要生成
- 句子解析和翻译
- 个性化问题推荐

### 5. 学习报告
- 学习数据统计
- 学习进度可视化
- 弱项分析
- 成就系统

### 6. 订阅管理
- 订阅套餐展示
- 微信支付集成
- 使用额度管理
- 订阅状态跟踪

## 🚀 快速开始

### 环境要求
- Node.js >= 16.0.0
- npm >= 8.0.0
- 微信开发者工具

### 安装依赖
```bash
npm install
```

### 开发调试
```bash
# 微信小程序
npm run dev:mp-weixin

# H5
npm run dev:h5
```

### 构建发布
```bash
# 微信小程序
npm run build:mp-weixin

# H5
npm run build:h5
```

## 📋 开发进度

### ✅ 已完成
- [x] 项目初始化和环境配置
- [x] 基础项目结构搭建
- [x] 全局样式和主题配置
- [x] HTTP请求封装
- [x] 用户API接口适配
- [x] 文章API接口适配
- [x] 用户状态管理
- [x] 登录页面开发
- [x] 首页开发

### 🔄 进行中
- [ ] API接口适配（生词本、AI助手、订阅管理）
- [ ] 文章阅读功能开发
- [ ] 生词本系统开发
- [ ] AI助手功能开发
- [ ] 学习报告功能开发
- [ ] 订阅管理功能开发

### 📅 待开发
- [ ] 测试和优化
- [ ] 小程序发布

## 🔧 配置说明

### 1. API配置
在 `utils/request.ts` 中配置API基础地址：
```typescript
// 微信小程序环境
this.baseURL = 'https://api.xreadup.com'

// H5环境
this.baseURL = '/api'
```

### 2. 微信小程序配置
在 `manifest.json` 中配置小程序AppID：
```json
{
  "mp-weixin": {
    "appid": "wx1234567890abcdef"
  }
}
```

### 3. 页面路由配置
在 `pages.json` 中配置页面路由和TabBar。

## 📝 开发规范

### 1. 代码规范
- 使用TypeScript进行类型检查
- 遵循Vue 3 Composition API规范
- 使用ESLint进行代码检查
- 使用Prettier进行代码格式化

### 2. 组件规范
- 组件命名使用PascalCase
- 组件文件放在components目录下
- 每个组件包含.vue、.ts、.scss文件

### 3. API规范
- API接口统一放在api目录下
- 使用TypeScript定义接口类型
- 统一错误处理和响应格式

### 4. 样式规范
- 使用SCSS预处理器
- 遵循BEM命名规范
- 使用CSS变量定义主题色彩
- 响应式设计适配不同屏幕

## 🐛 问题排查

### 常见问题
1. **登录失败**: 检查微信AppID配置和网络连接
2. **API请求失败**: 检查API地址配置和网络状态
3. **页面跳转失败**: 检查pages.json中的路由配置
4. **样式不生效**: 检查uni.scss导入和样式作用域

### 调试工具
- 微信开发者工具
- Chrome DevTools (H5)
- Vue DevTools
- Network面板查看API请求

## 📞 技术支持

如有问题，请联系开发团队或查看项目文档。

---

**注意**: 本项目正在积极开发中，部分功能可能尚未完成。请关注项目更新。
