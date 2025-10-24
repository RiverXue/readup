<template>
  <view class="chat-container">
    <!-- 聊天头部 -->
    <view class="chat-header">
      <view class="header-left">
        <button class="back-btn" @click="goBack">
          <image src="/static/back-icon.png" class="back-icon" />
        </button>
        <view class="header-info">
          <text class="chat-title">{{ currentSession?.title || 'AI助手' }}</text>
          <text class="chat-subtitle">Rayda老师在线</text>
        </view>
      </view>
      <view class="header-right">
        <button class="menu-btn" @click="showMenu">
          <image src="/static/menu-icon.png" class="menu-icon" />
        </button>
      </view>
    </view>

    <!-- 聊天消息列表 -->
    <scroll-view 
      class="chat-messages" 
      scroll-y="true" 
      :scroll-top="scrollTop"
      scroll-with-animation="true"
    >
      <view class="messages-list">
        <view 
          class="message-item"
          :class="{ 'user-message': message.type === 'user', 'assistant-message': message.type === 'assistant' }"
          v-for="message in messages"
          :key="message.id"
        >
          <view class="message-avatar" v-if="message.type === 'assistant'">
            <image src="/static/ai-avatar.png" class="avatar-image" />
          </view>
          
          <view class="message-content">
            <view class="message-bubble">
              <text class="message-text">{{ message.content }}</text>
              <text class="message-time">{{ formatTime(message.timestamp) }}</text>
            </view>
            
            <!-- 消息操作 -->
            <view class="message-actions" v-if="message.type === 'assistant'">
              <button class="action-btn" @click="copyMessage(message.content)">
                <image src="/static/copy-icon.png" class="action-icon" />
              </button>
              <button class="action-btn" @click="likeMessage(message.id)">
                <image src="/static/like-icon.png" class="action-icon" />
              </button>
              <button class="action-btn" @click="regenerateMessage(message.id)">
                <image src="/static/regenerate-icon.png" class="action-icon" />
              </button>
            </view>
          </view>
          
          <view class="message-avatar" v-if="message.type === 'user'">
            <image :src="userStore.user?.avatar || '/static/default-avatar.png'" class="avatar-image" />
          </view>
        </view>
      </view>

      <!-- 加载状态 -->
      <view class="loading-message" v-if="isLoading">
        <view class="message-avatar">
          <image src="/static/ai-avatar.png" class="avatar-image" />
        </view>
        <view class="message-content">
          <view class="message-bubble">
            <view class="typing-indicator">
              <view class="typing-dot"></view>
              <view class="typing-dot"></view>
              <view class="typing-dot"></view>
            </view>
          </view>
        </view>
      </view>
    </scroll-view>

    <!-- 快捷操作 -->
    <view class="quick-actions" v-if="showQuickActions">
      <view class="action-item" @click="askQuestion('请帮我总结这篇文章')">
        <image src="/static/summary-icon.png" class="action-icon" />
        <text class="action-text">文章总结</text>
      </view>
      <view class="action-item" @click="askQuestion('请分析这个句子的语法')">
        <image src="/static/grammar-icon.png" class="action-icon" />
        <text class="action-text">语法分析</text>
      </view>
      <view class="action-item" @click="askQuestion('请推荐一些学习建议')">
        <image src="/static/suggestion-icon.png" class="action-icon" />
        <text class="action-text">学习建议</text>
      </view>
      <view class="action-item" @click="askQuestion('请生成一些练习题')">
        <image src="/static/exercise-icon.png" class="action-icon" />
        <text class="action-text">生成练习</text>
      </view>
    </view>

    <!-- 输入区域 -->
    <view class="chat-input">
      <view class="input-container">
        <button class="voice-btn" @click="toggleVoiceInput">
          <image src="/static/voice-icon.png" class="voice-icon" />
        </button>
        
        <view class="input-wrapper">
          <input 
            class="message-input"
            placeholder="输入消息..."
            v-model="inputMessage"
            @confirm="sendMessage"
            :disabled="isLoading"
          />
        </view>
        
        <button 
          class="send-btn"
          :class="{ disabled: !inputMessage.trim() || isLoading }"
          @click="sendMessage"
          :disabled="!inputMessage.trim() || isLoading"
        >
          <image src="/static/send-icon.png" class="send-icon" />
        </button>
      </view>
    </view>

    <!-- 菜单弹窗 -->
    <view class="menu-modal" v-if="showMenuModal" @click="hideMenu">
      <view class="menu-content" @click.stop>
        <view class="menu-item" @click="clearChat">
          <image src="/static/clear-icon.png" class="menu-icon" />
          <text class="menu-text">清空聊天</text>
        </view>
        <view class="menu-item" @click="exportChat">
          <image src="/static/export-icon.png" class="menu-icon" />
          <text class="menu-text">导出聊天</text>
        </view>
        <view class="menu-item" @click="goToSettings">
          <image src="/static/settings-icon.png" class="menu-icon" />
          <text class="menu-text">AI设置</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onShow, nextTick } from 'vue'
import { aiApi, type ChatMessage, type ChatSession } from '@/api/ai'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

// 响应式数据
const messages = ref<ChatMessage[]>([])
const currentSession = ref<ChatSession | null>(null)
const inputMessage = ref('')
const isLoading = ref(false)
const scrollTop = ref(0)
const showQuickActions = ref(true)
const showMenuModal = ref(false)
const isVoiceInput = ref(false)

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return
  
  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  
  // 添加用户消息
  const userMsg: ChatMessage = {
    id: Date.now().toString(),
    type: 'user',
    content: userMessage,
    timestamp: new Date().toISOString()
  }
  messages.value.push(userMsg)
  
  // 滚动到底部
  await scrollToBottom()
  
  // 发送到AI
  isLoading.value = true
  
  try {
    const response = await aiApi.sendMessage({
      message: userMessage,
      sessionId: currentSession.value?.id,
      articleId: currentSession.value?.articleId
    })
    
    // 添加AI回复
    messages.value.push(response.data.message)
    
    // 更新会话信息
    if (response.data.sessionId && !currentSession.value) {
      currentSession.value = {
        id: response.data.sessionId,
        title: '新对话',
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        messageCount: messages.value.length
      }
    }
    
    // 滚动到底部
    await scrollToBottom()
    
  } catch (error) {
    console.error('发送消息失败:', error)
    uni.showToast({
      title: '发送失败，请重试',
      icon: 'error'
    })
  } finally {
    isLoading.value = false
  }
}

// 快捷提问
const askQuestion = (question: string) => {
  inputMessage.value = question
  sendMessage()
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  scrollTop.value = 999999
}

// 复制消息
const copyMessage = (content: string) => {
  uni.setClipboardData({
    data: content,
    success: () => {
      uni.showToast({
        title: '已复制',
        icon: 'success'
      })
    }
  })
}

// 点赞消息
const likeMessage = (messageId: string) => {
  uni.showToast({
    title: '已点赞',
    icon: 'success'
  })
}

// 重新生成消息
const regenerateMessage = async (messageId: string) => {
  // 找到对应的用户消息
  const messageIndex = messages.value.findIndex(msg => msg.id === messageId)
  if (messageIndex > 0) {
    const userMessage = messages.value[messageIndex - 1]
    if (userMessage.type === 'user') {
      // 删除AI回复
      messages.value.splice(messageIndex, 1)
      
      // 重新发送
      isLoading.value = true
      try {
        const response = await aiApi.sendMessage({
          message: userMessage.content,
          sessionId: currentSession.value?.id
        })
        messages.value.push(response.data.message)
        await scrollToBottom()
      } catch (error) {
        console.error('重新生成失败:', error)
        uni.showToast({
          title: '重新生成失败',
          icon: 'error'
        })
      } finally {
        isLoading.value = false
      }
    }
  }
}

// 切换语音输入
const toggleVoiceInput = () => {
  if (isVoiceInput.value) {
    // 停止录音
    uni.stopRecord()
    isVoiceInput.value = false
  } else {
    // 开始录音
    uni.startRecord({
      success: (res) => {
        // 调用语音转文字API
        convertSpeechToText(res.tempFilePath)
      },
      fail: (error) => {
        console.error('录音失败:', error)
        uni.showToast({
          title: '录音失败',
          icon: 'error'
        })
      }
    })
    isVoiceInput.value = true
  }
}

// 语音转文字
const convertSpeechToText = async (audioPath: string) => {
  try {
    // 这里需要将音频文件转换为base64或上传到服务器
    const response = await aiApi.speechToText(audioPath)
    inputMessage.value = response.data.text
  } catch (error) {
    console.error('语音转文字失败:', error)
    uni.showToast({
      title: '语音识别失败',
      icon: 'error'
    })
  }
}

// 显示菜单
const showMenu = () => {
  showMenuModal.value = true
}

// 隐藏菜单
const hideMenu = () => {
  showMenuModal.value = false
}

// 清空聊天
const clearChat = () => {
  uni.showModal({
    title: '确认清空',
    content: '确定要清空所有聊天记录吗？',
    success: (res) => {
      if (res.confirm) {
        messages.value = []
        hideMenu()
      }
    }
  })
}

// 导出聊天
const exportChat = () => {
  const chatText = messages.value.map(msg => 
    `${msg.type === 'user' ? '我' : 'AI'}: ${msg.content}`
  ).join('\n\n')
  
  uni.setClipboardData({
    data: chatText,
    success: () => {
      uni.showToast({
        title: '聊天记录已复制',
        icon: 'success'
      })
    }
  })
  
  hideMenu()
}

// 跳转到设置
const goToSettings = () => {
  uni.navigateTo({
    url: '/pages/settings/ai'
  })
  hideMenu()
}

// 返回
const goBack = () => {
  uni.navigateBack()
}

// 格式化时间
const formatTime = (timestamp: string) => {
  const time = new Date(timestamp)
  const now = new Date()
  const diff = now.getTime() - time.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (minutes < 1) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else {
    return time.toLocaleDateString()
  }
}

// 页面加载
onMounted(async () => {
  if (!userStore.checkLoginStatus()) return
  
  // 创建新会话
  try {
    const response = await aiApi.createChatSession('新对话')
    currentSession.value = response.data
    
    // 添加欢迎消息
    messages.value.push({
      id: 'welcome',
      type: 'assistant',
      content: '你好！我是Rayda老师，你的AI英语学习助手。我可以帮你分析文章、解答问题、生成练习题等。有什么可以帮助你的吗？',
      timestamp: new Date().toISOString()
    })
  } catch (error) {
    console.error('创建会话失败:', error)
  }
})

// 页面显示时刷新
onShow(() => {
  if (userStore.isLoggedIn) {
    // 可以在这里添加刷新逻辑
  }
})
</script>

<style lang="scss" scoped>
.chat-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--background-color);
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20rpx 30rpx;
  background: white;
  border-bottom: 1rpx solid var(--border-color);
  
  .header-left {
    display: flex;
    align-items: center;
    
    .back-btn {
      width: 60rpx;
      height: 60rpx;
      background: #F5F5F5;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
      border: none;
      
      .back-icon {
        width: 24rpx;
        height: 24rpx;
      }
    }
    
    .header-info {
      .chat-title {
        font-size: 28rpx;
        font-weight: bold;
        color: var(--text-primary);
        margin-bottom: 4rpx;
        display: block;
      }
      
      .chat-subtitle {
        font-size: 22rpx;
        color: var(--text-secondary);
      }
    }
  }
  
  .header-right {
    .menu-btn {
      width: 60rpx;
      height: 60rpx;
      background: #F5F5F5;
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      border: none;
      
      .menu-icon {
        width: 24rpx;
        height: 24rpx;
      }
    }
  }
}

.chat-messages {
  flex: 1;
  padding: 20rpx;
  
  .messages-list {
    .message-item {
      display: flex;
      margin-bottom: 30rpx;
      
      &.user-message {
        flex-direction: row-reverse;
        
        .message-content {
          margin-right: 20rpx;
          margin-left: 0;
        }
        
        .message-bubble {
          background: var(--primary-color);
          color: white;
        }
      }
      
      &.assistant-message {
        .message-content {
          margin-left: 20rpx;
          margin-right: 0;
        }
        
        .message-bubble {
          background: white;
          color: var(--text-primary);
        }
      }
      
      .message-avatar {
        .avatar-image {
          width: 60rpx;
          height: 60rpx;
          border-radius: 30rpx;
        }
      }
      
      .message-content {
        max-width: 70%;
        
        .message-bubble {
          padding: 20rpx;
          border-radius: 16rpx;
          box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
          
          .message-text {
            font-size: 26rpx;
            line-height: 1.4;
            margin-bottom: 8rpx;
            display: block;
          }
          
          .message-time {
            font-size: 20rpx;
            opacity: 0.7;
          }
        }
        
        .message-actions {
          display: flex;
          gap: 12rpx;
          margin-top: 12rpx;
          
          .action-btn {
            width: 40rpx;
            height: 40rpx;
            background: #F5F5F5;
            border-radius: 20rpx;
            display: flex;
            align-items: center;
            justify-content: center;
            border: none;
            
            .action-icon {
              width: 20rpx;
              height: 20rpx;
            }
          }
        }
      }
    }
  }
  
  .loading-message {
    display: flex;
    margin-bottom: 30rpx;
    
    .message-avatar {
      .avatar-image {
        width: 60rpx;
        height: 60rpx;
        border-radius: 30rpx;
      }
    }
    
    .message-content {
      margin-left: 20rpx;
      
      .message-bubble {
        padding: 20rpx;
        background: white;
        border-radius: 16rpx;
        box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
        
        .typing-indicator {
          display: flex;
          gap: 8rpx;
          
          .typing-dot {
            width: 8rpx;
            height: 8rpx;
            background: var(--primary-color);
            border-radius: 50%;
            animation: typing 1.4s infinite;
            
            &:nth-child(2) {
              animation-delay: 0.2s;
            }
            
            &:nth-child(3) {
              animation-delay: 0.4s;
            }
          }
        }
      }
    }
  }
}

.quick-actions {
  display: flex;
  padding: 20rpx;
  background: white;
  border-top: 1rpx solid var(--border-color);
  
  .action-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 20rpx 10rpx;
    
    .action-icon {
      width: 40rpx;
      height: 40rpx;
      margin-bottom: 8rpx;
    }
    
    .action-text {
      font-size: 20rpx;
      color: var(--text-secondary);
    }
  }
}

.chat-input {
  padding: 20rpx 30rpx;
  background: white;
  border-top: 1rpx solid var(--border-color);
  
  .input-container {
    display: flex;
    align-items: center;
    background: #F5F5F5;
    border-radius: 24rpx;
    padding: 0 20rpx;
    height: 80rpx;
    
    .voice-btn {
      width: 60rpx;
      height: 60rpx;
      background: var(--primary-color);
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 20rpx;
      border: none;
      
      .voice-icon {
        width: 24rpx;
        height: 24rpx;
      }
    }
    
    .input-wrapper {
      flex: 1;
      
      .message-input {
        width: 100%;
        font-size: 26rpx;
        color: var(--text-primary);
        background: none;
        border: none;
        
        &::placeholder {
          color: var(--text-secondary);
        }
      }
    }
    
    .send-btn {
      width: 60rpx;
      height: 60rpx;
      background: var(--primary-color);
      border-radius: 30rpx;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-left: 20rpx;
      border: none;
      
      &.disabled {
        background: #E0E0E0;
      }
      
      .send-icon {
        width: 24rpx;
        height: 24rpx;
      }
    }
  }
}

.menu-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  justify-content: center;
  z-index: 1000;
  
  .menu-content {
    background: white;
    border-radius: 20rpx 20rpx 0 0;
    padding: 40rpx;
    width: 100%;
    max-width: 600rpx;
    
    .menu-item {
      display: flex;
      align-items: center;
      padding: 30rpx 0;
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
        font-size: 28rpx;
        color: var(--text-primary);
      }
    }
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
  }
  30% {
    transform: translateY(-10rpx);
  }
}
</style>
