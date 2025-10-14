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
          <TactileButton
            variant="primary"
            size="lg"
            style="width: 100%"
            :loading="loading"
            @click="handleSubmit"
          >
            {{ isRegister ? '注册' : '登录' }}
          </TactileButton>
        </el-form-item>
      </el-form>

      <div class="switch-mode">
        <TactileButton variant="ghost" @click="toggleMode">
          {{ isRegister ? '已有账号？立即登录' : '没有账号？立即注册' }}
        </TactileButton>
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
import TactileButton from '@/components/common/TactileButton.vue'

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
@import '@/assets/design-system.css';

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, var(--primary-50) 0%, var(--warm-cream) 100%);
  padding: var(--space-6);
}

.login-form {
  background: var(--bg-primary);
  padding: var(--space-12);
  border-radius: var(--radius-3xl);
  box-shadow: var(--shadow-xl);
  width: 100%;
  max-width: 400px;
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
}

.login-form::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-500), var(--warm-orange));
}

h2 {
  text-align: center;
  margin-bottom: var(--space-2);
  font-size: var(--text-3xl);
  font-weight: var(--font-weight-bold);
  background: linear-gradient(135deg, var(--primary-600) 0%, var(--warm-orange) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  text-align: center;
  color: var(--text-secondary);
  margin-bottom: var(--space-8);
  font-size: var(--text-base);
}

.switch-mode {
  text-align: center;
  margin-top: var(--space-6);
  color: var(--text-secondary);
  font-size: var(--text-sm);
}

.switch-mode a {
  color: var(--primary-600);
  text-decoration: none;
  font-weight: var(--font-weight-medium);
  transition: color var(--transition-normal);
}

.switch-mode a:hover {
  color: var(--primary-700);
}

.demo-tips {
  margin-top: var(--space-8);
  text-align: center;
  color: var(--text-tertiary);
  font-size: var(--text-sm);
}

.demo-tips p {
  margin: var(--space-1) 0;
}

@media (max-width: 768px) {
  .login-container {
    padding: var(--space-4);
  }

  .login-form {
    padding: var(--space-8) var(--space-6);
  }
}
</style>
