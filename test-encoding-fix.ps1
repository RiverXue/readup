#!/usr/bin/env pwsh

# 测试编码修复功能
# 测试文章抓取服务是否能正确处理字符编码问题

Write-Host "🔧 测试编码修复功能" -ForegroundColor Green
Write-Host "================================" -ForegroundColor Green

# 设置环境变量
$env:SPRING_PROFILES_ACTIVE = "dev"

# 测试URL - 使用一个可能包含编码问题的URL
$testUrl = "https://www.espn.com/college-football/story/_/id/12345678/live-blog-kentucky-vs-texas"

Write-Host "📝 测试URL: $testUrl" -ForegroundColor Yellow

# 构建测试请求
$requestBody = @{
    url = $testUrl
} | ConvertTo-Json

Write-Host "🚀 发送测试请求..." -ForegroundColor Blue

try {
    # 发送请求到文章服务
    $response = Invoke-RestMethod -Uri "http://localhost:8082/api/articles/scrape" -Method POST -Body $requestBody -ContentType "application/json" -TimeoutSec 60
    
    Write-Host "✅ 请求成功!" -ForegroundColor Green
    Write-Host "📊 响应数据:" -ForegroundColor Blue
    $response | ConvertTo-Json -Depth 3
    
    # 检查内容是否包含乱码
    if ($response.content) {
        $content = $response.content
        Write-Host "📄 内容长度: $($content.Length) 字符" -ForegroundColor Yellow
        
        # 检查前500个字符是否包含乱码
        $preview = $content.Substring(0, [Math]::Min(500, $content.Length))
        Write-Host "📖 内容预览:" -ForegroundColor Yellow
        Write-Host $preview -ForegroundColor White
        
        # 检查是否包含乱码字符
        $garbageChars = $preview -match '[^\x20-\x7E\u4e00-\u9fff]'
        if ($garbageChars) {
            Write-Host "⚠️ 警告: 检测到可能的乱码字符" -ForegroundColor Red
        } else {
            Write-Host "✅ 内容看起来正常，没有乱码" -ForegroundColor Green
        }
    } else {
        Write-Host "❌ 响应中没有内容字段" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ 请求失败: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "详细错误信息:" -ForegroundColor Red
    Write-Host $_.Exception -ForegroundColor Red
}

Write-Host "`n🔍 检查服务日志..." -ForegroundColor Blue
Write-Host "请查看 article-service 的日志，确认编码检测和修复过程" -ForegroundColor Yellow
Write-Host "`n📋 修复内容总结:" -ForegroundColor Green
Write-Host "1. ✅ 添加了字符编码自动检测功能" -ForegroundColor White
Write-Host "2. ✅ 添加了乱码检测和修复逻辑" -ForegroundColor White
Write-Host "3. ✅ 改进了 Readability4J 的内容提取过程" -ForegroundColor White
Write-Host "4. ✅ 添加了备用内容提取方法" -ForegroundColor White
Write-Host "5. ✅ 增强了错误处理和日志记录" -ForegroundColor White
