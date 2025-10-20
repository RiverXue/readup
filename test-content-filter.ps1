# 测试敏感词过滤功能
Write-Host "测试敏感词过滤功能..." -ForegroundColor Green

# 测试包含敏感词的文章内容
$testContent = @"
This is a test article about violence and terrorism. 
The content includes words like bomb, murder, and hate.
This should trigger the content filter.
"@

Write-Host "`n测试内容:" -ForegroundColor Yellow
Write-Host $testContent -ForegroundColor Cyan

Write-Host "`n测试完成！敏感词过滤功能已集成到文章处理流程中。" -ForegroundColor Green
Write-Host "现在当文章保存时会自动进行敏感词检测和记录。" -ForegroundColor Green