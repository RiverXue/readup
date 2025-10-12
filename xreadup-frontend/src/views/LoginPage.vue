<template>
  <div class="login-container">
    <div class="login-form">
      <h2>{{ isRegister ? '注册账号' : '用户登录' }}</h2>
      <p class="subtitle">欢迎来到XReadUp英语学习平台</p>

      <el-form
        ref="loginForm"
        :model="form"
        :rules="rules"
        label-width="0"
        size="large"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item v-if="isRegister" prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="请输入昵称"
            prefix-icon="Avatar"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item v-if="isRegister" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="请确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isRegister ? '注册' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="switch-mode">
        <el-button type="text" @click="toggleMode">
          {{ isRegister ? '已有账号？立即登录' : '没有账号？立即注册' }}
        </el-button>
      </div>

      <!-- 演示账号提示 -->
      <div class="demo-tips">
        <el-divider>演示账号</el-divider>
        <p>用户名：郝润锋</p>
        <p>密码：123456</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const loginForm = ref()
const loading = ref(false)
const isRegister = ref(false)

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 20, message: '用户名长度1-20位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '昵称长度2-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== form.password) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const toggleMode = () => {
  isRegister.value = !isRegister.value
  loginForm.value?.clearValidate()
}

const handleSubmit = () => {
  loginForm.value?.validate((valid: boolean) => {
    if (valid) {
      if (isRegister.value) {
        handleRegister()
      } else {
        handleLogin()
      }
    }
  })
}

const handleLogin = async () => {
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  loading.value = true
  try {
    await userStore.register(form.username, form.password, undefined, form.nickname)
    ElMessage.success('注册成功')
    isRegister.value = false
    loginForm.value?.resetFields()
  } catch (error: unknown) {
    const err = error as { response?: { data?: { message?: string } } }
    ElMessage.error(err.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 10px;
  color: #303133;
}

.subtitle {
  text-align: center;
  color: #909399;
  margin-bottom: 30px;
}

.switch-mode {
  text-align: center;
  margin-top: 20px;
}

.demo-tips {
  margin-top: 30px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

.demo-tips p {
  margin: 5px 0;
}

@media (max-width: 768px) {
  .login-container {
    padding: 20px;
  }

  .login-form {
    padding: 30px 20px;
  }
}
</style>
