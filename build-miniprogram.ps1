# XReadUp 微信小程序构建脚本

Write-Host "🏗️  构建 XReadUp 微信小程序..." -ForegroundColor Green

# 进入项目目录
Set-Location "xreadup-miniprogram"

# 检查项目是否存在
if (-not (Test-Path "package.json")) {
    Write-Host "❌ 项目目录不存在或package.json文件缺失" -ForegroundColor Red
    exit 1
}

# 清理之前的构建
Write-Host "🧹 清理之前的构建文件..." -ForegroundColor Yellow
if (Test-Path "dist") {
    Remove-Item "dist" -Recurse -Force
    Write-Host "✅ 清理完成" -ForegroundColor Green
}

# 安装依赖（如果需要）
if (-not (Test-Path "node_modules")) {
    Write-Host "📦 安装项目依赖..." -ForegroundColor Yellow
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ 依赖安装失败" -ForegroundColor Red
        exit 1
    }
}

# 类型检查
Write-Host "🔍 执行TypeScript类型检查..." -ForegroundColor Yellow
npm run type-check
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 类型检查失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 类型检查通过" -ForegroundColor Green

# 代码检查
Write-Host "🔍 执行ESLint代码检查..." -ForegroundColor Yellow
npm run lint
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 代码检查失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 代码检查通过" -ForegroundColor Green

# 构建微信小程序
Write-Host "📱 构建微信小程序..." -ForegroundColor Yellow
npm run build:mp-weixin
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ 微信小程序构建失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ 微信小程序构建完成" -ForegroundColor Green

# 构建H5版本
Write-Host "🌐 构建H5版本..." -ForegroundColor Yellow
npm run build:h5
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ H5版本构建失败" -ForegroundColor Red
    exit 1
}
Write-Host "✅ H5版本构建完成" -ForegroundColor Green

# 检查构建结果
Write-Host "📊 检查构建结果..." -ForegroundColor Yellow

if (Test-Path "dist/build/mp-weixin") {
    $mpWeixinSize = (Get-ChildItem "dist/build/mp-weixin" -Recurse | Measure-Object -Property Length -Sum).Sum
    $mpWeixinSizeMB = [math]::Round($mpWeixinSize / 1MB, 2)
    Write-Host "✅ 微信小程序构建成功，大小: ${mpWeixinSizeMB}MB" -ForegroundColor Green
} else {
    Write-Host "❌ 微信小程序构建文件不存在" -ForegroundColor Red
}

if (Test-Path "dist/build/h5") {
    $h5Size = (Get-ChildItem "dist/build/h5" -Recurse | Measure-Object -Property Length -Sum).Sum
    $h5SizeMB = [math]::Round($h5Size / 1MB, 2)
    Write-Host "✅ H5版本构建成功，大小: ${h5SizeMB}MB" -ForegroundColor Green
} else {
    Write-Host "❌ H5版本构建文件不存在" -ForegroundColor Red
}

# 生成构建报告
$buildReport = @"
# XReadUp 微信小程序构建报告

## 构建时间
$(Get-Date -Format "yyyy-MM-dd HH:mm:ss")

## 构建结果
- 微信小程序: $(if (Test-Path "dist/build/mp-weixin") { "✅ 成功" } else { "❌ 失败" })
- H5版本: $(if (Test-Path "dist/build/h5") { "✅ 成功" } else { "❌ 失败" })

## 构建文件大小
- 微信小程序: ${mpWeixinSizeMB}MB
- H5版本: ${h5SizeMB}MB

## 下一步操作
1. 在微信开发者工具中导入 dist/build/mp-weixin 目录
2. 上传代码到微信小程序后台
3. 提交审核
4. 发布上线

## 注意事项
- 确保在微信小程序后台配置正确的AppID
- 检查API接口地址是否正确
- 测试所有功能是否正常工作
"@

$buildReport | Out-File "build-report.md" -Encoding UTF8
Write-Host "📄 构建报告已生成: build-report.md" -ForegroundColor Green

Write-Host "🎉 构建完成！" -ForegroundColor Green
Write-Host "📱 微信小程序构建文件: dist/build/mp-weixin" -ForegroundColor Cyan
Write-Host "🌐 H5版本构建文件: dist/build/h5" -ForegroundColor Cyan
Write-Host "📄 构建报告: build-report.md" -ForegroundColor Cyan
