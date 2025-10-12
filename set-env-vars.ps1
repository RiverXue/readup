# XReadUp 环境变量设置脚本
# 将.env文件中的内容设置到系统环境变量中

Write-Host "🚀 开始设置XReadUp环境变量..." -ForegroundColor Green

# 检查.env文件是否存在
if (-not (Test-Path ".env")) {
    Write-Host "❌ 错误: .env文件不存在！" -ForegroundColor Red
    Write-Host "请确保.env文件存在于当前目录中。" -ForegroundColor Yellow
    exit 1
}

Write-Host "📁 找到.env文件，开始读取配置..." -ForegroundColor Cyan

# 读取.env文件并设置环境变量
$envVars = @()
$lineNumber = 0

Get-Content ".env" | ForEach-Object {
    $lineNumber++
    $line = $_.Trim()
    
    # 跳过空行和注释行
    if ($line -eq "" -or $line.StartsWith("#")) {
        return
    }
    
    # 检查是否包含等号
    if ($line -match "^([^=]+)=(.*)$") {
        $key = $matches[1].Trim()
        $value = $matches[2].Trim()
        
        # 移除值两端的引号（如果存在）
        if ($value.StartsWith('"') -and $value.EndsWith('"')) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        elseif ($value.StartsWith("'") -and $value.EndsWith("'")) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        
        # 设置环境变量
        try {
            [Environment]::SetEnvironmentVariable($key, $value, "User")
            $envVars += $key
            Write-Host "✅ 设置环境变量: $key = $value" -ForegroundColor Green
        }
        catch {
            Write-Host "❌ 设置环境变量失败: $key - $($_.Exception.Message)" -ForegroundColor Red
        }
    }
    else {
        Write-Host "⚠️  跳过无效行 $lineNumber : $line" -ForegroundColor Yellow
    }
}

Write-Host "`n🎉 环境变量设置完成！" -ForegroundColor Green
Write-Host "📊 总共设置了 $($envVars.Count) 个环境变量:" -ForegroundColor Cyan

# 显示设置的环境变量
$envVars | ForEach-Object {
    Write-Host "   - $_" -ForegroundColor White
}

Write-Host "`n💡 提示:" -ForegroundColor Yellow
Write-Host "1. 环境变量已设置到用户级别" -ForegroundColor White
Write-Host "2. 需要重新启动命令行窗口或IDE才能生效" -ForegroundColor White
Write-Host "3. 或者运行 'refreshenv' 命令刷新环境变量" -ForegroundColor White

Write-Host "`n🔧 验证环境变量是否设置成功:" -ForegroundColor Cyan
Write-Host "运行以下命令验证:" -ForegroundColor White
Write-Host "echo `$env:MYSQL_PASSWORD" -ForegroundColor Gray
Write-Host "echo `$env:JWT_SECRET" -ForegroundColor Gray
Write-Host "echo `$env:GATEWAY_PORT" -ForegroundColor Gray
