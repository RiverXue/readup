 ReadUp AI - 完整项目测试脚本
Write-Host "🚀 ReadUp AI 完整项目测试开始..." -ForegroundColor Green

# 等待服务启动
Write-Host "⏳ 等待服务启动完成..." -ForegroundColor Yellow
Start-Sleep -Seconds 30

# 测试基础设施
Write-Host "🔍 测试基础设施连接..." -ForegroundColor Cyan

# MySQL测试
Write-Host "  测试MySQL连接..." -ForegroundColor Gray
try {
    $mysqlTest = Test-NetConnection -ComputerName localhost -Port 3307
    if ($mysqlTest.TcpTestSucceeded) {
        Write-Host "  ✅ MySQL 运行正常" -ForegroundColor Green
    } else {
        Write-Host "  ❌ MySQL 连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ MySQL 测试异常" -ForegroundColor Red
}

# Redis测试
Write-Host "  测试Redis连接..." -ForegroundColor Gray
try {
    $redisTest = Test-NetConnection -ComputerName localhost -Port 6379
    if ($redisTest.TcpTestSucceeded) {
        Write-Host "  ✅ Redis 运行正常" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Redis 连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ Redis 测试异常" -ForegroundColor Red
}

# Nacos测试
Write-Host "  测试Nacos连接..." -ForegroundColor Gray
try {
    $nacosTest = Test-NetConnection -ComputerName localhost -Port 8848
    if ($nacosTest.TcpTestSucceeded) {
        Write-Host "  ✅ Nacos 运行正常" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Nacos 连接失败" -ForegroundColor Red
    }
} catch {
    Write-Host "  ❌ Nacos 测试异常" -ForegroundColor Red
}

# 测试微服务端口
Write-Host "🔍 测试微服务端口..." -ForegroundColor Cyan
$services = @(
    @{Name="用户服务"; Port=8081},
    @{Name="文章服务"; Port=8082},
    @{Name="AI服务"; Port=8083},
    @{Name="报告服务"; Port=8084},
    @{Name="网关服务"; Port=8080}
)

foreach ($service in $services) {
    Write-Host "  测试$($service.Name)端口$($service.Port)..." -ForegroundColor Gray
    try {
        $test = Test-NetConnection -ComputerName localhost -Port $service.Port
        if ($test.TcpTestSucceeded) {
            Write-Host "  ✅ $($service.Name) 端口正常" -ForegroundColor Green
        } else {
            Write-Host "  ⚠️ $($service.Name) 端口未就绪" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "  ⚠️ $($service.Name) 测试异常" -ForegroundColor Yellow
    }
}

# 测试API端点
Write-Host "🔍 测试API端点..." -ForegroundColor Cyan
Start-Sleep -Seconds 5

$endpoints = @(
    @{Name="网关健康"; Url="http://localhost:8080/actuator/health"},
    @{Name="用户注册"; Url="http://localhost:8080/api/user/register"},
    @{Name="文章探索"; Url="http://localhost:8080/api/article/explore"},
    @{Name="AI翻译"; Url="http://localhost:8080/api/ai/translate/smart"},
    @{Name="API文档"; Url="http://localhost:8080/doc.html"}
)

foreach ($endpoint in $endpoints) {
    Write-Host "  测试$($endpoint.Name)..." -ForegroundColor Gray
    try {
        $response = Invoke-RestMethod -Uri $endpoint.Url -TimeoutSec 5
        Write-Host "  ✅ $($endpoint.Name) 响应正常" -ForegroundColor Green
    } catch {
        Write-Host "  ⚠️ $($endpoint.Name) 响应异常" -ForegroundColor Yellow
    }
}

Write-Host ""
Write-Host "🎉 测试完成！" -ForegroundColor Green
Write-Host "📋 访问地址："
Write-Host "  🌐 统一入口: http://localhost:8080"
Write-Host "  📖 API文档: http://localhost:8080/doc.html"
Write-Host "  🏠 Nacos控制台: http://localhost:8848/nacos"
Write-Host ""
Write-Host "🔧 如果某些服务未就绪，请等待1-2分钟后重试运行此脚本" -ForegroundColor Yellow