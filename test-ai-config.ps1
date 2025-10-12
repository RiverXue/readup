# 简单的YAML语法测试脚本
Write-Host "🔍 测试AI服务YAML配置..." -ForegroundColor Cyan

# 检查AI服务的application.yml
$aiConfigFile = "xreadup\ai-service\src\main\resources\application.yml"

if (Test-Path $aiConfigFile) {
    Write-Host "✅ AI服务配置文件存在" -ForegroundColor Green
    
    # 读取文件内容
    $content = Get-Content $aiConfigFile -Raw
    
    # 检查是否有重复的spring键
    $springCount = ($content -split "spring:").Count - 1
    Write-Host "📊 发现 $springCount 个 'spring:' 键" -ForegroundColor $(if ($springCount -eq 1) { "Green" } else { "Red" })
    
    if ($springCount -gt 1) {
        Write-Host "❌ 发现重复的spring键！" -ForegroundColor Red
    } else {
        Write-Host "✅ spring键配置正确" -ForegroundColor Green
    }
    
    # 检查MyBatis Plus配置是否在spring块内
    $lines = Get-Content $aiConfigFile
    $inSpringBlock = $false
    $mybatisPlusInSpring = $false
    
    foreach ($line in $lines) {
        $trimmedLine = $line.Trim()
        
        if ($trimmedLine -eq "spring:") {
            $inSpringBlock = $true
        }
        elseif ($trimmedLine -match "^[a-zA-Z][a-zA-Z0-9_-]*:" -and $trimmedLine -notmatch "^\s") {
            $inSpringBlock = $false
        }
        elseif ($trimmedLine -eq "mybatis-plus:" -and $inSpringBlock) {
            $mybatisPlusInSpring = $true
        }
    }
    
    Write-Host "📊 MyBatis Plus配置位置: $(if ($mybatisPlusInSpring) { '在spring块内 ✅' } else { '在spring块外 ❌' })" -ForegroundColor $(if ($mybatisPlusInSpring) { "Green" } else { "Red" })
    
} else {
    Write-Host "❌ AI服务配置文件不存在" -ForegroundColor Red
}

Write-Host "`n✨ 检查完成！" -ForegroundColor Cyan
