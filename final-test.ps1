# 二级词库和Function Calling最终测试脚本
Write-Host "=== 二级词库策略升级最终验证 ===" -ForegroundColor Green

# 测试1: 二级词库智能查询（本地缓存优先 + AI兜底）
Write-Host "`n1. 测试二级词库智能查询..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/vocabulary/lookup" -Method Post -Body '{
        "word": "algorithm",
        "context": "计算机科学",
        "userId": 1,
        "articleId": 1001
    }' -ContentType "application/json"
    
    Write-Host "✅ 查询成功: $($response.message)" -ForegroundColor Green
    Write-Host "   单词: $($response.data.word)" -ForegroundColor Cyan
    Write-Host "   释义: $($response.data.meaning)" -ForegroundColor Cyan
    Write-Host "   例句: $($response.data.example)" -ForegroundColor Cyan
    Write-Host "   来源: $($response.data.source)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 查询失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试2: 获取词库统计
Write-Host "`n2. 测试获取用户词库统计..." -ForegroundColor Yellow
try {
    $stats = Invoke-RestMethod -Uri "http://localhost:8081/api/vocabulary/stats/1" -Method Get
    Write-Host "✅ 统计获取成功" -ForegroundColor Green
    Write-Host "   总词汇量: $($stats.data.totalWords)" -ForegroundColor Cyan
    Write-Host "   新词: $($stats.data.newWords)" -ForegroundColor Cyan
    Write-Host "   学习中: $($stats.data.learningWords)" -ForegroundColor Cyan
    Write-Host "   已掌握: $($stats.data.masteredWords)" -ForegroundColor Cyan
    Write-Host "   本地词汇: $($stats.data.localWords)" -ForegroundColor Cyan
    Write-Host "   AI生成词汇: $($stats.data.aiWords)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ 统计获取失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试3: Function Calling - AI单词查询
Write-Host "`n3. 测试Function Calling单词查询..." -ForegroundColor Yellow
try {
    $aiWord = Invoke-RestMethod -Uri "http://localhost:8084/api/ai/assistant/word/quantum" -Method Get
    Write-Host "✅ AI单词查询成功" -ForegroundColor Green
    Write-Host "   单词: $($aiWord.data.word)" -ForegroundColor Cyan
    Write-Host "   音标: $($aiWord.data.phonetic)" -ForegroundColor Cyan
    Write-Host "   释义: $($aiWord.data.meaning)" -ForegroundColor Cyan
    Write-Host "   例句: $($aiWord.data.example)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ AI单词查询失败: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试4: 批量查询单词
Write-Host "`n4. 测试批量单词查询..." -ForegroundColor Yellow
try {
    $batchResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/vocabulary/batch-lookup" -Method Post -Body '{
        "words": ["machine", "learning", "neural", "network"],
        "context": "人工智能",
        "userId": 1,
        "articleId": 1002
    }' -ContentType "application/json"
    
    Write-Host "✅ 批量查询成功: $($batchResponse.message)" -ForegroundColor Green
    Write-Host "   返回单词数量: $($batchResponse.data.Count)" -ForegroundColor Cyan
    foreach ($word in $batchResponse.data) {
        Write-Host "   - $($word.word): $($word.meaning.Substring(0, [Math]::Min(50, $word.meaning.Length)))..." -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ 批量查询失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 验证完成 ===" -ForegroundColor Green