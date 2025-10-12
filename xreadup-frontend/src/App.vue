<template>
  <div class="app">
    <!-- 管理员布局 -->
    <AdminLayout v-if="isAdminRoute" />
    
    <!-- 普通用户布局 -->
    <div v-else>
      <!-- 导航栏 -->
      <Header />
      
      <!-- 主内容区域 -->
      <main class="main-content">
        <router-view />
      </main>
      
      <!-- 页脚 -->
      <Footer />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import Header from './components/Header.vue'
import Footer from './components/Footer.vue'
import AdminLayout from './components/admin/AdminLayout.vue'

const router = useRouter()
const route = useRoute()

// 判断当前是否为管理端路由
const isAdminRoute = computed(() => {
  return route.path.startsWith('/admin/')
})

// 全局初始化逻辑
onMounted(() => {
  const userStore = useUserStore()
  // 在应用启动时检查用户登录状态
  userStore.initializeUser()
})
</script>

<style>
/* 全局样式移至 assets/main.css */
@import './assets/main.css';

.app {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 0 auto;
  padding: 20px;
  background-color: #fff;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  margin-top: 20px;
  margin-bottom: 20px;
  min-height: calc(100vh - 140px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-content {
    margin: 10px;
    padding: 15px;
    border-radius: 8px;
    min-height: calc(100vh - 120px);
  }
}
</style>
