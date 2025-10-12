# 测试AI服务环境变量配置
Write-Host "🔍 测试AI服务环境变量配置..." -ForegroundColor Cyan

# 检查环境变量
Write-Host "`n📋 当前环境变量:" -ForegroundColor Yellow
Write-Host "TENCENT_CLOUD_SECRET_ID: $env:TENCENT_CLOUD_SECRET_ID" -ForegroundColor White
Write-Host "TENCENT_CLOUD_SECRET_KEY: $env:TENCENT_CLOUD_SECRET_KEY" -ForegroundColor White

# 测试AI服务健康检查
Write-Host "`n🔍 测试AI服务健康检查..." -ForegroundColor Yellow
try {
    $healthResponse = Invoke-RestMethod -Uri "http://localhost:8084/api/ai/health" -Method GET
    Write-Host "✅ AI服务健康检查: $($healthResponse.data)" -ForegroundColor Green
} catch {
    Write-Host "❌ AI服务健康检查失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试腾讯云翻译API
Write-Host "`n🔍 测试腾讯云翻译API..." -ForegroundColor Yellow
try {
    $translateResponse = Invoke-RestMethod -Uri "http://localhost:8084/api/ai/tencent-translate/en-to-zh" -Method POST -ContentType "application/json" -Body '{"text":"Hello World"}'
    Write-Host "✅ 腾讯云翻译API测试成功" -ForegroundColor Green
    Write-Host "翻译结果: $($translateResponse.data.translatedText)" -ForegroundColor White
} catch {
    Write-Host "❌ 腾讯云翻译API测试失败: $($_.Exception.Message)" -ForegroundColor Red
    
    # 尝试获取详细错误信息
    try {
        $errorResponse = Invoke-WebRequest -Uri "http://localhost:8084/api/ai/tencent-translate/en-to-zh" -Method POST -ContentType "application/json" -Body '{"text":"Hello World"}'
        Write-Host "详细错误信息: $($errorResponse.Content)" -ForegroundColor Red
    } catch {
        Write-Host "无法获取详细错误信息" -ForegroundColor Red
    }
}

Write-Host "`n✨ 测试完成！" -ForegroundColor Cyan
