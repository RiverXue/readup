# 测试内容提取优化效果
# 用于验证gnews和readability4j内容获取是否完整

Write-Host "=== 测试内容提取优化效果 ===" -ForegroundColor Green



# 2. 测试获取热点文章
Write-Host "`n2. 测试获取热点文章..." -ForegroundColor Yellow
try {
    $hotArticles = Invoke-RestMethod -Uri "http://localhost:8082/api/article/discover/trending?limit=3" -Method Post
    Write-Host "✅ 成功获取热点文章: $($hotArticles.data.Count) 篇" -ForegroundColor Green
    
    foreach ($article in $hotArticles.data) {
        Write-Host "`n--- 文章: $($article.title) ---" -ForegroundColor Cyan
        Write-Host "URL: $($article.url)" -ForegroundColor Gray
        Write-Host "内容长度: $($article.contentEn.Length) 字符" -ForegroundColor Gray
        Write-Host "内容预览: $($article.contentEn.Substring(0, [Math]::Min(200, $article.contentEn.Length)))..." -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 获取热点文章失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 3. 测试按分类获取文章
Write-Host "`n3. 测试按分类获取文章..." -ForegroundColor Yellow
try {
    $categoryArticles = Invoke-RestMethod -Uri "http://localhost:8082/api/article/discover/category?category=technology&limit=2" -Method Post
    Write-Host "✅ 成功获取技术类文章: $($categoryArticles.data.Count) 篇" -ForegroundColor Green
    
    foreach ($article in $categoryArticles.data) {
        Write-Host "`n--- 文章: $($article.title) ---" -ForegroundColor Cyan
        Write-Host "URL: $($article.url)" -ForegroundColor Gray
        Write-Host "内容长度: $($article.contentEn.Length) 字符" -ForegroundColor Gray
        Write-Host "内容预览: $($article.contentEn.Substring(0, [Math]::Min(200, $article.contentEn.Length)))..." -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 获取分类文章失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 4. 测试搜索文章
Write-Host "`n4. 测试搜索文章..." -ForegroundColor Yellow
try {
    $searchArticles = Invoke-RestMethod -Uri "http://localhost:8082/api/article/discover/search?keyword=AI&limit=2" -Method Post
    Write-Host "✅ 成功搜索文章: $($searchArticles.data.Count) 篇" -ForegroundColor Green
    
    foreach ($article in $searchArticles.data) {
        Write-Host "`n--- 文章: $($article.title) ---" -ForegroundColor Cyan
        Write-Host "URL: $($article.url)" -ForegroundColor Gray
        Write-Host "内容长度: $($article.contentEn.Length) 字符" -ForegroundColor Gray
        Write-Host "内容预览: $($article.contentEn.Substring(0, [Math]::Min(200, $article.contentEn.Length)))..." -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 搜索文章失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 5. 测试内容提取功能
Write-Host "`n5. 测试内容提取功能..." -ForegroundColor Yellow
try {
    $testUrl = "https://www.bbc.com/news/technology"
    $extractResult = Invoke-RestMethod -Uri "http://localhost:8082/api/article/extract-content?url=$testUrl" -Method Get
    Write-Host "✅ 成功提取内容，长度: $($extractResult.data.Length) 字符" -ForegroundColor Green
    Write-Host "内容预览: $($extractResult.data.Substring(0, [Math]::Min(300, $extractResult.data.Length)))..." -ForegroundColor Gray
} catch {
    Write-Host "❌ 内容提取失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green
Write-Host "如果内容长度明显增加且内容更完整，说明优化生效" -ForegroundColor Yellow
Write-Host "`n优化要点:" -ForegroundColor Cyan
Write-Host "- 内容验证阈值已降低" -ForegroundColor White
Write-Host "- 清理逻辑已优化" -ForegroundColor White
Write-Host "- 增加了详细调试日志" -ForegroundColor White
