<template>
  <footer class="footer">
    <div class="footer-container">
      <!-- 页脚内容 -->
      <div class="footer-content">
        <!-- 关于我们 -->
        <div class="footer-section">
          <h3>关于ReadUp AI</h3>
          <p>ReadUp AI是一款智能英语学习助手，帮助用户提升英语阅读能力，扩展词汇量。</p>
        </div>
        
        <!-- 快速链接 -->
        <div class="footer-section">
          <h3>快速链接</h3>
          <ul>
            <li><a href="/" @click.prevent="goTo('/')">首页</a></li>
            <li><a href="/article/1" @click.prevent="goTo('/article/1')">文章阅读</a></li>
            <li v-if="userStore.isLoggedIn"><a href="/vocabulary" @click.prevent="goTo('/vocabulary')">生词本</a></li>
            <li v-if="userStore.isLoggedIn"><a href="/report" @click.prevent="goTo('/report')">学习报告</a></li>
          </ul>
        </div>
        
        <!-- 联系方式 -->
        <div class="footer-section">
          <h3>联系我们</h3>
          <p>邮箱: support@readupai.com</p>
          <p>微信: ReadUp_AI</p>
        </div>
        
        <!-- 版权信息 -->
        <div class="footer-section">
          <h3>ReadUp AI</h3>
          <p>© 2025 ReadUp AI. 保留所有权利。</p>
        </div>
      </div>
      
      <!-- 底部版权行 -->
      <div class="footer-bottom">
        <div class="footer-links">
          <a href="#" @click.prevent>隐私政策</a>
          <a href="#" @click.prevent>使用条款</a>
          <a href="#" @click.prevent>帮助中心</a>
        </div>
        <div class="copyright">
          <p>ReadUp AI - 智能英语学习助手</p>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

// 跳转到指定路径
const goTo = (path: string) => {
  router.push(path)
}
</script>

<style scoped>
@import '@/assets/design-system.css';

.footer {
  background-color: var(--bg-secondary);
  border-top: 1px solid var(--border-light);
  margin-top: var(--space-16);
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: var(--space-12) var(--space-6) var(--space-6);
}

.footer-content {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-bottom: var(--space-8);
}

.footer-section {
  flex: 1;
  min-width: 200px;
  margin-bottom: var(--space-5);
  padding: 0 var(--space-4);
}

.footer-section h3 {
  font-size: var(--text-base);
  font-weight: var(--font-weight-semibold);
  color: var(--text-primary);
  margin-bottom: var(--space-4);
  border-bottom: 2px solid var(--primary-500);
  padding-bottom: var(--space-2);
  display: inline-block;
}

.footer-section p {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  line-height: var(--line-height-normal);
  margin: 0 0 var(--space-3) 0;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section li {
  margin-bottom: var(--space-3);
}

.footer-section a {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  transition: color var(--transition-normal);
}

.footer-section a:hover {
  color: var(--primary-600);
}

.footer-bottom {
  border-top: 1px solid var(--border-light);
  padding-top: var(--space-5);
  text-align: center;
}

.footer-links {
  margin-bottom: var(--space-4);
}

.footer-links a {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  margin: 0 var(--space-4);
  transition: color var(--transition-normal);
}

.footer-links a:hover {
  color: var(--primary-600);
}

.copyright p {
  font-size: var(--text-sm);
  color: var(--text-tertiary);
  margin: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
  }
  
  .footer-section {
    min-width: 100%;
    padding: 0;
    margin-bottom: var(--space-8);
  }
  
  .footer-container {
    padding: var(--space-8) var(--space-4) var(--space-6);
  }
  
  .footer-links a {
    margin: 0 var(--space-3);
  }
}
</style>