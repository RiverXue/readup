# XReadUp - 真实新闻驱动的AI英语学习平台

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.50-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.18-4FC08D?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?logo=spring)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.0-3178C6?logo=typescript)
![License](https://img.shields.io/badge/license-Apache_2.0-green.svg)

**🌍 真实新闻 + 🤖 AI导师 + ⚡ 智能学习 = 革命性英语学习体验**

[📚 文档中心](docs/README.md) • [🚀 快速开始](docs/02-development/STARTUP_GUIDE.md) • [🎯 核心特色](#-核心特色) • [📊 技术优势](#-技术优势)

</div>

---

## 🎯 项目概述

**XReadUp** 是基于**真实新闻内容**的AI辅助英语学习平台。我们通过GNews API获取全球实时新闻，结合**智能词汇系统**和**AI学习助手**，为学习者提供真实、有趣、高效的英语学习体验。

### 🌟 为什么选择XReadUp？

- **📰 真实内容**：告别枯燥教材，学习真实新闻，了解世界动态
- **🎯 精准定位**：针对考研、四六级、职场、留学四大用户群体，设计差异化学习路径
- **🔄 完整闭环**：真实新闻→智能查词→生词管理→艾宾浩斯复习→学习报告→AI指导
- **💼 商业变现**：四层订阅制商业模式，基于用户画像的权限控制，可持续的商业价值
- **🧠 智能学习**：艾宾浩斯遗忘曲线复习，科学记忆管理
- **📊 数据驱动**：多维度学习分析，智能识别薄弱环节，个性化推荐系统
- **💎 现代UI**：全屏沉浸式AI助手界面，8种个性化问题推荐，完美响应式适配
- **🛡️ 内容安全**：智能内容质量检测系统，解决75%文章截断问题，敏感词过滤机制
- **🔧 系统稳定**：完善的异常处理机制，统一的API设计，详细的日志监控系统

## 🚀 快速开始

### 环境要求
- **Java**: >= 17
- **Node.js**: >= 20.19.0
- **MySQL**: >= 8.0
- **Redis**: >= 6.0
- **Docker**: >= 20.0 (可选)

### 快速启动
```bash
# 1. 克隆项目
git clone https://github.com/RiverXue/readup.git
cd readup

# 2. 启动基础设施
docker-compose up -d mysql redis nacos

# 3. 启动后端服务
cd xreadup
mvn clean install
mvn spring-boot:run -pl gateway
mvn spring-boot:run -pl user-service
mvn spring-boot:run -pl article-service
mvn spring-boot:run -pl report-service
mvn spring-boot:run -pl ai-service
mvn spring-boot:run -pl admin-service

# 4. 启动前端
cd xreadup-frontend
npm install
npm run dev
```

### 访问地址
- **前端应用**: http://localhost:5173
- **管理员后台**: http://localhost:5173/admin/login
- **API网关**: http://localhost:8080
- **Nacos控制台**: http://localhost:8848/nacos

## 📚 文档中心

**📁 [完整文档中心](docs/README.md)** - 所有项目文档的索引和导航

### 核心文档
- **[项目主文档](docs/01-project/README.md)** - 详细的项目介绍和技术文档
- **[开发指南](docs/02-development/STARTUP_GUIDE.md)** - 开发环境配置和项目启动
- **[功能特性](docs/03-features/)** - 各功能模块的详细文档
- **[技术分析](docs/04-analysis/)** - 深度技术分析和算法研究

### 技术文档
- **[API 接口文档](xreadup/API_Doc.md)** - 完整的 REST API 接口说明
- **[后端技术文档](xreadup/BACKEND_README.md)** - Spring Boot 微服务架构详解
- **[前端开发指南](xreadup-frontend/FRONTEND_README.md)** - Vue 3 前端技术栈说明
- **[微信小程序文档](docs/02-development/)** - 微信小程序开发相关文档

## 🏗 系统架构

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
| **微信小程序** | Uni-app + Vue 3 + TypeScript | 移动端英语学习体验 | ✅ 开发完成 |

## 🎯 核心特色

### 📰 真实新闻学习
- **全球实时新闻**：通过GNews API获取BBC、CNN等权威媒体最新资讯
- **多领域覆盖**：科技、商业、文化、科学、健康等8大分类
- **内容质量保证**：智能筛选适合学习的新闻，自动评估难度等级
- **时效性强**：每日更新，学习内容与时代同步

### ⚡ 智能词汇系统
- **快速查词**：点击单词即时查询，支持上下文理解
- **智能缓存**：本地词汇缓存，提升查询效率
- **AI增强**：AI生成词汇释义和例句
- **学习管理**：生词本管理，支持复习提醒

### 🎓 AI学习助手
- **学习指导**：提供英语学习建议和答疑服务
- **数据驱动**：基于用户学习数据提供学习建议
- **学习分析**：分析学习进度和薄弱环节
- **学习支持**：提供学习信心和动力支持

### 🧠 学习系统
- **艾宾浩斯复习**：基于遗忘曲线的复习管理
- **闪卡式批量复习**：以闪卡形式批量复习单词
- **学习数据分析**：多维度数据统计，分析学习规律和问题
- **进度追踪**：可视化学习进度，成就系统激励持续学习

## 📈 更新日志

### 🎆 v1.0.50 (2025-10-24) - 当前版本

**📚 文档系统重构:**
- ✅ 建立完整的文档中心，按功能分类整理
- ✅ 删除30+个过时文档，保留核心有用文档
- ✅ 创建清晰的文档导航和索引系统
- ✅ 优化文档结构和命名规范

**🛡️ 智能敏感词过滤系统:**
- ✅ 实现全文章类型覆盖的敏感词过滤系统
- ✅ 热点文章、主题分类文章、自定义搜索文章全覆盖
- ✅ AI对话内容过滤：AI阅读助手和Rayda老师对话的实时过滤
- ✅ 分级过滤策略：高风险词汇直接拦截，一般敏感词记录但允许通过

**🔧 内容质量提升:**
- ✅ 改进内容质量检测和分段处理
- ✅ 优化文章分段算法，提升阅读体验
- ✅ 完善错误处理和日志记录
- ✅ 添加内容验证和备用提取机制

## 💼 商业价值

### 🎯 目标用户群体
- **考研学生**：需要提高英语阅读能力，关注时事
- **四六级考生**：需要真实语境下的英语学习
- **职场人士**：需要提升商务英语和时事理解能力
- **英语爱好者**：希望在学习语言的同时了解世界

### 💰 商业模式
- **订阅制**：免费版、基础版、专业版、企业版
- **差异化定价**：基于使用量、功能权限、服务等级
- **企业服务**：面向教育机构和企业的定制化解决方案
- **数据价值**：学习数据分析和个性化推荐服务

## 🤝 贡献指南

### 📝 如何贡献

1. **Fork 项目**
2. **创建功能分支**
3. **提交代码**
4. **推送分支**
5. **创建 Pull Request**

### 🔍 代码审查清单

- [ ] 代码符合项目编码规范
- [ ] 添加了必要的单元测试
- [ ] 更新了相关文档
- [ ] API 变更已更新 Swagger 文档
- [ ] 性能测试通过
- [ ] 安全扫描无问题

## 📞 技术支持

### 💬 联系方式

- **项目地址**: [GitHub Repository](https://github.com/RiverXue/readup)
- **问题反馈**: [GitHub Issues](https://github.com/RiverXue/readup/issues)
- **邮箱**: dev@xreadup.com

### 🆘 获得帮助

1. **查看文档**: 首先查阅[文档中心](docs/README.md)
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
  <h3>🎉 加入XReadUp，开启真实新闻英语学习之旅!</h3>
  <p>🌍 真实新闻 + 🤖 AI导师 + ⚡ 智能学习 = 革命性学习体验</p>
  <p>如果这个项目对你有帮助，请给我们一个 ⭐ Star!</p>

![GitHub stars](https://img.shields.io/github/stars/RiverXue/readup?style=social)
![GitHub forks](https://img.shields.io/github/forks/RiverXue/readup?style=social)
![GitHub issues](https://img.shields.io/github/issues/RiverXue/readup)

**🚀 让英语学习变得更真实、更智能、更高效！**

*构建于 ❤️ 与现代技术栈，致力于改变英语学习方式*
</div>
