<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <img src="/ReadUpLogonosans.png" alt="XReadUp" class="logo-img" />
          <span v-if="!sidebarCollapsed" class="logo-text">XReadUp 后台管理</span>
        </div>
        <el-button
          type="text"
          @click="toggleSidebar"
          class="collapse-btn"
        >
          <el-icon><Fold v-if="!sidebarCollapsed" /><Expand v-else /></el-icon>
        </el-button>
      </div>

      <el-menu
        :default-active="currentRoute"
        class="admin-menu"
        :collapse="sidebarCollapsed"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/articles">
          <el-icon><Document /></el-icon>
          <span>文章管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/subscriptions">
          <el-icon><CreditCard /></el-icon>
          <span>订阅管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/ai-analysis">
          <el-icon><MagicStick /></el-icon>
          <span>AI分析</span>
        </el-menu-item>

        <el-menu-item
          index="/admin/admins"
          v-if="adminStore.hasPermission(['ADMIN_MANAGE'])"
        >
          <el-icon><UserFilled /></el-icon>
          <span>管理员管理</span>
        </el-menu-item>

        <el-menu-item index="/admin/settings">
          <el-icon><Setting /></el-icon>
          <span>系统设置</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区域 -->
    <div class="admin-main">
      <!-- 顶部导航栏 -->
      <div class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item
              v-for="item in breadcrumbs"
              :key="item.path"
              :to="item.path"
            >
              {{ item.name }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ adminStore.getAdminUserInfo()?.username || '管理员' }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人资料</el-dropdown-item>
                <el-dropdown-item command="settings">系统设置</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 页面内容 -->
      <div class="admin-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  DataBoard,
  User,
  Document,
  CreditCard,
  MagicStick,
  UserFilled,
  Setting,
  Fold,
  Expand,
  ArrowDown
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

// 侧边栏状态
const sidebarCollapsed = ref(false)

// 当前路由
const currentRoute = computed(() => route.path)

// 面包屑导航
const breadcrumbs = computed(() => {
  const pathMap: Record<string, string> = {
    '/admin/dashboard': '仪表盘',
    '/admin/users': '用户管理',
    '/admin/articles': '文章管理',
    '/admin/subscriptions': '订阅管理',
    '/admin/ai-analysis': 'AI分析',
    '/admin/admins': '管理员管理',
    '/admin/settings': '系统设置'
  }

  const currentPath = route.path
  const name = pathMap[currentPath] || '未知页面'

  return [
    { name: '管理后台', path: '/admin/dashboard' },
    ...(currentPath !== '/admin/dashboard' ? [{ name, path: currentPath }] : [])
  ]
})

// 切换侧边栏
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 处理下拉菜单命令
const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中')
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '退出确认', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        // 清除管理员状态
        adminStore.clearAdminState()
        localStorage.removeItem('admin_token')
        localStorage.removeItem('admin_token_expiry')
        localStorage.removeItem('admin_user_info')
        localStorage.removeItem('admin_role')

        ElMessage.success('已退出登录')
        router.push('/admin/login')
      } catch (error) {
        // 用户取消
      }
      break
  }
}

// 监听路由变化，自动收起侧边栏（移动端）
watch(() => route.path, () => {
  if (window.innerWidth < 768) {
    sidebarCollapsed.value = true
  }
})
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background-color: #f5f7fa;
}

.admin-sidebar {
  width: 250px;
  background-color: #304156;
  transition: width 0.3s ease;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.admin-sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background-color: #2b3a4b;
  border-bottom: 1px solid #3c4b5c;
}

.logo {
  display: flex;
  align-items: center;
  color: white;
}

.logo-img {
  width: 32px;
  height: 32px;
  margin-right: 10px;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  white-space: nowrap;
}

.collapse-btn {
  color: white;
  font-size: 18px;
}

.admin-menu {
  flex: 1;
  border: none;
  background-color: #304156;
}

.admin-menu .el-menu-item {
  color: #bfcbd9;
  height: 50px;
  line-height: 50px;
}

.admin-menu .el-menu-item:hover {
  background-color: #263445;
  color: white;
}

.admin-menu .el-menu-item.is-active {
  background-color: #409eff;
  color: white;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.admin-header {
  height: 60px;
  background-color: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin: 0 8px;
  font-size: 14px;
  color: #606266;
}

.dropdown-icon {
  font-size: 12px;
  color: #c0c4cc;
}

.admin-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    height: 100vh;
  }

  .admin-sidebar.collapsed {
    transform: translateX(-100%);
  }

  .admin-main {
    margin-left: 0;
  }
}

/* 面包屑样式 */
:deep(.el-breadcrumb__item) {
  font-size: 14px;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #606266;
  font-weight: 500;
}
</style>
