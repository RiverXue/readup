# 测试内容质量检测功能
# 验证不完整内容的优雅处理

Write-Host "=== 测试内容质量检测功能 ===" -ForegroundColor Green

# 测试短内容文章
Write-Host "`n1. 测试短内容文章（ID: 1984）..." -ForegroundColor Yellow
try {
    $shortArticle = Invoke-RestMethod -Uri "http://localhost:8082/api/article/read/1984" -Method Get
    Write-Host "✅ 文章获取成功" -ForegroundColor Green
    Write-Host "标题: $($shortArticle.data.article.title)" -ForegroundColor White
    Write-Host "内容长度: $($shortArticle.data.article.contentEn.Length) 字符" -ForegroundColor White
    Write-Host "内容预览: $($shortArticle.data.article.contentEn.Substring(0, [Math]::Min(200, $shortArticle.data.article.contentEn.Length)))..." -ForegroundColor Gray
    
    if ($shortArticle.data.article.contentEn.Length -lt 500) {
        Write-Host "✅ 检测到短内容，应该显示质量警告" -ForegroundColor Green
    } else {
        Write-Host "❌ 短内容检测失败" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 短内容文章测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试正常内容文章
Write-Host "`n2. 测试正常内容文章..." -ForegroundColor Yellow
try {
    $normalArticle = Invoke-RestMethod -Uri "http://localhost:8082/api/article/read/1985" -Method Get
    Write-Host "✅ 文章获取成功" -ForegroundColor Green
    Write-Host "标题: $($normalArticle.data.article.title)" -ForegroundColor White
    Write-Host "内容长度: $($normalArticle.data.article.contentEn.Length) 字符" -ForegroundColor White
    
    if ($normalArticle.data.article.contentEn.Length -ge 1000) {
        Write-Host "✅ 检测到正常内容，不应该显示质量警告" -ForegroundColor Green
    } else {
        Write-Host "⚠️ 内容长度: $($normalArticle.data.article.contentEn.Length) 字符" -ForegroundColor Yellow
    }
} catch {
    Write-Host "❌ 正常内容文章测试失败: $($_.Exception.Message)" -ForegroundColor Red
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
        Write-Host "✅ 检测到质量警告已添加到内容中" -ForegroundColor Green
        Write-Host "质量警告内容:" -ForegroundColor Cyan
        $warningMatch = [regex]::Match($extractResult.data, "--- 内容质量提示 ---.*?--- 内容结束 ---", [System.Text.RegularExpressions.RegexOptions]::Singleline)
        if ($warningMatch.Success) {
            Write-Host $warningMatch.Value -ForegroundColor Gray
        }
    } else {
        Write-Host "❌ 未检测到质量警告" -ForegroundColor Red
    }
} catch {
    Write-Host "❌ 内容提取测试失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
Write-Host "`n功能说明:" -ForegroundColor Cyan
Write-Host "- 短内容（<500字符）会显示严重警告" -ForegroundColor White
Write-Host "- 中等内容（500-1000字符）会显示提示" -ForegroundColor White
Write-Host "- 正常内容（>1000字符）不显示警告" -ForegroundColor White
Write-Host "- 用户可以通过'查看完整原文'按钮访问原始文章" -ForegroundColor White
