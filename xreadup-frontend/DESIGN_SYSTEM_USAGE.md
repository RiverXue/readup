# 现代设计系统使用指南

## 🎨 新增设计元素

基于文章卡片和"换一批"按钮的成功设计，我们已将以下设计元素融入到全局 `design-system.css` 中：

### 1. 现代卡片系统 (Modern Card System)

#### 基础用法
```html
<div class="modern-card">
  <div class="modern-card-content">
    <h3 class="modern-card-title">卡片标题</h3>
    <p class="modern-card-description">卡片描述内容...</p>
    <div class="modern-card-meta">
      <!-- 元信息内容 -->
    </div>
  </div>
</div>
```

#### 特性
- ✅ **玻璃态背景**：渐变背景 + 模糊效果
- ✅ **悬停动画**：动态渐变叠加层
- ✅ **立体阴影**：多层阴影创造深度
- ✅ **响应式设计**：移动端优化

### 2. 胶囊标签系统 (Capsule Tag System)

#### 基础用法
```html
<!-- 基础胶囊标签 -->
<span class="capsule-tag capsule-tag--primary">主要标签</span>
<span class="capsule-tag capsule-tag--success">成功标签</span>
<span class="capsule-tag capsule-tag--warning">警告标签</span>
<span class="capsule-tag capsule-tag--error">错误标签</span>
<span class="capsule-tag capsule-tag--info">信息标签</span>

<!-- 带图标的胶囊标签 -->
<span class="capsule-tag capsule-tag--category">科技</span>
<span class="capsule-tag capsule-tag--difficulty">B2</span>
<span class="capsule-tag capsule-tag--time">5分钟</span>
<span class="capsule-tag capsule-tag--word-count">500词</span>
```

#### 变体类型
- `capsule-tag--primary`: 蓝色渐变
- `capsule-tag--success`: 绿色渐变
- `capsule-tag--warning`: 橙色渐变
- `capsule-tag--error`: 红色渐变
- `capsule-tag--info`: 青色渐变
- `capsule-tag--time`: 时间标签（红色）
- `capsule-tag--word-count`: 词数标签（青色）

#### 特性
- ✅ **玻璃态效果**：backdrop-filter模糊
- ✅ **悬停动画**：向上移动 + 阴影增强
- ✅ **图标支持**：自动添加相关图标
- ✅ **渐变背景**：彩色渐变背景

### 3. TactileButton 系统

#### 基础用法
```html
<!-- 基础按钮 -->
<button class="tactile-button tactile-button--primary">主要按钮</button>
<button class="tactile-button tactile-button--secondary">次要按钮</button>
<button class="tactile-button tactile-button--warning">警告按钮</button>
<button class="tactile-button tactile-button--liquid-glass">玻璃态按钮</button>

<!-- 不同尺寸 -->
<button class="tactile-button tactile-button--primary tactile-button--sm">小按钮</button>
<button class="tactile-button tactile-button--primary tactile-button--lg">大按钮</button>

<!-- 带图标 -->
<button class="tactile-button tactile-button--primary">
  <span>🔄</span>
  换一批
</button>
```

#### 变体类型
- `tactile-button--primary`: 绿色渐变（主要操作）
- `tactile-button--secondary`: 蓝色边框（次要操作）
- `tactile-button--warning`: 黄色渐变（警告操作）
- `tactile-button--liquid-glass`: 玻璃态效果

#### 特性
- ✅ **涟漪效果**：点击时的水波纹动画
- ✅ **触觉反馈**：悬停时的立体效果
- ✅ **渐变背景**：现代渐变色彩
- ✅ **多层阴影**：创造立体感

## 🔄 迁移指南

### 从旧设计迁移

#### 1. 卡片组件迁移
```html
<!-- 旧设计 -->
<div class="airbnb-modern-card">
  <div class="card-content compact">
    <!-- 内容 -->
  </div>
</div>

<!-- 新设计 -->
<div class="modern-card">
  <div class="modern-card-content">
    <h3 class="modern-card-title">标题</h3>
    <p class="modern-card-description">描述</p>
    <div class="modern-card-meta">
      <!-- 元信息 -->
    </div>
  </div>
</div>
```

#### 2. 标签组件迁移
```html
<!-- 旧设计 -->
<span class="tag category">科技</span>
<span class="tag difficulty">B2</span>

<!-- 新设计 -->
<span class="capsule-tag capsule-tag--category">科技</span>
<span class="capsule-tag capsule-tag--difficulty">B2</span>
```

#### 3. 按钮组件迁移
```html
<!-- 旧设计 -->
<button class="modern-button modern-button--primary">按钮</button>

<!-- 新设计 -->
<button class="tactile-button tactile-button--primary">按钮</button>
```

## 📱 响应式支持

所有新增的设计元素都包含完整的响应式支持：

```css
@media (max-width: 768px) {
  .modern-card { border-radius: 16px; }
  .modern-card-content { padding: 20px; }
  .capsule-tag { padding: 4px 8px; font-size: 11px; }
  .tactile-button { padding: 10px 20px; font-size: 13px; }
}
```

## 🎯 最佳实践

### 1. 卡片使用
- 使用 `modern-card` 作为容器
- 内容放在 `modern-card-content` 中
- 标题使用 `modern-card-title`
- 描述使用 `modern-card-description`
- 元信息放在 `modern-card-meta` 中

### 2. 标签使用
- 优先使用语义化的变体（如 `capsule-tag--time`）
- 利用自动图标功能
- 保持标签文字简洁

### 3. 按钮使用
- 主要操作使用 `tactile-button--primary`
- 次要操作使用 `tactile-button--secondary`
- 警告操作使用 `tactile-button--warning`
- 特殊效果使用 `tactile-button--liquid-glass`

## ⚠️ 重要注意事项

### 避免类嵌套冲突
**问题**: 组件同时使用多个设计系统类会导致样式冲突和嵌套问题。

**错误示例**:
```html
<!-- ❌ 错误：同时使用卡片类和按钮类 -->
<div class="airbnb-modern-card tactile-button" @click="handleClick">
<div class="discovery-card tactile-button" @click="handleClick">
```

**正确做法**:
```html
<!-- ✅ 正确：每个组件使用独立的类 -->
<div class="airbnb-modern-card" @click="handleClick">
<div class="discovery-card" @click="handleClick">
<button class="tactile-button tactile-button--primary">按钮</button>
```

### 设计系统类使用原则
1. **单一职责**: 每个组件只使用一个主要设计系统类
2. **避免混合**: 不要将不同系统的类混合使用
3. **独立设计**: 每个组件保持自己的独立设计风格
4. **功能分离**: 卡片组件负责展示，按钮组件负责交互

### 已修复的组件
- ✅ `ArticleCard.vue`: 移除 `tactile-button` 类，只使用 `airbnb-modern-card`
- ✅ `DiscoveryArticleCard.vue`: 移除 `tactile-button` 类，只使用 `discovery-card`

## 🚀 未来扩展

这些设计元素为未来的UI组件提供了坚实的基础：

1. **一致性**：所有组件使用相同的设计语言
2. **可维护性**：集中管理设计系统
3. **可扩展性**：易于添加新的变体和功能
4. **性能优化**：使用CSS变量和现代CSS特性

## 📚 相关文件

- `src/assets/design-system.css` - 主要设计系统文件
- `src/components/ArticleCard.vue` - 文章卡片组件
- `src/components/DiscoveryArticleCard.vue` - 探索文章卡片组件
- `src/components/common/TactileButton.vue` - 触觉按钮组件
