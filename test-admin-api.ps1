# 测试admin-service的文章管理API
Write-Host "测试admin-service的文章管理API..." -ForegroundColor Green

# 测试文章列表API
Write-Host "`n1. 测试文章列表API..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8085/api/admin/articles/list?page=1&pageSize=5" -Method GET
    Write-Host "状态码: $($response.StatusCode)" -ForegroundColor Green
    Write-Host "响应内容: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "请求失败: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "状态码: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host "`n测试完成！" -ForegroundColor Green

