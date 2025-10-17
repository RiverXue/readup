# 后端AI API清理方案

## 📊 清理分析

### 前端实际使用的后端接口
1. ✅ `/api/ai/translate/smart` - 智能翻译（付费用户）
2. ✅ `/api/ai/tencent-translate/en-to-zh` - 基础翻译（免费用户）
3. ✅ `/api/ai/summary` - AI摘要（DeepSeekController）
4. ✅ `/api/ai/parse` - 句子解析（DeepSeekController）
5. ✅ `/api/ai/assistant/chat` - AI对话（AiReadingAssistantController）
6. ✅ `/api/ai/assistant/quiz` - 生成测验（AiReadingAssistantController）

### 后端控制器分析

#### 1. DeepSeekController.java ✅ **保留**
- `/api/ai/summary` - 使用中
- `/api/ai/parse` - 使用中
- `/api/ai/quiz` - 未使用（前端用assistantGenerateQuiz）
- `/api/ai/tip` - 未使用
- `/api/ai/comprehensive` - 未使用

**建议：** 删除未使用的3个接口

#### 2. AiReadingAssistantController.java ✅ **保留**
- `/api/ai/assistant/chat` - 使用中
- `/api/ai/assistant/quiz` - 使用中
- `/api/ai/assistant/word/{word}` - 未使用
- `/api/ai/assistant/translate` - 未使用

**建议：** 删除未使用的2个接口

#### 3. TencentTranslateController.java ⚠️ **部分保留**
- `/api/ai/tencent-translate/en-to-zh` - 使用中
- `/api/ai/tencent-translate/text` - 未使用
- `/api/ai/tencent-translate/zh-to-en` - 未使用
- `/api/ai/tencent-translate/batch` - 未使用

**建议：** 仅保留 en-to-zh 接口

#### 4. UnifiedTranslateController.java ⚠️ **部分保留**
- `/api/ai/translate/smart` - 使用中
- `/api/ai/translate/batch` - 未使用

**建议：** 仅保留 smart 接口

#### 5. AiController.java ❌ **可删除**
- `/api/ai/analyze` - 未使用
- `/api/ai/analyze/batch` - 未使用
- `/api/ai/translate/fulltext` - 未使用
- `/api/ai/translate/word` - 未使用
- `/api/ai/analysis` - 管理后台使用
- `/api/ai/analysis/{id}` - 管理后台使用
- `/api/ai/health` - 健康检查

**建议：** 保留管理后台和健康检查接口，删除其他

#### 6. UnifiedAiAnalysisController.java ❌ **完全删除**
- `/api/ai/smart/analyze` - 未使用
- `/api/ai/smart/assistant` - 未使用

**建议：** 整个控制器可以删除

## 🎯 清理计划

### 步骤1：清理DeepSeekController
- 删除 `/api/ai/quiz` 方法
- 删除 `/api/ai/tip` 方法
- 删除 `/api/ai/comprehensive` 方法

### 步骤2：清理AiReadingAssistantController
- 删除 lookupWord 方法
- 删除 translate 方法

### 步骤3：清理TencentTranslateController
- 删除 translateText 方法
- 删除 translateZhToEn 方法
- 删除 translateBatch 方法

### 步骤4：清理UnifiedTranslateController
- 删除 batchTranslate 方法

### 步骤5：清理AiController
- 删除 analyzeArticle 方法
- 删除 batchAnalyzeArticles 方法
- 删除 translateFullText 方法
- 删除 translateWord 方法

### 步骤6：删除UnifiedAiAnalysisController
- 整个文件可以删除

## 💰 预期收益

- 减少约70%的未使用API代码
- 简化后端维护
- 降低误用高成本API的风险
- 提高代码可读性和可维护性

## ⚠️ 注意事项

- 保留所有前端正在使用的接口
- 保留管理后台使用的接口
- 保留健康检查接口
- 删除所有未使用的高成本AI接口

