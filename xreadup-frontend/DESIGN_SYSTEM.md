# XReadUp Frontend 设计系统文档

<div align="center">

![Design System](https://img.shields.io/badge/Design_System-XReadUp-blue.svg)
![Version](https://img.shields.io/badge/Version-1.0.43-green.svg)
![Status](https://img.shields.io/badge/Status-Production_Ready-success.svg)

**现代化双系统前端设计规范**

</div>

## 📋 设计系统概述

XReadUp Frontend 设计系统是一个完整的视觉和交互设计规范，专为智能英语学习平台打造。系统采用现代化的设计理念，支持双系统架构（用户端+管理员端），确保整个应用的视觉一致性和用户体验的连贯性。

## 🎨 设计理念

### 核心设计原则

1. **用户为中心**：以学习者的需求为核心，提供直观、高效的学习体验
2. **双系统统一**：用户端和管理员端保持设计语言一致，但功能区分明确
3. **现代化简约**：采用现代扁平化设计，减少视觉干扰，突出内容
4. **响应式优先**：移动端优先设计，确保多设备一致性体验
5. **无障碍友好**：支持键盘导航、屏幕阅读器等无障碍功能

### 设计语言

- **风格**：现代简约、专业可信
- **调性**：温暖友好、专业高效
- **情感**：鼓励学习、成就导向

## 🌈 色彩系统

### 主色调系统

#### Element Plus 色彩体系
```css
:root {
  /* 主色调 */
  --el-color-primary: #409eff;
  --el-color-primary-light-3: #79bbff;
  --el-color-primary-light-5: #a0cfff;
  --el-color-primary-light-7: #c6e2ff;
  --el-color-primary-light-8: #d9ecff;
  --el-color-primary-light-9: #ecf5ff;
  --el-color-primary-dark-2: #337ecc;
  
  /* 功能色 */
  --el-color-success: #67c23a;
  --el-color-warning: #e6a23c;
  --el-color-danger: #f56c6c;
  --el-color-error: #f56c6c;
  --el-color-info: #909399;
  
  /* 中性色 */
  --el-text-color-primary: #303133;
  --el-text-color-regular: #606266;
  --el-text-color-secondary: #909399;
  --el-text-color-placeholder: #c0c4cc;
  --el-text-color-disabled: #c0c4cc;
  
  /* 背景色 */
  --el-bg-color: #ffffff;
  --el-bg-color-page: #f2f3f5;
  --el-bg-color-overlay: #ffffff;
  
  /* 边框色 */
  --el-border-color: #dcdfe6;
  --el-border-color-light: #e4e7ed;
  --el-border-color-lighter: #ebeef5;
  --el-border-color-extra-light: #f2f6fc;
}
```

#### 自定义色彩扩展
```css
:root {
  /* 学习相关色彩 */
  --learning-primary: #409eff;      /* 学习主色 */
  --learning-success: #67c23a;      /* 学习成功 */
  --learning-warning: #e6a23c;     /* 学习提醒 */
  --learning-danger: #f56c6c;      /* 学习错误 */
  
  /* 阅读相关色彩 */
  --reading-bg: #fafafa;           /* 阅读背景 */
  --reading-text: #2c3e50;         /* 阅读文本 */
  --reading-highlight: #e3f2fd;    /* 高亮背景 */
  --reading-border: #e0e0e0;       /* 阅读边框 */
  
  /* 管理员系统色彩 */
  --admin-primary: #409eff;         /* 管理员主色 */
  --admin-secondary: #909399;      /* 管理员次要色 */
  --admin-success: #67c23a;        /* 操作成功 */
  --admin-warning: #e6a23c;        /* 操作警告 */
  --admin-danger: #f56c6c;         /* 操作危险 */
  
  /* 状态色彩 */
  --status-active: #67c23a;        /* 激活状态 */
  --status-inactive: #909399;      /* 非激活状态 */
  --status-pending: #e6a23c;       /* 待处理状态 */
  --status-error: #f56c6c;         /* 错误状态 */
}
```

### 色彩使用规范

| 色彩类型 | 颜色值 | 使用场景 | 示例 |
|----------|--------|----------|------|
| **主色调** | #409eff | 品牌色、主要按钮、关键交互 | 登录按钮、导航激活状态 |
| **成功色** | #67c23a | 成功状态、确认操作 | 打卡成功、操作完成 |
| **警告色** | #e6a23c | 警告信息、待处理状态 | 提醒信息、待确认操作 |
| **危险色** | #f56c6c | 错误状态、删除操作 | 错误提示、删除按钮 |
| **信息色** | #909399 | 辅助信息、次要文本 | 提示文本、元数据信息 |
| **学习高亮** | #e3f2fd | 单词高亮、重点内容 | 双击朗读反馈、选中状态 |

### 暗色模式支持

```css
@media (prefers-color-scheme: dark) {
  :root {
    --el-bg-color: #1a1a1a;
    --el-bg-color-page: #141414;
    --el-text-color-primary: #e5eaf3;
    --el-text-color-regular: #cfd3dc;
    --el-text-color-secondary: #a3a6ad;
    --el-border-color: #4c4d4f;
    --el-border-color-light: #414243;
    --el-border-color-lighter: #363637;
    
    /* 学习相关暗色 */
    --reading-bg: #1e1e1e;
    --reading-text: #e5eaf3;
    --reading-highlight: #2c3e50;
    --reading-border: #4c4d4f;
  }
}
```

## 📝 排版系统

### 字体系统

#### 字体族定义
```css
:root {
  /* 中文字体 */
  --font-family-chinese: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  
  /* 英文字体 */
  --font-family-english: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
  
  /* 等宽字体 */
  --font-family-mono: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
  
  /* 默认字体 */
  --font-family-base: var(--font-family-chinese);
}
```

#### 字体层级系统

| 层级 | 字号 | 字重 | 行高 | 用途 | 示例 |
|------|------|------|------|------|------|
| **H1** | 32px | 700 | 1.2 | 页面主标题 | 首页标题、Logo |
| **H2** | 24px | 600 | 1.3 | 区域标题 | 文章标题、功能区块 |
| **H3** | 20px | 600 | 1.4 | 组件标题 | 卡片标题、表单标题 |
| **H4** | 18px | 500 | 1.5 | 次级标题 | 列表标题、分组标题 |
| **Body** | 16px | 400 | 1.6 | 正文内容 | 文章内容、描述文本 |
| **Small** | 14px | 400 | 1.5 | 辅助文本 | 提示信息、元数据 |
| **Caption** | 12px | 400 | 1.4 | 说明文字 | 版权信息、时间戳 |

#### 字体样式类
```css
/* 标题样式 */
.text-h1 { font-size: 32px; font-weight: 700; line-height: 1.2; }
.text-h2 { font-size: 24px; font-weight: 600; line-height: 1.3; }
.text-h3 { font-size: 20px; font-weight: 600; line-height: 1.4; }
.text-h4 { font-size: 18px; font-weight: 500; line-height: 1.5; }

/* 正文样式 */
.text-body { font-size: 16px; font-weight: 400; line-height: 1.6; }
.text-small { font-size: 14px; font-weight: 400; line-height: 1.5; }
.text-caption { font-size: 12px; font-weight: 400; line-height: 1.4; }

/* 字重样式 */
.font-light { font-weight: 300; }
.font-normal { font-weight: 400; }
.font-medium { font-weight: 500; }
.font-semibold { font-weight: 600; }
.font-bold { font-weight: 700; }

/* 文本颜色 */
.text-primary { color: var(--el-text-color-primary); }
.text-regular { color: var(--el-text-color-regular); }
.text-secondary { color: var(--el-text-color-secondary); }
.text-placeholder { color: var(--el-text-color-placeholder); }
.text-disabled { color: var(--el-text-color-disabled); }
```

## 🧩 组件设计规范

### 1. 按钮组件系统

#### 按钮类型定义

**主要按钮 (Primary)**
```css
.btn-primary {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  color: #ffffff;
}

.btn-primary:hover {
  background-color: var(--el-color-primary-light-3);
  border-color: var(--el-color-primary-light-3);
}
```

**次要按钮 (Secondary)**
```css
.btn-secondary {
  background-color: transparent;
  border-color: var(--el-color-primary);
  color: var(--el-color-primary);
}

.btn-secondary:hover {
  background-color: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-light-3);
}
```

**文字按钮 (Text)**
```css
.btn-text {
  background-color: transparent;
  border: none;
  color: var(--el-color-primary);
}

.btn-text:hover {
  background-color: var(--el-color-primary-light-9);
}
```

#### 按钮尺寸系统

| 尺寸 | 高度 | 内边距 | 字体大小 | 使用场景 |
|------|------|--------|----------|----------|
| **Large** | 40px | 12px 20px | 16px | 主要操作、移动端 |
| **Default** | 32px | 8px 16px | 14px | 常规操作 |
| **Small** | 24px | 4px 12px | 12px | 次要操作、紧凑布局 |

#### 按钮状态系统

```css
/* 按钮状态 */
.btn {
  transition: all 0.2s ease-in-out;
  cursor: pointer;
  border-radius: 4px;
  font-weight: 500;
}

.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.btn:active {
  transform: translateY(0);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.btn:disabled:hover {
  transform: none;
  box-shadow: none;
}
```

### 2. 卡片组件系统

#### 卡片类型定义

**基础卡片**
```css
.card {
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  transition: all 0.3s ease;
}

.card:hover {
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
```

**文章卡片**
```css
.article-card {
  padding: 20px;
  margin-bottom: 16px;
}

.article-card-header {
  margin-bottom: 12px;
}

.article-card-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 8px;
}

.article-card-meta {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
```

**统计卡片**
```css
.stat-card {
  text-align: center;
  padding: 24px;
}

.stat-card-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--el-color-primary);
  margin-bottom: 8px;
}

.stat-card-label {
  font-size: 14px;
  color: var(--el-text-color-secondary);
}
```

### 3. 导航组件系统

#### 顶部导航栏
```css
.header {
  background-color: var(--el-bg-color);
  border-bottom: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
}

.nav-menu ul {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: 32px;
}

.nav-link {
  color: var(--el-text-color-regular);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s ease;
}

.nav-link:hover,
.nav-link.router-link-active {
  color: var(--el-color-primary);
}
```

#### 管理员侧边栏
```css
.admin-sidebar {
  width: 240px;
  background-color: var(--el-bg-color);
  border-right: 1px solid var(--el-border-color-lighter);
  height: 100vh;
  position: fixed;
  left: 0;
  top: 0;
  overflow-y: auto;
}

.admin-sidebar-menu {
  padding: 20px 0;
}

.admin-sidebar-item {
  padding: 12px 20px;
  color: var(--el-text-color-regular);
  text-decoration: none;
  display: block;
  transition: all 0.2s ease;
}

.admin-sidebar-item:hover {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.admin-sidebar-item.active {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  border-right: 3px solid var(--el-color-primary);
}
```

### 4. 表单组件系统

#### 输入框组件
```css
.form-input {
  width: 100%;
  height: 32px;
  padding: 0 12px;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.form-input:focus {
  border-color: var(--el-color-primary);
  outline: none;
  box-shadow: 0 0 0 2px var(--el-color-primary-light-8);
}

.form-input.error {
  border-color: var(--el-color-danger);
}

.form-input:disabled {
  background-color: var(--el-bg-color-page);
  color: var(--el-text-color-disabled);
  cursor: not-allowed;
}
```

#### 表单标签
```css
.form-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.form-label.required::after {
  content: ' *';
  color: var(--el-color-danger);
}
```

#### 表单验证
```css
.form-error {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-color-danger);
}

.form-help {
  margin-top: 4px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
```

### 5. 阅读相关组件

#### 文章阅读器
```css
.article-reader {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  line-height: 1.8;
}

.article-title {
  font-size: 24px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.article-content {
  font-size: 16px;
  color: var(--el-text-color-primary);
  margin-bottom: 20px;
}

.article-word {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.article-word:hover {
  background-color: var(--reading-highlight);
}

.article-word.active {
  background-color: var(--el-color-primary-light-8);
}
```

#### TTS控制组件
```css
.tts-control {
  position: fixed;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  background-color: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.tts-control-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.tts-control-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.tts-control-buttons {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.tts-speed-control {
  margin-bottom: 16px;
}

.tts-speed-label {
  font-size: 14px;
  color: var(--el-text-color-regular);
  margin-bottom: 8px;
}
```

## 🎯 图标系统

### Element Plus 图标库

#### 图标分类使用

| 分类 | 图标示例 | 使用场景 |
|------|----------|----------|
| **操作图标** | Edit, Delete, Save, Search | 编辑、删除、保存、搜索操作 |
| **状态图标** | Success, Warning, Error, Info | 成功、警告、错误、信息提示 |
| **导航图标** | Home, Menu, User, Setting | 首页、菜单、用户、设置导航 |
| **功能图标** | Reading, Book, Chart, Star | 阅读、生词、报告、收藏功能 |
| **学习图标** | Trophy, Clock, Check, CircleClose | 成就、时间、完成、关闭 |

#### 图标使用规范

```css
/* 图标尺寸 */
.icon-xs { font-size: 12px; }
.icon-sm { font-size: 14px; }
.icon-md { font-size: 16px; }
.icon-lg { font-size: 20px; }
.icon-xl { font-size: 24px; }

/* 图标颜色 */
.icon-primary { color: var(--el-color-primary); }
.icon-success { color: var(--el-color-success); }
.icon-warning { color: var(--el-color-warning); }
.icon-danger { color: var(--el-color-danger); }
.icon-info { color: var(--el-color-info); }
.icon-secondary { color: var(--el-text-color-secondary); }
```

## 🎬 动画效果系统

### 过渡动画

#### 标准过渡
```css
/* 标准过渡时间 */
.transition-fast { transition: all 0.15s ease; }
.transition-normal { transition: all 0.3s ease; }
.transition-slow { transition: all 0.5s ease; }

/* 缓动函数 */
.ease-in { transition-timing-function: ease-in; }
.ease-out { transition-timing-function: ease-out; }
.ease-in-out { transition-timing-function: ease-in-out; }
```

#### 页面转场动画
```css
/* 淡入动画 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

/* 滑入动画 */
@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.slide-in {
  animation: slideIn 0.3s ease-out;
}
```

#### 交互反馈动画
```css
/* 悬停上浮效果 */
.hover-lift {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

/* 点击缩放效果 */
.click-scale {
  transition: transform 0.1s ease;
}

.click-scale:active {
  transform: scale(0.98);
}

/* 加载动画 */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

/* 脉冲动画 */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.pulse {
  animation: pulse 2s ease-in-out infinite;
}
```

## 📱 响应式设计系统

### 断点系统

```css
/* 断点定义 */
:root {
  --breakpoint-xs: 480px;
  --breakpoint-sm: 768px;
  --breakpoint-md: 1024px;
  --breakpoint-lg: 1200px;
  --breakpoint-xl: 1440px;
}

/* 媒体查询 */
@media (max-width: 767px) {
  /* 移动端样式 */
  .container { padding: 0 16px; }
  .nav-menu { display: none; }
  .mobile-menu { display: block; }
}

@media (min-width: 768px) and (max-width: 1023px) {
  /* 平板端样式 */
  .container { padding: 0 24px; }
  .grid-2 { grid-template-columns: 1fr 1fr; }
}

@media (min-width: 1024px) {
  /* 桌面端样式 */
  .container { padding: 0 32px; }
  .grid-3 { grid-template-columns: 1fr 1fr 1fr; }
}
```

### 网格系统

```css
/* 网格容器 */
.grid-container {
  display: grid;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

/* 网格布局 */
.grid-1 { grid-template-columns: 1fr; }
.grid-2 { grid-template-columns: 1fr 1fr; }
.grid-3 { grid-template-columns: 1fr 1fr 1fr; }
.grid-4 { grid-template-columns: 1fr 1fr 1fr 1fr; }

/* 响应式网格 */
@media (max-width: 767px) {
  .grid-2, .grid-3, .grid-4 { grid-template-columns: 1fr; }
}

@media (min-width: 768px) and (max-width: 1023px) {
  .grid-3, .grid-4 { grid-template-columns: 1fr 1fr; }
}
```

### 移动端优化

```css
/* 触摸友好 */
.touch-target {
  min-height: 44px;
  min-width: 44px;
  padding: 12px 16px;
}

/* 移动端导航 */
.mobile-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: var(--el-bg-color);
  border-top: 1px solid var(--el-border-color-lighter);
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
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.mobile-nav-item.active {
  color: var(--el-color-primary);
}
```

## 🎨 主题系统

### 主题切换

```css
/* 主题变量 */
[data-theme="light"] {
  --el-bg-color: #ffffff;
  --el-text-color-primary: #303133;
  --el-border-color: #dcdfe6;
}

[data-theme="dark"] {
  --el-bg-color: #1a1a1a;
  --el-text-color-primary: #e5eaf3;
  --el-border-color: #4c4d4f;
}

/* 主题切换动画 */
.theme-transition {
  transition: background-color 0.3s ease, color 0.3s ease, border-color 0.3s ease;
}
```

### 学习模式主题

```css
/* 专注阅读模式 */
.reading-mode {
  --reading-bg: #f8f9fa;
  --reading-text: #2c3e50;
  --reading-highlight: #e3f2fd;
}

.reading-mode .article-reader {
  background-color: var(--reading-bg);
  color: var(--reading-text);
  padding: 40px;
  border-radius: 8px;
}

/* 夜间阅读模式 */
.night-reading {
  --reading-bg: #1e1e1e;
  --reading-text: #e5eaf3;
  --reading-highlight: #2c3e50;
}
```

## 📊 数据可视化设计

### ECharts 主题配置

```javascript
// 默认主题配置
const defaultTheme = {
  color: [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
    '#79bbff', '#95d475', '#f0c78a', '#f89898', '#b1b3b8'
  ],
  backgroundColor: 'transparent',
  textStyle: {
    color: '#303133',
    fontSize: 12
  },
  title: {
    textStyle: {
      color: '#303133',
      fontSize: 16,
      fontWeight: 600
    }
  },
  legend: {
    textStyle: {
      color: '#606266'
    }
  },
  grid: {
    borderColor: '#e4e7ed'
  }
}

// 暗色主题配置
const darkTheme = {
  color: [
    '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
    '#79bbff', '#95d475', '#f0c78a', '#f89898', '#b1b3b8'
  ],
  backgroundColor: 'transparent',
  textStyle: {
    color: '#e5eaf3',
    fontSize: 12
  },
  title: {
    textStyle: {
      color: '#e5eaf3',
      fontSize: 16,
      fontWeight: 600
    }
  },
  legend: {
    textStyle: {
      color: '#cfd3dc'
    }
  },
  grid: {
    borderColor: '#4c4d4f'
  }
}
```

### 图表样式规范

```css
/* 图表容器 */
.chart-container {
  background-color: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.04);
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin-bottom: 16px;
}

.chart-subtitle {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 20px;
}
```

## 🛠️ 组件开发规范

### Vue 组件规范

#### 组件命名
```typescript
// 组件命名：PascalCase
const ArticleReader = defineComponent({...})
const UserManagement = defineComponent({...})
const TTSControl = defineComponent({...})
```

#### 组件结构
```vue
<template>
  <!-- 使用语义化标签 -->
  <div class="component-container">
    <header class="component-header">
      <h2 class="component-title">{{ title }}</h2>
    </header>
    <main class="component-content">
      <!-- 组件内容 -->
    </main>
    <footer class="component-footer">
      <!-- 组件操作 -->
    </footer>
  </div>
</template>

<script setup lang="ts">
// 导入顺序：Vue -> 第三方库 -> 自定义模块
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

// Props 定义
interface Props {
  title: string
  data?: any[]
}

const props = withDefaults(defineProps<Props>(), {
  data: () => []
})

// Emits 定义
interface Emits {
  update: [value: any]
  close: []
}

const emit = defineEmits<Emits>()

// 响应式数据
const isLoading = ref(false)
const userStore = useUserStore()

// 计算属性
const filteredData = computed(() => {
  return props.data.filter(item => item.active)
})

// 方法
const handleUpdate = (value: any) => {
  emit('update', value)
}

// 生命周期
onMounted(() => {
  console.log('Component mounted')
})
</script>

<style scoped>
/* 使用 BEM 命名规范 */
.component-container {
  background-color: var(--el-bg-color);
  border-radius: 8px;
  padding: 20px;
}

.component-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.component-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  margin: 0;
}

.component-content {
  margin-bottom: 16px;
}

.component-footer {
  padding-top: 12px;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
```

### CSS 类命名规范

```css
/* BEM 命名规范 */
.block { }                    /* 块 */
.block__element { }          /* 元素 */
.block--modifier { }         /* 修饰符 */

/* 示例 */
.article-card { }            /* 文章卡片块 */
.article-card__title { }     /* 文章卡片标题元素 */
.article-card--featured { } /* 文章卡片特色修饰符 */

/* 状态类 */
.is-active { }               /* 激活状态 */
.is-disabled { }             /* 禁用状态 */
.is-loading { }              /* 加载状态 */
.has-error { }               /* 错误状态 */
```

## 🔍 设计系统检查清单

### 视觉一致性检查

- [ ] **颜色使用**：所有组件使用设计系统定义的颜色变量
- [ ] **字体层级**：文本使用正确的字体大小、字重和行高
- [ ] **间距规范**：组件间、元素间的间距保持一致
- [ ] **圆角规范**：统一使用 4px、8px 圆角
- [ ] **阴影规范**：统一使用设计系统定义的阴影效果

### 交互一致性检查

- [ ] **按钮状态**：悬停、点击、禁用状态符合规范
- [ ] **表单交互**：输入框聚焦、验证状态符合规范
- [ ] **导航交互**：链接激活、悬停状态符合规范
- [ ] **动画效果**：过渡时间、缓动函数符合规范

### 响应式检查

- [ ] **移动端适配**：在 768px 以下设备显示正常
- [ ] **平板端适配**：在 768px-1024px 设备显示正常
- [ ] **桌面端适配**：在 1024px 以上设备显示正常
- [ ] **触摸友好**：触摸目标大小符合 44px 最小要求

### 无障碍检查

- [ ] **颜色对比度**：文本与背景对比度符合 WCAG 标准
- [ ] **键盘导航**：支持 Tab 键导航
- [ ] **屏幕阅读器**：提供适当的 ARIA 标签
- [ ] **焦点指示**：清晰的焦点状态指示

## 📚 设计系统工具

### CSS 变量工具

```css
/* 颜色工具类 */
.bg-primary { background-color: var(--el-color-primary); }
.bg-success { background-color: var(--el-color-success); }
.bg-warning { background-color: var(--el-color-warning); }
.bg-danger { background-color: var(--el-color-danger); }

.text-primary { color: var(--el-color-primary); }
.text-success { color: var(--el-color-success); }
.text-warning { color: var(--el-color-warning); }
.text-danger { color: var(--el-color-danger); }

/* 间距工具类 */
.m-0 { margin: 0; }
.m-1 { margin: 4px; }
.m-2 { margin: 8px; }
.m-3 { margin: 12px; }
.m-4 { margin: 16px; }
.m-5 { margin: 20px; }

.p-0 { padding: 0; }
.p-1 { padding: 4px; }
.p-2 { padding: 8px; }
.p-3 { padding: 12px; }
.p-4 { padding: 16px; }
.p-5 { padding: 20px; }
```

### 设计系统文档生成

```javascript
// 设计系统配置
const designSystem = {
  colors: {
    primary: '#409eff',
    success: '#67c23a',
    warning: '#e6a23c',
    danger: '#f56c6c',
    info: '#909399'
  },
  typography: {
    h1: { fontSize: '32px', fontWeight: 700, lineHeight: 1.2 },
    h2: { fontSize: '24px', fontWeight: 600, lineHeight: 1.3 },
    h3: { fontSize: '20px', fontWeight: 600, lineHeight: 1.4 },
    body: { fontSize: '16px', fontWeight: 400, lineHeight: 1.6 }
  },
  spacing: {
    xs: '4px',
    sm: '8px',
    md: '12px',
    lg: '16px',
    xl: '20px',
    xxl: '24px'
  },
  borderRadius: {
    sm: '4px',
    md: '8px',
    lg: '12px'
  }
}
```

## 🌟 设计系统总结

### 核心价值

1. **一致性**：确保整个应用的视觉和交互一致性
2. **效率**：提高设计和开发效率，减少重复工作
3. **可维护性**：便于后续维护和更新
4. **扩展性**：支持新功能和组件的快速开发
5. **用户体验**：提供统一、优质的用户体验

### 设计原则

1. **用户为中心**：以学习者的需求为核心设计
2. **简洁明了**：避免过度设计，保持界面简洁
3. **功能导向**：设计服务于功能，不为了设计而设计
4. **响应式优先**：确保多设备一致性体验
5. **无障碍友好**：支持所有用户的使用需求

### 持续改进

设计系统是一个持续演进的过程，需要：

- 定期收集用户反馈
- 持续优化组件和样式
- 保持与最新设计趋势的同步
- 确保技术实现的可行性
- 维护文档的及时更新

---

<div align="center">

**🎨 构建现代化设计系统，打造一致的用户体验**

*设计于 ❤️ 与最佳实践*

</div>