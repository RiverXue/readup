# XReadUp 环境变量设置脚本 (当前会话版本)
# 将.env文件中的内容设置到当前PowerShell会话的环境变量中

Write-Host "🚀 开始设置XReadUp环境变量到当前会话..." -ForegroundColor Green

# 检查.env文件是否存在
if (-not (Test-Path ".env")) {
    Write-Host "❌ 错误: .env文件不存在！" -ForegroundColor Red
    Write-Host "请确保.env文件存在于当前目录中。" -ForegroundColor Yellow
    return
}

Write-Host "📁 找到.env文件，开始读取配置..." -ForegroundColor Cyan

# 读取.env文件并设置环境变量到当前会话
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
        
        # 设置环境变量到当前会话
        try {
            Set-Item -Path "env:$key" -Value $value
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
Write-Host "📊 总共设置了 $($envVars.Count) 个环境变量到当前会话:" -ForegroundColor Cyan

# 显示设置的环境变量
$envVars | ForEach-Object {
    Write-Host "   - $_" -ForegroundColor White
}

Write-Host "`n🔧 验证环境变量是否设置成功:" -ForegroundColor Cyan
Write-Host "MYSQL_PASSWORD = $env:MYSQL_PASSWORD" -ForegroundColor White
Write-Host "JWT_SECRET = $env:JWT_SECRET" -ForegroundColor White
Write-Host "GATEWAY_PORT = $env:GATEWAY_PORT" -ForegroundColor White

Write-Host "`n💡 提示:" -ForegroundColor Yellow
Write-Host "环境变量已设置到当前PowerShell会话中，可以立即使用。" -ForegroundColor White
Write-Host "要永久设置，请运行 set-env-vars.ps1 脚本。" -ForegroundColor White
