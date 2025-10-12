# Nacos配置中心使用指南

## ✅ 配置已成功导入

所有配置文件已通过 `import-simple.ps1` 成功导入到Nacos配置中心。

# Nacos配置中心使用指南

## 📁 当前文件结构
```
nacos-configs/
├── README.md                           # 本使用指南
├── import-all.bat                      # 批量导入脚本
├── import-to-nacos.md                  # 详细导入说明
├── migration-process.md                # 迁移流程文档
├── *.yml.template                      # 配置模板文件（安全，可提交Git）
└── *.yml                               # 实际配置文件（包含敏感信息，已忽略）
```

## 🔒 安全说明

- **模板文件** (`*.yml.template`) - 包含占位符，安全可提交
- **实际配置文件** (`*.yml`) - 包含敏感信息，已被.gitignore忽略
- **环境变量** - 所有敏感信息通过.env文件管理

## 🚀 如何使用

### 1. 创建实际配置文件
```bash
# 复制模板文件为实际配置文件
cp *.yml.template *.yml

# 编辑实际配置文件，替换占位符为实际值
# 或通过环境变量自动替换
```

### 2. 导入配置到Nacos
```bash
# 使用批量导入脚本
./import-all.bat

# 或参考 import-to-nacos.md 手动导入
```

### 3. 环境变量配置
确保.env文件包含所有必要的环境变量：
```bash
MYSQL_PASSWORD=your_actual_password
REDIS_PASSWORD=your_actual_password
JWT_SECRET=your_actual_jwt_secret
DEEPSEEK_API_KEY=your_actual_api_key
# ... 其他配置
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