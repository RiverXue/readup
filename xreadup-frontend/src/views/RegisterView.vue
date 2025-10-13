<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <h1>创建账户</h1>
        <p>加入ReadUp AI，开启智能英语学习之旅</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>



        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="确认密码"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            style="width: 100%"
            :loading="loading"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <p>已有账户？
          <router-link to="/login">立即登录</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validatePass = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validatePass, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      const result = await userStore.register(
        registerForm.username,
        registerForm.password,
        undefined
      )

      if (result.success) {
        router.push('/')
      }
      loading.value = false
    }
  })
}
</script>

<style scoped>
@import '@/assets/design-system.css';

.register-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--primary-50) 0%, var(--warm-cream) 100%);
  padding: var(--space-6);
}

.register-box {
  background: var(--bg-primary);
  border-radius: var(--radius-3xl);
  box-shadow: var(--shadow-xl);
  padding: var(--space-16) var(--space-12);
  width: 100%;
  max-width: 400px;
  border: 1px solid var(--border-light);
  position: relative;
  overflow: hidden;
}

.register-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, var(--primary-500), var(--warm-orange));
}

.register-header {
  text-align: center;
  margin-bottom: var(--space-12);
}

.register-header h1 {
  font-size: var(--text-4xl);
  color: var(--text-primary);
  margin-bottom: var(--space-3);
  font-weight: var(--font-weight-semibold);
  background: linear-gradient(135deg, var(--primary-600) 0%, var(--warm-orange) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.register-header p {
  color: var(--text-secondary);
  font-size: var(--text-base);
}

.register-form {
  margin-bottom: var(--space-8);
}

.register-footer {
  text-align: center;
  color: var(--text-secondary);
}

.register-footer a {
  color: var(--primary-600);
  text-decoration: none;
  font-weight: var(--font-weight-medium);
  transition: color var(--transition-normal);
}

.register-footer a:hover {
  color: var(--primary-700);
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px var(--border-light);
  border-radius: var(--radius-xl);
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--border-medium);
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--primary-500);
}
</style>
