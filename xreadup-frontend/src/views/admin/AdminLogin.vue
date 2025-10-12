<template>
  <div class="admin-login-container">
    <el-card class="login-card" shadow="hover">
      <div class="login-header">
        <h1>管理员登录</h1>
        <p>欢迎访问管理后台</p>
      </div>

      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入管理员账号"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="loginForm.remember">记住密码</el-checkbox>
          <el-link type="primary" style="float: right">忘记密码?</el-link>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
            :disabled="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <p>© 2025 XReadUp 智能阅读平台 版权所有</p>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { adminApi } from '@/api/admin/adminApi'
import api from '@/utils/api'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const adminStore = useAdminStore()

// 表单引用
const loginFormRef = ref()

// 登录表单数据
const loginForm = ref({
  username: '',
  password: '',
  remember: false
})

// 加载状态
const loading = ref(false)

// 表单验证规则
const loginRules = ref({
  username: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' },
    { min: 1, max: 20, message: '账号长度在1到20个字符之间', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度在6到32个字符之间', trigger: 'blur' }
  ]
})

// 处理登录
const handleLogin = async () => {
  try {
    // 验证表单
    const valid = await loginFormRef.value.validate()
    if (valid) {
      loading.value = true

      // 调用独立的管理员登录API
      console.log('开始调用独立管理员登录API:', loginForm.value.username)
      const response = await adminApi.login({
        username: loginForm.value.username,
        password: loginForm.value.password
      })

      console.log('管理员登录API响应:', response)

      // 兼容不同的响应格式
      const isSuccess = response.success !== undefined ? response.success :
                       (response.code === 200 || response.code === 0);

      if (isSuccess && response.data && response.data.token) {
        // 解构获取响应数据 - 根据实际API响应结构调整
        const { token, userId, username, role, permissions = [], superAdmin } = response.data;

        // 构建用户信息对象
        const userInfo = {
          id: userId,
          username: username,
          role: role
        };

        // 设置管理员专属Token，明确标记为admin token
        const expiryTime = response.data.tokenExpireTime || (Date.now() + (7 * 24 * 60 * 60 * 1000)); // 使用API返回的过期时间或默认7天

        // 存储管理员相关信息，使用独立的存储键名避免混淆
        localStorage.setItem('admin_token', token);
        localStorage.setItem('admin_token_expiry', expiryTime.toString());
        localStorage.setItem('admin_user_info', JSON.stringify(userInfo));
        localStorage.setItem('admin_role', role || 'ADMIN');

        // 设置API默认头，使用管理员专属Token
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`;

        // 直接设置管理员状态，不需要通过userStore
        adminStore.setAdminState({
          isAdmin: true,
          isSuperAdmin: superAdmin || role === 'SUPER_ADMIN',
          role: role as any,
          token: token,
          userId: userId?.toString() || ''
        }, permissions);

        // 如果勾选记住密码，保存相关信息
        if (loginForm.value.remember) {
          localStorage.setItem('adminUsername', loginForm.value.username);
          localStorage.setItem('adminRemember', 'true');
        } else {
          localStorage.removeItem('adminUsername');
          localStorage.removeItem('adminRemember');
        }

        // 登录成功，跳转到管理后台首页
        ElMessage.success(`登录成功，欢迎${role === 'SUPER_ADMIN' ? '超级管理员' : '管理员'}`);
        setTimeout(() => {
          router.replace('/admin/dashboard');
        }, 500);
      } else {
        console.error('管理员登录API返回失败:', response);
        ElMessage.error(response?.data?.message || '登录失败，请检查账号密码是否正确');
      }
    }
  } catch (error: any) {
    console.error('管理员登录失败:', error);
    ElMessage.error('登录失败，请稍后重试');
  } finally {
    loading.value = false;
  }
}

// 加载记住的用户名
const loadRememberedUsername = () => {
  const remember = localStorage.getItem('adminRemember') === 'true';
  const username = localStorage.getItem('adminUsername');

  if (remember && username) {
    loginForm.value.username = username;
    loginForm.value.remember = remember;
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 加载记住的用户名
  loadRememberedUsername();

  // 清除可能存在的普通用户会话，确保管理员会话的独立性
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  localStorage.removeItem('tokenExpiry');
})
</script>

<style scoped>
.admin-login-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background-color: #f5f7fa;
  background-image:
    radial-gradient(circle at 25% 25%, rgba(166, 172, 175, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 75% 75%, rgba(166, 172, 175, 0.1) 0%, transparent 50%);
}

.login-card {
  width: 400px;
  padding: 40px;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 10px;
}

.login-header p {
  color: #8c939d;
  font-size: 14px;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
  height: 40px;
  font-size: 16px;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  margin-top: 30px;
  color: #8c939d;
  font-size: 12px;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .login-card {
    width: 90%;
    padding: 30px 20px;
  }

  .login-header h1 {
    font-size: 24px;
  }
}
</style>
