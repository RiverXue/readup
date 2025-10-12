# XReadUp Nacos 配置中心管理文档

<div align="center">

![Nacos Config](https://img.shields.io/badge/Nacos-Config_Center-blue.svg)
![Version](https://img.shields.io/badge/Version-1.0.44-green.svg)
![Status](https://img.shields.io/badge/Status-Production_Ready-success.svg)

**微服务配置中心统一管理**

</div>

## 📋 配置中心概述

XReadUp 项目采用 **Nacos** 作为配置中心，实现微服务配置的统一管理和动态更新。所有微服务的配置都通过 Nacos 进行集中管理，支持配置的动态刷新和环境隔离。

## 🏗️ 微服务架构

### 服务列表

| 服务名称 | 端口 | 配置文件 | 功能描述 |
|----------|------|----------|----------|
| **Gateway** | 8080 | gateway-dev.yml | API网关，请求路由和负载均衡 |
| **User Service** | 8081 | user-service-dev.yml | 用户管理、认证和三级词库策略 |
| **Article Service** | 8082 | article-service-dev.yml | 文章内容管理和阅读功能 |
| **Report Service** | 8083 | report-service-dev.yml | 学习统计和报表生成 |
| **AI Service** | 8084 | ai-service-dev.yml | AI智能分析和翻译功能 |
| **Admin Service** | 8085 | admin-service-dev.yml | 管理员权限和系统配置 |

### 共享配置

| 配置类型 | 配置文件 | 功能描述 |
|----------|----------|----------|
| **MySQL** | shared-mysql-dev.yml | 数据库连接池配置 |
| **Redis** | shared-redis-dev.yml | 缓存连接配置 |

## 📁 配置文件结构

```
nacos-configs/
├── gateway-dev.yml           # 网关服务配置
├── user-service-dev.yml      # 用户服务配置
├── article-service-dev.yml   # 文章服务配置
├── report-service-dev.yml    # 报告服务配置
├── ai-service-dev.yml        # AI服务配置
├── admin-service-dev.yml     # 管理员服务配置
├── shared-mysql-dev.yml      # MySQL共享配置
├── shared-redis-dev.yml      # Redis共享配置
├── import-all.bat            # 批量导入脚本
├── import-to-nacos.md        # 导入指南
├── migration-process.md      # 迁移流程
└── README.md                 # 配置说明
```

## 🔧 配置导入方法

### 方法1：使用批量导入脚本（推荐）

```bash
# Windows环境
import-all.bat

# 脚本会自动导入所有配置文件到Nacos
```

### 方法2：使用Nacos控制台手动导入

1. **访问Nacos控制台**
   - URL: http://localhost:8848/nacos
   - 用户名: nacos
   - 密码: nacos

2. **导入配置步骤**
   - 进入 `配置管理` > `配置列表`
   - 点击 `+` 按钮创建配置
   - 填写配置信息：
     - **Data ID**: `{service-name}-dev.yml`
     - **Group**: `DEFAULT_GROUP`
     - **配置格式**: `YAML`
     - **配置内容**: 复制对应文件内容
   - 点击 `发布` 完成导入

### 方法3：使用Nacos OpenAPI导入

#### 导入网关配置
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=gateway-dev.yml&group=DEFAULT_GROUP&content=$(cat gateway-dev.yml | sed 's/$/\\n/' | tr -d '\n')&type=yaml"
```

#### 导入用户服务配置
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=user-service-dev.yml&group=DEFAULT_GROUP&content=$(cat user-service-dev.yml | sed 's/$/\\n/' | tr -d '\n')&type=yaml"
```

#### 导入文章服务配置
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=article-service-dev.yml&group=DEFAULT_GROUP&content=$(cat article-service-dev.yml | sed 's/$/\\n/' | tr -d '\n')&type=yaml"
```

#### 导入报告服务配置
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=report-service-dev.yml&group=DEFAULT_GROUP&content=$(cat report-service-dev.yml | sed 's/$/\\n/' | tr -d '\n')&type=yaml"
```

#### 导入AI服务配置
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=ai-service-dev.yml&group=DEFAULT_GROUP&content=$(cat ai-service-dev.yml | sed 's/$/\\n/' | tr -d '\n')&type=yaml"
```

#### 导入管理员服务配置
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=admin-service-dev.yml&group=DEFAULT_GROUP&content=$(cat admin-service-dev.yml | sed 's/$/\\n/' | tr -d '\n')&type=yaml"
```

#### 导入MySQL共享配置
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=shared-mysql-dev.yml&group=DEFAULT_GROUP&content=$(cat shared-mysql-dev.yml | sed 's/$/\\n/' | tr -d '\n')&type=yaml"
```

#### 导入Redis共享配置
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=shared-redis-dev.yml&group=DEFAULT_GROUP&content=$(cat shared-redis-dev.yml | sed 's/$/\\n/' | tr -d '\n')&type=yaml"
```

## 🔍 配置验证

### 1. 检查配置是否生效

```bash
# 启动Nacos服务器
docker-compose up -d nacos

# 启动各个微服务
cd xreadup
mvn spring-boot:run -pl gateway
mvn spring-boot:run -pl user-service
mvn spring-boot:run -pl article-service
mvn spring-boot:run -pl report-service
mvn spring-boot:run -pl ai-service
mvn spring-boot:run -pl admin-service
```

### 2. 查看配置加载日志

每个服务启动时会在控制台显示：
```
[Nacos] Loaded config from Nacos: {service-name}-dev.yml
[Nacos] Loaded shared config: shared-mysql-dev.yml
[Nacos] Loaded shared config: shared-redis-dev.yml
```

### 3. 测试配置动态更新

1. 在Nacos控制台修改某个配置
2. 观察对应服务的日志输出
3. 验证配置变更是否生效

## 📊 配置详情说明

### 网关服务配置 (gateway-dev.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: gateway-service
  
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/user/**
        - id: article-service
          uri: lb://article-service
          predicates:
            - Path=/api/article/**
        - id: report-service
          uri: lb://report-service
          predicates:
            - Path=/api/report/**
        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/ai/**
        - id: admin-service
          uri: lb://admin-service
          predicates:
            - Path=/api/admin/**
```

### 用户服务配置 (user-service-dev.yml)

```yaml
server:
  port: 8081

spring:
  application:
    name: user-service
  
  # MyBatis Plus配置
  mybatis-plus:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.xreadup.user.model
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    global-config:
      db-config:
        id-type: auto
        logic-delete-field: deleted
        logic-delete-value: 1
        logic-not-delete-value: 0

# JWT配置
jwt:
  secret: ${JWT_SECRET:xreadup-secret-key-2025}
  expiration: 86400000  # 24小时

# 三级词库配置
vocabulary:
  cache:
    enabled: true
    ttl: 3600  # 1小时
  strategy:
    local-first: true
    shared-fallback: true
    ai-generation: true
```

### AI服务配置 (ai-service-dev.yml)

```yaml
server:
  port: 8084

spring:
  application:
    name: ai-service

# DeepSeek AI配置
deepseek:
  api-key: ${DEEPSEEK_API_KEY:your-api-key}
  base-url: https://api.deepseek.com
  model: deepseek-chat
  max-tokens: 4000
  temperature: 0.7

# 腾讯云翻译配置
tencent:
  secret-id: ${TENCENT_SECRET_ID:your-secret-id}
  secret-key: ${TENCENT_SECRET_KEY:your-secret-key}
  region: ap-beijing

# AI功能配置
ai:
  features:
    translation: true
    summary: true
    quiz-generation: true
    function-calling: true
  limits:
    daily-calls: 1000
    per-user-calls: 100
```

### 管理员服务配置 (admin-service-dev.yml)

```yaml
server:
  port: 8085

spring:
  application:
    name: admin-service
  
  # MyBatis Plus配置
  mybatis-plus:
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.xreadup.admin.model
    configuration:
      map-underscore-to-camel-case: true
      log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
    global-config:
      db-config:
        id-type: auto
        logic-delete-field: deleted
        logic-delete-value: 1
        logic-not-delete-value: 0

# 管理员权限配置
admin:
  roles:
    - SUPER_ADMIN
    - ADMIN
  permissions:
    - USER_MANAGE
    - ARTICLE_MANAGE
    - SUBSCRIPTION_MANAGE
    - AI_RESULT_MANAGE
    - ADMIN_MANAGE
    - SYSTEM_CONFIG
  session:
    timeout: 14400  # 4小时
```

### MySQL共享配置 (shared-mysql-dev.yml)

```yaml
# MySQL共享配置 - Nacos配置中心
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3307}/${MYSQL_DB:readup_ai}?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:YOUR_MYSQL_PASSWORD}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 15
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
      connection-test-query: SELECT 1
```

### Redis共享配置 (shared-redis-dev.yml)

```yaml
# Redis共享配置 - Nacos配置中心
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:YOUR_REDIS_PASSWORD}
      timeout: 5000ms
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
```

## 🌍 环境变量支持

### 数据库环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `MYSQL_HOST` | localhost | MySQL主机地址 |
| `MYSQL_PORT` | 3307 | MySQL端口 |
| `MYSQL_DB` | readup_ai | 数据库名称 |
| `MYSQL_USERNAME` | root | 数据库用户名 |
| `MYSQL_PASSWORD` | 123456 | 数据库密码 |

### Redis环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `REDIS_HOST` | localhost | Redis主机地址 |
| `REDIS_PORT` | 6379 | Redis端口 |
| `REDIS_PASSWORD` | 123456 | Redis密码 |

### AI服务环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `DEEPSEEK_API_KEY` | your-api-key | DeepSeek API密钥 |
| `TENCENT_SECRET_ID` | your-secret-id | 腾讯云Secret ID |
| `TENCENT_SECRET_KEY` | your-secret-key | 腾讯云Secret Key |

### JWT环境变量

| 变量名 | 默认值 | 说明 |
|--------|--------|------|
| `JWT_SECRET` | xreadup-secret-key-2025 | JWT签名密钥 |

## 🔄 配置管理最佳实践

### 1. 配置命名规范

- **服务配置**: `{service-name}-{profile}.yml`
- **共享配置**: `shared-{resource}-{profile}.yml`
- **环境分组**: `{ENV}_GROUP` (DEV_GROUP, PROD_GROUP)

### 2. 配置优先级

```
服务专用配置 > 共享配置 > 本地application.yml > 默认配置
```

### 3. 配置刷新策略

- **共享配置**: 支持自动刷新 (`refresh: true`)
- **服务配置**: 需要在bootstrap.yml中配置refresh支持
- **敏感配置**: 不支持动态刷新，需要重启服务

### 4. 环境隔离

```yaml
# 开发环境
spring:
  profiles:
    active: dev
  cloud:
    nacos:
      config:
        group: DEV_GROUP

# 生产环境
spring:
  profiles:
    active: prod
  cloud:
    nacos:
      config:
        group: PROD_GROUP
```

## 🚀 部署配置

### Docker Compose配置

```yaml
version: '3.8'
services:
  nacos:
    image: nacos/nacos-server:v2.2.0
    container_name: nacos
    environment:
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=mysql
      - MYSQL_SERVICE_DB_NAME=nacos
      - MYSQL_SERVICE_USER=nacos
      - MYSQL_SERVICE_PASSWORD=nacos
    ports:
      - "8848:8848"
    depends_on:
      - mysql

  mysql:
    image: mysql:8.0
    container_name: mysql
    environment:
      - MYSQL_ROOT_PASSWORD=123456
      - MYSQL_DATABASE=readup_ai
    ports:
      - "3307:3306"
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:6.0-alpine
    container_name: redis
    ports:
      - "6379:6379"
    command: redis-server --requirepass 123456

volumes:
  mysql_data:
```

### 生产环境配置

```yaml
# 生产环境Nacos配置
spring:
  cloud:
    nacos:
      config:
        server-addr: nacos-cluster:8848
        namespace: prod
        group: PROD_GROUP
        file-extension: yaml
        refresh-enabled: true
        shared-configs:
          - data-id: shared-mysql-prod.yml
            group: PROD_GROUP
            refresh: true
          - data-id: shared-redis-prod.yml
            group: PROD_GROUP
            refresh: true
```

## 🔧 故障排查

### 1. 配置加载失败

**问题**: 服务启动时无法加载Nacos配置

**排查步骤**:
1. 检查Nacos服务是否正常运行
2. 验证配置文件是否正确导入
3. 检查网络连接和防火墙设置
4. 查看服务启动日志中的错误信息

**解决方案**:
```bash
# 检查Nacos服务状态
curl http://localhost:8848/nacos/v1/ns/instance/list?serviceName=nacos

# 检查配置是否存在
curl http://localhost:8848/nacos/v1/cs/configs?dataId=gateway-dev.yml&group=DEFAULT_GROUP
```

### 2. 配置动态更新不生效

**问题**: 修改Nacos配置后服务未自动刷新

**排查步骤**:
1. 检查bootstrap.yml中的refresh配置
2. 验证@RefreshScope注解是否正确使用
3. 查看Nacos控制台的配置历史

**解决方案**:
```yaml
# bootstrap.yml
spring:
  cloud:
    nacos:
      config:
        refresh-enabled: true
        shared-configs:
          - data-id: shared-mysql-dev.yml
            refresh: true
```

### 3. 环境变量覆盖不生效

**问题**: 环境变量无法覆盖配置文件中的值

**排查步骤**:
1. 检查环境变量名称是否正确
2. 验证变量格式是否符合要求
3. 确认服务启动时环境变量已设置

**解决方案**:
```bash
# 设置环境变量
export MYSQL_HOST=prod-mysql-server
export MYSQL_PORT=3306
export MYSQL_PASSWORD=secure-password

# 重启服务
docker-compose restart user-service
```

## 📚 相关文档

- [Nacos官方文档](https://nacos.io/zh-cn/docs/what-is-nacos.html)
- [Spring Cloud Nacos配置中心](https://spring-cloud-alibaba-group.github.io/github-pages/hoxton/zh-cn/index.html#_nacos_config)
- [XReadUp后端技术文档](../xreadup/BACKEND_README.md)
- [XReadUp API文档](../xreadup/API_Doc.md)

## 📞 技术支持

- **项目维护者**: XReadUp开发团队
- **文档版本**: 1.0.44
- **最后更新**: 2025-01-27
- **Nacos版本**: 2.2.0

---

<div align="center">

**🔧 统一配置管理，提升微服务运维效率**

*配置于 ❤️ 与最佳实践*

</div>