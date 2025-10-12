<template>
  <div class="system-settings">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon><Setting /></el-icon>
          系统设置
        </h1>
        <p class="page-description">管理系统功能开关和业务限制</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleSaveAll" :loading="saving">
          <el-icon><Check /></el-icon>
          保存所有设置
        </el-button>
        <el-button @click="handleRefresh">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <!-- 配置分类标签页 -->
    <el-card class="config-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h3>
            <el-icon><Tools /></el-icon>
            配置管理
          </h3>
        </div>
      </template>
      
      <el-tabs v-model="activeCategory" @tab-change="handleCategoryChange" class="config-tabs">
        <el-tab-pane 
          v-for="category in categories" 
          :key="category.value" 
          :label="category.label" 
          :name="category.value"
        >
          <div class="tab-content">
            <div v-if="loading" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>
            
            <div v-else-if="configsByCategory.length === 0" class="empty-container">
              <el-empty description="暂无配置项" />
            </div>
            
            <div v-else class="config-list">
            <el-form
                ref="configFormRef"
                :model="configForm"
                label-width="200px"
                class="config-form"
              >
                <el-form-item
                  v-for="config in configsByCategory"
                  :key="config.configKey"
                  :label="config.description"
                  :prop="config.configKey"
                >
                  <!-- 字符串类型 -->
                <el-input
                    v-if="config.configType === 'STRING'"
                    v-model="configForm[config.configKey]"
                    :placeholder="`请输入${config.description}`"
                  clearable
                    @blur="handleConfigChange(config.configKey, configForm[config.configKey])"
                  />
                  
                  <!-- 数字类型 -->
                <el-input-number
                    v-else-if="config.configType === 'NUMBER'"
                    v-model="configForm[config.configKey]"
                    :min="getNumberMin(config.configKey)"
                    :max="getNumberMax(config.configKey)"
                    :step="getNumberStep(config.configKey)"
                    @change="handleConfigChange(config.configKey, configForm[config.configKey])"
                  />
                  
                  <!-- 布尔类型 -->
                  <el-switch
                    v-else-if="config.configType === 'BOOLEAN'"
                    v-model="configForm[config.configKey]"
                    @change="handleConfigChange(config.configKey, configForm[config.configKey])"
                  />
                  
                  <!-- 配置操作 -->
                  <div class="config-actions">
                    <el-button
                      v-if="!config.isSystem"
                      type="danger"
                      text
                      size="small"
                      @click="handleResetConfig(config.configKey)"
                    >
                      重置
                    </el-button>
          </div>
              </el-form-item>
            </el-form>
          </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, Check, Refresh, Tools } from '@element-plus/icons-vue'
import { adminApi } from '@/api/admin/adminApi'

// 响应式数据
const loading = ref(false)
const saving = ref(false)
const activeCategory = ref('MAINTENANCE')
const configsByCategory = ref<any[]>([])
const configForm = reactive<Record<string, any>>({})

// 配置分类 - 基于新的22个功能开关设计
const categories = ref([
  { label: '系统维护', value: 'MAINTENANCE' },
  { label: 'AI功能开关', value: 'AI_FEATURES' },
  { label: '用户功能开关', value: 'USER_FEATURES' },
  { label: '词汇功能开关', value: 'VOCABULARY_FEATURES' },
  { label: '订阅功能开关', value: 'SUBSCRIPTION_FEATURES' },
  { label: '阅读功能开关', value: 'READING_FEATURES' },
  { label: '文章功能开关', value: 'ARTICLE_FEATURES' },
  { label: '系统功能开关', value: 'SYSTEM_FEATURES' },
  { label: '业务限制', value: 'LIMITS' }
])

// 表单引用
const configFormRef = ref()

// 获取配置项的中文描述
const getConfigDescription = (configKey: string): string => {
  const descriptions: Record<string, string> = {
    // AI功能开关
    'features.ai_article_analysis': 'AI文章分析功能',
    'features.ai_sentence_parsing': 'AI句子解析功能',
    'features.ai_quiz_generation': 'AI测验生成功能',
    'features.ai_summary_generation': 'AI摘要生成功能',
    'features.ai_word_translation': 'AI单词翻译功能',
    
    // 用户功能开关
    'features.user_registration': '用户注册功能',
    'features.user_login': '用户登录功能',
    'features.user_profile': '用户资料管理',
    
    // 词汇功能开关
    'features.vocabulary_lookup': '词汇查询功能',
    'features.vocabulary_review': '词汇复习功能',
    'features.vocabulary_add': '添加词汇功能',
    
    // 订阅功能开关
    'features.subscription_create': '创建订阅功能',
    'features.subscription_manage': '订阅管理功能',
    'features.subscription_payment': '订阅支付功能',
    
    // 阅读功能开关
    'features.reading_streak': '阅读连续天数',
    'features.reading_progress': '阅读进度跟踪',
    'features.reading_statistics': '阅读统计功能',
    
    // 文章功能开关
    'features.article_upload': '文章上传功能',
    'features.article_management': '文章管理功能',
    'features.article_sharing': '文章分享功能',
    
    // 系统功能开关
    'features.admin_panel': '管理员面板',
    'features.system_monitoring': '系统监控功能',
    'features.api_documentation': 'API文档功能',
    
    // 维护模式配置
    'maintenance.enabled': '系统维护模式',
    'maintenance.message': '维护提示信息',
    
    // 业务限制配置
    'limits.max_articles_per_user': '用户最大文章数',
    'limits.max_words_per_article': '单篇文章最大字数',
    'limits.max_vocabulary_per_user': '用户最大词汇量'
  }
  
  return descriptions[configKey] || configKey
}

// 方法
const loadConfigsByCategory = async (category: string) => {
  try {
    loading.value = true
    console.log('加载配置分类:', category)
    
    // 根据前端分类映射到后端分类
    let backendCategory = category
    if (category === 'AI_FEATURES' || category === 'USER_FEATURES' || category === 'VOCABULARY_FEATURES' || 
        category === 'SUBSCRIPTION_FEATURES' || category === 'READING_FEATURES' || category === 'ARTICLE_FEATURES' || 
        category === 'SYSTEM_FEATURES') {
      backendCategory = 'FEATURES'
    }
    
    const response = await adminApi.getSystemConfigsByCategory(backendCategory)
    console.log('API响应:', response)
    
    if (response.success) {
      let configs = response.data || []
      
      // 根据前端分类过滤配置项
      if (category === 'AI_FEATURES') {
        configs = configs.filter(config => config.configKey.startsWith('features.ai_'))
      } else if (category === 'USER_FEATURES') {
        configs = configs.filter(config => config.configKey.startsWith('features.user_'))
      } else if (category === 'VOCABULARY_FEATURES') {
        configs = configs.filter(config => config.configKey.startsWith('features.vocabulary_'))
      } else if (category === 'SUBSCRIPTION_FEATURES') {
        configs = configs.filter(config => config.configKey.startsWith('features.subscription_'))
      } else if (category === 'READING_FEATURES') {
        configs = configs.filter(config => config.configKey.startsWith('features.reading_'))
      } else if (category === 'ARTICLE_FEATURES') {
        configs = configs.filter(config => config.configKey.startsWith('features.article_'))
      } else if (category === 'SYSTEM_FEATURES') {
        configs = configs.filter(config => 
          config.configKey.startsWith('features.admin_') || 
          config.configKey.startsWith('features.system_') ||
          config.configKey.startsWith('features.api_')
        )
      }
      
      configsByCategory.value = configs
      console.log('配置数据:', configsByCategory.value)
      
      // 初始化表单数据
      configsByCategory.value.forEach(config => {
        // 如果描述包含问号，使用预设的中文描述
        if (config.description && config.description.includes('?')) {
          config.description = getConfigDescription(config.configKey)
        }
        
        if (config.configType === 'BOOLEAN') {
          configForm[config.configKey] = config.configValue === 'true'
        } else if (config.configType === 'NUMBER') {
          configForm[config.configKey] = Number(config.configValue)
        } else {
          configForm[config.configKey] = config.configValue
        }
      })
      
      console.log('表单数据:', configForm)
    } else {
      console.error('加载配置失败:', response.message)
      ElMessage.error('加载配置失败')
    }
  } catch (error) {
    console.error('加载配置异常:', error)
    ElMessage.error('加载配置失败，请重试')
  } finally {
    loading.value = false
  }
}

// 处理分类切换
const handleCategoryChange = (category: string) => {
  activeCategory.value = category
  loadConfigsByCategory(category)
}

// 处理配置变更
const handleConfigChange = async (configKey: string, value: any) => {
  try {
    console.log('配置变更:', configKey, value)
    
    // 防抖处理
    setTimeout(async () => {
      const response = await adminApi.updateSystemConfig(configKey, String(value))
      if (response.success) {
        ElMessage.success('配置更新成功')
      } else {
        ElMessage.error('配置更新失败')
      }
    }, 1000)
  } catch (error) {
    console.error('配置更新失败:', error)
    ElMessage.error('配置更新失败，请重试')
  }
}

// 重置配置
const handleResetConfig = async (configKey: string) => {
  try {
    const confirmResult = await ElMessageBox.confirm(
      '确定要重置此配置项吗？',
      '重置确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    if (confirmResult === 'confirm') {
      const response = await adminApi.resetSystemConfig(configKey)
      if (response.success) {
        ElMessage.success('配置重置成功')
        loadConfigsByCategory(activeCategory.value)
      } else {
        ElMessage.error('配置重置失败')
      }
    }
  } catch (error) {
    console.error('配置重置失败:', error)
    ElMessage.error('配置重置失败，请重试')
  }
}

// 保存所有设置
const handleSaveAll = async () => {
  try {
    saving.value = true
    
    const configsToUpdate: Record<string, string> = {}
    configsByCategory.value.forEach(config => {
      configsToUpdate[config.configKey] = String(configForm[config.configKey])
    })
    
    const response = await adminApi.batchUpdateSystemConfigs(configsToUpdate)
    if (response.success) {
      ElMessage.success('所有设置保存成功')
    } else {
      ElMessage.error('设置保存失败')
    }
  } catch (error) {
    console.error('保存设置失败:', error)
    ElMessage.error('保存设置失败，请重试')
  } finally {
    saving.value = false
  }
}

// 刷新页面
const handleRefresh = () => {
  loadConfigsByCategory(activeCategory.value)
}

// 获取数字输入框的最小值
const getNumberMin = (configKey: string) => {
  if (configKey.includes('max_')) return 1
  if (configKey.includes('min_')) return 0
  return 0
}

// 获取数字输入框的最大值
const getNumberMax = (configKey: string) => {
  if (configKey.includes('max_articles')) return 10000
  if (configKey.includes('max_words')) return 100000
  if (configKey.includes('max_vocabulary')) return 50000
  return 10000
}

// 获取数字输入框的步长
const getNumberStep = (configKey: string) => {
  if (configKey.includes('max_articles')) return 100
  if (configKey.includes('max_words')) return 1000
  if (configKey.includes('max_vocabulary')) return 500
  return 1
}

onMounted(() => {
  loadConfigsByCategory(activeCategory.value)
})
</script>

<style scoped>
.system-settings {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.page-description {
  margin: 0;
  color: #606266;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.config-card {
  margin-bottom: 20px;
}

.card-header h3 {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.config-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 20px 0;
}

.loading-container {
  padding: 40px;
}

.empty-container {
  padding: 60px;
  text-align: center;
}

.config-form {
  max-width: 800px;
}

.config-form .el-form-item {
  margin-bottom: 24px;
}

.config-form .el-form-item__label {
  font-weight: 500;
  color: #303133;
}

.config-actions {
  margin-left: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
  
  .header-actions {
    width: 100%;
    justify-content: center;
  }
  
  .config-form {
    max-width: 100%;
  }
}
</style>