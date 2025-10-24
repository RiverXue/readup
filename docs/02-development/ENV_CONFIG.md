# XReadUp 环境变量配置说明

## 📋 环境变量列表

### 数据库配置
```bash
# MySQL配置
MYSQL_HOST=localhost
MYSQL_PORT=3307
MYSQL_DB=readup_ai
MYSQL_USERNAME=root
MYSQL_PASSWORD=your_mysql_password
```

### Redis配置
```bash
# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password
```

### AI服务配置
```bash
# DeepSeek配置
DEEPSEEK_API_KEY=your_deepseek_api_key
DEEPSEEK_BASE_URL=https://api.deepseek.com
DEEPSEEK_MODEL=deepseek-chat

# GNews配置
GNEWS_API_KEY=your_gnews_api_key

# 其他AI服务
AI_SERVICE_URL=http://localhost:8084
```

### Nacos配置
```bash
# Nacos服务发现
NACOS_SERVER_ADDR=localhost:8848
NACOS_NAMESPACE=dev
NACOS_GROUP=DEFAULT_GROUP
```

### 应用配置
```bash
# 应用端口配置
GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
ARTICLE_SERVICE_PORT=8082
REPORT_SERVICE_PORT=8083
AI_SERVICE_PORT=8084
ADMIN_SERVICE_PORT=8085
```

## 🔧 配置方法

### 方法1：环境变量
```bash
# Linux/Mac
export MYSQL_PASSWORD=your_password
export REDIS_PASSWORD=your_password

# Windows PowerShell
$env:MYSQL_PASSWORD="your_password"
$env:REDIS_PASSWORD="your_password"
```

### 方法2：.env文件
在项目根目录创建`.env`文件：
```bash
MYSQL_PASSWORD=your_password
REDIS_PASSWORD=your_password
OPENAI_API_KEY=your_api_key
```

### 方法3：Nacos配置中心
1. 启动Nacos服务
2. 访问 http://localhost:8848/nacos
3. 在配置管理中添加配置
4. 使用模板文件创建实际配置

## 🚀 快速启动

1. **设置环境变量**
   ```bash
   cp .env.template .env
   # 编辑.env文件，填入实际配置
   ```

2. **启动基础设施**
   ```bash
   docker-compose up -d mysql redis nacos
   ```

3. **导入Nacos配置**
   ```bash
   cd nacos-configs
   ./import-all.bat
   ```

4. **启动微服务**
   ```bash
   cd xreadup
   mvn spring-boot:run -pl gateway
   # 其他服务...
   ```

## ⚠️ 安全注意事项

1. **永远不要提交敏感信息到git仓库**
2. **使用环境变量或配置中心管理敏感配置**
3. **定期轮换API密钥和密码**
4. **生产环境使用强密码**
5. **限制数据库和Redis的访问权限**

## 📚 相关文档

- [Nacos配置管理指南](nacos-configs/import-to-nacos.md)
- [部署文档](docs/DEPLOYMENT.md)
- [API文档](xreadup/API_Doc.md)
