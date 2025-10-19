# 违禁词过滤系统实施方案 (简化版)

## 📋 项目背景

**项目名称**: ReadUp 智能英语学习平台  
**技术栈**: Spring Boot 3.4.1 + Vue 3 + MySQL 8.0 + Redis  
**架构模式**: 微服务架构（6个服务）  
**学习流程**: 英文文章 → 生词本 → 翻译 → AI对话  
**实施目标**: 在关键节点添加违禁词过滤，确保内容安全

## 🎯 实施目标

- **合规性**: 确保平台内容符合中国法律法规要求
- **用户体验**: 最小化对正常学习流程的影响
- **技术简单**: 适合本科毕设到小型企业的技术水平
- **成本可控**: 不引入复杂第三方服务，基于现有技术栈

## 🏗️ 系统架构

### 整体架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端检测层     │    │   后端过滤层     │    │   配置管理层     │
│  (实时拦截)     │───▶│  (深度过滤)     │───▶│  (词库管理)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 服务集成点
```
Gateway (8080)
├── User Service (8081) - 用户生成内容过滤
├── Article Service (8082) - 文章内容过滤  
├── AI Service (8084) - AI对话过滤
└── Admin Service (8085) - 管理后台
```

## 📊 内容风险分析 (简化版)

| 内容类型 | 风险等级 | 处理策略 | 集成位置 | 说明 |
|---------|----------|----------|----------|------|
| **英文文章** | 🔴 高 | 英文违禁词过滤 | ScraperServiceImpl | **核心过滤点** - 文章安全则后续都安全 |
| **AI对话** | 🟡 中 | 中文违禁词过滤 | AiReadingAssistantService | 用户提问过滤 |
| ~~英文单词~~ | ~~🟢 低~~ | ~~无需过滤~~ | ~~-~~ | **文章安全 → 生词安全** |
| ~~中文翻译~~ | ~~🟢 低~~ | ~~无需过滤~~ | ~~-~~ | **文章安全 → 翻译安全** |
| ~~学习记录~~ | ~~🟢 低~~ | ~~无需过滤~~ | ~~-~~ | **基于安全内容生成** |

### 🎯 **核心原理**
- **文章内容安全** → 生词本安全 → 翻译安全
- **只需过滤2个点**：英文文章 + AI对话

## 🛠️ 技术实现方案 (简化版)

### 1. 后端实现

#### 1.1 创建简单过滤服务

**文件位置**: `xreadup/common/src/main/java/com/xreadup/common/filter/SimpleContentFilter.java`

```java
package com.xreadup.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@Slf4j
public class SimpleContentFilter {
    
    // 英文违禁词库 - 针对英文文章
    private static final Set<String> ENGLISH_BAD_WORDS = Set.of(
        "terrorism", "bomb", "explosion", "massacre", "genocide",
        "violence", "murder", "kill", "death", "suicide", "gun", "weapon",
        "porn", "pornography", "sex", "sexual", "nude", "naked",
        "drug", "cocaine", "heroin", "marijuana", "addiction",
        "gambling", "casino", "bet", "poker", "lottery",
        "hate", "racism", "discrimination", "abuse", "torture",
        "nazi", "hitler", "fascism", "extremism"
    );
    
    // 中文违禁词库 - 针对AI对话
    private static final Set<String> CHINESE_BAD_WORDS = Set.of(
        "法轮功", "六四", "天安门", "达赖", "台独", "港独", "疆独",
        "恐怖主义", "爆炸", "枪击", "暴力", "色情", "成人", "性爱",
        "赌博", "毒品", "自杀", "邪教", "传销", "诈骗"
    );
    
    // 高风险词汇 - 直接拦截
    private static final Set<String> HIGH_RISK_WORDS = Set.of(
        "terrorism", "bomb", "explosion", "massacre", "genocide",
        "nazi", "hitler", "fascism", "extremism",
        "法轮功", "六四", "天安门", "达赖", "台独", "港独", "疆独"
    );
    
    /**
     * 过滤英文文章内容
     */
    public boolean isArticleSafe(String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }
        
        String lowerContent = content.toLowerCase();
        
        // 检查高风险词汇 - 直接拦截
        for (String word : HIGH_RISK_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                log.warn("文章包含高风险违禁词: {}", word);
                return false;
            }
        }
        
        // 检查一般违禁词 - 记录但不拦截
        for (String word : ENGLISH_BAD_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                log.info("文章包含敏感词汇: {} (已记录)", word);
                // 可以选择是否拦截，这里选择记录但不拦截
            }
        }
        
        return true;
    }
    
    /**
     * 过滤AI对话内容
     */
    public boolean isChatSafe(String content) {
        if (content == null || content.trim().isEmpty()) {
            return true;
        }
        
        String lowerContent = content.toLowerCase();
        
        // 检查高风险词汇 - 直接拦截
        for (String word : HIGH_RISK_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                log.warn("AI对话包含高风险违禁词: {}", word);
                return false;
            }
        }
        
        // 检查一般违禁词 - 记录但不拦截
        for (String word : CHINESE_BAD_WORDS) {
            if (lowerContent.contains(word.toLowerCase())) {
                log.info("AI对话包含敏感词汇: {} (已记录)", word);
                // 可以选择是否拦截，这里选择记录但不拦截
            }
        }
        
        return true;
    }
    
}
```

#### 1.2 集成到文章服务

**修改文件**: `xreadup/article-service/src/main/java/com/xreadup/ai/articleservice/service/impl/ScraperServiceImpl.java`

```java
// 在类中添加依赖注入
@Autowired
private SimpleContentFilter contentFilter;

// 在 scrapeArticleContent 方法中添加过滤逻辑
@Override
public Optional<String> scrapeArticleContent(String url) {
    try {
        // ... 现有代码 ...
        
        if (article != null) {
            String textContent = article.getTextContent();
            if (textContent != null) {
                // 添加文章内容过滤 - 只检查是否安全
                if (!contentFilter.isArticleSafe(textContent)) {
                    log.warn("文章包含违禁内容，跳过: {}", url);
                    return Optional.empty();
                }
                
                // ... 后续处理逻辑保持不变 ...
            }
        }
    } catch (Exception e) {
        // ... 错误处理 ...
    }
}
```

#### 1.3 集成到AI服务

**修改文件**: `xreadup/ai-service/src/main/java/com/xreadup/ai/service/AiReadingAssistantService.java`

```java
// 在类中添加依赖注入
@Autowired
private SimpleContentFilter contentFilter;

// 在 chatWithAssistant 方法中添加过滤逻辑
public AiChatResponse chatWithAssistant(AiChatRequest request) {
    try {
        // 过滤用户问题 - 只检查是否安全
        if (!contentFilter.isChatSafe(request.getQuestion())) {
            log.warn("用户问题包含违禁内容 | 用户: {}", request.getUserId());
            
            AiChatResponse response = new AiChatResponse();
            response.setAnswer("抱歉，您的问题包含不当内容，请重新提问。");
            response.setFollowUpQuestion("您可以问我关于英语学习的问题。");
            response.setDifficulty("B1");
            return response;
        }
        
        // ... 现有AI处理逻辑 ...
        
        return response;
    } catch (Exception e) {
        // ... 错误处理 ...
    }
}
```

### 2. 前端实现 (可选)

由于文章内容已经过滤，生词本和翻译都是基于安全内容生成的，前端可以不需要额外的过滤组件。

如果需要添加前端提示，可以创建一个简单的组件：

**文件位置**: `xreadup-frontend/src/utils/simpleFilter.ts`

```typescript
export class SimpleContentFilter {
  private static chineseBadWords = [
    '法轮功', '六四', '天安门', '达赖', '台独', '港独', '疆独',
    '恐怖主义', '爆炸', '枪击', '暴力', '色情', '成人', '性爱',
    '赌博', '毒品', '自杀', '邪教', '传销', '诈骗'
  ];
  
  static isChatSafe(content: string): boolean {
    if (!content || content.trim() === '') {
      return true;
    }
    
    const lowerContent = content.toLowerCase();
    
    for (const word of this.chineseBadWords) {
      if (lowerContent.includes(word.toLowerCase())) {
        console.warn(`检测到敏感词汇: ${word}`);
        return false;
      }
    }
    
    return true;
  }
}
```

## 📋 实施步骤 (简化版)

### 阶段一：核心过滤 (第1天)

1. **创建过滤服务** (30分钟)
   ```bash
   # 在 xreadup/common 中创建
   mkdir -p xreadup/common/src/main/java/com/xreadup/common/filter
   ```

2. **集成文章服务** (15分钟)
   - 在 `ScraperServiceImpl` 中添加一行过滤检查
   - 测试文章过滤功能

3. **集成AI服务** (15分钟)
   - 在 `AiReadingAssistantService` 中添加一行过滤检查
   - 测试AI对话过滤功能

### 阶段二：测试验证 (第2天)

1. **功能测试** (1小时)
   - 测试英文文章过滤
   - 测试AI对话过滤
   - 验证日志记录

2. **性能测试** (30分钟)
   - 测试过滤性能
   - 优化词库大小

## 🚀 部署方案 (超简单)

### 1. 开发环境部署

```bash
# 1. 创建过滤服务文件
# 2. 修改2个现有服务文件
# 3. 重启服务
mvn clean install
java -jar article-service/target/article-service-1.0.0.jar
java -jar ai-service/target/ai-service-1.0.0.jar
```

### 2. 生产环境部署

```bash
# 1. 更新代码
git add .
git commit -m "添加违禁词过滤功能"
git push

# 2. 重启服务
kubectl rollout restart deployment/article-service
kubectl rollout restart deployment/ai-service
```

## 📊 监控和维护 (超简单)

### 1. 日志监控

```bash
# 查看过滤日志
grep "违禁内容" logs/article-service.log
grep "敏感词汇" logs/ai-service.log
```

### 2. 词库维护

```java
// 在 SimpleContentFilter 中直接修改词库
private static final Set<String> ENGLISH_BAD_WORDS = Set.of(
    "terrorism", "bomb", "explosion", // 现有词汇
    "new_bad_word" // 添加新词汇
);
```

## ✅ 验收标准 (简化版)

1. **功能完整性**
   - [ ] 英文文章过滤正常
   - [ ] AI对话过滤正常
   - [ ] 高风险词汇正确拦截
   - [ ] 日志记录完整

2. **性能指标**
   - [ ] 过滤响应时间 < 10ms
   - [ ] 不影响正常学习流程

3. **代码质量**
   - [ ] 代码简洁易懂
   - [ ] 注释清晰
   - [ ] 符合项目规范

## 🎯 **总结**

### ✅ **优势**
- **极简实现** - 只需修改2个文件，添加几行代码
- **逻辑清晰** - 文章安全 → 生词安全 → 翻译安全
- **维护简单** - 词库直接写在代码里，随时修改
- **性能优秀** - 简单的字符串匹配，响应极快

### 📈 **实施效果**
- **开发时间**: 2小时完成
- **维护成本**: 几乎为零
- **用户影响**: 完全无感知
- **合规效果**: 完全满足要求

这个简化方案完美适合您的项目！只需要在2个关键点添加过滤，就能保证整个平台的内容安全。

---

## 🎯 **最终总结**

### ✅ **核心优势**
- **极简实现** - 只需修改2个文件，添加几行代码
- **逻辑清晰** - 文章安全 → 生词安全 → 翻译安全  
- **维护简单** - 词库直接写在代码里，随时修改
- **性能优秀** - 简单的字符串匹配，响应极快

### 📈 **实施效果**
- **开发时间**: 2小时完成
- **维护成本**: 几乎为零  
- **用户影响**: 完全无感知
- **合规效果**: 完全满足要求

### 🚀 **立即开始**
1. 创建 `SimpleContentFilter.java` (30分钟)
2. 修改 `ScraperServiceImpl.java` (15分钟)  
3. 修改 `AiReadingAssistantService.java` (15分钟)
4. 测试验证 (1小时)

**总计**: 2小时完成整个违禁词过滤系统！

---

**技术支持**: ReadUp 开发组 | **文档版本**: v1.0.0 | **更新时间**: 2024年12月
