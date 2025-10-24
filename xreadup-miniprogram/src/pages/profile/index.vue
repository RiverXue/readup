<template>
  <view class="profile-container">
    <view class="profile-header">
      <text class="profile-title">个人中心</text>
    </view>
    
    <view class="profile-content">
      <view class="user-info">
        <image 
          :src="userStore.user?.avatar || '/static/default-avatar.png'" 
          class="user-avatar"
          mode="aspectFill"
        />
        <view class="user-details">
          <text class="username">{{ userStore.user?.nickname || '未登录' }}</text>
          <text class="user-level">{{ getUserLevelText() }}</text>
        </view>
      </view>
      
      <view class="menu-list">
        <view class="menu-item" @click="goToSettings">
          <image src="/static/settings-icon.png" class="menu-icon" />
          <text class="menu-text">设置</text>
          <text class="menu-arrow">></text>
        </view>
        
        <view class="menu-item" @click="goToAbout">
          <image src="/static/about-icon.png" class="menu-icon" />
          <text class="menu-text">关于我们</text>
          <text class="menu-arrow">></text>
        </view>
        
        <view class="menu-item" @click="logout" v-if="userStore.isLoggedIn">
          <image src="/static/logout-icon.png" class="menu-icon" />
          <text class="menu-text">退出登录</text>
          <text class="menu-arrow">></text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 获取用户等级文本
const getUserLevelText = () => {
  if (!userStore.isLoggedIn) return '游客'
  
  const levelMap = {
    free: '免费用户',
    basic: '基础会员',
    pro: '专业会员',
    enterprise: '企业会员',
    guest: '游客'
  }
  
  return levelMap[userStore.userLevel] || '免费用户'
}

// 跳转到设置页面
const goToSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/index'
  })
}

// 跳转到关于页面
const goToAbout = () => {
  uni.navigateTo({
    url: '/pages/about/index'
  })
}

// 退出登录
const logout = () => {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: (res) => {
      if (res.confirm) {
        userStore.logout()
        uni.switchTab({
          url: '/pages/index/index'
        })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.profile-container {
  min-height: 100vh;
  background-color: var(--background-color);
}

.profile-header {
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  padding: 40rpx 30rpx;
  
  .profile-title {
    font-size: 36rpx;
    font-weight: bold;
    color: white;
  }
}

.profile-content {
  padding: 30rpx;
  
  .user-info {
    background: white;
    border-radius: 16rpx;
    padding: 40rpx;
    margin-bottom: 30rpx;
    display: flex;
    align-items: center;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    
    .user-avatar {
      width: 120rpx;
      height: 120rpx;
      border-radius: 60rpx;
      margin-right: 30rpx;
    }
    
    .user-details {
      flex: 1;
      
      .username {
        font-size: 32rpx;
        font-weight: bold;
        color: var(--text-primary);
        margin-bottom: 8rpx;
        display: block;
      }
      
      .user-level {
        font-size: 24rpx;
        color: var(--text-secondary);
      }
    }
  }
  
  .menu-list {
    background: white;
    border-radius: 16rpx;
    overflow: hidden;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
    
    .menu-item {
      display: flex;
      align-items: center;
      padding: 30rpx;
      border-bottom: 1rpx solid var(--border-color);
      
      &:last-child {
        border-bottom: none;
      }
      
      .menu-icon {
        width: 40rpx;
        height: 40rpx;
        margin-right: 20rpx;
      }
      
      .menu-text {
        flex: 1;
        font-size: 28rpx;
        color: var(--text-primary);
      }
      
      .menu-arrow {
        font-size: 24rpx;
        color: var(--text-secondary);
      }
    }
  }
}
</style>