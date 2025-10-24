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

      <!-- 角落链接按钮演示 -->
      <section class="demo-section">
        <h2 class="section-title">🔗 角落链接按钮系统</h2>
        <div class="corner-link-showcase">
          <div class="corner-link-group">
            <h3>基础变体</h3>
            <div class="corner-demo-container">
              <div class="corner-demo-card">
                <h4>默认样式</h4>
                <p>这是默认的角落链接按钮，采用iOS蓝色主题</p>
                <div class="original-link-corner">
                  <button class="corner-link-btn" @click="handleCornerLinkClick">
                    <el-icon><Link /></el-icon>
                    查看原文
                  </button>
                </div>
              </div>
              
              <div class="corner-demo-card">
                <h4>成功样式</h4>
                <p>成功状态的角落链接按钮，采用绿色主题</p>
                <div class="original-link-corner">
                  <button class="corner-link-btn corner-link-btn--success" @click="handleCornerLinkClick">
                    <el-icon><Check /></el-icon>
                    确认完成
                  </button>
                </div>
              </div>
              
              <div class="corner-demo-card">
                <h4>警告样式</h4>
                <p>警告状态的角落链接按钮，采用橙色主题</p>
                <div class="original-link-corner">
                  <button class="corner-link-btn corner-link-btn--warning" @click="handleCornerLinkClick">
                    <el-icon><Warning /></el-icon>
                    注意
                  </button>
                </div>
              </div>
              
              <div class="corner-demo-card">
                <h4>错误样式</h4>
                <p>错误状态的角落链接按钮，采用红色主题</p>
                <div class="original-link-corner">
                  <button class="corner-link-btn corner-link-btn--error" @click="handleCornerLinkClick">
                    <el-icon><Close /></el-icon>
                    删除
                  </button>
                </div>
              </div>
              
              <div class="corner-demo-card">
                <h4>信息样式</h4>
                <p>信息状态的角落链接按钮，采用青色主题</p>
                <div class="original-link-corner">
                  <button class="corner-link-btn corner-link-btn--info" @click="handleCornerLinkClick">
                    <el-icon><InfoFilled /></el-icon>
                    详情
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <div class="corner-link-group">
            <h3>不同尺寸</h3>
            <div class="corner-demo-container">
              <div class="corner-demo-card">
                <h4>小尺寸</h4>
                <p>适合紧凑空间的角落链接按钮</p>
                <div class="original-link-corner">
                  <button class="corner-link-btn corner-link-btn--sm" @click="handleCornerLinkClick">
                    <el-icon><Link /></el-icon>
                    链接
                  </button>
                </div>
              </div>
              
              <div class="corner-demo-card">
                <h4>默认尺寸</h4>
                <p>标准尺寸的角落链接按钮</p>
                <div class="original-link-corner">
                  <button class="corner-link-btn" @click="handleCornerLinkClick">
                    <el-icon><Link /></el-icon>
                    查看原文
                  </button>
                </div>
              </div>
              
              <div class="corner-demo-card">
                <h4>大尺寸</h4>
                <p>适合重要操作的角落链接按钮</p>
                <div class="original-link-corner">
                  <button class="corner-link-btn corner-link-btn--lg" @click="handleCornerLinkClick">
                    <el-icon><Link /></el-icon>
                    立即查看原文
                  </button>
                </div>
              </div>
            </div>
          </div>
          
          <div class="corner-link-group">
            <h3>实际应用场景</h3>
            <div class="corner-demo-container">
              <div class="corner-demo-card article-demo">
                <h4>文章阅读器</h4>
                <p>在文章末尾显示原文链接，方便用户查看原始内容</p>
                <div class="article-content-demo">
                  <p>这是文章内容的一部分，展示了如何在文章阅读器中使用角落链接按钮...</p>
                </div>
                <div class="original-link-corner">
                  <button class="corner-link-btn" @click="handleCornerLinkClick">
                    <el-icon><Link /></el-icon>
                    查看原文
                  </button>
                </div>
              </div>
              
              <div class="corner-demo-card card-demo">
                <h4>卡片组件</h4>
                <p>在卡片组件中显示相关链接，保持界面整洁</p>
                <div class="card-content-demo">
                  <p>卡片内容描述...</p>
                </div>
                <div class="original-link-corner">
                  <button class="corner-link-btn corner-link-btn--success" @click="handleCornerLinkClick">
                    <el-icon><Link /></el-icon>
                    外部链接
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 筛选面板组件演示 -->
      <section class="demo-section">
        <h2 class="section-title">🎛️ 筛选面板组件系统</h2>
        <div class="filter-showcase">
          <div class="filter-group">
            <h3>拟物化筛选面板</h3>
            <div class="filter-demo-container">
              <div class="filter-panel">
                <div class="filter-panel-header">
                  <h3>筛选</h3>
                  <el-button type="text" size="small">
                    <el-icon><ArrowLeft /></el-icon>
                  </el-button>
                </div>
                
                <div class="filter-panel-content">
                  <!-- 搜索框 -->
                  <div class="filter-section">
                    <div class="filter-header">
                      <el-icon class="filter-icon"><Search /></el-icon>
                      <h4>搜索文章</h4>
                    </div>
                    <el-input
                      placeholder="输入关键词搜索..."
                      prefix-icon="Search"
                      clearable
                      class="search-input"
                    />
                  </div>
                  
                  <!-- 难度筛选 -->
                  <div class="filter-section">
                    <div class="filter-header">
                      <el-icon class="filter-icon"><TrendCharts /></el-icon>
                      <h4>难度等级</h4>
                    </div>
                    <div class="difficulty-tags">
                      <div class="difficulty-tag active">全部</div>
                      <div class="difficulty-tag level-A1">A1</div>
                      <div class="difficulty-tag level-A2">A2</div>
                      <div class="difficulty-tag level-B1">B1</div>
                      <div class="difficulty-tag level-B2">B2</div>
                      <div class="difficulty-tag level-C1">C1</div>
                      <div class="difficulty-tag level-C2">C2</div>
                    </div>
                  </div>
                  
                  <!-- 分类筛选 -->
                  <div class="filter-section">
                    <div class="filter-header">
                      <el-icon class="filter-icon"><Collection /></el-icon>
                      <h4>文章分类</h4>
                    </div>
                    <div class="category-tags">
                      <div class="category-tag active">全部</div>
                      <div class="category-tag">科技</div>
                      <div class="category-tag">健康</div>
                      <div class="category-tag">商业</div>
                      <div class="category-tag">教育</div>
                      <div class="category-tag">娱乐</div>
                    </div>
                  </div>
                  
                  <!-- 排序方式 -->
                  <div class="filter-section">
                    <div class="filter-header">
                      <el-icon class="filter-icon"><Sort /></el-icon>
                      <h4>排序方式</h4>
                    </div>
                    <div class="sort-options">
                      <div class="sort-option active">
                        <el-icon><Clock /></el-icon>
                        <span>最新</span>
                      </div>
                      <div class="sort-option">
                        <el-icon><Star /></el-icon>
                        <span>热门</span>
                      </div>
                    </div>
                  </div>

                  <!-- 重置筛选按钮 -->
                  <div class="filter-actions">
                    <el-button 
                      type="primary" 
                      class="reset-button"
                      :icon="Refresh"
                    >
                      重置筛选
                    </el-button>
                  </div>
                </div>
              </div>
              
              <div class="filter-demo-info">
                <h4>拟物化设计特色</h4>
                <ul>
                  <li>🎨 <strong>毛玻璃效果</strong>：现代化的半透明背景</li>
                  <li>🌈 <strong>渐变色彩</strong>：丰富的视觉层次</li>
                  <li>✨ <strong>微交互</strong>：悬停、点击的流畅动画</li>
                  <li>💫 <strong>光波效果</strong>：标签悬停时的扫光动画</li>
                  <li>🎯 <strong>彩色编码</strong>：难度等级的颜色区分</li>
                  <li>📱 <strong>响应式设计</strong>：适配不同屏幕尺寸</li>
                </ul>
                
                <h4>组件特点</h4>
                <ul>
                  <li>🔍 <strong>搜索框</strong>：毛玻璃效果，聚焦时动态提升</li>
                  <li>🏷️ <strong>难度标签</strong>：A1/A2(绿)、B1/B2(橙)、C1/C2(红)</li>
                  <li>📂 <strong>分类标签</strong>：胶囊形状，悬停光波效果</li>
                  <li>🔄 <strong>排序选项</strong>：图标+文字，滑动交互</li>
                  <li>🎛️ <strong>重置按钮</strong>：全宽设计，渐变背景</li>
                </ul>
              </div>
            </div>
          </div>
          
          <div class="filter-group">
            <h3>交互式演示</h3>
            <div class="interactive-demo">
              <div class="demo-controls">
                <h4>点击标签体验交互效果</h4>
                <div class="demo-tags">
                  <div class="difficulty-tag level-A1" @click="toggleTag($event)">A1</div>
                  <div class="difficulty-tag level-B2" @click="toggleTag($event)">B2</div>
                  <div class="difficulty-tag level-C1" @click="toggleTag($event)">C1</div>
                  <div class="category-tag" @click="toggleTag($event)">科技</div>
                  <div class="category-tag" @click="toggleTag($event)">健康</div>
                  <div class="sort-option" @click="toggleTag($event)">
                    <el-icon><Clock /></el-icon>
                    <span>最新</span>
                  </div>
                </div>
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
import { Link, Check, Warning, Close, InfoFilled, Search, TrendCharts, Collection, Sort, Clock, Star, Refresh, ArrowLeft } from '@element-plus/icons-vue'
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

const handleCornerLinkClick = () => {
  console.log('角落链接按钮被点击')
}

const toggleTag = (event: Event) => {
  const target = event.target as HTMLElement
  target.classList.toggle('active')
  console.log('标签被点击:', target.textContent)
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

/* 角落链接按钮展示 */
.corner-link-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.corner-link-group h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.corner-demo-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: var(--space-6);
}

.corner-demo-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: var(--space-6);
  position: relative;
  min-height: 120px;
  transition: all var(--transition-normal);
}

.corner-demo-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
  border-color: var(--primary-200);
}

.corner-demo-card h4 {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-2);
}

.corner-demo-card p {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
  line-height: var(--line-height-normal);
}

.article-demo {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.1) 0%, 
    rgba(255, 255, 255, 0.05) 100%);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.article-content-demo {
  background: var(--bg-primary);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--ios-blue);
  margin-bottom: var(--space-4);
}

.article-content-demo p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: var(--line-height-relaxed);
}

.card-demo {
  background: linear-gradient(135deg, 
    rgba(52, 199, 89, 0.05) 0%, 
    rgba(52, 199, 89, 0.02) 100%);
  border-color: rgba(52, 199, 89, 0.2);
}

.card-content-demo {
  background: var(--bg-primary);
  padding: var(--space-4);
  border-radius: var(--radius-md);
  border-left: 3px solid var(--ios-green);
  margin-bottom: var(--space-4);
}

.card-content-demo p {
  margin: 0;
  font-size: var(--text-sm);
  color: var(--text-primary);
  line-height: var(--line-height-relaxed);
}

/* 筛选面板展示 */
.filter-showcase {
  display: flex;
  flex-direction: column;
  gap: var(--space-8);
}

.filter-group h3 {
  font-size: var(--text-lg);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.filter-demo-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-8);
  align-items: start;
}

.filter-demo-info {
  background: var(--bg-secondary);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.filter-demo-info h4 {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-3);
  margin-top: var(--space-4);
}

.filter-demo-info h4:first-child {
  margin-top: 0;
}

.filter-demo-info ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.filter-demo-info li {
  padding: var(--space-2) 0;
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-normal);
}

.filter-demo-info li strong {
  color: var(--text-primary);
  font-weight: var(--font-weight-medium);
}

.interactive-demo {
  background: var(--bg-secondary);
  padding: var(--space-6);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-light);
}

.demo-controls h4 {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
}

.demo-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-3);
  align-items: center;
}

.demo-tags .difficulty-tag,
.demo-tags .category-tag,
.demo-tags .sort-option {
  cursor: pointer;
  user-select: none;
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
  
  .corner-demo-container {
    grid-template-columns: 1fr;
  }
  
  .corner-demo-card {
    min-height: 100px;
  }
  
  .filter-demo-container {
    grid-template-columns: 1fr;
  }
  
  .demo-tags {
    justify-content: center;
  }
}
</style>
