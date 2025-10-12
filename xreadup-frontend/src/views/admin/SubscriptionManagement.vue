<template>
  <div class="subscription-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>订阅管理</h1>
      <p>管理订阅计划和用户订阅记录</p>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 订阅计划管理 -->
      <el-tab-pane label="订阅计划" name="plans">
        <div class="tab-content">
          <!-- 操作按钮 -->
          <div class="action-bar">
            <el-button type="primary" @click="handleCreatePlan">
              <el-icon><Plus /></el-icon>
              创建计划
            </el-button>
            <el-button @click="refreshPlans" :loading="plansLoading">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
      
          <!-- 订阅计划列表 -->
        <el-table
            :data="subscriptionPlans"
            v-loading="plansLoading"
            stripe
          style="width: 100%"
        >
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="planType" label="计划类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getPlanTypeTagType(row.planType)">
                  {{ getPlanTypeName(row.planType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                {{ row.currency }} {{ row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="maxArticlesPerMonth" label="每月文章数" width="120" />
            <el-table-column prop="maxWordsPerArticle" label="每篇文章字数" width="140" />
            <el-table-column prop="aiFeaturesEnabled" label="AI功能" width="100">
              <template #default="{ row }">
                <el-tag :type="row.aiFeaturesEnabled ? 'success' : 'info'">
                  {{ row.aiFeaturesEnabled ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="prioritySupport" label="优先支持" width="100">
              <template #default="{ row }">
                <el-tag :type="row.prioritySupport ? 'success' : 'info'">
                  {{ row.prioritySupport ? '是' : '否' }}
                </el-tag>
            </template>
          </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
            </template>
          </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
                  @click="handleEditPlan(row)"
                >
                  编辑
              </el-button>
              <el-button
                type="danger"
                size="small"
                  @click="handleDeletePlan(row)"
              >
                  删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      </el-tab-pane>

      <!-- 用户订阅管理 -->
      <el-tab-pane label="用户订阅" name="subscriptions">
        <div class="tab-content">
          <!-- 筛选条件 -->
          <el-card class="filter-card" shadow="never">
            <el-form :model="subscriptionFilters" inline>
              <el-form-item label="用户ID">
                <el-input
                  v-model="subscriptionFilters.userId"
                  placeholder="请输入用户ID"
                  clearable
                  style="width: 150px"
                  @keyup.enter="handleSearchSubscriptions"
                />
              </el-form-item>
              <el-form-item label="计划类型">
                <el-select
                  v-model="subscriptionFilters.planType"
                  placeholder="请选择计划类型"
                  clearable
                  style="width: 150px"
                >
                  <el-option label="全部" value="" />
                  <el-option label="基础版" value="BASIC" />
                  <el-option label="专业版" value="PRO" />
                  <el-option label="企业版" value="ENTERPRISE" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select
                  v-model="subscriptionFilters.status"
                  placeholder="请选择状态"
                  clearable
                  style="width: 120px"
                >
                  <el-option label="全部" value="" />
                  <el-option label="活跃" value="ACTIVE" />
                  <el-option label="已取消" value="CANCELLED" />
                  <el-option label="已过期" value="EXPIRED" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleSearchSubscriptions" :loading="subscriptionsLoading">
                  <el-icon><Search /></el-icon>
                  搜索
                </el-button>
                <el-button @click="handleResetSubscriptions">
                  <el-icon><Refresh /></el-icon>
                  重置
                </el-button>
              </el-form-item>
            </el-form>
          </el-card>

          <!-- 用户订阅列表 -->
          <el-table
            :data="userSubscriptions"
            v-loading="subscriptionsLoading"
            stripe
            style="width: 100%"
          >
            <el-table-column prop="id" label="订阅ID" width="100" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="planType" label="计划类型" width="120">
              <template #default="{ row }">
                <el-tag :type="getPlanTypeTagType(row.planType)">
                  {{ getPlanTypeName(row.planType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="price" label="价格" width="100">
              <template #default="{ row }">
                {{ row.currency }} {{ row.price }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusTagType(row.status)">
                  {{ getStatusName(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startDate" label="开始日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.startDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="endDate" label="结束日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.endDate) }}
              </template>
            </el-table-column>
            <el-table-column prop="autoRenew" label="自动续费" width="100">
              <template #default="{ row }">
                <el-tag :type="row.autoRenew ? 'success' : 'info'">
                  {{ row.autoRenew ? '是' : '否' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="{ row }">
                <el-button
                  type="primary"
                  size="small"
                  @click="handleUpdateSubscriptionStatus(row)"
                >
                  更新状态
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
              v-model:current-page="subscriptionPagination.currentPage"
              v-model:page-size="subscriptionPagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
              :total="subscriptionPagination.total"
          layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSubscriptionSizeChange"
              @current-change="handleSubscriptionCurrentChange"
        />
      </div>
        </div>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 创建/编辑计划对话框 -->
    <el-dialog
      v-model="planDialogVisible"
      :title="isEditPlan ? '编辑计划' : '创建计划'"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="planFormRef"
        :model="planForm"
        :rules="planFormRules"
        label-width="120px"
      >
        <el-form-item label="计划类型" prop="planType">
          <el-select v-model="planForm.planType" placeholder="请选择计划类型">
            <el-option label="基础版" value="BASIC" />
            <el-option label="专业版" value="PRO" />
            <el-option label="企业版" value="ENTERPRISE" />
          </el-select>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number
            v-model="planForm.price"
            :min="0"
            :precision="2"
            placeholder="请输入价格"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="货币" prop="currency">
          <el-select v-model="planForm.currency" placeholder="请选择货币">
            <el-option label="人民币" value="CNY" />
            <el-option label="美元" value="USD" />
          </el-select>
        </el-form-item>
        <el-form-item label="每月文章数" prop="maxArticlesPerMonth">
          <el-input-number
            v-model="planForm.maxArticlesPerMonth"
            :min="0"
            placeholder="请输入每月文章数"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="每篇文章字数" prop="maxWordsPerArticle">
          <el-input-number
            v-model="planForm.maxWordsPerArticle"
            :min="0"
            placeholder="请输入每篇文章字数"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="AI功能" prop="aiFeaturesEnabled">
          <el-switch v-model="planForm.aiFeaturesEnabled" />
        </el-form-item>
        <el-form-item label="优先支持" prop="prioritySupport">
          <el-switch v-model="planForm.prioritySupport" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSavePlan" :loading="planSaving">
          {{ isEditPlan ? '更新' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 更新订阅状态对话框 -->
    <el-dialog
      v-model="statusDialogVisible"
      title="更新订阅状态"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="statusFormRef"
        :model="statusForm"
        :rules="statusFormRules"
        label-width="100px"
      >
        <el-form-item label="当前状态">
          <el-tag :type="getStatusTagType(selectedSubscription?.status)">
            {{ getStatusName(selectedSubscription?.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="新状态" prop="status">
          <el-select v-model="statusForm.status" placeholder="请选择新状态">
            <el-option label="活跃" value="ACTIVE" />
            <el-option label="已取消" value="CANCELLED" />
            <el-option label="已过期" value="EXPIRED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveStatus" :loading="statusSaving">
          更新
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { adminApi } from '@/api/admin/adminApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Search } from '@element-plus/icons-vue'
import type { AdminPermission } from '@/types/admin'

const adminStore = useAdminStore()

// 权限检查方法
const hasPermission = (requiredPermissions: AdminPermission[]): boolean => {
  if (!adminStore.isAdminUser || adminStore.isSessionExpired) {
    return false
  }
  
  if (adminStore.hasAllPermissions) {
    return true
  }
  
  return requiredPermissions.every(permission => {
    return adminStore.hasPermission(permission)
  })
}

// 响应式数据
const activeTab = ref('plans')

// 订阅计划相关
const subscriptionPlans = ref([])
const plansLoading = ref(false)
const planDialogVisible = ref(false)
const isEditPlan = ref(false)
const planSaving = ref(false)
const planForm = ref({
  planType: '',
  price: 0,
  currency: 'CNY',
  maxArticlesPerMonth: 0,
  maxWordsPerArticle: 0,
  aiFeaturesEnabled: false,
  prioritySupport: false
})

const planFormRules = {
  planType: [{ required: true, message: '请选择计划类型', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  currency: [{ required: true, message: '请选择货币', trigger: 'change' }],
  maxArticlesPerMonth: [{ required: true, message: '请输入每月文章数', trigger: 'blur' }],
  maxWordsPerArticle: [{ required: true, message: '请输入每篇文章字数', trigger: 'blur' }]
}

// 用户订阅相关
const userSubscriptions = ref([])
const subscriptionsLoading = ref(false)
const subscriptionFilters = ref({
  userId: '',
  planType: '',
  status: ''
})

const subscriptionPagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 状态更新相关
const statusDialogVisible = ref(false)
const statusSaving = ref(false)
const selectedSubscription = ref(null)
const statusForm = ref({
  status: ''
})

const statusFormRules = {
  status: [{ required: true, message: '请选择新状态', trigger: 'change' }]
}

// 获取订阅计划列表
const fetchSubscriptionPlans = async () => {
  try {
    plansLoading.value = true
    const response = await adminApi.getSubscriptionPlans()
    if (response && response.data) {
      subscriptionPlans.value = response.data
    } else {
      subscriptionPlans.value = []
    }
  } catch (error) {
    console.error('获取订阅计划列表失败:', error)
    ElMessage.error('获取订阅计划列表失败')
    subscriptionPlans.value = []
  } finally {
    plansLoading.value = false
  }
}

// 获取用户订阅列表
const fetchUserSubscriptions = async () => {
  try {
    subscriptionsLoading.value = true
    const response = await adminApi.getUserSubscriptionList({
      page: subscriptionPagination.value.currentPage,
      pageSize: subscriptionPagination.value.pageSize,
      userId: subscriptionFilters.value.userId || undefined,
      planType: subscriptionFilters.value.planType || undefined,
      status: subscriptionFilters.value.status || undefined
    })
    
    if (response && response.data) {
      // 处理后端返回的分页数据结构
      const responseData = response.data.data || response.data
      if (responseData && typeof responseData === 'object') {
        // 如果是分页对象，提取records数组
        if (responseData.records && Array.isArray(responseData.records)) {
          userSubscriptions.value = responseData.records
          subscriptionPagination.value.total = responseData.total || 0
        } else if (Array.isArray(responseData)) {
          // 如果直接是数组
          userSubscriptions.value = responseData
          subscriptionPagination.value.total = responseData.length
        } else {
          userSubscriptions.value = []
          subscriptionPagination.value.total = 0
        }
      } else {
        userSubscriptions.value = []
        subscriptionPagination.value.total = 0
      }
    } else {
      userSubscriptions.value = []
      subscriptionPagination.value.total = 0
    }
  } catch (error) {
    console.error('获取用户订阅列表失败:', error)
    ElMessage.error('获取用户订阅列表失败')
    userSubscriptions.value = []
    subscriptionPagination.value.total = 0
  } finally {
    subscriptionsLoading.value = false
  }
}

// 标签页切换
const handleTabChange = (tabName: string) => {
  if (tabName === 'plans') {
    fetchSubscriptionPlans()
  } else if (tabName === 'subscriptions') {
    fetchUserSubscriptions()
  }
}

// 刷新计划
const refreshPlans = () => {
  fetchSubscriptionPlans()
}

// 创建计划
const handleCreatePlan = () => {
  isEditPlan.value = false
  planForm.value = {
    planType: '',
    price: 0,
    currency: 'CNY',
    maxArticlesPerMonth: 0,
    maxWordsPerArticle: 0,
    aiFeaturesEnabled: false,
    prioritySupport: false
  }
  planDialogVisible.value = true
}

// 编辑计划
const handleEditPlan = (row: any) => {
  isEditPlan.value = true
  planForm.value = { ...row }
  planDialogVisible.value = true
}

// 保存计划
const handleSavePlan = async () => {
  try {
    planSaving.value = true
    
    if (isEditPlan.value) {
      await adminApi.updateSubscriptionPlan(planForm.value.id, planForm.value)
      ElMessage.success('更新计划成功')
    } else {
      await adminApi.createSubscriptionPlan(planForm.value)
      ElMessage.success('创建计划成功')
    }
    
    planDialogVisible.value = false
    fetchSubscriptionPlans()
  } catch (error) {
    console.error('保存计划失败:', error)
    ElMessage.error('保存计划失败')
  } finally {
    planSaving.value = false
  }
}

// 删除计划
const handleDeletePlan = async (row: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这个订阅计划吗？', '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
    })
    
    await adminApi.deleteSubscriptionPlan(row.id)
    ElMessage.success('删除计划成功')
    fetchSubscriptionPlans()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除计划失败:', error)
      ElMessage.error('删除计划失败')
    }
  }
}

// 搜索订阅
const handleSearchSubscriptions = () => {
  subscriptionPagination.value.currentPage = 1
  fetchUserSubscriptions()
}

// 重置订阅筛选
const handleResetSubscriptions = () => {
  subscriptionFilters.value = {
    userId: '',
    planType: '',
    status: ''
  }
  subscriptionPagination.value.currentPage = 1
  fetchUserSubscriptions()
}

// 订阅分页大小改变
const handleSubscriptionSizeChange = (size: number) => {
  subscriptionPagination.value.pageSize = size
  subscriptionPagination.value.currentPage = 1
  fetchUserSubscriptions()
}

// 订阅当前页改变
const handleSubscriptionCurrentChange = (page: number) => {
  subscriptionPagination.value.currentPage = page
  fetchUserSubscriptions()
}

// 更新订阅状态
const handleUpdateSubscriptionStatus = (row: any) => {
  selectedSubscription.value = row
  statusForm.value.status = row.status
  statusDialogVisible.value = true
}

// 保存状态
const handleSaveStatus = async () => {
  try {
    statusSaving.value = true
    
    await adminApi.updateUserSubscriptionStatus(selectedSubscription.value.id, statusForm.value.status)
    ElMessage.success('更新状态成功')
    
    statusDialogVisible.value = false
    fetchUserSubscriptions()
  } catch (error) {
    console.error('更新状态失败:', error)
    ElMessage.error('更新状态失败')
  } finally {
    statusSaving.value = false
  }
}

// 获取计划类型标签类型
const getPlanTypeTagType = (type: string) => {
  const typeMap = {
    'FREE': 'info',
    'BASIC': 'primary',
    'PRO': 'warning',
    'ENTERPRISE': 'danger'
  }
  return typeMap[type] || 'info'
}

// 获取计划类型名称
const getPlanTypeName = (type: string) => {
  const nameMap = {
    'FREE': '免费版',
    'BASIC': '基础版',
    'PRO': '专业版',
    'ENTERPRISE': '企业版'
  }
  return nameMap[type] || type
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  const statusMap = {
    'ACTIVE': 'success',
    'CANCELLED': 'warning',
    'EXPIRED': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态名称
const getStatusName = (status: string) => {
  const nameMap = {
    'ACTIVE': '活跃',
    'CANCELLED': '已取消',
    'EXPIRED': '已过期'
  }
  return nameMap[status] || status
}

// 格式化日期
const formatDate = (date: string) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

// 页面加载时获取数据
onMounted(() => {
  fetchSubscriptionPlans()
})
</script>

<style scoped>
.subscription-management {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

.page-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.tab-content {
  margin-top: 20px;
}

.action-bar {
  margin-bottom: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

@media (max-width: 768px) {
  .subscription-management {
    padding: 10px;
  }
  
  .filter-card .el-form {
    flex-direction: column;
  }
  
  .filter-card .el-form-item {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>