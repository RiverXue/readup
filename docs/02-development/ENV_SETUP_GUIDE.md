# XReadUp 环境变量设置指南

## 📋 概述

本指南说明如何将 `.env` 文件中的配置内容设置到系统环境变量中，以便Spring Boot应用能够直接使用这些环境变量。

## 🚀 快速开始

### 方法1: 当前会话设置（推荐用于开发测试）

```powershell
# 在PowerShell中运行
.\set-env-session.ps1
```

**特点**：
- ✅ 立即生效，无需重启
- ✅ 仅影响当前PowerShell会话
- ✅ 适合开发测试使用

### 方法2: 永久设置（推荐用于生产环境）

```powershell
# 在PowerShell中运行（需要管理员权限）
.\set-env-vars.ps1
```

**特点**：
- ✅ 永久设置到用户环境变量
- ✅ 所有应用都能访问
- ✅ 需要重启命令行窗口生效

### 方法3: 批处理脚本（Windows批处理）

```cmd
# 在命令提示符中运行
set-env-vars.bat
```

## 📊 设置的环境变量

脚本会自动设置以下环境变量：

### 数据库配置
- `MYSQL_HOST` = localhost
- `MYSQL_PORT` = 3307
- `MYSQL_DB` = readup_ai
- `MYSQL_USERNAME` = root
- `MYSQL_PASSWORD` = 123456

### Redis配置
- `REDIS_HOST` = localhost
- `REDIS_PORT` = 6379
- `REDIS_PASSWORD` = 123456

### 服务端口配置
- `GATEWAY_PORT` = 8080
- `USER_SERVICE_PORT` = 8081
- `ARTICLE_SERVICE_PORT` = 8082
- `REPORT_SERVICE_PORT` = 8083
- `AI_SERVICE_PORT` = 8084
- `ADMIN_SERVICE_PORT` = 8085

### JWT配置
- `JWT_SECRET` = readupSecretKey2025ForJWTAuthenticationWithStrongSecurity

### 第三方API配置
- `DEEPSEEK_API_KEY` = sk-923712b1f15446c0bf25c32343af4dfd
- `GNEWS_API_KEY` = 8b869b5e95410f82314acd62e8fb8641
- `TENCENT_CLOUD_SECRET_ID` = AKIDZ9XheOvEJFj170iFZjgQwFLDuRpsohT4
- `TENCENT_CLOUD_SECRET_KEY` = UlrkRxJTSDnAcs7C4pPp45tZ5PYVFH9n

## 🔧 验证环境变量

设置完成后，可以通过以下命令验证：

```powershell
# PowerShell中验证
echo $env:MYSQL_PASSWORD
echo $env:JWT_SECRET
echo $env:GATEWAY_PORT

# 命令提示符中验证
echo %MYSQL_PASSWORD%
echo %JWT_SECRET%
echo %GATEWAY_PORT%
```

## 💡 使用建议

### 开发环境
1. 使用 `set-env-session.ps1` 设置当前会话环境变量
2. 修改 `.env` 文件后重新运行脚本
3. 无需重启IDE或命令行窗口

### 生产环境
1. 使用 `set-env-vars.ps1` 永久设置环境变量
2. 重启应用服务
3. 确保所有服务都能访问环境变量

### IDE配置
如果使用IDE（如IntelliJ IDEA、VS Code），可能需要：
1. 重启IDE以读取新的环境变量
2. 或者在IDE中手动配置环境变量

## ⚠️ 注意事项

1. **安全性**：环境变量中的敏感信息（如密码、API密钥）会被所有应用访问
2. **权限**：永久设置环境变量可能需要管理员权限
3. **重启**：永久设置后需要重启命令行窗口或IDE才能生效
4. **备份**：建议备份原始的 `.env` 文件

## 🔄 更新环境变量

要更新环境变量：
1. 修改 `.env` 文件
2. 重新运行相应的设置脚本
3. 验证新值是否正确设置

## 📁 文件说明

- `set-env-session.ps1` - 设置当前会话环境变量
- `set-env-vars.ps1` - 永久设置用户环境变量
- `set-env-vars.bat` - 批处理版本（永久设置）
- `.env` - 环境变量配置文件
- `.env.example` - 环境变量配置模板
