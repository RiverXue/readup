<template>
  <view id="app">
    <!-- 应用根组件 -->
  </view>
</template>

<script setup lang="ts">
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'

// 应用启动
onLaunch(() => {
  console.log('ReadUp 小程序启动')
  
  // 检查更新
  checkForUpdate()
  
  // 初始化用户状态
  initUserState()
})

// 应用显示
onShow(() => {
  console.log('ReadUp 小程序显示')
})

// 应用隐藏
onHide(() => {
  console.log('ReadUp 小程序隐藏')
})

// 检查小程序更新
const checkForUpdate = () => {
  if (uni.canIUse('getUpdateManager')) {
    const updateManager = uni.getUpdateManager()
    
    updateManager.onCheckForUpdate((res) => {
      console.log('检查更新结果:', res.hasUpdate)
    })
    
    updateManager.onUpdateReady(() => {
      uni.showModal({
        title: '更新提示',
        content: '新版本已经准备好，是否重启应用？',
        success: (res) => {
          if (res.confirm) {
            updateManager.applyUpdate()
          }
        }
      })
    })
    
    updateManager.onUpdateFailed(() => {
      console.log('更新失败')
    })
  }
}

// 初始化用户状态
const initUserState = () => {
  // 检查本地存储的登录状态
  const token = uni.getStorageSync('token')
  const user = uni.getStorageSync('user')
  
  if (token && user) {
    console.log('用户已登录:', user.username)
  } else {
    console.log('用户未登录')
  }
}
</script>

<style lang="scss">
@import '@/uni.scss';

#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
