# Nacos配置迁移完整过程详解

## 🎯 迁移目标
将分散在各个微服务application.yml中的配置集中到Nacos配置中心，实现：
- ✅ 配置统一管理
- ✅ 动态配置更新
- ✅ 环境变量支持
- ✅ 配置版本控制

## 📋 迁移前状态分析

### 原始配置分布
```
xreadup/
├── article-service/
│   └── src/main/resources/
│       ├── application.yml (8082端口、数据库、Redis等)
│       └── bootstrap.yml (仅服务发现)
├── ai-service/
│   └── src/main/resources/
│       ├── application.yml (8084端口、DeepSeek API等)
│       └── bootstrap.yml (仅服务发现)
├── user-service/
│   └── src/main/resources/
│       ├── application.yml (8081端口、JWT等)
│       └── bootstrap.yml (仅服务发现)
├── gateway/
│   └── src/main/resources/
│       ├── application.yml (8080端口、路由等)
│       └── bootstrap.yml (仅服务发现)
└── report-service/
    └── src/main/resources/
        ├── application.yml (8083端口、数据库等)
        └── bootstrap.yml (仅服务发现)
```

## 🔄 迁移步骤详解

### 第1步：配置分析
**识别重复配置**：
- MySQL连接配置（5个服务重复）
- Redis连接配置（5个服务重复）
- MyBatis Plus配置（4个服务重复）

**识别服务特有配置**：
- 端口号（每个服务不同）
- JWT配置（user-service特有）
- DeepSeek API配置（ai-service特有）
- gnews API配置（article-service特有）

### 第2步：配置重构

#### 2.1 创建共享配置
**shared-mysql-dev.yml** - 提取MySQL通用配置
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://${MYSQL_HOST:localhost}:${MYSQL_PORT:3307}/${MYSQL_DB:readup_ai}?...
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:123456}
    hikari:
      minimum-idle: 5
      maximum-pool-size: 15
```

**shared-redis-dev.yml** - 提取Redis通用配置
```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:123456}
```

#### 2.2 创建服务专用配置
**article-service-dev.yml** - 仅保留服务特有配置
```yaml
server:
  port: 8082

spring:
  application:
    name: article-service
  # 其他通用配置通过shared-configs引入

# 服务特有配置
gnews:
  api:
    key: ${GNEWS_API_KEY:demo-key}
    base-url: ${GNEWS_BASE_URL:https://gnews.io/api/v4}
```

### 第3步：服务配置更新

#### 3.1 更新bootstrap.yml
每个服务的bootstrap.yml从：
```yaml
# 仅服务发现
spring:
  application:
    name: article-service
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: public
```

更新为：
```yaml
# 服务发现 + 配置中心
spring:
  application:
    name: article-service
  profiles:
    active: dev  # 指定环境
  cloud:
    nacos:
      server-addr: localhost:8848
      discovery:
        namespace: public
      config:
        server-addr: localhost:8848
        file-extension: yml
        namespace: public
        group: DEFAULT_GROUP
        shared-configs:  # 引入共享配置
          - data-id: shared-mysql-dev.yml
            group: DEFAULT_GROUP
            refresh: true
          - data-id: shared-redis-dev.yml
            group: DEFAULT_GROUP
            refresh: true
```

### 第4步：配置导入执行

#### 4.1 使用Nacos API导入
**curl命令原理**：
```bash
curl -X POST "http://localhost:8848/nacos/v1/cs/configs" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "dataId=article-service-dev.yml&group=DEFAULT_GROUP&type=yaml" \
  --data-urlencode "content@article-service-dev.yml"
```

**实际执行过程**：
1. 读取本地YAML文件内容
2. URL编码内容（处理特殊字符）
3. 构建HTTP POST请求
4. 发送到Nacos服务器
5. 接收响应确认导入成功

#### 4.2 验证导入结果
**API验证**：
```bash
curl "http://localhost:8848/nacos/v1/cs/configs?dataId=article-service-dev.yml&group=DEFAULT_GROUP"
```

### 第5步：服务重启验证

#### 5.1 启动顺序
1. **启动Nacos** → 2. **启动MySQL** → 3. **启动Redis** → 4. **启动微服务**

#### 5.2 验证日志
服务启动时应看到：
```
Loading nacos data, dataId: 'article-service-dev.yml', group: 'DEFAULT_GROUP'
Located property source: CompositePropertySource {name='NACOS', propertySources=[NacosPropertySource {name='article-service-dev.yml'}]}
```

## 🏗️ 配置优先级机制

**加载优先级**（从高到低）：
1. **服务专用配置** (`article-service-dev.yml`)
2. **共享配置** (`shared-mysql-dev.yml`)
3. **本地application.yml** (作为后备)

**配置覆盖规则**：
- 同名配置项，优先级高的覆盖低的
- 环境变量（如`${MYSQL_HOST:localhost}`）可以覆盖默认值

## 🔄 动态配置更新

**配置修改流程**：
1. 在Nacos控制台修改配置
2. 点击"发布"按钮
3. 配置了`refresh: true`的服务会自动热更新
4. 无需重启服务即可生效

## 📊 迁移效果对比

| 项目 | 迁移前 | 迁移后 |
|---|---|---|
| 配置分散度 | 5个独立文件 | 7个集中管理 |
| 重复配置 | 大量重复 | 共享复用 |
| 环境切换 | 需修改多个文件 | 修改profile即可 |
| 配置更新 | 需重启服务 | 支持热更新 |
| 配置共享 | 手动复制 | 自动继承 |

## 🎯 后续管理建议

### 配置命名规范
- **服务配置**：`{service-name}-{profile}.yml`
- **共享配置**：`shared-{resource}-{profile}.yml`
- **环境区分**：`dev/test/prod`后缀

### 环境变量使用
```bash
# 启动时覆盖配置
set MYSQL_HOST=192.168.1.100
set REDIS_PASSWORD=newpassword
java -jar article-service.jar
```

### 生产环境扩展
- 使用MySQL作为Nacos存储
- 配置Nacos集群
- 按环境分组管理配置