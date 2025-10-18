# 测试完整注册功能
Write-Host "测试完整用户注册功能..." -ForegroundColor Green

# 测试数据
$testData = @{
    username = "testuser_$(Get-Date -Format 'yyyyMMddHHmmss')"
    password = "testpassword123"
    phone = "13800138000"
    interestTag = "tech"
    identityTag = "考研"
    learningGoalWords = 3000
    targetReadingTime = 30
}

Write-Host "测试数据:" -ForegroundColor Yellow
$testData | ConvertTo-Json

# 发送注册请求
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/user/register" -Method POST -ContentType "application/json" -Body ($testData | ConvertTo-Json)
    
    Write-Host "注册响应:" -ForegroundColor Green
    $response | ConvertTo-Json -Depth 3
    
    if ($response.success) {
        Write-Host "✅ 注册成功！" -ForegroundColor Green
        Write-Host "用户ID: $($response.data.userId)" -ForegroundColor Cyan
        Write-Host "用户名: $($response.data.username)" -ForegroundColor Cyan
        Write-Host "消息: $($response.message)" -ForegroundColor Cyan
    } else {
        Write-Host "❌ 注册失败: $($response.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n测试完成！" -ForegroundColor Green
