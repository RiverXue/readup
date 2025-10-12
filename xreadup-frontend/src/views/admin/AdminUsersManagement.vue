<template>
  <div class="admin-users-management">
    <!-- 权限检查提示 -->
    <el-alert
      v-if="!isSuperAdmin"
      title="权限不足"
      type="warning"
      :closable="false"
      show-icon
      class="permission-alert"
    >
      <template #default>
        <p>您没有访问管理员管理界面的权限。</p>
        <p>只有超级管理员才能管理系统管理员账户。</p>
      </template>
    </el-alert>
    
    <el-card shadow="hover" v-if="isSuperAdmin">
      <template #header>
        <div class="card-header">
          <div style="display: flex; align-items: center; margin-right: 20px">
            <el-button 
              icon="ArrowLeft" 
              type="default" 
              style="margin-right: 10px" 
              @click="handleBack"
            >
              返回
            </el-button>
            <h2>管理员用户管理</h2>
          </div>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名、邮箱或手机号"
              prefix-icon="Search"
              style="width: 300px; margin-right: 10px"
              @input="handleSearch"
            />
            <el-select
              v-model="roleFilter"
              placeholder="角色"
              style="width: 120px; margin-right: 10px"
              @change="handleFilterChange"
            >
              <el-option label="全部" value="all" />
              <el-option label="超级管理员" value="SUPER_ADMIN" />
              <el-option label="管理员" value="ADMIN" />
            </el-select>
            <el-button type="primary" @click="handleAddAdmin">添加管理员</el-button>
            <el-button type="success" @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 数据统计信息 -->
      <div class="stats-container" v-if="adminUsersList.length > 0">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-statistic title="总管理员数" :value="pagination.total" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="超级管理员" :value="adminUsersList.filter(admin => admin.role === 'SUPER_ADMIN').length" />
          </el-col>
          <el-col :span="8">
            <el-statistic title="普通管理员" :value="adminUsersList.filter(admin => admin.role === 'ADMIN').length" />
          </el-col>
        </el-row>
      </div>
      
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="adminUsersList"
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="80" />
          <el-table-column prop="userId" label="用户ID" width="120" />
          <el-table-column prop="username" label="用户名" min-width="150">
            <template #default="scope">
              <el-tag type="primary" v-if="scope.row.userId === currentAdminId">
                {{ scope.row.username }}(当前)
              </el-tag>
              <span v-else>{{ scope.row.username }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="150">
            <template #default="scope">
              <el-input
                v-model="scope.row.phone"
                @blur="handlePhoneChange(scope.row)"
                size="small"
                placeholder="请输入手机号"
              />
            </template>
          </el-table-column>
          <el-table-column prop="role" label="角色" width="150">
            <template #default="scope">
              <el-select
                v-model="scope.row.role"
                @change="handleRoleChange(scope.row)"
                size="small"
                style="width: 120px"
              >
                <el-option label="超级管理员" value="SUPER_ADMIN" />
                <el-option label="管理员" value="ADMIN" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" min-width="180" />
          <el-table-column label="操作" min-width="150" fixed="right">
            <template #default="scope">
              <el-button
                type="warning"
                text
                size="small"
                @click="handleResetPassword(scope.row)"
              >
                重置密码
              </el-button>
              <el-button
                type="danger"
                text
                size="small"
                @click="handleDeleteAdmin(scope.row)"
                :disabled="scope.row.userId === currentAdminId"
              >
                取消管理员身份
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 添加/编辑管理员对话框 -->
    <el-dialog
      v-model="formDialogVisible"
      :title="editingAdmin ? '编辑管理员' : '添加管理员'"
      width="600px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="adminFormRef"
        :model="adminForm"
        :rules="formRules"
        label-width="100px"
        class="admin-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="adminForm.username" placeholder="请输入用户名" :disabled="editingAdmin" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="adminForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="adminForm.role" placeholder="请选择角色">
            <el-option label="超级管理员" value="SUPER_ADMIN" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
        <!-- 状态字段暂时移除，因为数据库中没有status字段 -->
        <el-form-item label="密码" prop="password" v-if="!editingAdmin">
          <el-input
            v-model="adminForm.password"
            placeholder="请输入密码"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword" v-if="!editingAdmin">
          <el-input
            v-model="adminForm.confirmPassword"
            placeholder="请确认密码"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="权限列表" prop="permissions" v-if="adminForm.role !== 'SUPER_ADMIN'">
          <el-checkbox-group v-model="adminForm.permissions">
            <el-checkbox v-for="permission in availablePermissions" :key="permission.key" :label="permission.key">
              {{ permission.name }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmitForm">确定</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      :title="`重置管理员 ${selectedAdmin?.username} 密码`"
      width="500px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordRules"
        label-width="100px"
        class="admin-form"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            placeholder="请输入新密码"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmNewPassword">
          <el-input
            v-model="resetPasswordForm.confirmNewPassword"
            placeholder="请确认新密码"
            type="password"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="sendEmailNotification">发送邮件通知</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleResetPasswordSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, computed } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { adminUtils } from '@/utils/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'
import { adminApi } from '@/api/admin/adminApi'
import type { AdminPermission } from '@/types/admin'

const adminStore = useAdminStore()

// 权限检查方法 - 管理员管理需要超级管理员权限
const hasPermission = (requiredPermissions: AdminPermission[]): boolean => {
  // 检查管理员是否登录且会话未过期
  if (!adminStore.isAdminUser || adminStore.isSessionExpired) {
    return false
  }
  
  // 管理员管理界面需要超级管理员权限
  return adminStore.isSuperAdminUser
}

// 计算属性：是否为超级管理员
const isSuperAdmin = computed(() => {
  return adminStore.isSuperAdminUser
})

// 数据和状态
const loading = ref(false)
const searchKeyword = ref('')
const adminUsersList = ref<any[]>([])
const formDialogVisible = ref(false)
const resetPasswordDialogVisible = ref(false)
const selectedAdmin = ref<any>(null)
const editingAdmin = ref<any>(null)
const roleFilter = ref('all')
const currentAdminId = ref('')
const sendEmailNotification = ref(true)

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 表单数据 - 简化字段
const adminForm = ref({
  userId: '',
  username: '',
  phone: '',
  role: 'ADMIN',
  password: '',
  confirmPassword: '',
  permissions: [] as string[]
})

// 重置密码表单数据
const resetPasswordForm = ref({
  newPassword: '',
  confirmNewPassword: ''
})

// 表单引用
const adminFormRef = ref<FormInstance>()
const resetPasswordFormRef = ref<FormInstance>()

// 可用权限列表 - 与AdminPermission类型保持一致
const availablePermissions = ref([
  { key: 'USER_MANAGE', name: '用户管理' },
  { key: 'ARTICLE_MANAGE', name: '文章管理' },
  { key: 'SUBSCRIPTION_MANAGE', name: '订阅管理' },
  { key: 'AI_RESULT_MANAGE', name: 'AI结果管理' },
  { key: 'ADMIN_MANAGE', name: '管理员管理' }
])

// 表单验证规则
const formRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为3-20个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== adminForm.value.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 重置密码表单验证规则
const resetPasswordRules = reactive({
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20个字符', trigger: 'blur' },
    { pattern: /^(?=.*[a-zA-Z])(?=.*\d).+$/, message: '密码必须包含字母和数字', trigger: 'blur' }
  ],
  confirmNewPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== resetPasswordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 这个hasPermission方法已经在上面定义了，删除重复定义

// 获取角色标签类型
const getRoleTagType = (role: string) => {
  switch (role) {
    case 'SUPER_ADMIN': return 'danger'
    case 'ADMIN': return 'primary'
    default: return 'default'
  }
}

// 获取角色文本
const getRoleText = (role: string) => {
  switch (role) {
    case 'SUPER_ADMIN': return '超级管理员'
    case 'ADMIN': return '管理员'
    default: return role
  }
}

// 获取权限文本
const getPermissionText = (permission: string) => {
  const perm = availablePermissions.value.find(p => p.key === permission)
  return perm ? perm.name : permission
}

// 初始化当前管理员信息
const initCurrentAdminInfo = () => {
  const currentAdmin = adminStore.getAdminUserInfo()
  if (currentAdmin) {
    currentAdminId.value = currentAdmin.id
  }
}

// 获取管理员列表
const fetchAdminUsersList = async () => {
  try {
    loading.value = true
    console.log('获取管理员列表参数:', {
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value.trim() || undefined,
      role: roleFilter.value !== 'all' ? roleFilter.value : undefined
    })
    
    const response = await adminApi.getAdminUsersList({
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      keyword: searchKeyword.value.trim() || undefined,
      role: roleFilter.value !== 'all' ? roleFilter.value : undefined
    })
    
    console.log('管理员列表API响应:', response)
    
    // 处理API返回的数据
    if (response && response.data) {
      // 确保adminUsersList始终是数组类型
      if (Array.isArray(response.data)) {
        adminUsersList.value = response.data
        pagination.value.total = adminUsersList.value.length
      } else if (response.data.list && Array.isArray(response.data.list)) {
        adminUsersList.value = response.data.list
        pagination.value.total = response.data.total || adminUsersList.value.length
      } else if (response.data.records && Array.isArray(response.data.records)) {
        adminUsersList.value = response.data.records
        pagination.value.total = response.data.total || adminUsersList.value.length
      } else {
        adminUsersList.value = []
        pagination.value.total = 0
        console.warn('API返回的数据结构不符合预期:', response.data)
      }
    } else {
      adminUsersList.value = []
      pagination.value.total = 0
    }
  } catch (error) {
    console.error('获取管理员列表失败:', error)
    ElMessage.error('获取管理员列表失败')
    adminUsersList.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 返回上一级
const handleBack = () => {
  // 使用路由返回上一页
  if (window.history.length > 1) {
    window.history.back()
  } else {
    // 如果没有历史记录，跳转到管理员首页
    window.location.href = '/admin'
  }
}

// 刷新数据
const handleRefresh = () => {
  fetchAdminUsersList()
}

// 处理搜索（添加防抖）
let searchTimeout: number | null = null
const handleSearch = () => {
  if (searchTimeout) {
    clearTimeout(searchTimeout)
  }
  searchTimeout = setTimeout(() => {
    pagination.value.currentPage = 1
    fetchAdminUsersList()
  }, 300) // 300ms防抖
}

// 处理筛选条件变更
const handleFilterChange = () => {
  pagination.value.currentPage = 1
  fetchAdminUsersList()
}

// 处理分页大小变更
const handleSizeChange = (newSize: number) => {
  pagination.value.pageSize = newSize
  pagination.value.currentPage = 1
  fetchAdminUsersList()
}

// 处理页码变更
const handleCurrentChange = (newPage: number) => {
  pagination.value.currentPage = newPage
  fetchAdminUsersList()
}

// 处理角色变更
const handleRoleChange = async (admin: any) => {
  try {
    const confirmResult = await ElMessageBox.confirm(
      `确定要将 ${admin.username} 的角色改为 ${getRoleText(admin.role)} 吗？`,
      '角色变更确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult === 'confirm') {
      loading.value = true
      await adminApi.updateAdmin(admin.userId, {
        userId: admin.userId,
        username: admin.username,
        phone: admin.phone,
        role: admin.role,
        permissions: admin.permissions || []
      })
      
      ElMessage.success('角色更新成功')
      
      // 更新统计信息
      fetchAdminUsersList()
    } else {
      // 用户取消，回滚角色
      admin.role = admin.role === 'SUPER_ADMIN' ? 'ADMIN' : 'SUPER_ADMIN'
    }
  } catch (error) {
    console.error('更新角色失败:', error)
    // 回滚角色
    admin.role = admin.role === 'SUPER_ADMIN' ? 'ADMIN' : 'SUPER_ADMIN'
    
    if ((error as any).response?.status === 404) {
      ElMessage.error('管理员不存在')
    } else if ((error as any).response?.status === 403) {
      ElMessage.error('没有权限修改此管理员角色')
    } else {
      ElMessage.error('更新角色失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 处理手机号变更（添加防抖）
let phoneChangeTimeout: number | null = null
const handlePhoneChange = async (admin: any) => {
  if (phoneChangeTimeout) {
    clearTimeout(phoneChangeTimeout)
  }
  
  phoneChangeTimeout = setTimeout(async () => {
    try {
      // 验证手机号格式
      if (admin.phone && !/^1[3-9]\d{9}$/.test(admin.phone)) {
        ElMessage.error('请输入有效的手机号')
        return
      }
      
      loading.value = true
      await adminApi.updateAdmin(admin.userId, {
        userId: admin.userId,
        username: admin.username,
        phone: admin.phone,
        role: admin.role,
        permissions: admin.permissions || []
      })
      
      ElMessage.success('手机号更新成功')
    } catch (error) {
      console.error('更新手机号失败:', error)
      
      if ((error as any).response?.status === 404) {
        ElMessage.error('管理员不存在')
      } else if ((error as any).response?.status === 403) {
        ElMessage.error('没有权限修改此管理员信息')
      } else {
        ElMessage.error('更新手机号失败，请重试')
      }
    } finally {
      loading.value = false
    }
  }, 1000) // 1秒防抖
}

// 添加管理员
const handleAddAdmin = () => {
  // 重置表单 - 简化字段
  adminForm.value = {
    userId: '',
    username: '',
    phone: '',
    role: 'ADMIN',
    password: '',
    confirmPassword: '',
    permissions: []
  }
  editingAdmin.value = null
  formDialogVisible.value = true
}

// 重置密码
const handleResetPassword = (admin: any) => {
  selectedAdmin.value = { ...admin }
  resetPasswordForm.value = {
    newPassword: '',
    confirmNewPassword: ''
  }
  resetPasswordDialogVisible.value = true
}

// 取消管理员身份
const handleDeleteAdmin = async (admin: any) => {
  try {
    const confirmResult = await ElMessageBox.confirm(
      `确定要取消 ${admin.username} 的管理员身份吗？\n\n注意：取消管理员身份后，该用户将变为普通用户，但仍可正常使用系统。`,
      '取消管理员身份确认',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult === 'confirm') {
      loading.value = true
      await adminApi.deleteAdmin(admin.userId)
      
      // 从列表中移除
      const index = adminUsersList.value.findIndex(item => item.userId === admin.userId)
      if (index !== -1) {
        adminUsersList.value.splice(index, 1)
      }
      
      // 如果当前是最后一页且删除后没有数据，返回上一页
      if (adminUsersList.value.length === 0 && pagination.value.currentPage > 1) {
        pagination.value.currentPage--
      }
      
      // 更新分页总数
      pagination.value.total--
      
      ElMessage.success('取消管理员身份成功')
    }
  } catch (error) {
    console.error('取消管理员身份失败:', error)
    // 根据错误类型显示不同的提示
    if ((error as any).response?.status === 404) {
      ElMessage.error('管理员不存在')
    } else if ((error as any).response?.status === 403) {
      ElMessage.error('没有权限取消此管理员身份')
    } else {
      ElMessage.error('取消管理员身份失败，请重试')
    }
  } finally {
    loading.value = false
  }
}


// 提交表单
const handleSubmitForm = async () => {
  try {
    await adminFormRef.value?.validate()
    loading.value = true
    
    const adminData = {
      userId: adminForm.value.userId,
      username: adminForm.value.username,
      phone: adminForm.value.phone || undefined,
      role: adminForm.value.role,
      permissions: adminForm.value.permissions
    }
    
    if (editingAdmin.value) {
      // 编辑管理员
      await adminApi.updateAdmin(adminForm.value.userId, adminData)
      
      // 更新列表中的数据
      const index = adminUsersList.value.findIndex(item => item.userId === adminForm.value.userId)
      if (index !== -1) {
        adminUsersList.value[index] = { ...adminUsersList.value[index], ...adminData }
      }
      
      ElMessage.success('编辑管理员成功')
    } else {
      // 添加新管理员
      const newAdminData = {
        ...adminData,
        password: adminForm.value.password
      }
      
      await adminApi.addAdmin(newAdminData)
      
      // 重置分页并重新获取列表
      pagination.value.currentPage = 1
      await fetchAdminUsersList()
      
      ElMessage.success('添加管理员成功')
    }
    
    // 关闭对话框
    handleDialogClose()
  } catch (error) {
    console.error('提交表单失败:', error)
    // 根据错误类型显示不同的提示
    if ((error as any).response?.status === 400) {
      ElMessage.error('数据验证失败，请检查输入信息')
    } else if ((error as any).response?.status === 409) {
      ElMessage.error('用户名已存在')
    } else {
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 提交重置密码
const handleResetPasswordSubmit = async () => {
  try {
    await resetPasswordFormRef.value?.validate()
    loading.value = true
    
    await adminApi.resetAdminPassword(selectedAdmin.value.userId, {
      newPassword: resetPasswordForm.value.newPassword,
      sendEmailNotification: sendEmailNotification.value
    })
    
    ElMessage.success('密码重置成功')
    handleDialogClose()
  } catch (error) {
    console.error('重置密码失败:', error)
    // 根据错误类型显示不同的提示
    if ((error as any).response?.status === 400) {
      ElMessage.error('密码格式不正确')
    } else if ((error as any).response?.status === 404) {
      ElMessage.error('管理员不存在')
    } else {
      ElMessage.error('重置密码失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 处理对话框关闭
const handleDialogClose = () => {
  selectedAdmin.value = null
  editingAdmin.value = null
  formDialogVisible.value = false
  resetPasswordDialogVisible.value = false
  adminFormRef.value?.resetFields()
  resetPasswordFormRef.value?.resetFields()
}

// 组件挂载时初始化数据
onMounted(() => {
  initCurrentAdminInfo()
  fetchAdminUsersList()
})
</script>

<style scoped>
.admin-users-management {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.stats-container {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}

.stats-container .el-statistic {
  text-align: center;
}

.stats-container .el-statistic__title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stats-container .el-statistic__number {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.table-container {
  margin-bottom: 20px;
}

/* 表格内编辑组件样式 */
.table-container .el-select {
  border: none;
}

.table-container .el-select .el-input__wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: border-color 0.2s;
}

.table-container .el-select .el-input__wrapper:hover {
  border-color: #c0c4cc;
}

.table-container .el-select .el-input__wrapper.is-focus {
  border-color: #409eff;
}

.table-container .el-input {
  border: none;
}

.table-container .el-input .el-input__wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: border-color 0.2s;
}

.table-container .el-input .el-input__wrapper:hover {
  border-color: #c0c4cc;
}

.table-container .el-input .el-input__wrapper.is-focus {
  border-color: #409eff;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
}

.admin-detail-content {
  padding: 10px 0;
}

.permission-alert {
  margin-bottom: 20px;
}

.permission-alert p {
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.5;
}

.permissions-list {
  display: flex;
  flex-wrap: wrap;
  max-height: 200px;
  overflow-y: auto;
}

.admin-form {
  margin-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style>