<template>
  <div class="user-management">
    <el-card shadow="hover">
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
            <h2>用户管理</h2>
          </div>
          <div class="header-actions">
            <el-select
              v-model="statusFilter"
              placeholder="用户状态"
              clearable
              style="width: 120px; margin-right: 10px"
              @change="handleFilterChange"
            >
              <el-option label="全部" value="" />
              <el-option label="启用" value="ACTIVE" />
              <el-option label="禁用" value="DISABLED" />
            </el-select>
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名/手机号"
              prefix-icon="Search"
              style="width: 240px; margin-right: 10px"
              @input="handleSearch"
            />
            <el-button type="primary" @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>
      
      <!-- 数据统计 -->
      <div v-if="usersData.length > 0" class="stats-container">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-statistic title="总用户数" :value="pagination.total" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="启用用户" :value="activeUsersCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="禁用用户" :value="disabledUsersCount" />
          </el-col>
          <el-col :span="6">
            <el-statistic title="今日新增" :value="todayNewUsersCount" />
          </el-col>
        </el-row>
      </div>
      
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="usersData"
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="80"></el-table-column>
          <el-table-column prop="id" label="用户ID" width="100"></el-table-column>
          <el-table-column prop="username" label="用户名" min-width="120"></el-table-column>
          <el-table-column prop="phone" label="手机号" min-width="130">
            <template #default="scope">
              <el-input
                v-model="scope.row.phone"
                @blur="handlePhoneChange(scope.row)"
                size="small"
                placeholder="请输入手机号"
              />
            </template>
          </el-table-column>
          <el-table-column prop="interestTag" label="兴趣标签" min-width="120">
            <template #default="scope">
              <el-input
                v-model="scope.row.interestTag"
                @blur="handleInterestTagChange(scope.row)"
                size="small"
                placeholder="兴趣标签"
              />
            </template>
          </el-table-column>
          <el-table-column prop="identityTag" label="身份标签" min-width="120">
            <template #default="scope">
              <el-input
                v-model="scope.row.identityTag"
                @blur="handleIdentityTagChange(scope.row)"
                size="small"
                placeholder="身份标签"
              />
            </template>
          </el-table-column>
          <el-table-column prop="learningGoalWords" label="目标词汇量" width="120">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.learningGoalWords"
                @blur="handleLearningGoalChange(scope.row)"
                size="small"
                :min="0"
                :max="100000"
                controls-position="right"
                style="width: 100px"
              />
            </template>
          </el-table-column>
          <el-table-column prop="targetReadingTime" label="目标阅读时间(分钟)" width="150">
            <template #default="scope">
              <el-input-number
                v-model="scope.row.targetReadingTime"
                @blur="handleReadingTimeChange(scope.row)"
                size="small"
                :min="0"
                :max="1440"
                controls-position="right"
                style="width: 120px"
              />
            </template>
          </el-table-column>
          <el-table-column prop="status" label="用户状态" width="120">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="ACTIVE"
                inactive-value="DISABLED"
                active-color="#13ce66"
                inactive-color="#ff4949"
                @change="handleUserStatusChange(scope.row)"
                :disabled="!hasPermission(['USER_MANAGE'])"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160">
            <template #default="scope">
              <span>{{ formatDate(scope.row.createdAt) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" min-width="120" fixed="right">
            <template #default="scope">
              <el-button
                type="danger"
                text
                size="small"
                @click="handleDeleteUser(scope.row)"
                :disabled="!hasPermission(['USER_MANAGE'])"
              >
                删除
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { adminApi } from '@/api/admin/adminApi'
import type { AdminPermission } from '@/types/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUserStatusThrottle } from '@/utils/throttle'

const adminStore = useAdminStore()
const router = useRouter()

// 数据和状态
const loading = ref(false)
const searchKeyword = ref('')
const statusFilter = ref('')
const usersData = ref<any[]>([])
const isInitializing = ref(true) // 添加初始化标志

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 计算属性
const activeUsersCount = computed(() => {
  return usersData.value.filter(user => user.status === 'ACTIVE').length
})

const disabledUsersCount = computed(() => {
  return usersData.value.filter(user => user.status === 'DISABLED').length
})

const todayNewUsersCount = computed(() => {
  const today = new Date().toISOString().split('T')[0]
  return usersData.value.filter(user => {
    if (!user.createdAt) return false
    const userDate = user.createdAt.split('T')[0]
    return userDate === today
  }).length
})


// 返回上一级
const handleBack = () => {
  router.back()
}

// 检查权限 - 适配adminStore的hasPermission方法
const hasPermission = (requiredPermissions: AdminPermission[]) => {
  // 检查管理员是否登录且会话未过期
  if (!adminStore.isAdminUser || adminStore.isSessionExpired) {
    return false
  }
  
  // 超级管理员拥有所有权限
  if (adminStore.hasAllPermissions) {
    return true
  }
  
  // 检查是否拥有所有必需的权限
  // 注意：adminStore.hasPermission只接受单个权限参数，所以我们需要遍历检查每个权限
  return requiredPermissions.every(permission => {
    // 由于adminStore.hasPermission只接受单个权限，这里直接调用它检查每个权限
    return adminStore.hasPermission(permission)
  })
}

// 获取用户列表
const fetchUsers = async () => {
  try {
    loading.value = true
    
    // 构建查询参数
    const params: any = {
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    }
    
    // 如果有搜索关键词，添加到参数中
    if (searchKeyword.value.trim()) {
      params.username = searchKeyword.value.trim()
    }
    
    // 如果有状态筛选，添加到参数中
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    
    // 调用API获取真实用户数据
    const response = await adminApi.getUserList(params)
    
    // 处理API返回的数据
    if (response && response.data) {
      console.log('API返回的原始数据:', response.data)
      console.log('API返回的用户列表:', response.data.list || response.data.records || response.data.items || response.data)
      
      // 确保usersData始终是数组类型
      if (Array.isArray(response.data)) {
        usersData.value = response.data
        pagination.value.total = usersData.value.length
      } else if (response.data.items && Array.isArray(response.data.items)) {
        usersData.value = response.data.items
        pagination.value.total = response.data.total || usersData.value.length
      } else if (response.data.records && Array.isArray(response.data.records)) {
        // 处理包含records属性的分页数据结构
        usersData.value = response.data.records
        pagination.value.total = response.data.total || usersData.value.length
      } else if (response.data.list && Array.isArray(response.data.list)) {
        // 处理包含list属性的分页数据结构（常见于某些API响应）
        usersData.value = response.data.list
        pagination.value.total = response.data.total || usersData.value.length
      } else {
        // 当数据结构不符合预期时，使用空数组
        usersData.value = []
        pagination.value.total = 0
        console.warn('API返回的数据结构不符合预期:', response.data)
      }
      
      console.log('处理后的用户数据:', usersData.value)
      
      // 检查每个用户的状态字段
      if (usersData.value.length > 0) {
        console.log('用户状态检查:')
        usersData.value.forEach((user, index) => {
          console.log(`用户${index + 1}: ID=${user.id}, 用户名=${user.username}, 状态=${user.status}, 状态类型=${typeof user.status}`)
        })
      }
      
      // 设置初始化完成标志
      isInitializing.value = false
    } else {
      usersData.value = []
      pagination.value.total = 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
    // 出错时显示空列表
    usersData.value = []
    pagination.value.total = 0
    ElMessage.error('获取用户列表失败')
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchUsers()
}

// 处理刷新
const handleRefresh = () => {
  searchKeyword.value = ''
  statusFilter.value = ''
  pagination.value.currentPage = 1
  fetchUsers()
}

// 处理筛选变更
const handleFilterChange = () => {
  pagination.value.currentPage = 1
  fetchUsers()
}

// 格式化日期
const formatDate = (dateString: string) => {
  if (!dateString) return '-'
  try {
    const date = new Date(dateString)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return dateString
  }
}

// 内联编辑方法
let phoneChangeTimeout: number | null = null
const handlePhoneChange = async (user: any) => {
  if (phoneChangeTimeout) {
    clearTimeout(phoneChangeTimeout)
  }
  phoneChangeTimeout = setTimeout(async () => {
    try {
      console.log('更新手机号:', user.id, user.phone)
      if (user.phone && !/^1[3-9]\d{9}$/.test(user.phone)) {
        ElMessage.error('请输入有效的手机号')
        return
      }
      loading.value = true
      const response = await adminApi.updateUser(user.id, { phone: user.phone })
      console.log('手机号更新响应:', response)
      ElMessage.success('手机号更新成功')
    } catch (error) {
      console.error('更新手机号失败:', error)
      ElMessage.error('更新手机号失败，请重试')
    } finally {
      loading.value = false
    }
  }, 1000) // 1秒防抖
}

let interestTagChangeTimeout: number | null = null
const handleInterestTagChange = async (user: any) => {
  if (interestTagChangeTimeout) {
    clearTimeout(interestTagChangeTimeout)
  }
  interestTagChangeTimeout = setTimeout(async () => {
    try {
      loading.value = true
      await adminApi.updateUser(user.id, { interestTag: user.interestTag })
      ElMessage.success('兴趣标签更新成功')
    } catch (error) {
      console.error('更新兴趣标签失败:', error)
      ElMessage.error('更新兴趣标签失败，请重试')
    } finally {
      loading.value = false
    }
  }, 1000)
}

let identityTagChangeTimeout: number | null = null
const handleIdentityTagChange = async (user: any) => {
  if (identityTagChangeTimeout) {
    clearTimeout(identityTagChangeTimeout)
  }
  identityTagChangeTimeout = setTimeout(async () => {
    try {
      loading.value = true
      await adminApi.updateUser(user.id, { identityTag: user.identityTag })
      ElMessage.success('身份标签更新成功')
    } catch (error) {
      console.error('更新身份标签失败:', error)
      ElMessage.error('更新身份标签失败，请重试')
    } finally {
      loading.value = false
    }
  }, 1000)
}

let learningGoalChangeTimeout: number | null = null
const handleLearningGoalChange = async (user: any) => {
  if (learningGoalChangeTimeout) {
    clearTimeout(learningGoalChangeTimeout)
  }
  learningGoalChangeTimeout = setTimeout(async () => {
    try {
      loading.value = true
      await adminApi.updateUser(user.id, { learningGoalWords: user.learningGoalWords })
      ElMessage.success('学习目标词汇量更新成功')
    } catch (error) {
      console.error('更新学习目标词汇量失败:', error)
      ElMessage.error('更新学习目标词汇量失败，请重试')
    } finally {
      loading.value = false
    }
  }, 1000)
}

let readingTimeChangeTimeout: number | null = null
const handleReadingTimeChange = async (user: any) => {
  if (readingTimeChangeTimeout) {
    clearTimeout(readingTimeChangeTimeout)
  }
  readingTimeChangeTimeout = setTimeout(async () => {
    try {
      loading.value = true
      await adminApi.updateUser(user.id, { targetReadingTime: user.targetReadingTime })
      ElMessage.success('目标阅读时间更新成功')
    } catch (error) {
      console.error('更新目标阅读时间失败:', error)
      ElMessage.error('更新目标阅读时间失败，请重试')
    } finally {
      loading.value = false
    }
  }, 1000)
}

// 处理用户状态变更（使用节流工具）
const handleUserStatusChange = async (user: any) => {
  // 如果正在初始化，忽略状态变更事件
  if (isInitializing.value) {
    console.log('正在初始化，忽略状态变更事件')
    return
  }
  
  const userId = parseInt(user.id)
  
  // 创建节流函数
  const throttledRequest = createUserStatusThrottle(userId)
  
  try {
    loading.value = true
    
    await throttledRequest(async () => {
      if (user.status === 'ACTIVE') {
        // 启用用户
        await adminApi.enableUser(userId)
        ElMessage.success('启用用户成功')
      } else {
        // 禁用用户
        await adminApi.disableUser(userId)
        ElMessage.success('禁用用户成功')
      }
    })
  } catch (error) {
    console.error('更新用户状态失败:', error)
    // 回滚状态
    user.status = user.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
    
    // 根据错误类型显示不同的提示
    if ((error as any).response?.status === 429) {
      ElMessage.error('请求过于频繁，请稍后再试')
    } else {
      ElMessage.error('操作失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 删除用户
const handleDeleteUser = async (user: any) => {
  try {
    const confirmResult = await ElMessageBox.confirm(
      `确定要删除用户 ${user.username} 吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult === 'confirm') {
      loading.value = true
      
      // 禁用用户
      await adminApi.disableUser(parseInt(user.id))
      
      // 从列表中移除
      const index = usersData.value.findIndex(u => u.id === user.id)
      if (index !== -1) {
        usersData.value.splice(index, 1)
      }
      
      // 更新分页总数
      pagination.value.total--
      
      ElMessage.success('删除用户成功')
    }
  } catch (error) {
    console.error('删除用户失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    loading.value = false
  }
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  fetchUsers()
}

const handleCurrentChange = (current: number) => {
  pagination.value.currentPage = current
  fetchUsers()
}

// 组件挂载时初始化数据
onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-management {
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

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
}

/* 内联编辑样式 */
.table-container .el-input,
.table-container .el-input-number {
  border: none;
}

.table-container .el-input .el-input__wrapper,
.table-container .el-input-number .el-input__wrapper {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: border-color 0.2s;
}

.table-container .el-input .el-input__wrapper:hover,
.table-container .el-input-number .el-input__wrapper:hover {
  border-color: #c0c4cc;
}

.table-container .el-input .el-input__wrapper.is-focus,
.table-container .el-input-number .el-input__wrapper.is-focus {
  border-color: #409eff;
}

/* 数据统计样式 */
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

</style>