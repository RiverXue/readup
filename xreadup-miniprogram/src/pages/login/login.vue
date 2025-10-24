<template>
  <view class="login-container">
    <!-- 背景装饰 -->
    <view class="background-decoration">
      <view class="circle circle-1"></view>
      <view class="circle circle-2"></view>
      <view class="circle circle-3"></view>
    </view>
    
    <!-- Logo区域 -->
    <view class="logo-section">
      <image src="/static/logo.png" class="logo" mode="aspectFit" />
      <text class="app-name">ReadUp</text>
      <text class="app-desc">真实新闻驱动的AI英语学习平台</text>
    </view>
    
    <!-- 登录区域 -->
    <view class="login-section">
      <button 
        class="wechat-login-btn"
        @click="handleWeChatLogin"
        :loading="loginLoading"
        :disabled="loginLoading"
      >
        <image 
          src="/static/wechat-icon.png" 
          class="wechat-icon" 
          v-if="!loginLoading"
        />
        <text class="login-text">
          {{ loginLoading ? '登录中...' : '微信一键登录' }}
        </text>
      </button>
      
      <!-- 协议说明 -->
      <view class="agreement">
        <text class="agreement-text">
          登录即表示同意
          <text class="link" @click="showAgreement('user')">《用户协议》</text>
          和
          <text class="link" @click="showAgreement('privacy')">《隐私政策》</text>
        </text>
      </view>
    </view>
    
    <!-- 功能介绍 -->
    <view class="features-section">
      <view class="feature-item">
        <image src="/static/feature-news.png" class="feature-icon" />
        <text class="feature-text">真实新闻学习</text>
      </view>
      <view class="feature-item">
        <image src="/static/feature-ai.png" class="feature-icon" />
        <text class="feature-text">AI智能助手</text>
      </view>
      <view class="feature-item">
        <image src="/static/feature-vocab.png" class="feature-icon" />
        <text class="feature-text">科学记忆法</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loginLoading = ref(false)

// 微信登录处理
const handleWeChatLogin = async () => {
  if (loginLoading.value) return
  
  loginLoading.value = true
  
  try {
    await userStore.loginWithWeChat()
    
    uni.showToast({
      title: '登录成功',
      icon: 'success',
      duration: 2000
    })
    
    // 延迟跳转，让用户看到成功提示
    setTimeout(() => {
      uni.switchTab({
        url: '/pages/index/index'
      })
    }, 2000)
    
  } catch (error: any) {
    console.error('登录失败:', error)
    
    let errorMessage = '登录失败，请重试'
    
    // 根据错误类型显示不同提示
    if (error.message?.includes('网络')) {
      errorMessage = '网络连接失败，请检查网络'
    } else if (error.message?.includes('用户取消')) {
      errorMessage = '用户取消登录'
    } else if (error.message?.includes('登录已过期')) {
      errorMessage = '登录已过期，请重新登录'
    }
    
    uni.showToast({
      title: errorMessage,
      icon: 'error',
      duration: 3000
    })
  } finally {
    loginLoading.value = false
  }
}

// 显示协议
const showAgreement = (type: 'user' | 'privacy') => {
  const title = type === 'user' ? '用户协议' : '隐私政策'
  
  uni.showModal({
    title,
    content: `这里是${title}的内容...`,
    showCancel: false,
    confirmText: '我知道了'
  })
}

// 页面加载时检查登录状态
onMounted(() => {
  // 如果已经登录，直接跳转到首页
  if (userStore.isLoggedIn) {
    uni.switchTab({
      url: '/pages/index/index'
    })
  }
})
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40rpx;
  position: relative;
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  
  .circle {
    position: absolute;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.1);
    
    &.circle-1 {
      width: 200rpx;
      height: 200rpx;
      top: 10%;
      right: 10%;
      animation: float 6s ease-in-out infinite;
    }
    
    &.circle-2 {
      width: 150rpx;
      height: 150rpx;
      bottom: 20%;
      left: 15%;
      animation: float 8s ease-in-out infinite reverse;
    }
    
    &.circle-3 {
      width: 100rpx;
      height: 100rpx;
      top: 30%;
      left: 20%;
      animation: float 10s ease-in-out infinite;
    }
  }
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-20px);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: 120rpx;
  z-index: 1;
  
  .logo {
    width: 120rpx;
    height: 120rpx;
    margin-bottom: 20rpx;
  }
  
  .app-name {
    display: block;
    font-size: 48rpx;
    font-weight: bold;
    color: white;
    margin-bottom: 10rpx;
    text-shadow: 0 2rpx 4rpx rgba(0, 0, 0, 0.3);
  }
  
  .app-desc {
    display: block;
    font-size: 28rpx;
    color: rgba(255, 255, 255, 0.9);
    line-height: 1.5;
  }
}

.login-section {
  width: 100%;
  z-index: 1;
  
  .wechat-login-btn {
    width: 100%;
    height: 88rpx;
    background: #07c160;
    color: white;
    border-radius: 44rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 32rpx;
    font-weight: bold;
    border: none;
    box-shadow: 0 8rpx 24rpx rgba(7, 193, 96, 0.3);
    transition: all 0.3s ease;
    
    &:active {
      transform: translateY(2rpx);
      box-shadow: 0 4rpx 12rpx rgba(7, 193, 96, 0.3);
    }
    
    &:disabled {
      opacity: 0.7;
      transform: none;
    }
    
    .wechat-icon {
      width: 40rpx;
      height: 40rpx;
      margin-right: 16rpx;
    }
    
    .login-text {
      font-size: 32rpx;
      font-weight: bold;
    }
  }
  
  .agreement {
    margin-top: 40rpx;
    text-align: center;
    
    .agreement-text {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.8);
      line-height: 1.5;
      
      .link {
        color: #ffd700;
        text-decoration: underline;
      }
    }
  }
}

.features-section {
  margin-top: 80rpx;
  display: flex;
  justify-content: space-around;
  width: 100%;
  z-index: 1;
  
  .feature-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    
    .feature-icon {
      width: 60rpx;
      height: 60rpx;
      margin-bottom: 12rpx;
      opacity: 0.9;
    }
    
    .feature-text {
      font-size: 24rpx;
      color: rgba(255, 255, 255, 0.9);
      text-align: center;
    }
  }
}

/* 响应式适配 */
@media screen and (max-width: 750rpx) {
  .login-container {
    padding: 30rpx;
  }
  
  .logo-section {
    margin-bottom: 100rpx;
    
    .logo {
      width: 100rpx;
      height: 100rpx;
    }
    
    .app-name {
      font-size: 42rpx;
    }
    
    .app-desc {
      font-size: 26rpx;
    }
  }
  
  .login-section {
    .wechat-login-btn {
      height: 80rpx;
      font-size: 30rpx;
      
      .wechat-icon {
        width: 36rpx;
        height: 36rpx;
      }
    }
  }
  
  .features-section {
    margin-top: 60rpx;
    
    .feature-item {
      .feature-icon {
        width: 50rpx;
        height: 50rpx;
      }
      
      .feature-text {
        font-size: 22rpx;
      }
    }
  }
}
</style>
