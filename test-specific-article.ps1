# 测试特定文章的内容提取
# 用于诊断文章1984的内容截断问题

Write-Host "=== 测试特定文章内容提取 ===" -ForegroundColor Green

$testUrl = "https://blockclubchicago.org/2025/10/18/thousands-gather-to-protest-trump-at-chicagos-no-kings-rally-downtown/"

Write-Host "`n测试URL: $testUrl" -ForegroundColor Yellow

try {
    Write-Host "`n1. 直接测试内容提取..." -ForegroundColor Cyan
    $extractResult = Invoke-RestMethod -Uri "http://localhost:8082/api/article/extract-content?url=$testUrl" -Method Get
    Write-Host "✅ 提取成功" -ForegroundColor Green
    Write-Host "内容长度: $($extractResult.data.Length) 字符" -ForegroundColor White
    Write-Host "内容预览:" -ForegroundColor White
    Write-Host $extractResult.data.Substring(0, [Math]::Min(500, $extractResult.data.Length)) -ForegroundColor Gray
} catch {
    Write-Host "❌ 内容提取失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n2. 检查数据库中的内容..." -ForegroundColor Cyan
try {
    $dbResult = Invoke-RestMethod -Uri "http://localhost:8082/api/article/read/1984" -Method Get
    Write-Host "✅ 数据库查询成功" -ForegroundColor Green
    Write-Host "数据库内容长度: $($dbResult.data.article.contentEn.Length) 字符" -ForegroundColor White
    Write-Host "数据库内容预览:" -ForegroundColor White
    Write-Host $dbResult.data.article.contentEn.Substring(0, [Math]::Min(500, $dbResult.data.article.contentEn.Length)) -ForegroundColor Gray
} catch {
    Write-Host "❌ 数据库查询失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n3. 重新获取热点文章..." -ForegroundColor Cyan
try {
    $hotArticles = Invoke-RestMethod -Uri "http://localhost:8082/api/article/discover/trending?limit=1" -Method Post
    Write-Host "✅ 热点文章获取成功" -ForegroundColor Green
    if ($hotArticles.data.Count -gt 0) {
        $article = $hotArticles.data[0]
        Write-Host "最新文章标题: $($article.title)" -ForegroundColor White
        Write-Host "最新文章内容长度: $($article.contentEn.Length) 字符" -ForegroundColor White
        Write-Host "最新文章内容预览:" -ForegroundColor White
        Write-Host $article.contentEn.Substring(0, [Math]::Min(500, $article.contentEn.Length)) -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 热点文章获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
Write-Host "如果内容长度仍然很短，说明问题可能在Readability4J本身" -ForegroundColor Yellow
