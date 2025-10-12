# 🚀 XReadUp 项目启动指南

## 📋 启动前准备

### 1. 环境变量配置

**⚠️ 重要：必须先配置环境变量才能启动项目！**

```bash
# 1. 复制环境变量模板
cp .env.example .env

# 2. 编辑 .env 文件，填入实际配置
# 参考 ENV_CONFIG.md 文件中的详细说明
```

**环境变量会自动加载，无需手动设置！** Spring Boot 会自动读取 `.env` 文件中的环境变量。

### 2. 检查环境要求

- **Java**: >= 17
- **Node.js**: >= 20.19.0  
- **MySQL**: >= 8.0
- **Redis**: >= 6.0
- **Docker**: >= 20.0 (推荐)

## 🏗️ 启动步骤

### 第一步：启动基础设施

```bash
# 启动 MySQL、Redis、Nacos
docker-compose up -d mysql redis nacos

# 等待服务启动完成（约30秒）
docker-compose ps
```

### 第二步：启动后端微服务

```bash
# 进入后端目录
cd xreadup

# 编译项目
mvn clean install

# 启动各个微服务（建议按顺序启动）
# 1. 先启动网关
mvn spring-boot:run -pl gateway

# 2. 启动用户服务
mvn spring-boot:run -pl user-service

# 3. 启动文章服务
mvn spring-boot:run -pl article-service

# 4. 启动报告服务
mvn spring-boot:run -pl report-service

# 5. 启动AI服务
mvn spring-boot:run -pl ai-service

# 6. 启动管理员服务
mvn spring-boot:run -pl admin-service
```

### 第三步：启动前端

```bash
# 进入前端目录
cd xreadup-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 🔧 环境变量自动加载机制

### Spring Boot 自动加载

Spring Boot 会自动读取以下环境变量：

```yaml
# 数据库配置
password: ${MYSQL_PASSWORD:YOUR_MYSQL_PASSWORD}

# Redis配置  
password: ${REDIS_PASSWORD:YOUR_REDIS_PASSWORD}

# AI服务配置
api-key: ${DEEPSEEK_API_KEY:YOUR_DEEPSEEK_API_KEY}

# 腾讯云配置
secret-id: ${TENCENT_CLOUD_SECRET_ID:YOUR_TENCENT_SECRET_ID}
secret-key: ${TENCENT_CLOUD_SECRET_KEY:YOUR_TENCENT_SECRET_KEY}

# JWT配置
secret: ${JWT_SECRET:YOUR_JWT_SECRET_KEY}
```

### 环境变量优先级

1. **系统环境变量** (最高优先级)
2. **`.env` 文件** (推荐)
3. **配置文件默认值** (最低优先级)

## 📊 服务状态检查

### 检查服务是否启动成功

```bash
# 检查Docker服务
docker-compose ps

# 检查Java进程
jps -l

# 检查端口占用
netstat -tlnp | grep -E ":(8080|8081|8082|8083|8084|8085|8848|3307|6379)"
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

## 🚨 常见问题解决

### 1. 环境变量未生效

**问题**: 服务启动失败，提示配置错误

**解决**:
```bash
# 检查 .env 文件是否存在
ls -la .env

# 检查环境变量内容
cat .env

# 重新启动服务
```

### 2. 数据库连接失败

**问题**: 数据库连接超时

**解决**:
```bash
# 检查MySQL是否启动
docker-compose ps mysql

# 检查端口是否开放
netstat -tlnp | grep 3307

# 重启MySQL
docker-compose restart mysql
```

### 3. Redis连接失败

**问题**: Redis连接超时

**解决**:
```bash
# 检查Redis是否启动
docker-compose ps redis

# 检查Redis端口
netstat -tlnp | grep 6379

# 重启Redis
docker-compose restart redis
```

### 4. Nacos连接失败

**问题**: 服务注册失败

**解决**:
```bash
# 检查Nacos是否启动
docker-compose ps nacos

# 访问Nacos控制台
curl http://localhost:8848/nacos

# 重启Nacos
docker-compose restart nacos
```

## 🔄 快速重启

### 重启所有服务

```bash
# 停止所有服务
docker-compose down
pkill -f "spring-boot:run"

# 重新启动
docker-compose up -d mysql redis nacos
cd xreadup
mvn spring-boot:run -pl gateway &
mvn spring-boot:run -pl user-service &
mvn spring-boot:run -pl article-service &
mvn spring-boot:run -pl report-service &
mvn spring-boot:run -pl ai-service &
mvn spring-boot:run -pl admin-service &
```

### 重启单个服务

```bash
# 停止特定服务
pkill -f "user-service"

# 重新启动
mvn spring-boot:run -pl user-service
```

## 📝 启动日志

### 查看服务日志

```bash
# 查看Docker服务日志
docker-compose logs -f mysql
docker-compose logs -f redis
docker-compose logs -f nacos

# 查看应用日志
tail -f logs/user-service/user-service.log
tail -f logs/ai-service/ai-service.log
```

### 关键日志信息

启动成功的关键日志：

```
# 网关启动成功
Started GatewayApplication in 15.234 seconds

# 用户服务启动成功  
Started UserServiceApplication in 12.456 seconds

# AI服务启动成功
Started AiServiceApplication in 18.789 seconds

# 前端启动成功
Local:   http://localhost:5173/
Network: http://192.168.1.100:5173/
```

## 🎯 启动验证

### 1. 检查所有服务状态

```bash
# 检查所有端口
netstat -tlnp | grep -E ":(5173|8080|8081|8082|8083|8084|8085|8848|3307|6379)"
```

### 2. 访问健康检查

```bash
# 检查API网关健康状态
curl http://localhost:8080/actuator/health

# 检查Nacos健康状态
curl http://localhost:8848/nacos/actuator/health
```

### 3. 测试API接口

```bash
# 测试用户注册接口
curl -X POST http://localhost:8080/api/user/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","email":"test@example.com"}'
```

## 🎉 启动完成

当所有服务都启动成功后，你就可以：

1. **访问前端**: http://localhost:5173
2. **访问API文档**: http://localhost:8080/doc.html  
3. **访问Nacos**: http://localhost:8848/nacos
4. **开始开发**: 所有环境变量已自动加载，无需手动配置

**环境变量会自动加载，你只需要确保 `.env` 文件存在且配置正确即可！** 🚀
