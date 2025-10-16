# 高级筛选功能使用指南

## 🎯 功能概述

高级筛选功能让用户可以精确控制新闻的获取方式，包括语言、国家、时间范围和排序方式。

## 🔍 探索文章页面的高级筛选

### 使用方式
1. **选择分类或输入自定义主题**
2. **点击"显示高级筛选"按钮**
3. **配置高级选项**
4. **点击"获取主题文章"或"搜索主题"**

### 筛选逻辑
- **基础模式**：使用简单的分类或关键词搜索
- **高级模式**：在基础搜索基础上，添加语言、国家、时间、排序筛选

### 示例场景
```
场景1：获取德国科技新闻
- 分类：technology
- 语言：English (获取英语新闻)
- 国家：Germany (德国新闻源)
- 结果：德国的英语科技新闻

场景2：获取美国的中文新闻
- 分类：business
- 语言：中文
- 国家：United States
- 结果：美国的中文商业新闻

场景3：获取最近一周的娱乐新闻
- 分类：entertainment
- 语言：English
- 国家：United States
- 开始日期：2024-01-01
- 结束日期：2024-01-07
- 结果：最近一周的美国英语娱乐新闻
```

## 📚 阅读列表页面的高级筛选

### 使用方式
1. **在左侧筛选面板设置基础筛选**
2. **点击"显示高级筛选"**
3. **配置高级选项**
4. **系统自动应用筛选**

### 筛选逻辑
- **基础筛选**：难度、分类、阅读状态、文章状态、字数、阅读时间
- **高级筛选**：语言、国家、时间范围、排序方式
- **组合筛选**：所有筛选条件同时生效

### 示例场景
```
场景1：筛选已读的德国科技文章
- 基础筛选：分类=technology, 阅读状态=已读
- 高级筛选：国家=Germany, 语言=English
- 结果：已读的德国英语科技文章

场景2：筛选最近一个月的中文商业文章
- 基础筛选：分类=business
- 高级筛选：语言=中文, 开始日期=2024-01-01, 结束日期=2024-01-31
- 结果：最近一个月的中文商业文章
```

## 🌍 语言与国家的区别

### 语言 (Language)
- **作用**：控制新闻的语言
- **示例**：English, 中文, Français, Deutsch
- **影响**：新闻内容的语言

### 国家 (Country)
- **作用**：控制新闻来源的国家
- **示例**：United States, Germany, China, Japan
- **影响**：新闻来源的地理位置

### 组合使用
- **德国英语新闻**：语言=English, 国家=Germany
- **美国中文新闻**：语言=中文, 国家=United States
- **法国法语新闻**：语言=Français, 国家=France

## ⚙️ 技术实现

### API选择逻辑
```typescript
// 探索文章页面
if (advancedFilters.useAdvanced) {
  // 使用增强版API
  response = await articleApi.getArticlesByCategoryAdvanced({
    category: selectedCategory.value,
    limit: 9,
    language: advancedFilters.language,
    country: advancedFilters.country,
    fromDate: advancedFilters.fromDate,
    toDate: advancedFilters.toDate,
    sortBy: advancedFilters.sortBy
  })
} else {
  // 使用基础版API
  response = await articleApi.getArticlesByCategory(selectedCategory.value, 9)
}
```

### 标题显示逻辑
```typescript
// 基础模式
resultTitle.value = `${categoryLabel}主题文章`

// 高级模式
resultTitle.value = `${categoryLabel}主题文章 (${languageLabel} - ${countryLabel})`
```

## 🎨 UI设计特点

### 折叠式设计
- 默认隐藏高级筛选
- 点击展开/收起
- 节省界面空间

### 视觉提示
- 国旗图标显示
- 帮助图标说明
- 描述文字解释

### 响应式布局
- 移动端友好
- 自适应筛选面板
- 触摸友好交互

## 📱 使用建议

### 新手用户
1. 先使用基础功能熟悉系统
2. 需要特定语言或国家新闻时使用高级筛选
3. 查看帮助提示了解各选项含义

### 高级用户
1. 充分利用语言和国家组合
2. 使用时间范围获取特定时期新闻
3. 根据需求选择合适排序方式

### 学习场景
1. **语言学习**：选择目标语言新闻
2. **国际视野**：选择不同国家新闻
3. **时效性**：使用时间范围筛选
4. **个性化**：结合多种筛选条件
