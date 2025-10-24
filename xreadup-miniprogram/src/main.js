// main.js - 应用入口文件
import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)
  
  // 创建 Pinia 实例
  const pinia = createPinia()
  app.use(pinia)
  
  return {
    app,
    pinia
  }
}
