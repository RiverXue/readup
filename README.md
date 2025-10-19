# ReadUp - 真实新闻驱动的AI英语学习平台

<div align="center">

![Version](https://img.shields.io/badge/version-1.0.44-blue.svg)
![Vue](https://img.shields.io/badge/Vue-3.5.18-4FC08D?logo=vue.js)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-6DB33F?logo=spring)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.0-3178C6?logo=typescript)
![License](https://img.shields.io/badge/license-Apache_2.0-green.svg)

**🌍 真实新闻 + 🤖 AI导师 + ⚡ 智能学习 = 革命性英语学习体验**

[🚀 快速开始](#-快速开始) • [🎯 核心特色](#-核心特色) • [📊 技术优势](#-技术优势) • [💼 商业价值](#-商业价值)

</div>

---

## 🎯 项目概述

**ReadUp** 是全球首个基于**真实新闻内容**的AI驱动英语学习平台。我们通过GNews API获取全球实时新闻，结合**智能词汇系统**和**Rayda老师AI导师**，为学习者提供真实、有趣、高效的英语学习体验。

### 🌟 为什么选择ReadUp？

- **📰 真实内容**：告别枯燥教材，学习真实新闻，了解世界动态
- **⚡ 智能查词**：点击查词，AI增强释义，学习更高效
- **🎓 AI导师**：Rayda老师提供个性化学习指导，基于学习画像定制建议
- **🧠 智能学习**：艾宾浩斯遗忘曲线复习，科学记忆管理
- **📊 数据驱动**：多维度学习分析，智能识别薄弱环节

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

### 📝 智能分段分词系统
- **多级分段策略**：基于标点符号和语义边界的智能识别
- **前后端协同**：后端预处理 + 前端动态处理，确保分段质量
- **双语同步**：中英文段落智能对应，保持阅读连贯性
- **语义完整性**：优先在自然段落边界分割，避免语义断裂
- **短段落优化**：自动合并过短段落，提升阅读体验

### 🔍 智能内容质量检测
- **三级质量评估**：HIGH/MEDIUM/LOW智能分级
- **多维度检测**：内容长度、句子完整性、截断指示词分析
- **置信度评分**：0-100%的准确度评估
- **优雅提示系统**：现代化毛玻璃卡片设计，引导用户操作
- **用户体验优化**：智能引导使用查看原文功能，避免内容不完整问题

### 🎓 Rayda老师AI导师
- **专业身份**：经验丰富的英语学习导师，耐心、专业、鼓励式教学
- **个性化指导**：基于用户学习画像提供定制化学习建议
- **学习诊断**：智能识别薄弱环节和优势能力
- **情感连接**：建立学习信心，增强学习动力

### 🧠 智能学习系统
- **艾宾浩斯复习**：科学记忆管理，1天→3天→7天→15天→30天复习周期
- **学习画像分析**：多维度数据分析，识别学习规律和问题
- **个性化推荐**：基于学习历史推荐适合的文章和练习
- **进度追踪**：可视化学习进度，成就系统激励持续学习

## 📊 技术优势

### 🚀 性能优化
- **快速响应**：优化的数据库查询和缓存机制
- **智能缓存**：本地词汇缓存，减少重复查询
- **AI集成**：智能词汇释义和例句生成
- **并发支持**：微服务架构，支持高并发访问

### 🏗️ 微服务架构
- **6个独立微服务**：Gateway、User、Article、Report、AI、Admin
- **Spring Cloud生态**：服务注册发现、配置中心、熔断限流
- **容器化部署**：Docker + Docker Compose，一键部署
- **监控完善**：健康检查、性能指标、日志聚合

### 🔧 核心技术架构
- **GNews + Readability4J组合**：新闻发现 + 内容提取的完美结合
- **Jsoup网页解析**：模拟浏览器请求，处理JavaScript渲染
- **智能内容验证**：8维度质量检查，确保内容有效性
- **多级分段算法**：基于标点符号和语义边界的智能分段
- **Spring Retry机制**：3次重试 + 5秒间隔，确保内容获取成功率

### 🔒 数据安全
- **JWT认证**：安全的用户身份验证
- **数据加密**：敏感信息加密存储
- **权限控制**：细粒度权限管理
- **隐私保护**：用户数据隐私保护机制

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

### 📈 市场前景
- **英语学习市场**：中国英语学习市场规模超过1000亿元
- **在线教育增长**：年增长率超过20%，疫情加速数字化
- **AI教育趋势**：AI+教育成为投资热点，技术驱动创新
- **内容差异化**：真实新闻内容形成独特竞争优势

## 🏗 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                        ReadUp 智能英语学习平台                    │
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
cp .env.example .env

# 2. 编辑环境变量文件，填入实际配置
# 参考 ENV_CONFIG.md 文件中的详细说明
```

**重要**: 请确保不要将包含敏感信息的配置文件提交到git仓库。

### 后端启动

```bash
# 1. 克隆项目
git clone https://github.com/RiverXue/readup.git
cd readup

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

### 📖 智能阅读器
- **双语对照阅读**：英文原文 + 中文翻译，理解更深入
- **智能分段分词**：多级分段策略，确保语义完整性和阅读体验
- **点击查词**：智能词汇查询，支持上下文理解
- **语音朗读**：TTS语音朗读，提升听力能力
- **AI分析**：智能摘要、语法解析、难度评估
- **生词管理**：一键收藏，智能复习提醒
- **个性化字体控制**：付费用户可自由调节阅读字体大小（14px-24px），提升阅读舒适度

### 🎓 Rayda老师AI导师
- **个性化对话**：基于学习画像的定制化指导
- **学习诊断**：智能识别薄弱环节和优势能力
- **问题推荐**：8种问题类型，动态生成学习建议
- **情感支持**：耐心、专业、鼓励式教学风格
- **学习规划**：制定个性化学习计划和目标

### 📊 学习数据分析
- **学习画像**：多维度分析学习行为和效果
- **进度追踪**：可视化学习进度和成就系统
- **艾宾浩斯复习**：科学记忆管理，提升学习效率
- **周报月报**：定期学习总结和改进建议
- **数据可视化**：ECharts图表展示学习成果

### 💳 商业化功能
- **订阅管理**：多套餐方案，灵活升级降级
- **额度控制**：实时监控使用量，智能提醒
- **企业服务**：团队管理、批量授权、数据分析
- **支付集成**：支持多种支付方式，安全便捷

## 📈 更新日志

### 🎆 v1.0.44 (2025-10-30) - 当前版本

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

### 🎆 v1.0.43 (2025-10-29)

**✨ 新增特性:**
- ⭐ 三级词库策略全面升级，性能提升 97%
- 🤖 Function Calling 架构完全实现
- 📊 多用户单词共享机制
- 🧠 **句子解析缓存共享** - 相同句子解析结果在用户间智能共享，大幅节省AI调用成本
- 🚀 异步缓存优化，响应时间降至 <10ms
- 🔍 智能上下文匹配和词义区分
- 📈 完整的学习数据分析和可视化

---

## 🚀 发展路线

### 📅 短期目标（6个月）
- **用户增长**：目标1万付费用户，建立用户基础
- **产品优化**：完善Rayda老师AI导师功能，提升用户体验
- **内容质量**：优化新闻筛选算法，提升内容质量
- **性能提升**：持续优化三级词库策略，提升系统性能

### 🎯 中期目标（1年）
- **市场扩张**：覆盖更多用户群体，扩大市场份额
- **功能扩展**：增加听力、口语练习功能
- **技术升级**：持续优化AI能力，提升个性化水平
- **国际化**：支持多语言学习，拓展海外市场

### 🌟 长期愿景（3年）
- **平台化**：成为英语学习生态平台，连接更多教育资源
- **AI领先**：成为AI教育领域的领导者
- **生态建设**：构建完整的学习生态系统
- **社会价值**：让更多人享受优质英语学习服务

## 💡 投资亮点

### 🏆 核心优势
- **真实新闻内容**：独特的内容优势，难以复制
- **AI导师系统**：个性化学习体验，增强用户粘性
- **智能学习系统**：科学的学习管理和进度追踪

### 📈 市场机会
- **千亿市场**：英语学习市场规模巨大且持续增长
- **技术驱动**：AI+教育成为投资热点
- **差异化竞争**：真实新闻内容形成独特优势

### 💰 商业价值
- **订阅模式**：现金流稳定，可预测性强
- **高客单价**：用户付费意愿强，ARPU值高
- **规模效应**：用户增长带来成本降低和收入增长

---

## 🤝 贡献指南

### 📝 如何贡献

1. **Fork 项目**
   ```bash
   git clone https://github.com/RiverXue/readup.git
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

- **项目地址**: [GitHub Repository](https://github.com/RiverXue/readup)
- **问题反馈**: [GitHub Issues](https://github.com/RiverXue/readup/issues)
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

感谢所有为 ReadUp 项目做出贡献的开发者、设计师和用户。特别感谢以下开源项目:

- [Vue.js](https://vuejs.org/) - 渐进式 JavaScript 框架
- [Spring Boot](https://spring.io/projects/spring-boot) - 企业级 Java 应用框架
- [Element Plus](https://element-plus.org/) - Vue 3 组件库
- [ECharts](https://echarts.apache.org/) - 数据可视化图表库
- [Nacos](https://nacos.io/) - 动态服务发现和配置管理
- [Docker](https://www.docker.com/) - 容器化平台

---

<div align="center">
  <h3>🎉 加入ReadUp，开启真实新闻英语学习之旅!</h3>
  <p>🌍 真实新闻 + 🤖 AI导师 + ⚡ 智能学习 = 革命性学习体验</p>
  <p>如果这个项目对你有帮助，请给我们一个 ⭐ Star!</p>

![GitHub stars](https://img.shields.io/github/stars/RiverXue/readup?style=social)
![GitHub forks](https://img.shields.io/github/forks/RiverXue/readup?style=social)
![GitHub issues](https://img.shields.io/github/issues/RiverXue/readup)

**🚀 让英语学习变得更真实、更智能、更高效！**

*构建于 ❤️ 与现代技术栈，致力于改变英语学习方式*
</div>