<template>
  <div class="ai-analysis-view">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>AI分析结果</h1>
      <p>查看和管理所有文章的AI分析结果</p>

      <!-- 历史数据提示 -->
      <div v-if="hasLegacyData" class="legacy-warning">
        <el-icon><Warning /></el-icon>
        <span>
          <strong>注意：</strong>检测到历史数据（负数ID），这些是旧版本的设计遗留问题。
          建议重新进行句子解析以获得更好的数据结构和用户体验。
        </span>
      </div>
    </div>

    <!-- 筛选条件 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="filters" inline>
        <el-form-item label="文章标题">
            <el-input
            v-model="filters.articleTitle"
            placeholder="请输入文章标题"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分析类型">
          <el-select
            v-model="filters.analysisType"
            placeholder="请选择分析类型"
            clearable
            style="width: 150px"
          >
            <el-option label="全部" value="" />
            <el-option label="摘要分析" value="summary" />
            <el-option label="句子解析" value="parse" />
            <el-option label="测验题" value="quiz" />
            <!-- 移除综合分析选项，因为用户端未使用 -->
            <!-- <el-option label="综合分析" value="comprehensive" /> -->
            </el-select>
        </el-form-item>
        <el-form-item label="数据类型">
          <el-select
            v-model="filters.idType"
            placeholder="请选择数据类型"
            clearable
            style="width: 140px"
          >
            <el-option label="全部" value="" />
            <el-option label="文章分析" value="real" />
            <el-option label="历史数据" value="virtual" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 分析结果列表 -->
    <el-card class="table-card" shadow="never">
        <el-table
        :data="analysisList"
          v-loading="loading"
        stripe
          style="width: 100%"
        @row-click="handleRowClick"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="articleId" label="关联ID" width="120">
          <template #default="{ row }">
            <div class="id-cell">
              <el-tag v-if="row.analysisCategory === 'sentence' && !row.sourceArticleId" type="warning" size="small" class="legacy-tag">
                <el-icon><Warning /></el-icon>
                历史数据
              </el-tag>
              <el-tag v-else type="success" size="small">
                <el-icon><Document /></el-icon>
                文章ID
              </el-tag>
              <span class="id-value">{{ row.sourceArticleId || Math.abs(row.articleId) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="articleTitle" label="分析对象" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <div class="analysis-object-cell">
              <el-tag v-if="row.analysisCategory === 'sentence' && !row.sourceArticleId" type="info" size="small" class="object-tag">
                <el-icon><ChatDotRound /></el-icon>
                句子解析
              </el-tag>
              <el-tag v-else type="primary" size="small" class="object-tag">
                <el-icon><Document /></el-icon>
                文章分析
              </el-tag>
              <div class="object-content">
                <div class="object-title">{{ row.articleTitle }}</div>
                <div v-if="row.analysisCategory === 'sentence' && !row.sourceArticleId" class="legacy-notice">
                  <el-icon><InfoFilled /></el-icon>
                  建议重新分析
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="lastAnalysisType" label="分析类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getAnalysisTypeTagType(row.lastAnalysisType)">
              {{ getAnalysisTypeName(row.lastAnalysisType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="分析时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="updatedAt" label="更新时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
              <el-button
                type="primary"
                size="small"
              @click.stop="handleViewDetail(row)"
              >
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="AI分析详情"
      width="80%"
      :close-on-click-modal="false"
    >
      <div v-if="selectedAnalysis" class="analysis-detail">
        <!-- 基本信息 -->
        <el-card class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>基本信息</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="分析ID">{{ selectedAnalysis.id }}</el-descriptions-item>
            <el-descriptions-item label="关联ID">
              <div class="id-cell">
                <el-tag v-if="selectedAnalysis.analysisCategory === 'sentence'" type="warning" size="small" class="legacy-tag">
                  <el-icon><Warning /></el-icon>
                  历史数据
                </el-tag>
                <el-tag v-else type="success" size="small">
                  <el-icon><Document /></el-icon>
                  文章ID
                </el-tag>
                <span class="id-value">{{ Math.abs(selectedAnalysis.articleId) }}</span>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="分析对象">
              <div class="analysis-object-cell">
                <el-tag v-if="selectedAnalysis.analysisCategory === 'sentence' && !selectedAnalysis.sourceArticleId" type="info" size="small" class="object-tag">
                  <el-icon><ChatDotRound /></el-icon>
                  句子解析
                </el-tag>
                <el-tag v-else type="primary" size="small" class="object-tag">
                  <el-icon><Document /></el-icon>
                  文章分析
                </el-tag>
                <div class="object-content">
                  <div class="object-title">{{ selectedAnalysis.title }}</div>
                  <div v-if="selectedAnalysis.analysisCategory === 'sentence' && !selectedAnalysis.sourceArticleId" class="legacy-notice">
                    <el-icon><InfoFilled /></el-icon>
                    建议重新分析
                  </div>
                </div>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="分析类型">
              <el-tag :type="getAnalysisTypeTagType(selectedAnalysis.lastAnalysisType)">
                {{ getAnalysisTypeName(selectedAnalysis.lastAnalysisType) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(selectedAnalysis.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(selectedAnalysis.updatedAt) }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <!-- 摘要信息 -->
        <el-card v-if="selectedAnalysis.summary" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>文章摘要</span>
            </div>
          </template>
          <div class="content-text">{{ selectedAnalysis.summary }}</div>
        </el-card>

        <!-- 关键词 -->
        <el-card v-if="selectedAnalysis.keywords" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>关键词</span>
            </div>
          </template>
          <div class="keywords-container">
            <el-tag
              v-for="keyword in getKeywordsArray(selectedAnalysis.keywords)"
              :key="keyword"
              class="keyword-tag"
              type="info"
            >
              {{ keyword }}
            </el-tag>
          </div>
        </el-card>

        <!-- 关键短语 -->
        <el-card v-if="selectedAnalysis.keyPhrases" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>关键短语</span>
            </div>
          </template>
          <div class="phrases-container">
            <el-tag
              v-for="phrase in getKeyPhrasesArray(selectedAnalysis.keyPhrases)"
              :key="phrase"
              class="phrase-tag"
              type="warning"
            >
              {{ phrase }}
            </el-tag>
        </div>
        </el-card>

        <!-- 中文翻译 -->
        <el-card v-if="selectedAnalysis.chineseTranslation" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>中文翻译</span>
        </div>
          </template>
          <div class="content-text">{{ selectedAnalysis.chineseTranslation }}</div>
        </el-card>

        <!-- 简化内容 -->
        <el-card v-if="selectedAnalysis.simplifiedContent" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>简化内容</span>
        </div>
          </template>
          <div class="content-text">{{ selectedAnalysis.simplifiedContent }}</div>
        </el-card>

        <!-- 学习建议 -->
        <el-card v-if="selectedAnalysis.learningTips" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>学习建议</span>
        </div>
          </template>
          <div class="content-text">{{ selectedAnalysis.learningTips }}</div>
        </el-card>

        <!-- 句子解析结果 -->
        <el-card v-if="selectedAnalysis.sentenceParseResults" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>句子解析结果</span>
            </div>
          </template>
          <div class="json-content">
            <pre>{{ formatJson(selectedAnalysis.sentenceParseResults) }}</pre>
          </div>
        </el-card>

        <!-- 测验题 -->
        <el-card v-if="selectedAnalysis.quizQuestions" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>测验题</span>
        </div>
          </template>
          <div class="json-content">
            <pre>{{ formatJson(selectedAnalysis.quizQuestions) }}</pre>
          </div>
        </el-card>

        <!-- 词汇翻译 -->
        <el-card v-if="selectedAnalysis.wordTranslations" class="detail-section" shadow="never">
          <template #header>
            <div class="section-header">
              <span>词汇翻译</span>
        </div>
          </template>
          <div class="json-content">
            <pre>{{ formatJson(selectedAnalysis.wordTranslations) }}</pre>
        </div>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAdminStore } from '@/stores/admin'
import { adminApi } from '@/api/admin/adminApi'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Warning, Document, ChatDotRound, InfoFilled } from '@element-plus/icons-vue'
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
const loading = ref(false)
const analysisList = ref([])
const pagination = ref({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const filters = ref({
  articleTitle: '',
  analysisType: '',
  idType: '',
  dateRange: []
})

const detailDialogVisible = ref(false)
const selectedAnalysis = ref<any>(null)
const hasLegacyData = ref(false)

// 获取AI分析结果列表
const fetchAnalysisList = async () => {
  try {
    loading.value = true

    const response = await adminApi.getAIAnalysisList({
      page: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      articleTitle: filters.value.articleTitle.trim() || undefined,
      analysisType: filters.value.analysisType || undefined,
      startDate: filters.value.dateRange?.[0] || undefined,
      endDate: filters.value.dateRange?.[1] || undefined
    })

    if (response && response.data) {
      let filteredList = response.data.analysisList || []

      // 前端数据类型筛选
      if (filters.value.idType) {
        if (filters.value.idType === 'real') {
          filteredList = filteredList.filter(item => item.analysisCategory === 'article')
        } else if (filters.value.idType === 'virtual') {
          filteredList = filteredList.filter(item => item.analysisCategory === 'sentence')
        }
      }

      analysisList.value = filteredList
      pagination.value.total = response.data.total || 0

      // 检测是否有历史数据
      hasLegacyData.value = filteredList.some(item => item.analysisCategory === 'sentence')
    } else {
      analysisList.value = []
      pagination.value.total = 0
    }
  } catch (error) {
    console.error('获取AI分析结果列表失败:', error)
    ElMessage.error('获取AI分析结果列表失败')
    analysisList.value = []
    pagination.value.total = 0
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.currentPage = 1
  fetchAnalysisList()
}

// 重置
const handleReset = () => {
  filters.value = {
    articleTitle: '',
    analysisType: '',
    idType: '',
    dateRange: []
  }
  pagination.value.currentPage = 1
  fetchAnalysisList()
}

// 分页大小改变
const handleSizeChange = (size: number) => {
  pagination.value.pageSize = size
  pagination.value.currentPage = 1
  fetchAnalysisList()
}

// 当前页改变
const handleCurrentChange = (page: number) => {
  pagination.value.currentPage = page
  fetchAnalysisList()
}

// 行点击
const handleRowClick = (row: any) => {
  handleViewDetail(row)
}

// 查看详情
const handleViewDetail = async (row: any) => {
  try {
    loading.value = true
    const response = await adminApi.getAIAnalysisDetail(row.id)
    if (response && response.data) {
      selectedAnalysis.value = response.data
    detailDialogVisible.value = true
    } else {
      ElMessage.error('获取分析详情失败')
    }
  } catch (error) {
    console.error('获取分析详情失败:', error)
    ElMessage.error('获取分析详情失败')
  } finally {
    loading.value = false
  }
}

// 获取分析类型标签类型
const getAnalysisTypeTagType = (type: string) => {
  if (!type) return 'info'

  const typeMap: Record<string, string> = {
    'summary': 'success',
    'parse': 'warning',
    'quiz': 'info',
    'tip': 'danger'
    // 移除comprehensive，因为用户端未使用
    // 'comprehensive': 'primary',
  }
  return typeMap[type] || 'info'
}

// 获取分析类型名称
const getAnalysisTypeName = (type: string) => {
  if (!type) return '未知类型'

  const nameMap: Record<string, string> = {
    'summary': '摘要分析',
    'parse': '句子解析',
    'quiz': '测验题',
    'tip': '学习建议'
    // 移除comprehensive，因为用户端未使用
    // 'comprehensive': '综合分析',
  }
  return nameMap[type] || '未知类型'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  if (!dateTime) return '-'
  const date = new Date(dateTime)
  return date.toLocaleString('zh-CN')
}

// 格式化JSON
const formatJson = (jsonString: string) => {
  try {
    const obj = JSON.parse(jsonString)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonString
  }
}

// 获取关键词数组
const getKeywordsArray = (keywords: string | string[]) => {
  if (!keywords) return []
  if (Array.isArray(keywords)) return keywords
  if (typeof keywords === 'string') {
    // 尝试按逗号分割
    return keywords.split(',').map(k => k.trim()).filter(k => k.length > 0)
  }
  return []
}

// 获取关键短语数组
const getKeyPhrasesArray = (keyPhrases: string | string[]) => {
  if (!keyPhrases) return []
  if (Array.isArray(keyPhrases)) return keyPhrases
  if (typeof keyPhrases === 'string') {
    // 尝试按逗号分割
    return keyPhrases.split(',').map(p => p.trim()).filter(p => p.length > 0)
  }
  return []
}

// 页面加载时获取数据
onMounted(() => {
  fetchAnalysisList()
})
</script>

<style scoped>
.ai-analysis-view {
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

.filter-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.analysis-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
}

.section-header {
  font-weight: 600;
  color: #303133;
}

.content-text {
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.keywords-container,
.phrases-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.keyword-tag,
.phrase-tag {
  margin: 2px;
}

.json-content {
  background: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.json-content pre {
  margin: 0;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.4;
  color: #606266;
}

@media (max-width: 768px) {
  .ai-analysis-view {
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

/* ID单元格样式 */
.id-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.id-value {
  font-family: 'Courier New', monospace;
  font-weight: 500;
}

/* 分析对象单元格样式 */
.analysis-object-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.object-tag {
  flex-shrink: 0;
  margin-top: 2px;
}

.object-content {
  flex: 1;
  min-width: 0;
}

.object-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.legacy-notice {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #f56c6c;
  background: #fef0f0;
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid #fbc4c4;
}

.legacy-tag {
  background: #fdf6ec;
  border-color: #f5dab1;
  color: #b88230;
}

/* 历史数据警告样式 */
.legacy-warning {
  background: linear-gradient(135deg, #fff7e6 0%, #fff2d9 100%);
  border: 1px solid #ffd591;
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 16px;
}
</style>
