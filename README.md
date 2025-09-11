# XReadUp - 智能英语学习平台

## 🎯 项目简介
XReadUp是一个基于Spring Cloud微服务架构的智能英语学习平台，提供文章阅读、AI智能分析、个性化学习等功能。

## 🏗️ 系统架构

### 微服务组件
- **Gateway (8080)**: 统一网关入口
- **User Service (8081)**: 用户管理服务
- **Article Service (8082)**: 文章管理服务
- **AI Service (8084)**: AI智能分析服务
- **Report Service (8083)**: 学习报告服务

### 基础设施
- **Nacos (8848)**: 服务注册与配置中心
- **MySQL (3307)**: 数据存储（Docker容器）
- **Redis (6379)**: 缓存服务

## 🚀 快速开始

### 前置条件
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Nacos 2.x

### 1. 启动基础设施
```bash
# 启动MySQL、Nacos、Redis
docker-compose up -d
```

### 2. 初始化数据库
```bash
# 执行数据库初始化脚本
mysql -u root -p < init.sql
```

### 3. 启动服务
```bash
# 启动各微服务（分别在对应目录执行）
mvn spring-boot:run
```

## 🧠 AI服务特性

### 智能分析功能
- **CEFR难度评估**: 自动评估文章难度等级(A1-C2)
- **关键词提取**: 智能提取文章核心词汇
- **中文翻译**: 高质量英文到中文翻译
- **内容摘要**: 自动生成中文摘要
- **简化内容**: 提供简化版英文内容
- **可读性评分**: 量化文章可读性

### 技术特性
- **DeepSeek AI集成**: 基于最新AI大模型
- **双语支持**: 中英文无缝切换
- **高性能**: 支持并发处理和缓存
- **可扩展**: 易于添加新的分析维度

### 📋 文章模块API接口详解

#### 🔍 AI服务接口 (端口: 8084)
| 接口 | 用途说明 | 适用场景 | Token消耗 |
|---|---|---|---|
| `POST /api/ai/analyze` | **全面分析** - 完整7项分析 | 短文章(&lt;800字) | 100% |
| `POST /api/ai/quick-analyze` | **快速分析** - 智能截断前400字 | 长文章快速预览 | 30% (节省70%) |
| `POST /api/ai/chunked-analyze` | **分段分析** - 分析前30%内容推断整体 | 超长文章(&gt;800字) | 35% (节省65%) |
| `POST /api/ai/translate` | **专项翻译** - 仅英文翻译中文 | 只需翻译功能 | 低 |
| `POST /api/ai/summary` | **专项摘要** - 仅生成中文摘要 | 只需摘要功能 | 低 |
| `POST /api/ai/keywords` | **专项关键词** - 仅提取关键词 | 只需关键词功能 | 低 |
| `GET /api/ai/health` | **健康检查** - 服务状态检测 | 系统监控 | 无 |

#### 📰 文章服务接口 (端口: 8082)
| 接口 | 用途说明 | 返回内容 | 是否增加阅读量 |
|---|---|---|---|
| `GET /api/article/list` | **文章列表** - 分页查询文章 | 文章基础信息列表 | 否 |
| `GET /api/article/detail/{id}` | **文章详情** - 获取单篇文章完整内容 | 文章完整信息 | **是** |
| `GET /api/article/detail/{id}/ai` | **智能详情** - 文章+AI分析结果 | 文章详情+AI分析 | **是** |
| `POST /api/article/{id}/analyze` | **触发分析** - 对文章进行AI分析 | 文章详情+新AI分析 | 否 |
| `POST /api/article/update-manual` | **难度标注** - 用户手动设置难度 | 更新结果 | 否 |

#### 🎯 使用场景选择指南

**场景1: 新文章首次分析**
- 文章&lt;800字 → `POST /api/ai/analyze` (全面分析)
- 文章800-2000字 → `POST /api/ai/quick-analyze` (快速预览)
- 文章&gt;2000字 → `POST /api/ai/chunked-analyze` (分段推断)

**场景2: 已分析文章查看**
- 直接访问 → `GET /api/article/detail/{id}/ai` (包含缓存的AI结果)

**场景3: 重新分析文章**
- 触发重新分析 → `POST /api/article/{id}/analyze` (强制重新调用AI)

**场景4: 专项功能需求**
- 仅翻译 → `POST /api/ai/translate`
- 仅摘要 → `POST /api/ai/summary`
- 仅关键词 → `POST /api/ai/keywords`

#### 📊 Token优化策略
- **快速分析**: 智能截断长文章，节省70% Token
- **分段分析**: 用前30%内容推断整体质量，节省65% Token
- **缓存机制**: 已分析文章直接返回结果，零Token消耗

### API调用示例

#### 1. 全面分析 (推荐用于新文章)
```bash
POST http://localhost:8084/api/ai/analyze
Content-Type: application/json

{
  "title": "The Future of AI in Education",
  "content": "Artificial intelligence is revolutionizing the way we learn...",
  "category": "education",
  "wordCount": 450
}
```

#### 2. 快速分析 (推荐用于长文章)
```bash
POST http://localhost:8084/api/ai/quick-analyze
Content-Type: application/json

{
  "title": "Comprehensive Guide to Machine Learning",
  "content": "Machine learning is a subset of artificial intelligence... [长文章内容]",
  "category": "technology",
  "wordCount": 2500
}
```

#### 3. 获取文章详情 (包含AI分析)
```bash
GET http://localhost:8082/api/article/detail/123/ai
```

#### 4. 专项翻译
```bash
POST http://localhost:8084/api/ai/translate
Content-Type: text/plain

Artificial intelligence is transforming modern education by providing personalized learning experiences.
```

## 🗄️ 数据库设计

### 核心数据表
- **users**: 用户信息
- **articles**: 文章信息
- **ai_analysis**: AI分析结果存储
- **user_articles**: 用户阅读记录
- **ai_cache**: AI缓存表（已弃用）

### AI分析表结构
```sql
CREATE TABLE ai_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    title VARCHAR(500),
    difficulty_level VARCHAR(10),
    keywords TEXT,
    summary TEXT,
    chinese_translation LONGTEXT,
    simplified_content LONGTEXT,
    key_phrases TEXT,
    readability_score DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## 🔧 配置说明

### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/readup_ai?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
  
  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: your_api_key
```

### Nacos配置
```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: localhost:8848
        namespace: public
        group: DEFAULT_GROUP
```

## 🧪 测试数据

### 插入测试数据
```bash
# 插入完整测试数据
mysql -u root -p < insert_test_data_complete.sql

# 或插入清理后的测试数据
mysql -u root -p < insert_test_data_clear.sql
```

## 📊 系统监控

### 健康检查
- **服务状态**: http://localhost:8848/nacos
- **API文档**: 各服务启动后访问 /swagger-ui.html

### 日志查看
```bash
# 查看服务日志
tail -f logs/user-service.log
```

## 🔄 缓存表处理建议

### 关于ai_cache表
原有的`ai_cache`表是为通用AI缓存设计的，但当前的AI服务采用了更专业的`ai_analysis`表结构。

#### 处理建议
**推荐：保留ai_cache表**
- ✅ 不影响现有功能
- ✅ 未来可能用于其他AI场景
- ✅ 数据量小，不占用显著空间
- ✅ 避免潜在的依赖问题

**如确需删除**：
```sql
-- 确认无数据后删除
SELECT COUNT(*) FROM ai_cache;
DROP TABLE IF EXISTS ai_cache;
```

## 🤝 贡献指南

1. Fork项目
2. 创建特性分支
3. 提交代码
4. 创建Pull Request

## 📄 许可证

MIT License - 详见LICENSE文件

## 📞 联系方式

如有问题，请提交Issue或联系维护团队。