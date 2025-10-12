# XReadUp - 智能英语学习平台

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.44-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.18-4FC08D?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?logo=spring)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.0-3178C6?logo=typescript)
![License](https://img.shields.io/badge/license-Apache_2.0-green.svg)

**让英语学习变得更智能、更高效！**

[🚀 快速开始](#-快速开始) • [📖 文档](#-相关文档) • [🤝 贡献](#-贡献指南) • [📞 支持](#-技术支持)

</div>

---

## 📖 项目概述

XReadUp 是一个基于 **Vue 3 + Spring Boot** 构建的现代化智能英语学习平台。采用微服务架构设计，集成了 AI 驱动的智能翻译、词汇管理、学习分析和订阅管理等核心功能，为用户提供个性化的英语学习体验。

### 🎯 核心特性

- **🤖 AI 驱动学习** - 集成多种 AI 功能：智能翻译、文章摘要、语法解析、个性化问答
- **📚 智能阅读器** - 双语对照阅读，点击查词，双击朗读，上下文理解
- **🔍 三级词库系统** - 本地缓存 + 共享词库 + AI 实时生成
- **📊 数据可视化报告** - ECharts 图表展示学习进度和成就
- **📱 响应式设计** - 完美适配桌面端和移动端
- **🎨 现代化 UI** - Element Plus 组件库，优雅的用户界面
- **⚡ 高性能架构** - Vite 构建，热更新，TypeScript 类型安全
- **💳 灵活订阅管理** - 多种订阅方案，自动配额管理，实时更新
- **📅 每日打卡功能** - 自动和手动打卡，连续打卡统计，状态持久化
- **🔧 管理员系统** - 完整的后台管理系统，支持用户管理、内容管理、系统配置

## 🏗 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        XReadUp 智能英语学习平台                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │   前端应用       │    │   管理员后台     │                    │
│  │  Vue 3 + TS     │    │  Vue 3 + TS     │                    │
│  │  Element Plus   │    │  Element Plus   │                    │
│  │  Port: 5173     │    │  Port: 5173     │                    │
│  └─────────────────┘    └─────────────────┘                    │
│           │                       │                             │
│           └───────────┬───────────┘                             │
│                       │                                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                Spring Cloud Gateway                       │ │
│  │                API 网关 + 路由 + 限流                      │ │
│  │                Port: 8080                                 │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                       │                                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    微服务集群                               │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │User Service │ │AI Service   │ │Admin Service│           │ │
│  │  │Port: 8081   │ │Port: 8084   │ │Port: 8085   │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │Article Svc  │ │Report Svc   │ │Gateway      │           │ │
│  │  │Port: 8082   │ │Port: 8083   │ │Port: 8080   │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                       │                                         │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │                    基础设施层                               │ │
│  │                                                             │ │
│  │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │ │
│  │  │   MySQL     │ │   Redis     │ │   Nacos     │           │ │
│  │  │   Port:3306 │ │  Port:6379  │ │ Port:8848   │           │ │
│  │  └─────────────┘ └─────────────┘ └─────────────┘           │ │
│  └─────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### 微服务架构

| **服务名称** | **端口** | **核心职责** | **技术栈** | **状态** |
|-------------|----------|-------------|------------|----------|
| **Gateway** | 8080 | API网关、路由、限流、CORS | Spring Cloud Gateway | ✅ 生产就绪 |
| **User Service** | 8081 | 用户管理、认证、订阅管理 | Spring Boot 3.4.1 | ✅ 生产就绪 |
| **Article Service** | 8082 | 文章管理、内容发现 | Spring Boot 3.4.1 | ✅ 生产就绪 |
| **Report Service** | 8083 | 学习报告、数据分析 | Spring Boot 3.4.1 | ✅ 生产就绪 |
| **AI Service** | 8084 | AI翻译、分析、智能助手 | Spring Boot 3.4.1 | ✅ 生产就绪 |
| **Admin Service** | 8085 | 系统配置管理、管理员权限控制 | Spring Boot 3.4.1 | ✅ 生产就绪 |

### 前端架构

| **系统** | **技术栈** | **核心功能** | **状态** |
|----------|------------|-------------|----------|
| **用户端** | Vue 3.5.18 + TypeScript 5.8.0 + Element Plus 2.11.2 | 智能阅读、生词本、学习报告、订阅管理 | ✅ 生产就绪 |
| **管理员端** | Vue 3.5.18 + TypeScript 5.8.0 + Element Plus 2.11.2 | 用户管理、内容管理、系统配置、数据分析 | ✅ 生产就绪 |

## 🚀 快速开始

### 环境要求

- **Java**: >= 17
- **Node.js**: >= 20.19.0
- **MySQL**: >= 8.0
- **Redis**: >= 6.0
- **Docker**: >= 20.0 (可选)

### 🔧 环境配置

在启动项目前，请先配置环境变量：

```bash
# 1. 复制环境变量模板
cp ENV_CONFIG.md .env

# 2. 编辑环境变量文件，填入实际配置
# 参考 ENV_CONFIG.md 文件中的详细说明
```

**重要**: 请确保不要将包含敏感信息的配置文件提交到git仓库。

### 后端启动

```bash
# 1. 克隆项目
git clone https://github.com/RiverXue/xreadup.git
cd xreadup

# 2. 启动基础设施
docker-compose up -d mysql redis nacos

# 3. 启动微服务
cd xreadup
mvn clean install
mvn spring-boot:run -pl gateway
mvn spring-boot:run -pl user-service
mvn spring-boot:run -pl article-service
mvn spring-boot:run -pl report-service
mvn spring-boot:run -pl ai-service
mvn spring-boot:run -pl admin-service
```

### 前端启动

```bash
# 1. 进入前端目录
cd xreadup-frontend

# 2. 安装依赖
npm install

# 3. 启动开发服务器
npm run dev
```

### 访问地址

| **服务** | **地址** | **说明** |
|----------|----------|----------|
| **前端应用** | http://localhost:5173 | 主要用户界面 |
| **管理员后台** | http://localhost:5173/admin/login | 管理员登录入口 |
| **API网关** | http://localhost:8080 | 后端服务入口 |
| **API文档** | http://localhost:8080/doc.html | Knife4j 聚合文档 |
| **Nacos控制台** | http://localhost:8848/nacos | 配置与服务管理 |
| **健康检查** | http://localhost:8080/actuator/health | 服务状态监控 |

## 📊 API 接口文档

### 🔗 文档概览

**系统共包含 60+ REST API 接口**，按服务分布:

```
🎯 总计 60+ REST API 接口

📊 按服务分布:
├── 🤖 AI Service: 22个接口
│   ├── 文章AI分析: 5个接口
│   ├── 翻译服务: 8个接口  
│   ├── Function Calling: 4个接口
│   └── 智能助手: 5个接口
│
├── 👤 User Service: 18个接口
│   ├── 用户管理: 6个接口
│   ├── 三级词库: 8个接口
│   └── 订阅管理: 4个接口
│
├── 📰 Article Service: 7个接口
│   ├── 文章管理: 4个接口
│   └── 内容发现: 3个接口
│
├── 📊 Report Service: 8个接口
│   ├── 学习统计: 4个接口
│   ├── 数据分析: 3个接口
│   └── 健康检查: 1个接口
│
└── 🔧 Admin Service: 5个接口
    ├── 管理员权限: 2个接口
    ├── 系统配置: 2个接口
    └── 用户管理: 1个接口
```

### 🔑 核心API示例

#### 1. 智能词汇查询
```http
POST /api/vocabulary/lookup
Content-Type: application/json

{
  "word": "artificial",
  "context": "technology", 
  "userId": 12345,
  "articleId": 67890
}

# Response
{
  "code": 200,
  "message": "查询成功",
  "data": {
    "id": 1001,
    "word": "artificial",
    "meaning": "人工的，人造的",
    "example": "Artificial intelligence is transforming our world.",
    "phonetic": "/ˌɑːrtɪˈfɪʃl/",
    "difficulty": "B2",
    "source": "ai",
    "context": "technology"
  }
}
```

#### 2. AI智能翻译
```http
POST /api/ai/translate/smart
Content-Type: application/json

{
  "text": "The quick brown fox jumps over the lazy dog.",
  "targetLanguage": "zh"
}

# Response
{
  "code": 200,
  "message": "翻译成功",
  "data": {
    "originalText": "The quick brown fox jumps over the lazy dog.",
    "translatedText": "这只敏捷的棕色狐狸跳过了那只懒惰的狗。",
    "confidence": 0.95,
    "translationMethod": "ai_smart"
  }
}
```

#### 3. 管理员系统配置
```http
GET /api/admin/system-config/all

# Response
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "MAINTENANCE": {
      "system.maintenance.mode": "false",
      "system.maintenance.message": "系统维护中，请稍后访问"
    },
    "FEATURE": {
      "feature.ai.enabled": "true",
      "feature.tts.enabled": "true"
    },
    "LIMIT": {
      "limit.free.articles": "30",
      "limit.free.words": "2000"
    }
  }
}
```

### 📚 详细API文档

完整的API文档请访问: [API文档详情](xreadup/API_Doc.md)

## 🎨 核心功能

### 1. 智能阅读器
- **双语对照**: 英文原文 + 中文翻译
- **智能查词**: 点击单词即时查询
- **双击朗读**: TTS语音朗读功能
- **AI功能**: 翻译、摘要、解析、问答
- **生词管理**: 一键添加到生词本

### 2. 三级词库系统
- **本地缓存**: 用户个人词汇缓存
- **共享词库**: 多用户词汇共享机制
- **AI生成**: 实时AI词汇解释
- **智能复习**: 基于遗忘曲线的复习算法

### 3. 学习报告系统
- **数据可视化**: ECharts图表展示
- **学习统计**: 词汇量、阅读时长、连续打卡
- **成就系统**: 学习里程碑和进度追踪
- **周报功能**: 每周学习总结

### 4. 订阅管理系统
- **多套餐方案**: 免费、基础、专业、企业版
- **实时额度管理**: 阅读篇数、单词量、AI功能
- **灵活升级**: 支持套餐升级和降级
- **数据驱动**: 完全从后端API获取配置

### 5. 管理员系统
- **用户管理**: 用户列表、状态管理、详细信息
- **内容管理**: 文章管理、发布审核、内容分析
- **订阅管理**: 套餐配置、用户订阅、数据统计
- **系统配置**: 功能开关、业务限制、配置管理
- **AI分析**: AI结果查看、服务监控、使用统计

## 📈 更新日志

### 🎆 v1.0.44 (2025-01-27) - 当前版本

**✨ 文档更新:**
- 📚 Nacos配置中心管理文档全面更新
- 🗑️ 清理100+个过时的一次性脚本和文件
- 📋 主README文档引用检查和修复
- 🏗️ 微服务配置管理最佳实践
- 🔧 环境变量和部署配置完善

**🛠️ 技术改进:**
- 升级 Spring Boot 至 3.4.1
- 升级 Spring Cloud 至 2024.0.0
- 升级 Vue 3 至 3.5.18
- 升级 TypeScript 至 5.8.0
- 新增 Knife4j 4.3.0 文档聚合
- 优化 Redis 缓存策略
- 完善监控和日志系统

**🐛 Bug 修复:**
- 修复词汇重复添加问题
- 解决 AI 服务调用超时
- 优化数据库连接池配置
- 修复跨域配置问题
- 修复管理员权限验证问题

### 🎆 v1.0.43 (2025-01-27)

**✨ 新增特性:**
- ⭐ 三级词库策略全面升级，性能提升 97%
- 🤖 Function Calling 架构完全实现
- 📊 多用户单词共享机制
- 🧠 **句子解析缓存共享** - 相同句子解析结果在用户间智能共享，大幅节省AI调用成本
- 🚀 异步缓存优化，响应时间降至 <10ms
- 🔍 智能上下文匹配和词义区分
- 📈 完整的学习数据分析和可视化

---

## 🛣️ 发展路线

### 🚀 即将发布 (v1.1.0)

- 📈 **高级分析**: AI驱动的学习建议系统
- 🔔 **实时通知**: WebSocket 推送学习提醒
- 🎨 **UI/UX 升级**: 全新界面设计和交互体验
- 🌍 **多语言支持**: 支持更多语言学习

### 🎨 长期规划 (v2.0.0)

- 🧠 **个性化学习**: AI驱动的个性化学习路径推荐
- 🎮 **游戏化元素**: 学习成就系统和排行榜
- 🤝 **社交学习**: 用户互动和学习小组功能
- 📱 **移动端App**: 原生移动应用开发
- 🎯 **企业版**: 面向教育机构和企业的解决方案

---

## 🤝 贡献指南

### 📝 如何贡献

1. **Fork 项目**
   ```bash
   git clone https://github.com/yourusername/xreadup.git
   cd xreadup
   ```

2. **创建功能分支**
   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **提交代码**
   ```bash
   git commit -m 'feat: add amazing feature'
   ```

4. **推送分支**
   ```bash
   git push origin feature/amazing-feature
   ```

5. **创建 Pull Request**

### 🔍 代码审查清单

- [ ] 代码符合项目编码规范
- [ ] 添加了必要的单元测试
- [ ] 更新了相关文档
- [ ] API 变更已更新 Swagger 文档
- [ ] 性能测试通过
- [ ] 安全扫描无问题

---

## 📚 相关文档

### 📖 技术文档

- **[API 接口文档](xreadup/API_Doc.md)** - 完整的 REST API 接口说明
- **[后端技术文档](xreadup/BACKEND_README.md)** - Spring Boot 微服务架构详解
- **[前端开发指南](xreadup-frontend/FRONTEND_README.md)** - Vue 3 前端技术栈说明
- **[前端架构设计](xreadup-frontend/ARCHITECTURE.md)** - 双系统架构和组件设计
- **[设计系统](xreadup-frontend/DESIGN_SYSTEM.md)** - UI/UX设计规范和组件库
- **[API契约文档](xreadup-frontend/API_CONTRACT.md)** - 前后端接口契约规范

### 📋 项目维护文档

- **[更新日志](CHANGELOG.md)** - 项目版本更新记录
- **[修复日志](FIX_LOG.md)** - 详细的技术修复记录
- **[Nacos配置管理](nacos-configs/import-to-nacos.md)** - 微服务配置中心管理指南

### 🔗 外部资源

- **Vue 3 官方文档**: https://cn.vuejs.org/
- **Spring Boot 官方文档**: https://spring.io/projects/spring-boot
- **Element Plus 组件库**: https://element-plus.gitee.io/zh-CN/
- **ECharts 图表库**: https://echarts.apache.org/zh/index.html

---

## 📞 技术支持

### 💬 联系方式

- **项目地址**: [GitHub Repository](https://github.com/RiverXue/xreadup)
- **问题反馈**: [GitHub Issues](https://github.com/RiverXue/xreadup/issues)
- **邮箱**: dev@xreadup.com

### 🆘 获得帮助

1. **查看文档**: 首先查阅相关技术文档
2. **搜索问题**: 在 GitHub Issues 中搜索类似问题
3. **提交 Issue**: 描述问题并提供必要信息
4. **邮件联系**: 发送详细问题描述到邮箱

---

## ⚖️ 许可证

本项目基于 [Apache License 2.0](LICENSE) 开源协议。

---

## 🙏 致谢

感谢所有为 XReadUp 项目做出贡献的开发者、设计师和用户。特别感谢以下开源项目:

- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架
- [Spring Boot](https://spring.io/projects/spring-boot) - 企业级 Java 应用框架
- [Element Plus](https://element-plus.org/) - Vue 3 组件库
- [ECharts](https://echarts.apache.org/) - 数据可视化图表库
- [Nacos](https://nacos.io/) - 动态服务发现和配置管理
- [Docker](https://www.docker.com/) - 容器化平台

---

<div align="center">
  <h3>🎉 感谢使用 XReadUp 智能英语学习平台!</h3>
  <p>如果这个项目对你有帮助，请给我们一个 ⭐ Star!</p>

![GitHub stars](https://img.shields.io/github/stars/RiverXue/xreadup?style=social)
![GitHub forks](https://img.shields.io/github/forks/RiverXue/xreadup?style=social)
![GitHub issues](https://img.shields.io/github/issues/RiverXue/xreadup)

**让英语学习变得更智能、更高效！**

*构建于 ❤️ 与现代技术栈*
</div>