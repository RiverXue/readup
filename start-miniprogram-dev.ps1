# XReadUp 微信小程序开发启动脚本

Write-Host "🚀 启动 XReadUp 微信小程序开发环境..." -ForegroundColor Green

# 检查Node.js版本
Write-Host "📋 检查开发环境..." -ForegroundColor Yellow
$nodeVersion = node --version
if ($nodeVersion -match "v(\d+)\.") {
    $majorVersion = [int]$matches[1]
    if ($majorVersion -lt 16) {
        Write-Host "❌ Node.js版本过低，需要 >= 16.0.0，当前版本: $nodeVersion" -ForegroundColor Red
        exit 1
    }
}
Write-Host "✅ Node.js版本: $nodeVersion" -ForegroundColor Green

# 检查npm版本
$npmVersion = npm --version
Write-Host "✅ npm版本: $npmVersion" -ForegroundColor Green

# 进入项目目录
Set-Location "xreadup-miniprogram"

# 检查是否已安装依赖
if (-not (Test-Path "node_modules")) {
    Write-Host "📦 安装项目依赖..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 依赖安装失败" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ 依赖安装完成" -ForegroundColor Green
} else {
    Write-Host "✅ 依赖已安装" -ForegroundColor Green
}

# 检查微信开发者工具
Write-Host "🔍 检查微信开发者工具..." -ForegroundColor Yellow
$wechatDevTools = Get-Process "wechatdevtools" -ErrorAction SilentlyContinue
if ($wechatDevTools) {
    Write-Host "✅ 微信开发者工具已运行" -ForegroundColor Green
} else {
    Write-Host "⚠️  微信开发者工具未运行，请手动启动" -ForegroundColor Yellow
}

# 启动开发服务器
Write-Host "🚀 启动开发服务器..." -ForegroundColor Yellow
Write-Host "📱 微信小程序开发模式" -ForegroundColor Cyan
Write-Host "🌐 H5开发模式" -ForegroundColor Cyan
Write-Host "📊 开发服务器将在 http://localhost:3000 启动" -ForegroundColor Cyan
Write-Host "🔗 微信开发者工具请导入项目目录: $(Get-Location)" -ForegroundColor Cyan

# 启动开发服务器
npm run dev:mp-weixin

Write-Host "🎉 开发环境启动完成！" -ForegroundColor Green
Write-Host "📝 开发提示:" -ForegroundColor Yellow
Write-Host "   - 修改代码后会自动重新编译" -ForegroundColor White
Write-Host "   - 在微信开发者工具中预览效果" -ForegroundColor White
Write-Host "   - 使用 Ctrl+C 停止开发服务器" -ForegroundColor White
