<template>
  <div class="article-management">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>文章管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文章标题/作者"
              prefix-icon="Search"
              style="width: 240px; margin-right: 10px"
              @input="handleSearch"
            />
            <el-button type="primary" @click="handleAddArticle" :disabled="!hasPermission(['ARTICLE_MANAGE'])"
              >新增文章</el-button
            >
            <el-button type="primary" @click="handleRefresh">刷新</el-button>
          </div>
        </div>
      </template>
      
      <div class="table-container">
        <el-table
          v-loading="loading"
          :data="articlesData"
          border
          style="width: 100%"
        >
          <el-table-column type="index" label="序号" width="80"></el-table-column>
          <el-table-column prop="id" label="文章ID" width="120"></el-table-column>
          <el-table-column prop="title" label="标题" min-width="200"></el-table-column>
          <el-table-column prop="author" label="作者" width="120"></el-table-column>
          <el-table-column prop="category" label="分类" width="100"></el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusTagType(scope.row.status)">{{ scope.row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="viewCount" label="浏览量" width="100"></el-table-column>
          <el-table-column prop="publishDate" label="发布时间" min-width="180"></el-table-column>
          <el-table-column label="操作" min-width="180" fixed="right">
            <template #default="scope">
              <el-button
                type="primary"
                text
                size="small"
                @click="handleViewArticle(scope.row)"
              >
                查看
              </el-button>
              <el-button
                type="success"
                text
                size="small"
                @click="handleEditArticle(scope.row)"
                :disabled="!hasPermission(['ARTICLE_MANAGE'])"
              >
                编辑
              </el-button>
              <el-button
                type="danger"
                text
                size="small"
                @click="handleDeleteArticle(scope.row)"
                :disabled="!hasPermission(['ARTICLE_MANAGE'])"
              >
                删除
              </el-button>
              <el-button
                type="info"
                text
                size="small"
                @click="handlePublishArticle(scope.row)"
                v-if="scope.row.status === 'draft'"
                :disabled="!hasPermission(['ARTICLE_MANAGE'])"
              >
                发布
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
    
    <!-- 文章详情对话框 -->
    <el-dialog
      v-model="articleDetailDialogVisible"
      title="文章详情"
      width="800px"
    >
      <div v-if="selectedArticle" class="article-detail-content">
        <h3>{{ selectedArticle.title }}</h3>
        <div class="article-meta">
          <span>作者：{{ selectedArticle.author }}</span>
          <span>发布时间：{{ selectedArticle.publishDate }}</span>
          <span>浏览量：{{ selectedArticle.viewCount }}</span>
          <span>分类：{{ selectedArticle.category }}</span>
        </div>
        <div class="article-content" v-html="selectedArticle.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAdminStore } from '@/stores/admin'
import { adminUtils } from '@/utils/admin'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminApi } from '@/api/admin/adminApi'
import type { AdminPermission } from '@/types/admin'

const router = useRouter()
const adminStore = useAdminStore()

// 数据和状态
const loading = ref(false)
const searchKeyword = ref('')
const articlesData = ref<any[]>([])
const articleDetailDialogVisible = ref(false)
const selectedArticle = ref<any>(null)

// 分页数据
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// 权限检查方法
const hasPermission = (requiredPermissions: AdminPermission[]): boolean => {
  // 检查管理员是否登录且会话未过期
  if (!adminStore.isAdminUser || adminStore.isSessionExpired) {
    return false
  }
  
  // 超级管理员拥有所有权限
  if (adminStore.hasAllPermissions) {
    return true
  }
  
  // 检查是否拥有所有必需的权限
  return requiredPermissions.every(permission => {
    return adminStore.hasPermission(permission)
  })
}

// 获取状态标签类型
const getStatusTagType = (status: string) => {
  switch (status) {
    case 'published': return 'success'
    case 'draft': return 'warning'
    case 'reviewing': return 'info'
    case 'rejected': return 'danger'
    default: return 'primary'
  }
}

// 获取文章列表
const fetchArticles = async () => {
  try {
    loading.value = true
    
    // 调用真实的API获取文章列表
    const response = await adminApi.getArticleList({
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      title: searchKeyword.value.trim() || undefined
    })
    
    // 处理API响应数据
    if (response && response.data) {
      if (Array.isArray(response.data)) {
        articlesData.value = response.data
        pagination.value.total = response.data.length
      } else if (response.data.items && Array.isArray(response.data.items)) {
        articlesData.value = response.data.items
        pagination.value.total = response.data.total || response.data.items.length
      } else if (response.data.records && Array.isArray(response.data.records)) {
        articlesData.value = response.data.records
        pagination.value.total = response.data.total || response.data.records.length
      } else if (response.data.list && Array.isArray(response.data.list)) {
        articlesData.value = response.data.list
        pagination.value.total = response.data.total || response.data.list.length
      } else {
        articlesData.value = []
        pagination.value.total = 0
        console.warn('API返回的数据结构不符合预期:', response.data)
      }
    } else {
      articlesData.value = []
      pagination.value.total = 0
    }
    
  } catch (error) {
    console.error('获取文章列表失败:', error)
    
    // 检查是否是API未实现的错误
    if (error.response?.status === 500) {
      ElMessage.warning('文章管理API尚未实现，请等待后端开发完成')
      // 显示一些模拟数据作为示例
      articlesData.value = [
        {
          id: '1',
          title: '示例文章：英语学习技巧',
          author: '管理员',
          category: '学习技巧',
          status: 'published',
          viewCount: 0,
          publishDate: new Date().toLocaleString('zh-CN'),
          content: '<p>这是一篇示例文章，用于展示文章管理界面的功能。</p>'
        }
      ]
      pagination.value.total = 1
    } else {
      ElMessage.error('获取文章列表失败')
      articlesData.value = []
      pagination.value.total = 0
    }
  } finally {
    loading.value = false
  }
}

// 处理搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchArticles()
}

// 处理刷新
const handleRefresh = () => {
  searchKeyword.value = ''
  pagination.value.currentPage = 1
  fetchArticles()
}

// 添加文章
const handleAddArticle = () => {
  router.push('/admin/articles/add')
}

// 查看文章
const handleViewArticle = (article: any) => {
  selectedArticle.value = { ...article }
  articleDetailDialogVisible.value = true
}

// 编辑文章
const handleEditArticle = (article: any) => {
  router.push({ path: '/admin/articles/edit', query: { id: article.id } })
}

// 删除文章
const handleDeleteArticle = async (article: any) => {
  try {
    const confirmResult = await ElMessageBox.confirm(
      `确定要删除文章《${article.title}》吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult === 'confirm') {
      loading.value = true
      
      // 调用真实的API删除文章
      await adminApi.deleteArticle(article.id)
      
      // 从列表中移除
      const index = articlesData.value.findIndex(a => a.id === article.id)
      if (index !== -1) {
        articlesData.value.splice(index, 1)
        pagination.value.total -= 1
      }
      
      ElMessage.success('删除文章成功')
    }
  } catch (error) {
    console.error('删除文章失败:', error)
    
    // 检查是否是API未实现的错误
    if (error.response?.status === 500) {
      ElMessage.warning('删除文章API尚未实现，请等待后端开发完成')
    } else {
      ElMessage.error('删除文章失败，请重试')
    }
  } finally {
    loading.value = false
  }
}

// 发布文章
const handlePublishArticle = async (article: any) => {
  try {
    loading.value = true
    
    // 调用真实的API发布文章
    await adminApi.publishArticle(article.id)
    
    // 更新本地状态
    article.status = 'published'
    ElMessage.success('发布文章成功')
  } catch (error) {
    console.error('发布文章失败:', error)
    
    // 检查是否是API未实现的错误
    if (error.response?.status === 500) {
      ElMessage.warning('发布文章API尚未实现，请等待后端开发完成')
    } else {
      ElMessage.error('发布文章失败，请重试')
    }
  } finally {
    loading.value = false
  }
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

// 组件挂载时初始化数据
onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
.article-management {
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

.article-detail-content {
  padding: 10px 0;
}

.article-meta {
  display: flex;
  gap: 20px;
  margin: 15px 0;
  color: #606266;
  font-size: 14px;
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 10px;
}

.article-content {
  margin-top: 20px;
  line-height: 1.8;
  color: #303133;
}
</style>