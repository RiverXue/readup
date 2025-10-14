<template>
  <div class="ui-demo-page">
    <!-- 页面头部 -->
    <header class="demo-header">
      <div class="demo-header-content">
        <h1 class="demo-title">🎨 XReadUp UI 设计系统演示</h1>
        <p class="demo-subtitle">现代化双系统前端设计规范 - iOS 26 + Airbnb 风格</p>
        <div class="demo-badges">
          <span class="capsule-tag capsule-tag--primary">Liquid Glass</span>
          <span class="capsule-tag capsule-tag--success">iOS 26</span>
          <span class="capsule-tag capsule-tag--warning">Airbnb</span>
          <span class="capsule-tag capsule-tag--info">Vue 3</span>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="demo-main">
      <!-- 色彩系统演示 -->
      <section class="demo-section">
        <h2 class="section-title">🌈 色彩系统</h2>
        <div class="color-grid">
          <div class="color-group">
            <h3>主色调 (60%)</h3>
            <div class="color-palette">
              <div class="color-item" style="background: var(--primary-500);">
                <span>Primary 500</span>
                <code>#0a8bff</code>
              </div>
              <div class="color-item" style="background: var(--ios-blue);">
                <span>iOS Blue</span>
                <code>#007AFF</code>
              </div>
              <div class="color-item" style="background: var(--ios-green);">
                <span>iOS Green</span>
                <code>#34C759</code>
              </div>
            </div>
          </div>
          
          <div class="color-group">
            <h3>辅助色 (30%)</h3>
            <div class="color-palette">
              <div class="color-item" style="background: var(--neutral-100); color: var(--text-primary);">
                <span>Neutral 100</span>
                <code>#f5f5f5</code>
              </div>
              <div class="color-item" style="background: var(--neutral-500);">
                <span>Neutral 500</span>
                <code>#737373</code>
              </div>
              <div class="color-item" style="background: var(--neutral-900);">
                <span>Neutral 900</span>
                <code>#171717</code>
              </div>
            </div>
          </div>
          
          <div class="color-group">
            <h3>强调色 (10%)</h3>
            <div class="color-palette">
              <div class="color-item" style="background: var(--accent-success);">
                <span>Success</span>
                <code>#10b981</code>
              </div>
              <div class="color-item" style="background: var(--accent-warning);">
                <span>Warning</span>
                <code>#f59e0b</code>
              </div>
              <div class="color-item" style="background: var(--accent-error);">
                <span>Error</span>
                <code>#ef4444</code>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 按钮系统演示 -->
      <section class="demo-section">
        <h2 class="section-title">🔘 按钮系统</h2>
        <div class="button-showcase">
          <div class="button-group">
            <h3>TactileButton 触觉反馈按钮</h3>
            <div class="button-row">
              <TactileButton variant="primary" @click="handleButtonClick">
                <template #icon>🚀</template>
                主要按钮
              </TactileButton>
              <TactileButton variant="secondary" @click="handleButtonClick">
                次要按钮
              </TactileButton>
              <TactileButton variant="success" @click="handleButtonClick">
                成功按钮
              </TactileButton>
              <TactileButton variant="warning" @click="handleButtonClick">
                警告按钮
              </TactileButton>
              <TactileButton variant="danger" @click="handleButtonClick">
                危险按钮
              </TactileButton>
              <TactileButton variant="liquid-glass" @click="handleButtonClick">
                玻璃态按钮
              </TactileButton>
            </div>
          </div>
          
          <div class="button-group">
            <h3>不同尺寸</h3>
            <div class="button-row">
              <TactileButton variant="primary" size="sm" @click="handleButtonClick">小按钮</TactileButton>
              <TactileButton variant="primary" size="md" @click="handleButtonClick">中等按钮</TactileButton>
              <TactileButton variant="primary" size="lg" @click="handleButtonClick">大按钮</TactileButton>
            </div>
          </div>
          
          <div class="button-group">
            <h3>特殊状态</h3>
            <div class="button-row">
              <TactileButton variant="primary" :loading="true">加载中</TactileButton>
              <TactileButton variant="primary" :disabled="true">禁用状态</TactileButton>
              <TactileButton variant="primary" block>块级按钮</TactileButton>
            </div>
          </div>
        </div>
      </section>

      <!-- 卡片系统演示 -->
      <section class="demo-section">
        <h2 class="section-title">📄 卡片系统</h2>
        <div class="card-showcase">
          <div class="card-group">
            <h3>现代卡片 (Modern Card)</h3>
            <div class="card-grid">
              <div class="modern-card" @click="handleCardClick">
                <div class="modern-card-content">
                  <h3 class="modern-card-title">现代卡片标题</h3>
                  <p class="modern-card-description">
                    这是一个现代化的卡片组件，采用了玻璃态效果和Airbnb风格的设计理念。
                    具有优雅的悬停动画和触觉反馈。
                  </p>
                  <div class="modern-card-meta">
                    <span class="capsule-tag capsule-tag--category">设计</span>
                    <span class="capsule-tag capsule-tag--difficulty">中等</span>
                  </div>
                </div>
              </div>
              
              <div class="modern-card" @click="handleCardClick">
                <div class="modern-card-content">
                  <h3 class="modern-card-title">Liquid Glass 效果</h3>
                  <p class="modern-card-description">
                    展示了iOS 26风格的Liquid Glass效果，包括背景模糊、渐变叠加和动态阴影。
                  </p>
                  <div class="modern-card-meta">
                    <span class="capsule-tag capsule-tag--time">5分钟</span>
                    <span class="capsule-tag capsule-tag--word-count">300词</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="card-group">
            <h3>文章卡片 (Article Card)</h3>
            <div class="card-grid">
              <ArticleCard :article="sampleArticle1" />
              <ArticleCard :article="sampleArticle2" />
            </div>
          </div>
        </div>
      </section>

      <!-- 标签系统演示 -->
      <section class="demo-section">
        <h2 class="section-title">🏷️ 标签系统</h2>
        <div class="tag-showcase">
          <div class="tag-group">
            <h3>胶囊标签 (Capsule Tags)</h3>
            <div class="tag-row">
              <span class="capsule-tag capsule-tag--primary">主要标签</span>
              <span class="capsule-tag capsule-tag--success">成功标签</span>
              <span class="capsule-tag capsule-tag--warning">警告标签</span>
              <span class="capsule-tag capsule-tag--error">错误标签</span>
              <span class="capsule-tag capsule-tag--info">信息标签</span>
            </div>
          </div>
          
          <div class="tag-group">
            <h3>语义化标签</h3>
            <div class="tag-row">
              <span class="capsule-tag capsule-tag--category">科技</span>
              <span class="capsule-tag capsule-tag--difficulty">B2</span>
              <span class="capsule-tag capsule-tag--time">5分钟</span>
              <span class="capsule-tag capsule-tag--word-count">500词</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 加载状态演示 -->
      <section class="demo-section">
        <h2 class="section-title">⏳ 加载状态系统</h2>
        <div class="loading-showcase">
          <div class="loading-group">
            <h3>智能加载组件</h3>
            <div class="loading-grid">
              <div class="loading-item">
                <h4>旋转加载器</h4>
                <SmartLoading type="spinner" text="加载中..." />
              </div>
              <div class="loading-item">
                <h4>脉冲加载器</h4>
                <SmartLoading type="pulse" text="处理中..." />
              </div>
              <div class="loading-item">
                <h4>波浪加载器</h4>
                <SmartLoading type="wave" text="同步中..." />
              </div>
              <div class="loading-item skeleton-demo">
                <h4>骨架屏</h4>
                <div class="skeleton-container">
                  <SmartLoading type="skeleton" :lines="4" />
                </div>
              </div>
            </div>
          </div>
          
          <div class="loading-group">
            <h3>进度条</h3>
            <div class="progress-demo">
              <SmartLoading 
                type="spinner" 
                text="上传文件..." 
                :show-progress="true" 
                :progress="progressValue" 
              />
              <div class="progress-controls">
                <TactileButton variant="secondary" size="sm" @click="updateProgress">
                  更新进度
                </TactileButton>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 表单系统演示 -->
      <section class="demo-section">
        <h2 class="section-title">📝 表单系统</h2>
        <div class="form-showcase">
          <div class="form-demo">
            <h3>现代输入框</h3>
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input 
                class="modern-input-enhanced" 
                type="text" 
                placeholder="请输入用户名"
                v-model="formData.username"
              />
            </div>
            <div class="form-group">
              <label class="form-label">邮箱</label>
              <input 
                class="modern-input-enhanced" 
                type="email" 
                placeholder="请输入邮箱地址"
                v-model="formData.email"
              />
            </div>
            <div class="form-group">
              <label class="form-label">描述</label>
              <textarea 
                class="modern-input-enhanced" 
                placeholder="请输入描述信息"
                rows="4"
                v-model="formData.description"
              ></textarea>
            </div>
            <div class="form-actions">
              <TactileButton variant="primary" @click="handleSubmit">提交</TactileButton>
              <TactileButton variant="secondary" @click="handleReset">重置</TactileButton>
            </div>
          </div>
        </div>
      </section>

      <!-- 网格系统演示 -->
      <section class="demo-section">
        <h2 class="section-title">📐 网格系统</h2>
        <div class="grid-showcase">
          <div class="modern-grid modern-grid--2">
            <div class="grid-item">网格项 1</div>
            <div class="grid-item">网格项 2</div>
          </div>
          <div class="modern-grid modern-grid--3">
            <div class="grid-item">网格项 1</div>
            <div class="grid-item">网格项 2</div>
            <div class="grid-item">网格项 3</div>
          </div>
          <div class="modern-grid modern-grid--4">
            <div class="grid-item">网格项 1</div>
            <div class="grid-item">网格项 2</div>
            <div class="grid-item">网格项 3</div>
            <div class="grid-item">网格项 4</div>
          </div>
        </div>
      </section>

      <!-- 动画效果演示 -->
      <section class="demo-section">
        <h2 class="section-title">✨ 动画效果</h2>
        <div class="animation-showcase">
          <div class="animation-group">
            <h3>悬停效果</h3>
            <div class="animation-demo">
              <div class="hover-demo liquid-glass-card">
                <h4>悬停我</h4>
                <p>体验Liquid Glass悬停效果</p>
              </div>
              <div class="hover-demo floating-card">
                <h4>浮动卡片</h4>
                <p>Airbnb风格悬停效果</p>
              </div>
            </div>
          </div>
          
          <div class="animation-group">
            <h3>加载动画</h3>
            <div class="animation-demo">
              <div class="loading-demo">
                <div class="modern-spinner-enhanced"></div>
                <span>智能加载中...</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 响应式演示 -->
      <section class="demo-section">
        <h2 class="section-title">📱 响应式设计</h2>
        <div class="responsive-showcase">
          <div class="responsive-demo">
            <h3>自适应网格</h3>
            <div class="modern-grid-enhanced modern-grid-enhanced--auto">
              <div class="responsive-item">响应式项目 1</div>
              <div class="responsive-item">响应式项目 2</div>
              <div class="responsive-item">响应式项目 3</div>
              <div class="responsive-item">响应式项目 4</div>
              <div class="responsive-item">响应式项目 5</div>
              <div class="responsive-item">响应式项目 6</div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 页面底部 -->
    <footer class="demo-footer">
      <div class="demo-footer-content">
        <p>🎨 XReadUp UI 设计系统 - 现代化双系统前端设计规范</p>
        <p>融合 iOS 26 + Airbnb 设计理念，打造一致的用户体验</p>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import TactileButton from '@/components/common/TactileButton.vue'
import SmartLoading from '@/components/common/SmartLoading.vue'
import ArticleCard from '@/components/ArticleCard.vue'

// 响应式数据
const progressValue = ref(0)
const formData = reactive({
  username: '',
  email: '',
  description: ''
})

// 示例文章数据
const sampleArticle1 = {
  id: 1,
  title: '现代前端设计系统的最佳实践',
  description: '探索如何构建一个现代化、可扩展的前端设计系统，包括色彩、排版、组件和交互设计。',
  category: '设计',
  difficulty: 3,
  wordCount: 1200,
  enContent: 'Modern frontend design systems represent the pinnacle of user interface development...'
}

const sampleArticle2 = {
  id: 2,
  title: 'Liquid Glass 效果实现指南',
  description: '学习如何使用CSS实现iOS 26风格的Liquid Glass效果，包括背景模糊、渐变叠加和动态阴影。',
  category: '技术',
  difficulty: 4,
  wordCount: 800,
  enContent: 'Liquid Glass effects have become a defining feature of modern iOS design...'
}

// 事件处理
const handleButtonClick = () => {
  console.log('按钮被点击')
}

const handleCardClick = () => {
  console.log('卡片被点击')
}

const updateProgress = () => {
  progressValue.value = Math.min(progressValue.value + 20, 100)
}

const handleSubmit = () => {
  console.log('表单提交:', formData)
}

const handleReset = () => {
  Object.assign(formData, {
    username: '',
    email: '',
    description: ''
  })
}
</script>

<style scoped>
@import '@/assets/design-system.css';

.ui-demo-page {
  min-height: 100vh;
  background: linear-gradient(135deg, 
    var(--bg-secondary) 0%, 
    var(--bg-primary) 50%, 
    var(--bg-secondary) 100%);
  background-attachment: fixed;
}

/* 页面头部 */
.demo-header {
  background: var(--gradient-liquid-glass);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--glass-border);
  padding: var(--space-16) 0;
  text-align: center;
}

.demo-header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-6);
}

.demo-title {
  font-size: var(--text-5xl);
  font-weight: var(--font-weight-bold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
  background: linear-gradient(135deg, var(--text-primary) 0%, var(--ios-blue) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.demo-subtitle {
  font-size: var(--text-xl);
  color: var(--text-secondary);
  margin-bottom: var(--space-6);
  line-height: var(--line-height-relaxed);
}

.demo-badges {
  display: flex;
  gap: var(--space-3);
  justify-content: center;
  flex-wrap: wrap;
}

/* 主要内容区域 */
.demo-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-16) var(--space-6);
}

.demo-section {
  margin-bottom: var(--space-20);
  background: var(--glass-white);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-radius: var(--radius-ios-large);
  padding: var(--space-8);
  border: 1px solid var(--glass-border);
  box-shadow: var(--shadow-ios-medium);
}

.section-title {
  font-size: var(--text-3xl);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-8);
  text-align: center;
  position: relative;
}

.section-title::after {
  content: '';
  position: absolute;
  bottom: -var(--space-2);
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background: var(--gradient-primary);
  border-radius: var(--radius-full);
}

/* 色彩系统 */
.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-8);
}

.color-group h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.color-palette {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.color-item {
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: var(--font-weight-medium);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.color-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.color-item code {
  background: rgba(0, 0, 0, 0.2);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-sm);
  font-family: var(--font-family-mono);
  font-size: var(--text-xs);
}

/* 按钮展示 */
.button-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.button-group h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.button-row {
  display: flex;
  gap: var(--space-4);
  flex-wrap: wrap;
  align-items: center;
}

/* 卡片展示 */
.card-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.card-group h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: var(--space-6);
}

/* 标签展示 */
.tag-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.tag-group h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.tag-row {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

/* 加载状态展示 */
.loading-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.loading-group h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.loading-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: var(--space-6);
}

.loading-item {
  background: var(--bg-secondary);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  text-align: center;
  border: 1px solid var(--border-light);
}

.loading-item h4 {
  font-size: var(--text-base);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.skeleton-demo {
  min-height: 120px;
}

.skeleton-container {
  width: 100%;
  max-width: 300px;
  margin: 0 auto;
}

.progress-demo {
  background: var(--bg-secondary);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  text-align: center;
  border: 1px solid var(--border-light);
}

.progress-controls {
  margin-top: var(--space-4);
}

/* 表单展示 */
.form-showcase {
  display: flex;
  justify-content: center;
}

.form-demo {
  width: 100%;
  max-width: 500px;
  background: var(--bg-secondary);
  padding: var(--space-8);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.form-group {
  margin-bottom: var(--space-6);
}

.form-label {
  display: block;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-medium);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.form-actions {
  display: flex;
  gap: var(--space-4);
  justify-content: center;
  margin-top: var(--space-8);
}

/* 网格展示 */
.grid-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.grid-item {
  background: var(--gradient-primary);
  color: white;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  text-align: center;
  font-weight: var(--font-weight-medium);
  box-shadow: var(--shadow-sm);
}

/* 动画展示 */
.animation-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.animation-group h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.animation-demo {
  display: flex;
  gap: var(--space-6);
  flex-wrap: wrap;
}

.hover-demo {
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  text-align: center;
  cursor: pointer;
  transition: all var(--transition-normal);
  min-width: 200px;
}

.hover-demo h4 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.hover-demo p {
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.loading-demo {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-6);
  background: var(--bg-secondary);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

/* 响应式展示 */
.responsive-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-6);
}

.responsive-demo h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.responsive-item {
  background: var(--gradient-warm);
  color: white;
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  text-align: center;
  font-weight: var(--font-weight-medium);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-normal);
}

.responsive-item:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

/* 页面底部 */
.demo-footer {
  background: var(--gradient-liquid-glass);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-top: 1px solid var(--glass-border);
  padding: var(--space-12) 0;
  text-align: center;
}

.demo-footer-content p {
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.demo-footer-content p:last-child {
  margin-bottom: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .demo-main {
    padding: var(--space-8) var(--space-4);
  }
  
  .demo-section {
    padding: var(--space-6);
  }
  
  .demo-title {
    font-size: var(--text-4xl);
  }
  
  .demo-subtitle {
    font-size: var(--text-lg);
  }
  
  .button-row {
    flex-direction: column;
    align-items: stretch;
  }
  
  .card-grid {
    grid-template-columns: 1fr;
  }
  
  .loading-grid {
    grid-template-columns: 1fr;
  }
  
  .animation-demo {
    flex-direction: column;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .demo-header {
    padding: var(--space-12) 0;
  }
  
  .demo-title {
    font-size: var(--text-3xl);
  }
  
  .demo-badges {
    flex-direction: column;
    align-items: center;
  }
  
  .color-grid {
    grid-template-columns: 1fr;
  }
  
  .tag-row {
    justify-content: center;
  }
}
</style>
