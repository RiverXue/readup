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

### API端点
```bash
# 健康检查
GET http://localhost:8084/api/ai/health

# 文章全面分析
POST http://localhost:8084/api/ai/analyze
Content-Type: application/json

{
  "title": "文章标题",
  "content": "文章内容",
  "category": "科技",
  "wordCount": 500
}

# 英文翻译中文
POST http://localhost:8084/api/ai/translate
Content-Type: text/plain

# 生成摘要
POST http://localhost:8084/api/ai/summary
Content-Type: text/plain

# 提取关键词
POST http://localhost:8084/api/ai/keywords
Content-Type: text/plain
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