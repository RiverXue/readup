import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import ArticleReader from '../views/ArticleReader.vue'
import VocabularyPage from '../views/VocabularyPage.vue'
import ReportPage from '../views/ReportPage.vue'
import LoginPage from '../views/LoginPage.vue'
import RegisterView from '../views/RegisterView.vue'
import SubscriptionPage from '../views/SubscriptionPage.vue'
import ArticleListView from '../views/ArticleListView.vue'
import AdminLogin from '../views/admin/AdminLogin.vue'
import AdminDashboard from '../views/admin/AdminDashboard.vue'
import UserManagement from '../views/admin/UserManagement.vue'
import ArticleManagement from '../views/admin/ArticleManagement.vue'
import SubscriptionManagement from '../views/admin/SubscriptionManagement.vue'
import SystemSettings from '../views/admin/SystemSettings.vue'
import AiAnalysisView from '../views/admin/AiAnalysisView.vue'
import AdminUsersManagement from '../views/admin/AdminUsersManagement.vue'
import UIDemoPage from '../views/UIDemoPage.vue'
import { adminGuard, adminLoginGuard } from './guards/adminGuard'
import { userGuard, loginGuard } from './guards/userGuard'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { 
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: false }
    },
    { 
      path: '/article/:id',
      name: 'article',
      component: ArticleReader,
      meta: { requiresAuth: false }
    },
    { 
      path: '/vocabulary',
      name: 'vocabulary',
      component: VocabularyPage,
      meta: { requiresAuth: true }
    },
    { 
      path: '/report',
      name: 'report',
      component: ReportPage,
      meta: { requiresAuth: true }
    },
    { 
      path: '/subscription',
      name: 'subscription',
      component: SubscriptionPage,
      meta: { requiresAuth: true }
    },
    { 
      path: '/login',
      name: 'login',
      component: LoginPage,
      meta: { requiresAuth: false },
      beforeEnter: loginGuard
    },
    { 
      path: '/register',
      name: 'register',
      component: RegisterView,
      meta: { requiresAuth: false },
      beforeEnter: loginGuard
    },
    // 阅读列表路由
    { 
      path: '/reading-list',
      name: 'readingList',
      component: ArticleListView,
      meta: { requiresAuth: false }
    },
    // UI演示页面
    { 
      path: '/ui-demo',
      name: 'uiDemo',
      component: UIDemoPage,
      meta: { requiresAuth: false }
    },
    
    // 管理员路由
    { 
      path: '/admin/login',
      name: 'adminLogin',
      component: AdminLogin,
      meta: { requiresAuth: false, requiresAdmin: false },
      beforeEnter: adminLoginGuard
    },
    { 
      path: '/admin/dashboard',
      name: 'adminDashboard',
      component: AdminDashboard,
      meta: { requiresAuth: false, requiresAdmin: true }
    },
    { 
      path: '/admin/users',
      name: 'adminUsers',
      component: UserManagement,
      meta: { requiresAuth: false, requiresAdmin: true }
    },
    { 
      path: '/admin/articles',
      name: 'adminArticles',
      component: ArticleManagement,
      meta: { requiresAuth: false, requiresAdmin: true }
    },
    { 
      path: '/admin/subscriptions',
      name: 'adminSubscriptions',
      component: SubscriptionManagement,
      meta: { requiresAuth: false, requiresAdmin: true }
    },
    { 
      path: '/admin/settings',
      name: 'adminSettings',
      component: SystemSettings,
      meta: { requiresAuth: false, requiresAdmin: true }
    },
    { 
      path: '/admin/ai-analysis',
      name: 'adminAiAnalysis',
      component: AiAnalysisView,
      meta: { requiresAuth: false, requiresAdmin: true }
    },
    { 
      path: '/admin/admins',
      name: 'adminAdmins',
      component: AdminUsersManagement,
      meta: { requiresAuth: false, requiresAdmin: true }
    }
  ]
})

// 应用全局路由守卫
// 注意：路由守卫的顺序很重要，adminGuard应该在userGuard之前
router.beforeEach((to, from, next) => {
  // 优先处理管理员路由
  if (to.meta.requiresAdmin) {
    adminGuard(to, from, next)
  } else if (to.meta.requiresAuth) {
    // 非管理员路由但需要认证
    userGuard(to, from, next)
  } else {
    // 公开路由
    next()
  }
})

export default router
