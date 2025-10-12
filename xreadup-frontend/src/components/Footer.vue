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
.footer {
  background-color: #f8f9fa;
  border-top: 1px solid #e9ecef;
  margin-top: 40px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 20px 20px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  margin-bottom: 30px;
}

.footer-section {
  flex: 1;
  min-width: 200px;
  margin-bottom: 20px;
  padding: 0 15px;
}

.footer-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 15px;
  border-bottom: 2px solid #409eff;
  padding-bottom: 8px;
  display: inline-block;
}

.footer-section p {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0 0 10px 0;
}

.footer-section ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.footer-section li {
  margin-bottom: 10px;
}

.footer-section a {
  text-decoration: none;
  color: #606266;
  font-size: 14px;
  transition: color 0.3s ease;
}

.footer-section a:hover {
  color: #409eff;
}

.footer-bottom {
  border-top: 1px solid #e9ecef;
  padding-top: 20px;
  text-align: center;
}

.footer-links {
  margin-bottom: 15px;
}

.footer-links a {
  text-decoration: none;
  color: #606266;
  font-size: 14px;
  margin: 0 15px;
  transition: color 0.3s ease;
}

.footer-links a:hover {
  color: #409eff;
}

.copyright p {
  font-size: 14px;
  color: #909399;
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
    margin-bottom: 30px;
  }
  
  .footer-container {
    padding: 30px 15px 20px;
  }
  
  .footer-links a {
    margin: 0 10px;
  }
}
</style>