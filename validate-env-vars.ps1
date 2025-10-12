# XReadUp 环境变量验证脚本
# 检查所有必需的环境变量是否正确设置

Write-Host "🔍 XReadUp 环境变量验证脚本" -ForegroundColor Cyan
Write-Host "=================================" -ForegroundColor Cyan

# 定义必需的环境变量
$requiredVars = @{
    # 数据库配置
    "MYSQL_HOST" = "MySQL主机地址"
    "MYSQL_PORT" = "MySQL端口"
    "MYSQL_DB" = "MySQL数据库名"
    "MYSQL_USERNAME" = "MySQL用户名"
    "MYSQL_PASSWORD" = "MySQL密码"
    
    # Redis配置
    "REDIS_HOST" = "Redis主机地址"
    "REDIS_PORT" = "Redis端口"
    "REDIS_PASSWORD" = "Redis密码"
    
    # AI服务配置
    "DEEPSEEK_API_KEY" = "DeepSeek API密钥"
    "DEEPSEEK_BASE_URL" = "DeepSeek API基础URL"
    "DEEPSEEK_MODEL" = "DeepSeek模型名称"
    
    # 腾讯云翻译配置
    "TENCENT_CLOUD_SECRET_ID" = "腾讯云SecretId"
    "TENCENT_CLOUD_SECRET_KEY" = "腾讯云SecretKey"
    "TENCENT_CLOUD_REGION" = "腾讯云区域"
    "TENCENT_CLOUD_ENDPOINT" = "腾讯云API端点"
    
    # GNews配置
    "GNEWS_API_KEY" = "GNews API密钥"
    "GNEWS_BASE_URL" = "GNews API基础URL"
    
    # 应用端口配置
    "GATEWAY_PORT" = "网关服务端口"
    "USER_SERVICE_PORT" = "用户服务端口"
    "ARTICLE_SERVICE_PORT" = "文章服务端口"
    "REPORT_SERVICE_PORT" = "报告服务端口"
    "AI_SERVICE_PORT" = "AI服务端口"
    "ADMIN_SERVICE_PORT" = "管理员服务端口"
    
    # JWT配置
    "JWT_SECRET" = "JWT密钥"
}

# 检查结果统计
$totalVars = $requiredVars.Count
$validVars = 0
$invalidVars = 0
$missingVars = 0

Write-Host "`n📋 检查环境变量状态:" -ForegroundColor Yellow
Write-Host "========================" -ForegroundColor Yellow

foreach ($var in $requiredVars.GetEnumerator()) {
    $value = [Environment]::GetEnvironmentVariable($var.Key, "User")
    
    if ($value -eq $null -or $value -eq "") {
        Write-Host "❌ $($var.Key): 未设置 - $($var.Value)" -ForegroundColor Red
        $missingVars++
    }
    elseif ($value -match "YOUR_|your_|placeholder|default") {
        Write-Host "⚠️  $($var.Key): 使用默认值 - $($var.Value) (值: $value)" -ForegroundColor Yellow
        $invalidVars++
    }
    else {
        # 隐藏敏感信息的部分内容
        $displayValue = $value
        if ($var.Key -match "PASSWORD|SECRET|KEY") {
            if ($value.Length -gt 8) {
                $displayValue = $value.Substring(0, 4) + "****" + $value.Substring($value.Length - 4)
            } else {
                $displayValue = "****"
            }
        }
        
        Write-Host "✅ $($var.Key): 已设置 - $($var.Value) (值: $displayValue)" -ForegroundColor Green
        $validVars++
    }
}

# 显示统计结果
Write-Host "`n📊 验证结果统计:" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
Write-Host "总变量数: $totalVars" -ForegroundColor White
Write-Host "✅ 有效变量: $validVars" -ForegroundColor Green
Write-Host "⚠️  默认值变量: $invalidVars" -ForegroundColor Yellow
Write-Host "❌ 缺失变量: $missingVars" -ForegroundColor Red

# 计算健康度
$healthPercentage = [math]::Round(($validVars / $totalVars) * 100, 2)
Write-Host "`n🏥 配置健康度: $healthPercentage%" -ForegroundColor $(if ($healthPercentage -ge 80) { "Green" } elseif ($healthPercentage -ge 60) { "Yellow" } else { "Red" })

# 提供建议
Write-Host "`n💡 建议:" -ForegroundColor Yellow
if ($missingVars -gt 0) {
    Write-Host "1. 请设置缺失的环境变量" -ForegroundColor White
}
if ($invalidVars -gt 0) {
    Write-Host "2. 请更新使用默认值的环境变量" -ForegroundColor White
}
if ($validVars -eq $totalVars) {
    Write-Host "🎉 所有环境变量配置正确！" -ForegroundColor Green
}

Write-Host "`n🔧 设置环境变量的方法:" -ForegroundColor Cyan
Write-Host "1. 运行 set-env-vars.ps1 脚本" -ForegroundColor White
Write-Host "2. 手动设置: `$env:变量名 = '值'" -ForegroundColor White
Write-Host "3. 重启IDE或命令行窗口使环境变量生效" -ForegroundColor White

# 特别检查腾讯云配置
Write-Host "`n🔍 腾讯云翻译API特别检查:" -ForegroundColor Magenta
Write-Host "=========================" -ForegroundColor Magenta

$tencentSecretId = [Environment]::GetEnvironmentVariable("TENCENT_CLOUD_SECRET_ID", "User")
$tencentSecretKey = [Environment]::GetEnvironmentVariable("TENCENT_CLOUD_SECRET_KEY", "User")

if ($tencentSecretId -and $tencentSecretId -notmatch "YOUR_|your_|placeholder") {
    Write-Host "✅ 腾讯云SecretId已正确设置" -ForegroundColor Green
} else {
    Write-Host "❌ 腾讯云SecretId未正确设置" -ForegroundColor Red
}

if ($tencentSecretKey -and $tencentSecretKey -notmatch "YOUR_|your_|placeholder") {
    Write-Host "✅ 腾讯云SecretKey已正确设置" -ForegroundColor Green
} else {
    Write-Host "❌ 腾讯云SecretKey未正确设置" -ForegroundColor Red
}

if ($tencentSecretId -and $tencentSecretKey -and 
    $tencentSecretId -notmatch "YOUR_|your_|placeholder" -and 
    $tencentSecretKey -notmatch "YOUR_|your_|placeholder") {
    Write-Host "🎉 腾讯云翻译API配置完整！" -ForegroundColor Green
} else {
    Write-Host "⚠️  腾讯云翻译API配置不完整，请检查环境变量" -ForegroundColor Yellow
}

Write-Host "`n✨ 验证完成！" -ForegroundColor Cyan
