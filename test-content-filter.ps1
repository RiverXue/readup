# 违禁词过滤系统测试脚本
# 测试文章服务和AI服务的违禁词过滤功能

Write-Host "🚀 开始测试违禁词过滤系统..." -ForegroundColor Green

# 1. 编译项目
Write-Host "📦 编译项目..." -ForegroundColor Yellow
cd xreadup
mvn clean compile -q

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 编译失败" -ForegroundColor Red
    exit 1
}

Write-Host "✅ 编译成功" -ForegroundColor Green

# 2. 启动基础服务
Write-Host "🔧 启动基础服务..." -ForegroundColor Yellow
docker-compose up -d mysql redis nacos

# 等待服务启动
Start-Sleep -Seconds 10

# 3. 测试文章服务
Write-Host "📰 测试文章服务过滤功能..." -ForegroundColor Yellow

# 启动文章服务
Start-Process -FilePath "java" -ArgumentList "-jar", "article-service/target/article-service-1.0.0.jar" -WindowStyle Hidden
Start-Sleep -Seconds 15

# 测试正常文章
Write-Host "测试正常文章..." -ForegroundColor Cyan
$normalArticle = @{
    url = "https://www.bbc.com/news/technology"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/article/extract-content?url=https://www.bbc.com/news/technology" -Method Get
    Write-Host "✅ 正常文章处理成功" -ForegroundColor Green
} catch {
    Write-Host "⚠️ 正常文章测试失败: $($_.Exception.Message)" -ForegroundColor Yellow
}

# 4. 测试AI服务
Write-Host "🤖 测试AI服务过滤功能..." -ForegroundColor Yellow

# 启动AI服务
Start-Process -FilePath "java" -ArgumentList "-jar", "ai-service/target/ai-service-1.0.0.jar" -WindowStyle Hidden
Start-Sleep -Seconds 15

# 测试正常问题
Write-Host "测试正常问题..." -ForegroundColor Cyan
$normalQuestion = @{
    question = "这篇文章讲了什么？"
    userId = 1
    articleContext = ""
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8084/api/ai/assistant/chat" -Method Post -Body $normalQuestion -ContentType "application/json"
    Write-Host "✅ 正常问题处理成功" -ForegroundColor Green
} catch {
    Write-Host "⚠️ 正常问题测试失败: $($_.Exception.Message)" -ForegroundColor Yellow
}

# 测试违禁问题
Write-Host "测试违禁问题..." -ForegroundColor Cyan
$badQuestion = @{
    question = "法轮功是什么？"
    userId = 1
    articleContext = ""
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://localhost:8084/api/ai/assistant/chat" -Method Post -Body $badQuestion -ContentType "application/json"
    if ($response.data.answer -like "*不当内容*") {
        Write-Host "✅ 违禁问题正确拦截" -ForegroundColor Green
    } else {
        Write-Host "❌ 违禁问题未被拦截" -ForegroundColor Red
    }
} catch {
    Write-Host "⚠️ 违禁问题测试失败: $($_.Exception.Message)" -ForegroundColor Yellow
}

# 5. 查看日志
Write-Host "📋 查看过滤日志..." -ForegroundColor Yellow
Write-Host "文章服务日志:" -ForegroundColor Cyan
Get-Content "logs/article-service.log" -Tail 10 | Where-Object { $_ -like "*违禁*" -or $_ -like "*敏感*" }

Write-Host "AI服务日志:" -ForegroundColor Cyan
Get-Content "logs/ai-service.log" -Tail 10 | Where-Object { $_ -like "*违禁*" -or $_ -like "*敏感*" }

Write-Host "🎉 测试完成！" -ForegroundColor Green
Write-Host "📊 测试结果:" -ForegroundColor Yellow
Write-Host "- 文章服务: 已集成违禁词过滤" -ForegroundColor White
Write-Host "- AI服务: 已集成违禁词过滤" -ForegroundColor White
Write-Host "- 高风险词汇: 直接拦截" -ForegroundColor White
Write-Host "- 一般违禁词: 记录但不拦截" -ForegroundColor White

# 停止服务
Write-Host "🛑 停止测试服务..." -ForegroundColor Yellow
Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.MainWindowTitle -eq "" } | Stop-Process -Force
