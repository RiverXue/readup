# Nacos配置中心使用指南

## ✅ 配置已成功导入

所有配置文件已通过 `import-simple.ps1` 成功导入到Nacos配置中心。

### 📁 当前文件结构
```
nacos-configs/
├── README.md                 # 本使用指南
├── import-simple.ps1        # ✅ 成功使用的导入脚本
├── verify-import.ps1        # 验证配置导入状态
├── article-service-dev.yml   # 文章服务配置
├── ai-service-dev.yml        # AI服务配置  
├── gateway-dev.yml          # 网关服务配置
├── user-service-dev.yml      # 用户服务配置
├── report-service-dev.yml    # 报告服务配置
├── shared-mysql-dev.yml      # MySQL共享配置
└── shared-redis-dev.yml      # Redis共享配置
```

### 🚀 如何使用

#### 1. 重新导入配置（如有需要）
```powershell
# 在nacos-configs目录下运行
powershell -ExecutionPolicy Bypass -File import-simple.ps1
```

#### 2. 验证配置状态
```powershell
# 检查所有配置是否已导入
powershell -ExecutionPolicy Bypass -File verify-import.ps1
```

#### 3. 访问Nacos控制台
- 地址：http://localhost:8848/nacos
- 账号：nacos/nacos
- 分组：DEFAULT_GROUP

### 🔧 为什么之前失败

| 问题 | 原因 | 解决方案 |
|---|---|---|
| 导入脚本无效 | 使用了不兼容的命令格式 | 使用curl的`--data-urlencode @filename` |
| PowerShell错误 | 执行策略限制 | 添加`-ExecutionPolicy Bypass` |
| 路径问题 | Windows路径转义错误 | 使用相对路径和正确引号 |

### 📋 下一步

1. **重启微服务** - 各服务会自动从Nacos加载配置
2. **验证配置** - 检查服务日志确认配置加载成功
3. **动态管理** - 后续可直接在Nacos控制台修改配置
4. **🧠 新特性** - 系统已集成句子解析缓存共享机制，大幅提升AI服务响应速度并节省成本