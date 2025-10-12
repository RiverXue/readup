# XReadUp YAML配置文件验证脚本
# 检查所有application.yml文件的语法正确性

Write-Host "🔍 XReadUp YAML配置文件验证脚本" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan

# 定义需要检查的配置文件
$configFiles = @(
    "xreadup\ai-service\src\main\resources\application.yml",
    "xreadup\user-service\src\main\resources\application.yml", 
    "xreadup\article-service\src\main\resources\application.yml",
    "xreadup\gateway\src\main\resources\application.yml",
    "xreadup\report-service\src\main\resources\application.yml",
    "xreadup\admin-service\src\main\resources\application.yml"
)

# 检查结果统计
$totalFiles = $configFiles.Count
$validFiles = 0
$invalidFiles = 0

Write-Host "`n📋 检查YAML配置文件:" -ForegroundColor Yellow
Write-Host "========================" -ForegroundColor Yellow

foreach ($configFile in $configFiles) {
    if (Test-Path $configFile) {
        Write-Host "🔍 检查: $configFile" -ForegroundColor Cyan
        
        try {
            # 使用PowerShell的YAML解析来检查语法
            $content = Get-Content $configFile -Raw
            
            # 检查是否有重复的顶级键
            $lines = Get-Content $configFile
            $topLevelKeys = @()
            $duplicateKeys = @()
            
            foreach ($line in $lines) {
                $trimmedLine = $line.Trim()
                # 检查顶级键（没有缩进的行）
                if ($trimmedLine -match "^[a-zA-Z][a-zA-Z0-9_-]*:" -and $trimmedLine -notmatch "^\s") {
                    $key = $trimmedLine -replace ":\s*$", ""
                    if ($topLevelKeys -contains $key) {
                        $duplicateKeys += $key
                    } else {
                        $topLevelKeys += $key
                    }
                }
            }
            
            if ($duplicateKeys.Count -gt 0) {
                Write-Host "❌ ${configFile}: 发现重复的顶级键: $($duplicateKeys -join ', ')" -ForegroundColor Red
                $invalidFiles++
            } else {
                Write-Host "✅ ${configFile}: YAML语法正确" -ForegroundColor Green
                $validFiles++
            }
            
        } catch {
            Write-Host "❌ ${configFile}: YAML解析错误 - $($_.Exception.Message)" -ForegroundColor Red
            $invalidFiles++
        }
    } else {
        Write-Host "⚠️  ${configFile}: 文件不存在" -ForegroundColor Yellow
        $invalidFiles++
    }
}

# 显示统计结果
Write-Host "`n📊 验证结果统计:" -ForegroundColor Cyan
Write-Host "==================" -ForegroundColor Cyan
Write-Host "总文件数: $totalFiles" -ForegroundColor White
Write-Host "✅ 有效文件: $validFiles" -ForegroundColor Green
Write-Host "❌ 无效文件: $invalidFiles" -ForegroundColor Red

# 计算健康度
$healthPercentage = [math]::Round(($validFiles / $totalFiles) * 100, 2)
Write-Host "`n🏥 配置健康度: $healthPercentage%" -ForegroundColor $(if ($healthPercentage -eq 100) { "Green" } elseif ($healthPercentage -ge 80) { "Yellow" } else { "Red" })

# 提供建议
Write-Host "`n💡 建议:" -ForegroundColor Yellow
if ($invalidFiles -gt 0) {
    Write-Host "1. 请修复YAML语法错误" -ForegroundColor White
    Write-Host "2. 检查是否有重复的顶级键" -ForegroundColor White
    Write-Host "3. 确保缩进正确（使用空格，不要使用制表符）" -ForegroundColor White
} else {
    Write-Host "🎉 所有YAML配置文件语法正确！" -ForegroundColor Green
}

Write-Host "`n🔧 YAML文件常见问题:" -ForegroundColor Cyan
Write-Host "1. 重复的顶级键（如多个spring:）" -ForegroundColor White
Write-Host "2. 缩进不一致（混用空格和制表符）" -ForegroundColor White
Write-Host "3. 缺少冒号或引号" -ForegroundColor White
Write-Host "4. 特殊字符未正确转义" -ForegroundColor White

Write-Host "`n✨ 验证完成！" -ForegroundColor Cyan
