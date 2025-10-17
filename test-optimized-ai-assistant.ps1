# AI学习助手优化测试脚本
# 测试优化后的AI助手功能和用户体验

Write-Host "🎓 AI学习助手优化测试" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

# 配置
$baseUrl = "http://localhost:8080"
$chatEndpoint = "$baseUrl/api/ai/assistant/chat"

# 测试用例
$testCases = @(
    @{
        Name = "基础问答测试"
        Question = "这篇文章的主要观点是什么？"
        ExpectedKeywords = @("观点", "主要", "内容", "文章")
    },
    @{
        Name = "词汇学习测试"
        Question = "有哪些重要的词汇需要学习？"
        ExpectedKeywords = @("词汇", "单词", "重要", "学习")
    },
    @{
        Name = "语法分析测试"
        Question = "请解释这个句子的语法结构"
        ExpectedKeywords = @("语法", "结构", "句子", "解释")
    },
    @{
        Name = "翻译功能测试"
        Question = "请帮我翻译这个段落"
        ExpectedKeywords = @("翻译", "段落", "中文", "英文")
    },
    @{
        Name = "写作风格测试"
        Question = "这篇文章的写作风格有什么特点？"
        ExpectedKeywords = @("写作", "风格", "特点", "修辞")
    }
)

# 测试函数
function Test-AIAssistant {
    param(
        [string]$Question,
        [string]$TestName,
        [array]$ExpectedKeywords
    )
    
    Write-Host "`n🧪 测试: $TestName" -ForegroundColor Yellow
    Write-Host "问题: $Question" -ForegroundColor Gray
    
    $requestBody = @{
        question = $Question
        userId = 1
        articleContext = "This is a sample article about artificial intelligence and its impact on education. The article discusses how AI can help students learn more effectively and provide personalized learning experiences."
    } | ConvertTo-Json -Depth 3
    
    try {
        Write-Host "🔄 发送请求..." -ForegroundColor Gray
        
        $response = Invoke-RestMethod -Uri $chatEndpoint -Method POST -Body $requestBody -ContentType "application/json" -TimeoutSec 30
        
        if ($response.success -eq $true -and $response.data) {
            Write-Host "✅ 响应成功!" -ForegroundColor Green
            
            $answer = $response.data.answer
            $followUp = $response.data.followUpQuestion
            
            Write-Host "📄 回答内容:" -ForegroundColor White
            Write-Host $answer -ForegroundColor Gray
            
            Write-Host "`n💡 后续建议:" -ForegroundColor Yellow
            Write-Host $followUp -ForegroundColor Gray
            
            # 检查回答质量
            $qualityScore = 0
            foreach ($keyword in $ExpectedKeywords) {
                if ($answer -match $keyword) {
                    $qualityScore++
                    Write-Host "✅ 包含关键词: $keyword" -ForegroundColor Green
                } else {
                    Write-Host "❌ 缺少关键词: $keyword" -ForegroundColor Red
                }
            }
            
            $qualityPercentage = ($qualityScore / $ExpectedKeywords.Count) * 100
            Write-Host "`n📊 回答质量评分: $qualityPercentage%" -ForegroundColor Cyan
            
            # 检查回答长度
            $answerLength = $answer.Length
            Write-Host "📏 回答长度: $answerLength 字符" -ForegroundColor Cyan
            
            # 检查是否包含教学元素
            $teachingElements = @("建议", "学习", "练习", "理解", "技巧", "方法")
            $teachingScore = 0
            foreach ($element in $teachingElements) {
                if ($answer -match $element) {
                    $teachingScore++
                }
            }
            Write-Host "🎓 教学元素: $teachingScore/6" -ForegroundColor Cyan
            
            return @{
                Success = $true
                QualityScore = $qualityPercentage
                AnswerLength = $answerLength
                TeachingScore = $teachingScore
            }
        } else {
            Write-Host "❌ 响应失败!" -ForegroundColor Red
            Write-Host "错误信息: $($response.message)" -ForegroundColor Red
            return @{ Success = $false }
        }
    }
    catch {
        Write-Host "❌ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
        return @{ Success = $false }
    }
}

# 执行测试
$results = @()
foreach ($testCase in $testCases) {
    $result = Test-AIAssistant -Question $testCase.Question -TestName $testCase.Name -ExpectedKeywords $testCase.ExpectedKeywords
    $results += @{
        TestName = $testCase.Name
        Result = $result
    }
}

# 生成测试报告
Write-Host "`n📊 测试报告" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

$successCount = 0
$totalQualityScore = 0
$totalAnswerLength = 0
$totalTeachingScore = 0

foreach ($result in $results) {
    if ($result.Result.Success) {
        $successCount++
        $totalQualityScore += $result.Result.QualityScore
        $totalAnswerLength += $result.Result.AnswerLength
        $totalTeachingScore += $result.Result.TeachingScore
        
        Write-Host "✅ $($result.TestName): 通过" -ForegroundColor Green
        Write-Host "   质量评分: $($result.Result.QualityScore)%" -ForegroundColor Gray
        Write-Host "   回答长度: $($result.Result.AnswerLength) 字符" -ForegroundColor Gray
        Write-Host "   教学元素: $($result.Result.TeachingScore)/6" -ForegroundColor Gray
    } else {
        Write-Host "❌ $($result.TestName): 失败" -ForegroundColor Red
    }
}

$successRate = ($successCount / $results.Count) * 100
$avgQualityScore = if ($successCount -gt 0) { $totalQualityScore / $successCount } else { 0 }
$avgAnswerLength = if ($successCount -gt 0) { $totalAnswerLength / $successCount } else { 0 }
$avgTeachingScore = if ($successCount -gt 0) { $totalTeachingScore / $successCount } else { 0 }

Write-Host "`n📈 总体统计" -ForegroundColor Cyan
Write-Host "成功率: $successRate%" -ForegroundColor White
Write-Host "平均质量评分: $([math]::Round($avgQualityScore, 1))%" -ForegroundColor White
Write-Host "平均回答长度: $([math]::Round($avgAnswerLength, 0)) 字符" -ForegroundColor White
Write-Host "平均教学元素: $([math]::Round($avgTeachingScore, 1))/6" -ForegroundColor White

# 优化建议
Write-Host "`n💡 优化建议" -ForegroundColor Yellow
if ($avgQualityScore -lt 70) {
    Write-Host "⚠️  回答质量需要提升，建议优化提示词" -ForegroundColor Red
}
if ($avgAnswerLength -lt 100) {
    Write-Host "⚠️  回答长度偏短，建议增加详细解释" -ForegroundColor Red
}
if ($avgTeachingScore -lt 3) {
    Write-Host "⚠️  教学元素不足，建议增加学习建议" -ForegroundColor Red
}
if ($successRate -eq 100 -and $avgQualityScore -gt 80 -and $avgTeachingScore -gt 4) {
    Write-Host "🎉 AI助手优化效果良好！" -ForegroundColor Green
}

Write-Host "`n测试完成！" -ForegroundColor Cyan
