<template>
  <div class="article-management">
    <!-- 筛选和搜索区域 -->
    <el-card shadow="hover" class="filter-card">
      <template #header>
        <div class="card-header">
          <h2>文章管理</h2>
          <div class="header-actions">
            <el-button type="primary" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
            <el-button type="success" @click="showFilterLogsDialog = true">
              <el-icon><View /></el-icon>
              敏感词记录
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 筛选条件 -->
      <div class="filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-input
              v-model="filters.title"
              placeholder="搜索文章标题"
              prefix-icon="Search"
              clearable
              @input="handleSearch"
            />
          </el-col>
          <el-col :span="4">
            <el-select v-model="filters.category" placeholder="选择分类" clearable @change="handleSearch">
              <el-option
                v-for="category in categories"
                :key="category"
                :label="category"
                :value="category"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="filters.difficulty" placeholder="选择难度" clearable @change="handleSearch">
              <el-option
                v-for="difficulty in difficulties"
                :key="difficulty"
                :label="difficulty"
                :value="difficulty"
              />
            </el-select>
          </el-col>
          <el-col :span="4">
            <el-select v-model="filters.status" placeholder="选择状态" clearable @change="handleSearch">
              <el-option label="正常" value="normal" />
              <el-option label="草稿" value="draft" />
              <el-option label="已发布" value="published" />
              <el-option label="已删除" value="deleted" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-button type="primary" @click="handleSearch">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="resetFilters">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- 文章列表 -->
    <el-card shadow="hover" class="table-card">
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="articlesData"
          border
          style="width: 100%"
          @selection-change="handleSelectionChange"
        >
          <el-table-column type="selection" width="55" />
          <el-table-column type="index" label="序号" width="80" />
          <el-table-column prop="id" label="文章ID" width="100" />
          <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
          <el-table-column prop="category" label="分类" width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.category" type="primary">{{ scope.row.category }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="difficultyLevel" label="难度" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.difficultyLevel" :type="getDifficultyTagType(scope.row.difficultyLevel)">
                {{ scope.row.difficultyLevel }}
              </el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop="readCount" label="阅读数" width="100" />
          <el-table-column prop="likeCount" label="点赞数" width="100" />
          <el-table-column prop="isFeatured" label="精选" width="80">
            <template #default="scope">
              <el-tag v-if="scope.row.isFeatured" type="success">是</el-tag>
              <el-tag v-else type="info">否</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleViewArticle(scope.row)">
                查看
              </el-button>
              <el-button type="success" size="small" @click="handleEditArticle(scope.row)">
                编辑
              </el-button>
              <el-button type="danger" size="small" @click="handleDeleteArticle(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <!-- 分页 -->
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
    
    <!-- 文章详情对话框 -->
    <el-dialog
      v-model="articleDetailDialogVisible"
      title="文章详情"
      width="90%"
      :before-close="handleCloseDetailDialog"
    >
      <div v-if="selectedArticle" class="article-detail-content">
        <div class="article-header">
          <h2>{{ selectedArticle.title }}</h2>
        <div class="article-meta">
            <el-tag type="primary">{{ selectedArticle.category }}</el-tag>
            <el-tag :type="getDifficultyTagType(selectedArticle.difficultyLevel)">
              {{ selectedArticle.difficultyLevel }}
            </el-tag>
            <el-tag :type="getStatusTagType(selectedArticle.status)">
              {{ getStatusText(selectedArticle.status) }}
            </el-tag>
            <span>阅读数: {{ selectedArticle.readCount }}</span>
            <span>点赞数: {{ selectedArticle.likeCount }}</span>
            <span>创建时间: {{ selectedArticle.createTime }}</span>
          </div>
        </div>
        
        <el-tabs v-model="activeTab" class="article-tabs">
          <el-tab-pane label="英文原文" name="english">
            <div class="article-content" v-html="formatContent(selectedArticle.contentEn)"></div>
          </el-tab-pane>
          <el-tab-pane label="中文翻译" name="chinese">
            <div class="article-content" v-html="formatContent(selectedArticle.contentCn)"></div>
          </el-tab-pane>
          <el-tab-pane label="敏感词记录" name="filter-logs">
            <div class="filter-logs-section">
              <div class="filter-logs-header">
                <h3>敏感词拦截记录</h3>
                <el-button type="primary" size="small" @click="loadArticleFilterLogs">
                  <el-icon><Refresh /></el-icon>
                  刷新记录
                </el-button>
              </div>
              <el-table :data="articleFilterLogs" border>
                <el-table-column prop="filterType" label="过滤类型" width="120" />
                <el-table-column prop="matchedContent" label="匹配内容" min-width="200" show-overflow-tooltip />
                <el-table-column prop="severityLevel" label="严重程度" width="100">
                  <template #default="scope">
                    <el-tag :type="getSeverityTagType(scope.row.severityLevel)">
                      {{ scope.row.severityLevel }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="actionTaken" label="采取行动" width="100" />
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="scope">
                    <el-tag :type="getLogStatusTagType(scope.row.status)">
                      {{ scope.row.status }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="createdAt" label="创建时间" width="160" />
              </el-table>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>

    <!-- 敏感词记录管理对话框（只读） -->
    <el-dialog
      v-model="showFilterLogsDialog"
      title="敏感词记录管理"
      width="90%"
    >
      <div class="filter-logs-management">
        <!-- 筛选条件 -->
        <div class="filter-section">
          <el-row :gutter="20">
            
            <el-col :span="6">
              <el-select v-model="filterLogsFilters.status" placeholder="状态" clearable @change="loadFilterLogs">
                <el-option label="活跃" value="active" />
                <el-option label="已解决" value="resolved" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-date-picker
                v-model="filterLogsFilters.dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                @change="loadFilterLogs"
              />
            </el-col>
            <el-col :span="6">
              <el-button type="primary" @click="loadFilterLogs">
                <el-icon><Search /></el-icon>
                搜索
              </el-button>
              <el-button @click="resetFilterLogsFilters">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </el-col>
          </el-row>
        </div>

        

        <!-- 记录列表（只读） -->
        <el-table
          v-loading="filterLogsLoading"
          :data="filterLogsData"
          border
        >
          <el-table-column type="index" label="序号" width="80" />
          <el-table-column prop="articleId" label="文章ID" width="100" />
          <el-table-column prop="filterType" label="过滤类型" width="120" />
          <el-table-column prop="matchedContent" label="匹配内容" min-width="200" show-overflow-tooltip />
          <el-table-column prop="severityLevel" label="严重程度" width="100">
            <template #default="scope">
              <el-tag :type="getSeverityTagType(scope.row.severityLevel)">
                {{ scope.row.severityLevel }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="actionTaken" label="采取行动" width="100" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getLogStatusTagType(scope.row.status)">
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="创建时间" width="160" />
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleViewArticleById(scope.row.articleId)">
                查看文章
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="filterLogsPagination.currentPage"
            v-model:page-size="filterLogsPagination.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="filterLogsPagination.total"
            @size-change="handleFilterLogsSizeChange"
            @current-change="handleFilterLogsCurrentChange"
          />
        </div>
      </div>
    </el-dialog>

    <!-- 编辑文章对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑文章" width="500px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="文章标题">
          <el-input v-model="editForm.title" disabled />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="editForm.category" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="editForm.difficultyLevel" placeholder="请选择难度" style="width: 100%">
            <el-option
              v-for="difficulty in difficulties"
              :key="difficulty"
              :label="difficulty"
              :value="difficulty"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="精选状态">
          <el-switch
            v-model="editForm.isFeatured"
            active-text="精选"
            inactive-text="普通"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmEditArticle">确定</el-button>
      </template>
    </el-dialog>

    <!-- 更新分类对话框 -->
    <el-dialog v-model="categoryDialogVisible" title="更新文章分类" width="400px">
      <el-form :model="categoryForm" label-width="80px">
        <el-form-item label="文章标题">
          <el-input v-model="categoryForm.title" disabled />
        </el-form-item>
        <el-form-item label="当前分类">
          <el-input v-model="categoryForm.currentCategory" disabled />
        </el-form-item>
        <el-form-item label="新分类">
          <el-select v-model="categoryForm.newCategory" placeholder="选择新分类">
            <el-option
              v-for="category in categories"
              :key="category"
              :label="category"
              :value="category"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmUpdateCategory">确定</el-button>
      </template>
    </el-dialog>

    <!-- 更新难度对话框 -->
    <el-dialog v-model="difficultyDialogVisible" title="更新文章难度" width="400px">
      <el-form :model="difficultyForm" label-width="80px">
        <el-form-item label="文章标题">
          <el-input v-model="difficultyForm.title" disabled />
        </el-form-item>
        <el-form-item label="当前难度">
          <el-input v-model="difficultyForm.currentDifficulty" disabled />
        </el-form-item>
        <el-form-item label="新难度">
          <el-select v-model="difficultyForm.newDifficulty" placeholder="选择新难度">
            <el-option
              v-for="difficulty in difficulties"
              :key="difficulty"
              :label="difficulty"
              :value="difficulty"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="difficultyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirmUpdateDifficulty">确定</el-button>
      </template>
    </el-dialog>

    
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { adminUtils } from '@/utils/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getArticleList, 
  getArticleDetail, 
  deleteArticle, 
  publishArticle,
  updateArticleCategory,
  updateArticleDifficulty,
  markArticleFeatured,
  getArticleFilterLogs,
  getFilterLogsPage
} from '@/api/admin/adminApi'
import type { AdminPermission } from '@/types/admin'

const router = useRouter()
const adminStore = useAdminStore()

// 数据和状态
const loading = ref(false)
const articlesData = ref<any[]>([])
const articleDetailDialogVisible = ref(false)
const selectedArticle = ref<any>(null)
const activeTab = ref('english')

// 筛选条件
const filters = ref({
  title: '',
  category: '',
  difficulty: '',
  status: ''
})

// 分类和难度选项
const categories = ref<string[]>([])
const difficulties = ref<string[]>([])

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 敏感词记录相关
const showFilterLogsDialog = ref(false)
const filterLogsLoading = ref(false)
const filterLogsData = ref<any[]>([])
const articleFilterLogs = ref<any[]>([])
// 只读模式：移除统计

const filterLogsFilters = ref({
  status: '',
  dateRange: null as any
})

const filterLogsPagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 对话框状态
const editDialogVisible = ref(false)
const categoryDialogVisible = ref(false)
const difficultyDialogVisible = ref(false)
const logStatusDialogVisible = ref(false)

// 表单数据
const editForm = ref({
  articleId: '',
  title: '',
  category: '',
  difficultyLevel: '',
  isFeatured: false
})

const categoryForm = ref({
  articleId: '',
  title: '',
  currentCategory: '',
  newCategory: ''
})

const difficultyForm = ref({
  articleId: '',
  title: '',
  currentDifficulty: '',
  newDifficulty: ''
})

const logStatusForm = ref({
  logId: '',
  matchedContent: '',
  currentStatus: '',
  newStatus: ''
})

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

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'normal': return 'success'
    case 'draft': return 'warning'
    case 'published': return 'primary'
    case 'deleted': return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'normal': return '正常'
    case 'draft': return '草稿'
    case 'published': return '已发布'
    case 'deleted': return '已删除'
    default: return status
  }
}

const getDifficultyTagType = (difficulty: string) => {
  switch (difficulty) {
    case 'A1': return 'success'
    case 'A2': return 'success'
    case 'B1': return 'warning'
    case 'B2': return 'warning'
    case 'C1': return 'danger'
    case 'C2': return 'danger'
    default: return 'info'
  }
}

const getSeverityTagType = (severity: string) => {
  switch (severity) {
    case 'low': return 'success'
    case 'medium': return 'warning'
    case 'high': return 'danger'
    default: return 'info'
  }
}

const getLogStatusTagType = (status: string) => {
  switch (status) {
    case 'active': return 'warning'
    case 'resolved': return 'success'
    case 'ignored': return 'info'
    default: return 'primary'
  }
}

// 格式化内容
const formatContent = (content: string) => {
  if (!content) return ''
  return content.replace(/\n/g, '<br>')
}

// 获取文章列表
const fetchArticles = async () => {
  try {
    loading.value = true
    
    console.log('开始获取文章列表，参数:', {
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      title: filters.value.title.trim() || undefined,
      category: filters.value.category || undefined,
      difficulty: filters.value.difficulty || undefined,
      status: filters.value.status || undefined
    })
    
    const response = await getArticleList({
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      title: filters.value.title.trim() || undefined,
      category: filters.value.category || undefined,
      difficulty: filters.value.difficulty || undefined,
      status: filters.value.status || undefined
    })
    
    console.log('文章列表API响应:', response)
    
    if (response && response.data) {
      // 后端返回的是 ApiResponse.success(PageResult)
      // PageResult结构: { list: [], total: number, current: number, size: number }
      if (response.data.list && Array.isArray(response.data.list)) {
        articlesData.value = response.data.list
        pagination.value.total = response.data.total || response.data.list.length
        console.log('成功获取文章列表，数量:', response.data.list.length, '总数:', response.data.total)
      } else if (Array.isArray(response.data)) {
        // 兼容直接返回数组的情况
        articlesData.value = response.data
        pagination.value.total = response.data.length
        console.log('成功获取文章列表（数组格式），数量:', response.data.length)
      } else {
        articlesData.value = []
        pagination.value.total = 0
        console.log('响应数据格式不正确:', response.data)
      }
    } else {
      articlesData.value = []
      pagination.value.total = 0
      console.log('API响应为空或失败:', response)
    }
    
    // 从文章数据中提取分类和难度选项
    extractOptionsFromArticles()
    
  } catch (error) {
    console.error('获取文章列表失败:', error)
    ElMessage.error('获取文章列表失败')
    articlesData.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 从文章数据中提取分类和难度选项
const extractOptionsFromArticles = () => {
  // 从文章数据中提取唯一的分类
  const uniqueCategories = new Set<string>()
  const uniqueDifficulties = new Set<string>()
  
  articlesData.value.forEach(article => {
    if (article.category) {
      uniqueCategories.add(article.category)
    }
    if (article.difficultyLevel) {
      uniqueDifficulties.add(article.difficultyLevel)
    }
  })
  
  categories.value = Array.from(uniqueCategories).sort()
  difficulties.value = Array.from(uniqueDifficulties).sort()
}

// 获取敏感词记录
const loadFilterLogs = async () => {
  try {
    filterLogsLoading.value = true
    
    const params: any = {
      page: filterLogsPagination.value.currentPage,
      pageSize: filterLogsPagination.value.pageSize
    }
    
    if (filterLogsFilters.value.status) {
      params.status = filterLogsFilters.value.status
    }
    if (filterLogsFilters.value.dateRange && filterLogsFilters.value.dateRange.length === 2) {
      params.startDate = filterLogsFilters.value.dateRange[0].toISOString().split('T')[0]
      params.endDate = filterLogsFilters.value.dateRange[1].toISOString().split('T')[0]
    }
    
    const response = await getFilterLogsPage(params)
    if (response && response.data) {
      if (response.data.records) {
        filterLogsData.value = response.data.records
        filterLogsPagination.value.total = response.data.total
      } else if (Array.isArray(response.data)) {
        filterLogsData.value = response.data
        filterLogsPagination.value.total = response.data.length
      }
    }
  } catch (error) {
    console.error('获取敏感词记录失败:', error)
    ElMessage.error('获取敏感词记录失败')
  } finally {
    filterLogsLoading.value = false
  }
}

// 获取文章敏感词记录
const loadArticleFilterLogs = async () => {
  if (!selectedArticle.value) return
  
  try {
    const response = await getArticleFilterLogs(selectedArticle.value.id.toString())
    if (response && response.data) {
      articleFilterLogs.value = response.data
    }
  } catch (error) {
    console.error('获取文章敏感词记录失败:', error)
  }
}

// 获取敏感词统计
// 只读模式：移除统计加载

// 处理搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchArticles()
}

// 重置筛选条件
const resetFilters = () => {
  filters.value = {
    title: '',
    category: '',
    difficulty: '',
    status: ''
  }
  pagination.value.currentPage = 1
  fetchArticles()
}

// 重置敏感词记录筛选条件
const resetFilterLogsFilters = () => {
  filterLogsFilters.value = {
    status: '',
    dateRange: null
  }
  filterLogsPagination.value.currentPage = 1
  loadFilterLogs()
}

// 处理刷新
const handleRefresh = () => {
  fetchArticles()
}

// 处理选择变化
const handleSelectionChange = (selection: any[]) => {
  console.log('选中的文章:', selection)
}

// 查看文章
const handleViewArticle = async (article: any) => {
  try {
    const response = await getArticleDetail(article.id.toString())
    if (response && response.data) {
      selectedArticle.value = response.data
      articleDetailDialogVisible.value = true
      activeTab.value = 'english'
      // 加载敏感词记录
      loadArticleFilterLogs()
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败')
  }
}

// 根据ID查看文章
const handleViewArticleById = async (articleId: number) => {
  try {
    const response = await getArticleDetail(articleId.toString())
    if (response && response.data) {
      selectedArticle.value = response.data
      articleDetailDialogVisible.value = true
      activeTab.value = 'english'
      loadArticleFilterLogs()
    }
  } catch (error) {
    console.error('获取文章详情失败:', error)
    ElMessage.error('获取文章详情失败')
  }
}

// 编辑文章
const handleEditArticle = (article: any) => {
  editForm.value = {
    articleId: article.id,
    title: article.title,
    category: article.category || '',
    difficultyLevel: article.difficultyLevel || '',
    isFeatured: article.isFeatured || false
  }
  editDialogVisible.value = true
}

// 确认编辑文章
const handleConfirmEditArticle = async () => {
  try {
    // 更新分类
    if (editForm.value.category) {
      await updateArticleCategory(editForm.value.articleId.toString(), editForm.value.category)
    }
    
    // 更新难度
    if (editForm.value.difficultyLevel) {
      await updateArticleDifficulty(editForm.value.articleId.toString(), editForm.value.difficultyLevel)
    }
    
    // 更新精选状态
    await markArticleFeatured(editForm.value.articleId.toString(), editForm.value.isFeatured)
    
    // 更新本地数据
    const article = articlesData.value.find(a => a.id === editForm.value.articleId)
    if (article) {
      article.category = editForm.value.category
      article.difficultyLevel = editForm.value.difficultyLevel
      article.isFeatured = editForm.value.isFeatured
    }
    
    editDialogVisible.value = false
    ElMessage.success('文章更新成功')
  } catch (error) {
    console.error('更新文章失败:', error)
    ElMessage.error('更新文章失败')
  }
}

// 确认更新分类
const handleConfirmUpdateCategory = async () => {
  try {
    await updateArticleCategory(categoryForm.value.articleId.toString(), categoryForm.value.newCategory)
    ElMessage.success('更新分类成功')
    categoryDialogVisible.value = false
    fetchArticles()
  } catch (error) {
    console.error('更新分类失败:', error)
    ElMessage.error('更新分类失败')
  }
}

// 编辑难度
const handleEditDifficulty = (article: any) => {
  difficultyForm.value = {
    articleId: article.id,
    title: article.title,
    currentDifficulty: article.difficultyLevel || '',
    newDifficulty: article.difficultyLevel || ''
  }
  difficultyDialogVisible.value = true
}

// 确认更新难度
const handleConfirmUpdateDifficulty = async () => {
  try {
    await updateArticleDifficulty(difficultyForm.value.articleId.toString(), difficultyForm.value.newDifficulty)
    ElMessage.success('更新难度成功')
    difficultyDialogVisible.value = false
    fetchArticles()
  } catch (error) {
    console.error('更新难度失败:', error)
    ElMessage.error('更新难度失败')
  }
}

// 切换精选状态
const handleToggleFeatured = async (article: any) => {
  try {
    await markArticleFeatured(article.id.toString(), !article.isFeatured)
    article.isFeatured = !article.isFeatured
    ElMessage.success(article.isFeatured ? '设为精选成功' : '取消精选成功')
  } catch (error) {
    console.error('更新精选状态失败:', error)
    ElMessage.error('更新精选状态失败')
  }
}

// 删除文章
const handleDeleteArticle = async (article: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文章《${article.title}》吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await deleteArticle(article.id.toString())
    ElMessage.success('删除文章成功')
    fetchArticles()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除文章失败:', error)
      ElMessage.error('删除文章失败')
    }
  }
}

// 更新记录状态
// 只读模式：移除状态更新

// 确认更新记录状态
// 只读模式：移除状态更新

// 删除记录
// 只读模式：移除删除

// 关闭详情对话框
const handleCloseDetailDialog = () => {
  articleDetailDialogVisible.value = false
  selectedArticle.value = null
  articleFilterLogs.value = []
}

// 分页处理
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  fetchArticles()
}

const handleCurrentChange = (current: number) => {
  pagination.value.currentPage = current
  fetchArticles()
}

const handleFilterLogsSizeChange = (size: number) => {
  filterLogsPagination.value.pageSize = size
  loadFilterLogs()
}

const handleFilterLogsCurrentChange = (current: number) => {
  filterLogsPagination.value.currentPage = current
  loadFilterLogs()
}

// 组件挂载时初始化数据
onMounted(() => {
  fetchArticles()
})

// 打开敏感词记录对话框时，自动加载并重置分页到第1页
watch(showFilterLogsDialog, (opened: boolean) => {
  if (opened) {
    filterLogsPagination.value.currentPage = 1
    loadFilterLogs()
  }
})
</script>

<style scoped>
.article-management {
  padding: 20px;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
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
  gap: 10px;
}

.filter-section {
  margin-bottom: 20px;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
}

.article-detail-content {
  padding: 10px 0;
}

.article-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.article-header h2 {
  margin: 0 0 15px 0;
  color: #303133;
}

.article-meta {
  display: flex;
  gap: 20px;
  align-items: center;
  color: #606266;
  font-size: 14px;
}

.article-meta span {
  margin-right: 10px;
}

.article-tabs {
  margin-top: 20px;
}

.article-content {
  margin-top: 20px;
  line-height: 1.8;
  color: #303133;
  max-height: 500px;
  overflow-y: auto;
  padding: 15px;
  background-color: #fafafa;
  border-radius: 4px;
}

.filter-logs-section {
  margin-top: 20px;
}

.filter-logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.filter-logs-header h3 {
  margin: 0;
  color: #303133;
}

.filter-logs-management {
  padding: 10px 0;
}

.statistics-section {
  margin: 20px 0;
}

.stat-item {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .article-management {
    padding: 10px;
  }
  
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  .filter-section .el-row {
    flex-direction: column;
  }
  
  .filter-section .el-col {
    margin-bottom: 10px;
  }
  
  .article-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .statistics-section .el-row {
    flex-direction: column;
  }
  
  .statistics-section .el-col {
    margin-bottom: 10px;
  }
}

/* 表格样式优化 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background-color: #fafafa;
  font-weight: 600;
}

:deep(.el-table td) {
  padding: 12px 0;
}

/* 标签样式 */
:deep(.el-tag) {
  font-size: 12px;
}

/* 按钮样式 */
:deep(.el-button--small) {
  padding: 5px 10px;
  font-size: 12px;
}

/* 对话框样式 */
:deep(.el-dialog__body) {
  padding: 20px;
}

:deep(.el-dialog__header) {
  padding: 20px 20px 10px;
}

:deep(.el-dialog__footer) {
  padding: 10px 20px 20px;
}

/* 分页样式 */
:deep(.el-pagination) {
  margin-top: 20px;
}

/* 加载状态 */
:deep(.el-loading-mask) {
  background-color: rgba(255, 255, 255, 0.8);
}

/* 表格选择框 */
:deep(.el-table__selection) {
  text-align: center;
}

/* 表格操作列 */
:deep(.el-table__fixed-right) {
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
}

/* 标签页样式 */
:deep(.el-tabs__header) {
  margin-bottom: 20px;
}

:deep(.el-tabs__content) {
  padding-top: 10px;
}

/* 表单样式 */
:deep(.el-form-item__label) {
  font-weight: 600;
}

:deep(.el-input.is-disabled .el-input__inner) {
  background-color: #f5f7fa;
  color: #909399;
}

/* 日期选择器样式 */
:deep(.el-date-editor) {
  width: 100%;
}

/* 统计卡片样式 */
:deep(.el-card__body) {
  padding: 0;
}

.stat-item {
  padding: 20px;
  text-align: center;
  border-radius: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.stat-item .stat-value {
  color: white;
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 8px;
}

.stat-item .stat-label {
  color: rgba(255, 255, 255, 0.8);
  font-size: 14px;
}

/* 滚动条样式 */
.article-content::-webkit-scrollbar {
  width: 6px;
}

.article-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.article-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.article-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>