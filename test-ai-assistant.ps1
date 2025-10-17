# AI助手功能测试脚本
# 测试AI助手是否能正常回答问题

Write-Host "🤖 开始测试AI助手功能..." -ForegroundColor Green

# 测试数据
$testQuestions = @(
    "这篇文章讲了什么？",
    "technology这个单词是什么意思？",
    "请翻译这个句子：Technology is changing our lives.",
    "这篇文章的语法有什么特点？",
    "你能帮我分析一下这篇文章的难度吗？"
)

# API端点
$baseUrl = "http://localhost:8084"
$chatEndpoint = "$baseUrl/api/ai/assistant/chat"

# 测试文章内容
$articleContent = @"
Artificial Intelligence and Machine Learning

Artificial intelligence (AI) and machine learning (ML) are rapidly transforming the way we live and work. These technologies have the potential to revolutionize industries, from healthcare to transportation, and create new opportunities for innovation.

Machine learning, a subset of AI, enables computers to learn and improve from experience without being explicitly programmed. This technology is already being used in various applications, such as recommendation systems, image recognition, and natural language processing.

The development of AI and ML technologies requires significant investment in research and development. Companies and governments around the world are investing billions of dollars to advance these technologies and ensure they are used responsibly.

As these technologies continue to evolve, it is important to consider their ethical implications and ensure they are developed and deployed in a way that benefits society as a whole.
"@

Write-Host "📝 测试文章内容长度: $($articleContent.Length) 字符" -ForegroundColor Yellow

foreach ($question in $testQuestions) {
    Write-Host "`n❓ 测试问题: $question" -ForegroundColor Cyan
    
    # 构建请求体
    $requestBody = @{
        question = $question
        userId = 1
        articleContext = $articleContent
    } | ConvertTo-Json -Depth 10
    
    try {
        Write-Host "🔄 发送请求到: $chatEndpoint" -ForegroundColor Gray
        
        # 发送POST请求
        $response = Invoke-RestMethod -Uri $chatEndpoint -Method POST -Body $requestBody -ContentType "application/json" -TimeoutSec 30
        
        if ($response.success -eq $true -and $response.data) {
            Write-Host "✅ 响应成功!" -ForegroundColor Green
            Write-Host "📄 回答内容: $($response.data.answer)" -ForegroundColor White
            Write-Host "💡 后续问题: $($response.data.followUpQuestion)" -ForegroundColor Yellow
            Write-Host "📊 难度等级: $($response.data.difficulty)" -ForegroundColor Magenta
        } else {
            Write-Host "❌ 响应失败: $($response.message)" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "❌ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            $errorStream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errorStream)
            $errorBody = $reader.ReadToEnd()
            Write-Host "错误详情: $errorBody" -ForegroundColor Red
        }
    }
    
    Write-Host "⏳ 等待2秒..." -ForegroundColor Gray
    Start-Sleep -Seconds 2
}

Write-Host "`n🎉 AI助手功能测试完成!" -ForegroundColor Green
Write-Host "如果所有测试都显示'响应成功'，说明AI助手功能正常。" -ForegroundColor Yellow
Write-Host "如果出现错误，请检查AI服务是否正常运行。" -ForegroundColor Red
