# OpenFeign服务间调用集成指南

## 概述

本项目已成功集成OpenFeign实现文章服务与AI服务之间的服务间调用。通过Spring Cloud OpenFeign，我们可以优雅地实现微服务间的HTTP调用。

## 集成内容

### 1. 依赖配置
在`pom.xml`中添加了以下依赖：
- `spring-cloud-starter-openfeign`: OpenFeign核心功能
- `spring-cloud-starter-loadbalancer`: 客户端负载均衡

### 2. Feign客户端定义
创建了`AiServiceClient`接口，定义了与AI服务交互的三个方法：
- `analyzeArticle`: 完整文章分析
- `quickAnalyze`: 快速文章分析
- `chunkedAnalyze`: 分段文章分析

### 3. DTO数据传输对象
- `ArticleAnalysisRequest`: 文章分析请求对象
- `ArticleAnalysisResponse`: 文章分析响应对象
- `ArticleDetailVO`: 文章详情视图对象（包含AI分析结果）

### 4. 服务集成
- `AiIntegrationService`: 封装了AI服务调用逻辑，根据文章长度选择不同的分析策略
- `ArticleServiceImpl`: 扩展了文章服务，增加了AI分析功能

### 5. 新的API端点
- `GET /api/article/detail/{id}/ai`: 获取文章详情及AI分析结果
- `POST /api/article/{id}/analyze`: 对文章进行AI分析
- `POST /api/test/ai/health`: 测试AI服务连通性

## 使用说明

### 启动服务
1. 确保Nacos注册中心已启动
2. 启动AI服务（端口8081）
3. 启动文章服务（端口8080）

### 测试调用
可以通过以下方式测试服务间调用：

1. **测试AI服务连通性**:
```bash
curl -X GET http://localhost:8080/api/test/ai/health
```

2. **获取文章详情及AI分析**:
```bash
curl -X GET http://localhost:8080/api/article/detail/1/ai
```

3. **对文章进行AI分析**:
```bash
curl -X POST http://localhost:8080/api/article/1/analyze
```

### 缓存策略
- 使用Spring Cache进行缓存
- `getArticleDetailWithAiAnalysis`: 缓存文章详情和AI分析结果
- `analyzeArticleWithAI`: 清除缓存并重新分析

## 配置要点

### 1. 启用OpenFeign
在启动类`ArticleServiceApplication`上添加：
```java
@EnableFeignClients
```

### 2. 服务发现
确保两个服务都注册到Nacos，服务名分别为：
- 文章服务：`article-service`
- AI服务：`ai-service`

### 3. 负载均衡
使用Spring Cloud LoadBalancer实现客户端负载均衡。

## 异常处理

- 当AI服务不可用时，会返回降级响应
- 所有API调用都有适当的异常处理机制
- 日志记录详细的调用信息

## 下一步计划

1. 添加异步消息队列支持
2. 实现批量文章分析
3. 添加分析结果持久化
4. 优化缓存策略
5. 添加监控和指标收集