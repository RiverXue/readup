<template>
  <header class="header">
    <div class="header-container">
      <!-- Logo -->
      <div class="logo-container" @click="goToHome">
        <img src="../../public/ReadUpLogonosans.png" class="logo-image" alt="品牌logo"/>
        <div class="logo-text">
          <h1>ReadUp</h1>
        </div>
      </div>

      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <ul>
          <li class="nav-item">
            <router-link to="/" class="nav-link">首页</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/article/1" class="nav-link">文章阅读</router-link>
          </li>
          <li class="nav-item">
            <router-link to="/reading-list" class="nav-link">阅读列表</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <router-link to="/vocabulary" class="nav-link">生词本</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <router-link to="/report" class="nav-link">学习报告</router-link>
          </li>
          <li class="nav-item" v-if="userStore.isLoggedIn">
            <router-link to="/subscription" class="nav-link">💎 会员</router-link>
          </li>
        </ul>
      </nav>

      <!-- 用户操作区 -->
      <div class="user-actions">
        <!-- 未登录状态 -->
        <div v-if="!userStore.isLoggedIn">
          <router-link to="/login" class="btn btn-login">登录</router-link>
          <router-link to="/register" class="btn btn-register">注册</router-link>
        </div>

        <!-- 已登录状态 -->
        <div v-else class="user-info">
          <!-- 每日打卡按钮 -->
          <el-button
            :icon="hasCheckedInToday ? Check : Clock"
            size="small"
            :type="hasCheckedInToday ? 'success' : 'primary'"
            :loading="isCheckingIn"
            :disabled="hasCheckedInToday || isCheckingIn"
            @click="performCheckIn"
            class="checkin-button"
          >
            <template #default>
              <span v-if="!isCheckingIn">
                {{ hasCheckedInToday ? `已打卡${streakDays}天` : '每日打卡' }}
              </span>
              <span v-else>
                打卡中...
              </span>
            </template>
          </el-button>

          <div class="user-avatar" v-if="userStore.userInfo?.avatar">
            <img :src="userStore.userInfo.avatar" alt="用户头像">
          </div>
          <div class="user-avatar" v-else>
            <span>{{ getAvatarText }}</span>
          </div>
          <div class="user-details">
            <div class="username">{{ userStore.userInfo?.username || '用户' }}</div>
            <div class="user-level" v-if="userStore.userInfo?.level">
              Lv.{{ userStore.userInfo.level }}
            </div>
          </div>
          <el-dropdown @command="handleUserCommand">
            <el-button size="small" type="text" :icon="CaretBottom" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { CaretBottom, Check, Clock } from '@element-plus/icons-vue'
import { learningApi } from '@/utils/api'

const router = useRouter()
const userStore = useUserStore()

// 打卡相关状态
const hasCheckedInToday = ref(false)
const isCheckingIn = ref(false)
const streakDays = ref(0)

// 获取打卡状态
const fetchCheckInStatus = async () => {
  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    console.warn('用户未登录，无法获取打卡状态')
    return
  }

  // 处理用户ID可能延迟加载的情况
  if (!userStore.userInfo?.id) {
    console.log('用户ID还未加载完成，等待...')
    // 尝试等待用户信息加载完成
    const maxWaitTime = 2000; // 最多等待2秒
    const waitStep = 100; // 每100毫秒检查一次
    const startTime = Date.now();

    // 轮询等待用户ID出现
    while (!userStore.userInfo?.id && (Date.now() - startTime) < maxWaitTime) {
      await new Promise(resolve => setTimeout(resolve, waitStep));
    }

    // 如果等待后仍然没有用户ID，记录警告并返回
    if (!userStore.userInfo?.id) {
      console.warn('等待超时，用户ID仍不存在，无法获取打卡状态')
      return
    }
  }

  try {
    const userId = userStore.userInfo.id.toString()
    const todayKey = `checkin_${userId}_${getTodayKey()}`

    // 1. 先从本地存储加载状态，保证快速显示
    const localCheckinStatus = localStorage.getItem(todayKey)
    if (localCheckinStatus !== null) {
      hasCheckedInToday.value = localCheckinStatus === 'true'
      console.log('从本地存储加载打卡状态:', hasCheckedInToday.value)
    }

    // 2. 获取今日学习统计数据，更新打卡状态
    try {
      const todayStatsResponse = await learningApi.getTodaySummary(userId)

      // 更新状态并保存到本地存储
      if (todayStatsResponse.data?.hasCheckedInToday !== undefined) {
        hasCheckedInToday.value = todayStatsResponse.data.hasCheckedInToday
        localStorage.setItem(todayKey, hasCheckedInToday.value.toString())
        console.log('从API更新打卡状态:', hasCheckedInToday.value)
      } else if (todayStatsResponse.data?.studyTime > 0 || todayStatsResponse.data?.wordsLearned > 0) {
        // 如果有学习时间或学习单词数，也可以视为已打卡
        hasCheckedInToday.value = true
        localStorage.setItem(todayKey, 'true')
        console.log('根据学习活动推断打卡状态:', hasCheckedInToday.value)
      }
    } catch (statsError) {
      console.warn('获取学习统计数据失败，保持本地状态不变:', statsError)
    }

    // 3. 获取连续打卡天数 - 直接使用学习记录API的打卡接口
    try {
      // 直接调用dailyCheckIn接口获取真实的连续打卡天数
      // 注意：即使当天已经打卡，这个接口也会正确返回当前连续打卡天数
      const checkInResponse = await learningApi.dailyCheckIn(userId)
      if (checkInResponse.data !== undefined) {
        streakDays.value = checkInResponse.data
        console.log('直接从打卡API获取连续打卡天数:', streakDays.value)
        // 更新打卡状态（如果从打卡API可以推断）
        if (!hasCheckedInToday.value && streakDays.value > 0) {
          hasCheckedInToday.value = true
          localStorage.setItem(todayKey, 'true')
        }
        return
      }

      // 如果获取失败，保持现有值不变
      console.warn('获取连续打卡天数失败，保持现有值不变')
    } catch (streakError) {
      console.warn('获取连续打卡天数过程中发生错误，保持当前值:', streakError)
      // 出错时不重置streakDays，保留之前的值
    }
  } catch (error) {
    console.error('获取打卡相关数据时发生错误:', error)
  }
}

// 获取今日的唯一标识（用于本地存储键名）
const getTodayKey = () => {
  const today = new Date()
  return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, '0')}-${String(today.getDate()).padStart(2, '0')}`
}

// 执行打卡
const performCheckIn = async () => {
  if (isCheckingIn.value || hasCheckedInToday.value) {
    return
  }

  if (!userStore.userInfo?.id) {
    ElMessage.warning('请先登录')
    return
  }

  isCheckingIn.value = true
  try {
    const userId = userStore.userInfo.id.toString()
    const response = await learningApi.dailyCheckIn(userId)
    hasCheckedInToday.value = true

    // 将打卡状态保存到本地存储，防止刷新丢失
    const todayKey = `checkin_${userId}_${getTodayKey()}`
    localStorage.setItem(todayKey, 'true')

    // 重新获取连续打卡天数
    try {
      const checkInResponse = await learningApi.dailyCheckIn(userId)
      if (checkInResponse.data !== undefined) {
        streakDays.value = checkInResponse.data
        console.log('打卡后更新连续打卡天数:', streakDays.value)
      }
    } catch (streakError) {
      console.warn('获取连续打卡天数失败:', streakError)
    }

    // 显示成功消息
    ElMessage.success('打卡成功！获得积分奖励')
  } catch (error) {
    console.error('打卡失败:', error)
    ElMessage.error('打卡失败，请稍后再试')
  } finally {
    isCheckingIn.value = false
  }
}

// 导入ElMessage
import { ElMessage } from 'element-plus'

// 获取头像文字（用户名首字符）
const getAvatarText = computed(() => {
  if (!userStore.userInfo?.username) return '用户'
  return userStore.userInfo.username.charAt(0).toUpperCase()
})

// 跳转到首页
const goToHome = () => {
  router.push('/')
}

// 页面挂载时获取打卡状态
onMounted(() => {
  // 如果用户已登录，获取完整的打卡状态
  if (userStore.isLoggedIn) {
    fetchCheckInStatus()
  }
})

// 监听用户登录状态变化，更新打卡状态
watch(() => userStore.isLoggedIn, (newVal) => {
  if (newVal) {
    // 登录成功后延迟一点时间，确保用户信息已经加载完成
    setTimeout(() => {
      fetchCheckInStatus()
    }, 300)
  } else {
    // 用户登出时重置状态
    hasCheckedInToday.value = false
    streakDays.value = 0
  }
})

// 监听用户信息变化，确保在用户信息加载完成后获取打卡状态
watch(() => userStore.userInfo?.id, (newUserId, oldUserId) => {
  if (newUserId && newUserId !== oldUserId) {
    // 用户信息加载完成或切换后获取打卡状态
    fetchCheckInStatus()
  }
})

// 处理用户操作命令
const handleUserCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      // 这里可以添加跳转到个人资料页面的逻辑
      router.push('/profile')
      break
    case 'subscription':
      // 跳转到会员订阅页面
      router.push('/subscription')
      break
    case 'settings':
      // 这里可以添加跳转到设置页面的逻辑
      router.push('/settings')
      break
    case 'logout':
      await userStore.logout()
      router.push('/login')
      break
  }
}
</script>

<style scoped>
@import '@/assets/design-system.css';

.header {
  background-color: var(--bg-primary);
  box-shadow: var(--shadow-lg);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid var(--border-light);
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-6);
  height: 64px;
}

.logo-container {
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all var(--transition-normal);
}

.logo-container:hover {
  transform: scale(1.02);
}

.logo-image {
  width: 40px;
  height: 40px;
  margin-right: var(--space-2);
  border-radius: var(--radius-lg);
}

.logo-text h1 {
  margin: 0;
  font-size: var(--text-2xl);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--primary-600) 0%, var(--warm-orange) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.nav-menu ul {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.nav-item {
  margin: 0 var(--space-4);
}

.nav-link {
  text-decoration: none;
  color: var(--text-secondary);
  font-size: var(--text-sm);
  padding: var(--space-2) var(--space-3);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
  font-weight: var(--font-weight-medium);
}

.nav-link:hover {
  color: var(--primary-600);
  background-color: var(--primary-50);
}

.nav-link.router-link-active {
  color: var(--primary-600);
  font-weight: var(--font-weight-semibold);
  background-color: var(--primary-50);
}

.user-actions {
  display: flex;
  align-items: center;
}

.btn {
  padding: var(--space-2) var(--space-4);
  border-radius: var(--radius-lg);
  text-decoration: none;
  font-size: var(--text-sm);
  transition: all var(--transition-normal);
  margin-left: var(--space-3);
  font-weight: var(--font-weight-medium);
}

.btn-login {
  color: var(--primary-600);
  border: 1px solid var(--border-medium);
  background-color: var(--bg-primary);
}

.btn-login:hover {
  border-color: var(--primary-300);
  background-color: var(--primary-50);
}

.btn-register {
  color: var(--text-inverse);
  background-color: var(--primary-600);
  border: 1px solid var(--primary-600);
}

.btn-register:hover {
  background-color: var(--primary-700);
  border-color: var(--primary-700);
}

.user-info {
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  background: linear-gradient(135deg, var(--primary-500), var(--warm-orange));
  color: var(--text-inverse);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: var(--text-sm);
  font-weight: var(--font-weight-semibold);
  overflow: hidden;
  transition: all var(--transition-normal);
}

.user-avatar:hover {
  transform: scale(1.05);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-details {
  text-align: right;
}

.checkin-button {
  margin-right: var(--space-6);
}

.username {
  font-size: var(--text-sm);
  color: var(--text-primary);
  font-weight: var(--font-weight-medium);
}

.user-level {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-top: var(--space-1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-menu {
    display: none;
  }

  .header-container {
    padding: 0 var(--space-4);
  }

  .logo-text h1 {
    font-size: var(--text-xl);
  }

  .logo-image {
    width: 32px;
    height: 32px;
  }
}
</style>
