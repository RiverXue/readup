# XReadUp 设计系统

<div align="center">

![Design System](https://img.shields.io/badge/Design_System-v1.0.0-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.18-4FC08D?logo=vue.js)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.0-3178C6?logo=typescript)

</div>

## 📖 设计系统概述

XReadUp 设计系统是一个基于 Vue 3 + TypeScript 的现代化 UI 设计系统，融合了 iOS 26 设计语言和 Airbnb 的温暖美学，为智能英语学习平台提供一致且优雅的用户体验。

### 🎯 设计原则

- **一致性** - 统一的视觉语言和交互模式
- **可访问性** - 支持多种设备和用户需求
- **可扩展性** - 易于维护和扩展的组件架构
- **现代化** - 采用最新的设计趋势和技术

## 🎨 视觉设计

### 设计理念

#### Liquid Glass 效果
- **玻璃态背景** - 半透明模糊效果，现代感十足
- **柔和阴影** - 多层次阴影系统，增强层次感
- **边框光效** - 微妙的边框高光，提升质感

#### iOS 26 风格
- **简洁直观** - 符合用户习惯的交互模式
- **圆角设计** - 柔和的圆角，友好的视觉感受
- **层次分明** - 清晰的信息层级和视觉权重

#### Airbnb 温暖美学
- **温馨色彩** - 温暖而舒适的配色方案
- **友好交互** - 人性化的交互反馈
- **情感化设计** - 注重用户体验的情感连接

### 色彩系统

#### 主色调
```css
:root {
  /* iOS 系统色彩 */
  --ios-blue: #007AFF;        /* 主色调 - 系统蓝 */
  --ios-green: #34C759;       /* 成功色 - 系统绿 */
  --ios-orange: #FF9500;      /* 警告色 - 系统橙 */
  --ios-red: #FF3B30;         /* 危险色 - 系统红 */
  --ios-gray: #8E8E93;        /* 信息色 - 系统灰 */
  
  /* 背景色系 */
  --bg-primary: #FFFFFF;      /* 主背景 - 纯白 */
  --bg-secondary: #F2F2F7;    /* 次背景 - iOS 背景灰 */
  --bg-tertiary: #F9F9F9;     /* 三级背景 - 浅灰 */
  
  /* 文本色系 */
  --text-primary: #000000;    /* 主文本 - 纯黑 */
  --text-secondary: #6D6D70;  /* 次文本 - 深灰 */
  --text-tertiary: #8E8E93;   /* 三级文本 - 中灰 */
  --text-quaternary: #C7C7CC; /* 四级文本 - 浅灰 */
}
```

#### 语义化颜色
```css
:root {
  /* 状态色彩 */
  --success: var(--ios-green);
  --warning: var(--ios-orange);
  --danger: var(--ios-red);
  --info: var(--ios-blue);
  
  /* 渐变色彩 */
  --gradient-primary: linear-gradient(135deg, #007AFF 0%, #5AC8FA 100%);
  --gradient-success: linear-gradient(135deg, #34C759 0%, #30D158 100%);
  --gradient-warning: linear-gradient(135deg, #FF9500 0%, #FFCC00 100%);
  --gradient-danger: linear-gradient(135deg, #FF3B30 0%, #FF6B6B 100%);
}
```

### 字体系统

#### 字体族
```css
:root {
  --font-family-primary: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Helvetica Neue', Arial, sans-serif;
  --font-family-mono: 'SF Mono', Monaco, 'Cascadia Code', 'Roboto Mono', Consolas, 'Courier New', monospace;
}
```

#### 字体大小
```css
:root {
  --text-xs: 12px;      /* 小号文本 */
  --text-sm: 14px;      /* 标准小文本 */
  --text-base: 16px;    /* 基础文本 */
  --text-lg: 18px;      /* 大号文本 */
  --text-xl: 20px;      /* 超大文本 */
  --text-2xl: 24px;     /* 标题文本 */
  --text-3xl: 30px;     /* 大标题 */
  --text-4xl: 36px;     /* 超大标题 */
}
```

#### 字体权重
```css
:root {
  --font-weight-light: 300;    /* 细体 */
  --font-weight-normal: 400;   /* 常规 */
  --font-weight-medium: 500;   /* 中等 */
  --font-weight-semibold: 600; /* 半粗 */
  --font-weight-bold: 700;     /* 粗体 */
}
```

### 间距系统

#### 基础间距
```css
:root {
  --space-1: 4px;       /* 最小间距 */
  --space-2: 8px;       /* 小间距 */
  --space-3: 12px;      /* 中小间距 */
  --space-4: 16px;      /* 标准间距 */
  --space-5: 20px;      /* 中大间距 */
  --space-6: 24px;      /* 大间距 */
  --space-8: 32px;      /* 超大间距 */
  --space-10: 40px;     /* 特大间距 */
  --space-12: 48px;     /* 巨大间距 */
  --space-16: 64px;     /* 最大间距 */
}
```

### 圆角系统

```css
:root {
  --radius-sm: 4px;     /* 小圆角 */
  --radius-md: 8px;     /* 中圆角 */
  --radius-lg: 12px;    /* 大圆角 */
  --radius-xl: 16px;    /* 超大圆角 */
  --radius-full: 50%;   /* 完全圆角 */
}
```

### 阴影系统

```css
:root {
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);
  --shadow-xl: 0 20px 25px rgba(0, 0, 0, 0.1);
  --shadow-2xl: 0 25px 50px rgba(0, 0, 0, 0.25);
  
  /* 玻璃态阴影 */
  --glass-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  --glass-shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.15);
}
```

## 🧩 组件系统

### 按钮组件

#### 按钮变体
```vue
<!-- 主要按钮 -->
<button class="btn btn--primary">主要操作</button>

<!-- 次要按钮 -->
<button class="btn btn--secondary">次要操作</button>

<!-- 成功按钮 -->
<button class="btn btn--success">成功操作</button>

<!-- 警告按钮 -->
<button class="btn btn--warning">警告操作</button>

<!-- 危险按钮 -->
<button class="btn btn--danger">危险操作</button>

<!-- 文本按钮 -->
<button class="btn btn--text">文本按钮</button>
```

#### 按钮尺寸
```vue
<!-- 小按钮 -->
<button class="btn btn--sm">小按钮</button>

<!-- 标准按钮 -->
<button class="btn">标准按钮</button>

<!-- 大按钮 -->
<button class="btn btn--lg">大按钮</button>
```

#### 按钮状态
```vue
<!-- 加载状态 -->
<button class="btn btn--loading">
  <span class="btn__spinner"></span>
  加载中...
</button>

<!-- 禁用状态 -->
<button class="btn" disabled>禁用按钮</button>
```

### 卡片组件

#### 基础卡片
```vue
<div class="card">
  <div class="card__header">
    <h3 class="card__title">卡片标题</h3>
  </div>
  <div class="card__content">
    <p>卡片内容...</p>
  </div>
  <div class="card__footer">
    <button class="btn btn--primary">操作按钮</button>
  </div>
</div>
```

#### 玻璃态卡片
```vue
<div class="card card--glass">
  <div class="card__content">
    <h3>玻璃态卡片</h3>
    <p>具有模糊背景效果的卡片</p>
  </div>
</div>
```

### 标签组件

#### 胶囊标签
```vue
<!-- 分类标签 -->
<span class="capsule-tag capsule-tag--category">技术</span>

<!-- 难度标签 -->
<span class="capsule-tag capsule-tag--difficulty">中等</span>

<!-- 状态标签 -->
<span class="capsule-tag capsule-tag--status capsule-tag--success">已完成</span>
<span class="capsule-tag capsule-tag--status capsule-tag--warning">进行中</span>
<span class="capsule-tag capsule-tag--status capsule-tag--danger">已取消</span>
```

### 加载组件

#### 旋转加载器
```vue
<div class="smart-spinner">
  <div class="spinner-ring"></div>
  <div class="spinner-ring"></div>
  <div class="spinner-ring"></div>
</div>
```

#### 脉冲加载器
```vue
<div class="smart-pulse">
  <div class="pulse-dot"></div>
  <div class="pulse-dot"></div>
  <div class="pulse-dot"></div>
</div>
```

#### 波浪加载器
```vue
<div class="smart-wave">
  <div class="wave-bar"></div>
  <div class="wave-bar"></div>
  <div class="wave-bar"></div>
  <div class="wave-bar"></div>
  <div class="wave-bar"></div>
</div>
```

#### 骨架屏
```vue
<div class="smart-skeleton">
  <div class="skeleton-line"></div>
  <div class="skeleton-line"></div>
  <div class="skeleton-line"></div>
  <div class="skeleton-line"></div>
</div>
```

### 表单组件

#### 输入框
```vue
<div class="form-group">
  <label class="form-label">用户名</label>
  <input type="text" class="form-input" placeholder="请输入用户名">
  <div class="form-help">请输入3-20个字符</div>
</div>
```

#### 选择框
```vue
<div class="form-group">
  <label class="form-label">难度等级</label>
  <select class="form-select">
    <option value="">请选择难度</option>
    <option value="easy">简单</option>
    <option value="medium">中等</option>
    <option value="hard">困难</option>
  </select>
</div>
```

#### 复选框
```vue
<div class="form-group">
  <label class="checkbox">
    <input type="checkbox" class="checkbox__input">
    <span class="checkbox__checkmark"></span>
    <span class="checkbox__label">记住我</span>
  </label>
</div>
```

## 🎭 动画系统

### 过渡动画

#### 基础过渡
```css
.transition-base {
  transition: all 0.3s ease;
}

.transition-fast {
  transition: all 0.15s ease;
}

.transition-slow {
  transition: all 0.5s ease;
}
```

#### 微交互
```css
/* 悬停效果 */
.hover-lift:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg);
}

/* 点击效果 */
.click-scale:active {
  transform: scale(0.98);
}

/* 焦点效果 */
.focus-ring:focus {
  outline: none;
  box-shadow: 0 0 0 3px rgba(0, 122, 255, 0.3);
}
```

### 关键帧动画

#### 旋转动画
```css
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

@keyframes smartSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
```

#### 脉冲动画
```css
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

@keyframes smartPulse {
  0%, 100% { 
    transform: scale(1);
    opacity: 1;
  }
  50% { 
    transform: scale(1.1);
    opacity: 0.7;
  }
}
```

#### 波浪动画
```css
@keyframes smartWave {
  0%, 100% {
    transform: scaleY(0.4);
    opacity: 0.5;
  }
  50% {
    transform: scaleY(1);
    opacity: 1;
  }
}
```

## 📱 响应式设计

### 断点系统

```css
:root {
  --breakpoint-sm: 480px;   /* 小屏手机 */
  --breakpoint-md: 768px;   /* 平板 */
  --breakpoint-lg: 1024px;  /* 小屏笔记本 */
  --breakpoint-xl: 1200px;  /* 大屏显示器 */
  --breakpoint-2xl: 1536px; /* 超大屏显示器 */
}
```

### 媒体查询

```css
/* 移动端优先 */
@media (max-width: 480px) {
  .container {
    padding: var(--space-4);
  }
}

@media (min-width: 768px) {
  .container {
    padding: var(--space-6);
  }
}

@media (min-width: 1024px) {
  .container {
    padding: var(--space-8);
  }
}
```

### 网格系统

```css
.grid {
  display: grid;
  gap: var(--space-4);
}

.grid--2 {
  grid-template-columns: repeat(2, 1fr);
}

.grid--3 {
  grid-template-columns: repeat(3, 1fr);
}

.grid--4 {
  grid-template-columns: repeat(4, 1fr);
}

/* 响应式网格 */
@media (max-width: 768px) {
  .grid--2,
  .grid--3,
  .grid--4 {
    grid-template-columns: 1fr;
  }
}
```

## 🎨 主题系统

### 浅色主题（默认）

```css
:root {
  color-scheme: light;
  
  /* 背景色 */
  --bg-primary: #FFFFFF;
  --bg-secondary: #F2F2F7;
  --bg-tertiary: #F9F9F9;
  
  /* 文本色 */
  --text-primary: #000000;
  --text-secondary: #6D6D70;
  --text-tertiary: #8E8E93;
  
  /* 边框色 */
  --border-light: #E5E5EA;
  --border-medium: #D1D1D6;
  --border-dark: #C7C7CC;
}
```

### 深色主题

```css
[data-theme="dark"] {
  color-scheme: dark;
  
  /* 背景色 */
  --bg-primary: #1C1C1E;
  --bg-secondary: #2C2C2E;
  --bg-tertiary: #3A3A3C;
  
  /* 文本色 */
  --text-primary: #FFFFFF;
  --text-secondary: #EBEBF5;
  --text-tertiary: #8E8E93;
  
  /* 边框色 */
  --border-light: #38383A;
  --border-medium: #48484A;
  --border-dark: #636366;
}
```

## 🛠 使用指南

### 安装和引入

```typescript
// 在 main.ts 中引入设计系统
import '@/assets/design-system.css'

// 在组件中使用
import { SmartLoading } from '@/components/common/SmartLoading.vue'
```

### 自定义主题

```css
/* 覆盖 CSS 变量来自定义主题 */
:root {
  --ios-blue: #your-brand-color;
  --primary-gradient: linear-gradient(135deg, #your-color-1, #your-color-2);
}
```

### 组件使用示例

```vue
<template>
  <div class="page-container">
    <!-- 使用设计系统组件 -->
    <div class="card card--glass">
      <div class="card__header">
        <h2 class="card__title">学习进度</h2>
        <span class="capsule-tag capsule-tag--success">已完成</span>
      </div>
      
      <div class="card__content">
        <p class="text-secondary">今日学习单词：<span class="text-primary font-semibold">25</span></p>
        
        <!-- 加载状态 -->
        <SmartLoading type="spinner" text="加载中..." />
      </div>
      
      <div class="card__footer">
        <button class="btn btn--primary">继续学习</button>
        <button class="btn btn--secondary">查看详情</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-container {
  padding: var(--space-6);
  background: var(--bg-secondary);
  min-height: 100vh;
}
</style>
```

## 📚 最佳实践

### 1. 保持一致性
- 使用设计系统提供的组件和样式
- 遵循命名规范和代码约定
- 保持视觉和交互的一致性

### 2. 响应式设计
- 移动端优先的设计方法
- 使用断点系统进行适配
- 测试各种设备尺寸

### 3. 可访问性
- 提供足够的颜色对比度
- 支持键盘导航
- 添加适当的 ARIA 标签

### 4. 性能优化
- 使用 CSS 变量减少重复代码
- 合理使用动画，避免过度动画
- 优化图片和资源加载

## 🔄 更新日志

### v1.0.0 (2024-01-15)
- ✨ 初始版本发布
- 🎨 完整的色彩系统
- 🧩 基础组件库
- 📱 响应式设计支持
- 🎭 动画系统
- 🎨 Liquid Glass 效果

## 🤝 贡献指南

### 如何贡献
1. Fork 项目
2. 创建功能分支
3. 遵循设计规范
4. 提交 Pull Request

### 设计规范
- 遵循现有的设计语言
- 保持组件的可复用性
- 提供完整的文档和示例
- 确保跨浏览器兼容性

---

<div align="center">

**🎨 让设计更美好，让体验更流畅**

*XReadUp Design System - 为智能英语学习而设计*

</div>