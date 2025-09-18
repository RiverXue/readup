# XReadUp 后端微服务架构

<div align="center">
  <h2>基于 Spring Cloud 的 AI 智能英语学习平台后端服务</h2>
  <p>微服务架构 | 二级词库策略 | Function Calling | 高性能缓存</p>
</div>

## 🎯 项目概览

XReadUp 后端是一个基于 Spring Cloud 微服务架构的智能英语学习平台，集成了最新的**二级词库策略**和**Function Calling**功能，为用户提供个性化、智能化的英语学习体验。

## 🏗️ 系统架构

### 微服务架构

| 服务 | 端口 | 核心功能 | 状态 |
|---|---|---|---|
| **Gateway** | 8080 | API 网关、路由转发、Knife4j 文档聚合、权限控制 | ✅ 运行中 |
| **User Service** | 8081 | 用户管理、认证授权、二级词库策略 | ✅ 运行中 |
| **Article Service** | 8082 | 文章管理、内容抓取、智能推荐 | ✅ 运行中 |
| **Report Service** | 8083 | 学习报告、数据分析、统计生成 | ✅ 运行中 |
| **AI Service** | 8084 | Function Calling、智能对话、NLP 处理 | ✅ 运行中 |

### 基础设施

- **Nacos**: 8848 (配置中心 + 服务注册发现)
- **MySQL**: 3306/3307 (主从分离)
- **Redis**: 6379 (缓存 + 会话存储)
- **Docker**: 容器化部署

## 🚀 核心技术栈

- **基础框架**: Spring Boot 3.x
- **微服务框架**: Spring Cloud 2023.x
- **服务治理**: Spring Cloud Alibaba (Nacos)
- **API 网关**: Spring Cloud Gateway
- **ORM 框架**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.0
- **容器化**: Docker + Docker Compose
- **文档聚合**: Knife4j (基于 Swagger)
- **AI 集成**: 自定义 Function Calling 实现
- **消息队列**: (计划中)

## 🆕 核心功能升级

### 🧠 三级词库策略 (V3.0)

```
智能查询策略：本地缓存优先 + 用户共享词库 + AI 兜底生成
├── 一级策略：当前用户词库查询（精确匹配）
├── 二级策略：其他用户共享词库（共享复用）
├── 三级策略：AI 实时生成（异步缓存）
└── 性能提升：响应时间从 500ms 降至 10ms
```

**核心实现:**
- 本地词库与用户历史查询智能匹配
- AI 生成结果自动缓存优化
- 高频词汇预加载机制
- 分布式缓存一致性保证
- 多用户单词共享功能（userIds列表存储）
- 逻辑删除机制（从共享列表移除而非物理删除）

**主要端点:**
- `POST /api/vocabulary/lookup` - 智能单词查询
- `POST /api/vocabulary/batch-lookup` - 批量查询
- `GET /api/vocabulary/stats/{userId}` - 学习统计
- `POST /api/vocabulary/cleanup/{userId}` - 重复清理

### 🤖 Function Calling 功能

**AI 工具集成:**
- `WordLookupTool` - 智能单词解析
- `TranslationTool` - 多语言翻译
- `QuizGenerator` - 学习测验生成
- `ArticleAnalyzer` - 文章情感分析

**主要端点:**
- `GET /api/ai/assistant/word/{word}` - 单词详情
- `POST /api/ai/assistant/chat` - 智能对话
- `POST /api/ai/assistant/translate` - 文本翻译
- `POST /api/ai/assistant/quiz` - 生成测验

## 📂 项目结构

```
xreadup/
├── gateway/              # API 网关服务
├── user-service/         # 用户服务
├── article-service/      # 文章服务
├── report-service/       # 报告服务
├── ai-service/           # AI 服务
├── docker-compose.yml    # Docker 编排文件
├── pom.xml               # 父项目 Maven 配置
└── init.sql              # 数据库初始化脚本
```

### 各服务核心模块

**1. Gateway (网关服务)**
- 路由配置与负载均衡
- 统一认证授权
- API 文档聚合
- 请求限流与熔断

**2. User Service (用户服务)**
- 用户认证与授权
- 二级词库策略实现
- 词汇管理与智能查询
- 用户偏好设置

**3. Article Service (文章服务)**
- 文章内容管理
- GNews API 集成
- 文章内容抓取与解析
- 个性化推荐算法

**4. Report Service (报告服务)**
- 学习数据统计分析
- 可视化报告生成
- 学习进度跟踪
- 成就系统

**5. AI Service (AI 服务)**
- Function Calling 机制实现
- 智能对话管理
- NLP 文本处理
- 外部 AI 服务集成

## 💻 开发环境配置

### 前置要求
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+ (前端开发)
- Docker 20.10+ (容器化部署)

### 一键启动 (推荐)

```bash
# 启动所有基础设施 (Nacos、MySQL、Redis)
docker-compose up -d

# 等待基础设施完全启动 (约 30 秒)

# 一键启动所有服务
./test-all-services.ps1

# 验证服务状态
./test-all-services.ps1
```

### 手动启动

```bash
# 在不同终端分别启动各服务

# 1. 启动网关服务
cd gateway && mvn spring-boot:run

# 2. 启动用户服务（包含二级词库）
cd user-service && mvn spring-boot:run

# 3. 启动文章服务
cd article-service && mvn spring-boot:run

# 4. 启动报告服务
cd report-service && mvn spring-boot:run

# 5. 启动 AI 服务（包含 Function Calling）
cd ai-service && mvn spring-boot:run
```

## 🔧 服务配置与部署

### Nacos 配置管理

所有服务的配置文件存储在 `nacos-configs/` 目录下，包含：
- `gateway-dev.yml`
- `user-service-dev.yml`
- `article-service-dev.yml`
- `report-service-dev.yml`
- `ai-service-dev.yml`
- `shared-mysql-dev.yml`
- `shared-redis-dev.yml`

配置上传脚本：
```bash
./upload-all-nacos-configs.ps1
```

### 数据库初始化

运行初始化脚本创建必要的数据库和表：
```bash
mysql -u root -p < init.sql
```

### Docker 部署

完整的 Docker 部署配置在 `docker-compose.yml` 文件中，支持一键部署整个系统。

## 📊 API 文档

项目使用 Knife4j 聚合所有微服务的 API 文档：

- **统一文档入口**: http://localhost:8080/doc.html
- **单服务文档**: http://localhost:{服务端口}/doc.html

## 🔍 功能特性对比

| 功能模块 | v1.0 | v2.0 升级 |
|---|---|---|
| **词汇查询** | 简单查询 | 二级智能策略 |
| **响应时间** | 500ms | 10ms |
| **缓存策略** | 无 | 本地+AI 双层缓存 |
| **AI 集成** | 基础 API | Function Calling |
| **上下文理解** | 无 | 智能上下文匹配 |
| **批量处理** | 无 | 支持批量查询 |
| **扩展性** | 单一服务 | 微服务架构 |
| **文档系统** | 分散文档 | Knife4j 聚合文档 |

#### 🚀 核心功能升级

### 多用户共享词库 (V3.0)

**全新特性:**
- ✅ **单词共享机制**: 支持多个用户共享同一个单词记录
- ✅ **逻辑删除**: 从用户共享列表移除而非物理删除单词
- ✅ **权限控制**: 确保用户只能访问和管理自己共享的单词
- ✅ **优化统计**: 基于用户ID集合的智能单词统计

**技术实现:**
- 数据库模型升级：将单一userId字段扩展为userIds列表
- 服务层逻辑重构：支持多用户权限验证和共享管理
- 性能优化：保持10ms级响应时间的同时支持多用户场景

### 📚 词汇复习功能

**当前实现:**
- 基于状态的固定间隔复习机制
- 三种复习状态：new（新单词）、learning（学习中）、mastered（已掌握）
- 固定复习时间间隔：
  - 新单词（new）：当天复习
  - 学习中单词（learning）：次日复习
  - 已掌握单词（mastered）：一周后复习

**注意事项:** 当前复习功能采用简单的固定时间间隔机制，尚未实现完整的艾宾浩斯遗忘曲线算法。

### 🛠️ 开发工具

### 实用脚本

```bash
# 服务管理
./test-all-services.ps1     # 测试所有服务
./restart-all-services.ps1  # 重启所有服务
./port-check.bat            # 端口占用检查

# 调试工具
cd article-service && ./start-debug.ps1  # 启动文章服务调试模式
```

### 开发规范

- 遵循 Spring Boot 最佳实践
- 代码注释完整，包含 Javadoc
- 服务间通信使用 OpenFeign
- 数据库操作使用 MyBatis-Plus
- 错误处理统一，返回标准格式
- 接口文档实时更新

## 📈 性能优化

### 关键优化点

1. **二级词库策略**: 本地缓存优先，AI 兜底
2. **Redis 缓存**: 热点数据缓存，会话存储
3. **数据库索引**: 优化查询性能
4. **异步处理**: 耗时操作异步化
5. **服务熔断**: 防止级联失败

## 🤝 贡献指南

1. Fork 仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📝 更新日志

- **v1.0.0**: 基础微服务架构搭建，核心功能实现
- **v1.5.0**: 集成 Nacos 配置中心和服务注册发现
- **v2.0.0**: 升级二级词库策略，提升响应速度至 10ms
- **v2.1.0**: 实现 Function Calling 功能，增强 AI 交互能力
- **v2.2.0**: 完善 Knife4j 文档聚合，优化开发体验
- **v3.0.0**: 升级词库为多用户共享模式，支持单词共享和逻辑删除

## 📚 相关文档

- [IMPLEMENTATION_STATUS.md](IMPLEMENTATION_STATUS.md): 实现状态报告
- [KNIFE4J_CONFIG_STANDARD.md](KNIFE4J_CONFIG_STANDARD.md): Knife4j 配置标准
- [KNIFE4J_CONSISTENCY_REPORT.md](KNIFE4J_CONSISTENCY_REPORT.md): Knife4j 一致性报告
- [VOCABULARY_UPGRADE_SUMMARY.md](VOCABULARY_UPGRADE_SUMMARY.md): 词汇系统升级总结

## 📧 联系我们

如有任何问题或建议，请联系项目维护团队。