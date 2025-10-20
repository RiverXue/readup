# 文章管理功能测试脚本
# 测试文章管理和敏感词记录功能

Write-Host "=== 文章管理功能测试 ===" -ForegroundColor Green

# 设置环境变量
$env:SPRING_PROFILES_ACTIVE = "dev"
$env:ADMIN_SERVICE_PORT = "8080"
$env:ARTICLE_SERVICE_PORT = "8081"

# 测试文章列表获取
Write-Host "`n1. 测试获取文章列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/list?page=1&pageSize=10" -Method GET
    Write-Host "文章列表获取成功，共 $($response.data.Count) 篇文章" -ForegroundColor Green
    if ($response.data.Count -gt 0) {
        $firstArticle = $response.data[0]
        Write-Host "第一篇文章: $($firstArticle.title)" -ForegroundColor Cyan
        $articleId = $firstArticle.id
    }
} catch {
    Write-Host "文章列表获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试文章详情获取
if ($articleId) {
    Write-Host "`n2. 测试获取文章详情..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/$articleId" -Method GET
        Write-Host "文章详情获取成功: $($response.data.title)" -ForegroundColor Green
    } catch {
        Write-Host "文章详情获取失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 测试文章分类获取
Write-Host "`n3. 测试获取文章分类列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/categories" -Method GET
    Write-Host "文章分类获取成功，共 $($response.data.Count) 个分类" -ForegroundColor Green
    $response.data | ForEach-Object { Write-Host "  - $_" -ForegroundColor Cyan }
} catch {
    Write-Host "文章分类获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试文章难度获取
Write-Host "`n4. 测试获取文章难度列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/difficulties" -Method GET
    Write-Host "文章难度获取成功，共 $($response.data.Count) 个难度等级" -ForegroundColor Green
    $response.data | ForEach-Object { Write-Host "  - $_" -ForegroundColor Cyan }
} catch {
    Write-Host "文章难度获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试敏感词记录功能
Write-Host "`n5. 测试敏感词记录功能..." -ForegroundColor Yellow

# 记录一条测试过滤日志
if ($articleId) {
    try {
        $filterData = @{
            articleId = $articleId
            filterType = "sensitive_word"
            matchedContent = "测试敏感词"
            filterReason = "测试敏感词过滤"
            severityLevel = "medium"
            actionTaken = "blocked"
            adminId = 1
        }
        
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/filter-logs" -Method POST -Body $filterData
        Write-Host "敏感词记录创建成功" -ForegroundColor Green
    } catch {
        Write-Host "敏感词记录创建失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 获取过滤记录列表
Write-Host "`n6. 测试获取过滤记录列表..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/filter-logs?page=1&pageSize=10" -Method GET
    Write-Host "过滤记录列表获取成功，共 $($response.data.records.Count) 条记录" -ForegroundColor Green
    if ($response.data.records.Count -gt 0) {
        $firstLog = $response.data.records[0]
        Write-Host "第一条记录: $($firstLog.filterType) - $($firstLog.matchedContent)" -ForegroundColor Cyan
        $logId = $firstLog.id
    }
} catch {
    Write-Host "过滤记录列表获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 获取文章过滤记录
if ($articleId) {
    Write-Host "`n7. 测试获取文章过滤记录..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/$articleId/filter-logs" -Method GET
        Write-Host "文章过滤记录获取成功，共 $($response.data.Count) 条记录" -ForegroundColor Green
    } catch {
        Write-Host "文章过滤记录获取失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 获取过滤统计信息
Write-Host "`n8. 测试获取过滤统计信息..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/filter-logs/statistics" -Method GET
    Write-Host "过滤统计信息获取成功" -ForegroundColor Green
    Write-Host "今日新增记录: $($response.data.todayCount)" -ForegroundColor Cyan
} catch {
    Write-Host "过滤统计信息获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试更新过滤记录状态
if ($logId) {
    Write-Host "`n9. 测试更新过滤记录状态..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/admin/articles/filter-logs/$logId/status?status=resolved&adminId=1" -Method PUT
        Write-Host "过滤记录状态更新成功" -ForegroundColor Green
    } catch {
        Write-Host "过滤记录状态更新失败: $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n=== 文章管理功能测试完成 ===" -ForegroundColor Green
