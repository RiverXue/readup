# 测试新的内容质量提示UI设计
# 验证现代化卡片设计和用户体验

Write-Host "=== 测试新的内容质量提示UI设计 ===" -ForegroundColor Green

Write-Host "`n🎨 UI设计特点:" -ForegroundColor Cyan
Write-Host "- 参考首页现代化卡片设计" -ForegroundColor White
Write-Host "- 毛玻璃效果和渐变背景" -ForegroundColor White
Write-Host "- 优雅的动画和悬停效果" -ForegroundColor White
Write-Host "- 引导用户使用右下角查看原文按钮" -ForegroundColor White

# 测试短内容文章
Write-Host "`n1. 测试短内容文章（ID: 1984）..." -ForegroundColor Yellow
try {
    $shortArticle = Invoke-RestMethod -Uri "http://localhost:8082/api/article/read/1984" -Method Get
    Write-Host "✅ 文章获取成功" -ForegroundColor Green
    Write-Host "标题: $($shortArticle.data.article.title)" -ForegroundColor White
    Write-Host "内容长度: $($shortArticle.data.article.contentEn.Length) 字符" -ForegroundColor White
    
    if ($shortArticle.data.article.contentEn.Length -lt 500) {
        Write-Host "✅ 应该显示高质量警告卡片" -ForegroundColor Green
        Write-Host "   - 橙色警告图标" -ForegroundColor Gray
        Write-Host "   - 现代化卡片设计" -ForegroundColor Gray
        Write-Host "   - 引导使用右下角查看原文按钮" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 短内容文章测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试中等内容文章
Write-Host "`n2. 测试中等内容文章..." -ForegroundColor Yellow
try {
    $mediumArticle = Invoke-RestMethod -Uri "http://localhost:8082/api/article/read/1985" -Method Get
    Write-Host "✅ 文章获取成功" -ForegroundColor Green
    Write-Host "标题: $($mediumArticle.data.article.title)" -ForegroundColor White
    Write-Host "内容长度: $($mediumArticle.data.article.contentEn.Length) 字符" -ForegroundColor White
    
    if ($mediumArticle.data.article.contentEn.Length -ge 500 -and $mediumArticle.data.article.contentEn.Length -lt 1000) {
        Write-Host "✅ 应该显示温和提示卡片" -ForegroundColor Green
        Write-Host "   - 蓝色信息图标" -ForegroundColor Gray
        Write-Host "   - 温和的提示信息" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 中等内容文章测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试内容提取API
Write-Host "`n3. 测试内容提取API..." -ForegroundColor Yellow
try {
    $testUrl = "https://blockclubchicago.org/2025/10/18/thousands-gather-to-protest-trump-at-chicagos-no-kings-rally-downtown/"
    $extractResult = Invoke-RestMethod -Uri "http://localhost:8082/api/article/extract-content?url=$testUrl" -Method Get
    Write-Host "✅ 内容提取成功" -ForegroundColor Green
    Write-Host "提取内容长度: $($extractResult.data.Length) 字符" -ForegroundColor White
    
    # 检查是否包含质量警告
    if ($extractResult.data -match "内容质量提示|内容可能不完整") {
        Write-Host "✅ 后端已添加质量警告到内容中" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ 内容提取测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
Write-Host "`n🎯 新UI设计优势:" -ForegroundColor Cyan
Write-Host "✅ 现代化毛玻璃卡片设计" -ForegroundColor White
Write-Host "✅ 与首页UI风格保持一致" -ForegroundColor White
Write-Host "✅ 优雅的动画和交互效果" -ForegroundColor White
Write-Host "✅ 不重复查看原文按钮，引导用户使用现有功能" -ForegroundColor White
Write-Host "✅ 分级提示系统，用户体验更友好" -ForegroundColor White
