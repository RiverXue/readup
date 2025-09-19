# 清理后端空文件夹脚本
# 注意：此脚本不会删除 .git、.idea、target、logs 等可能被构建工具或IDE使用的文件夹

$rootDir = "d:\xreadup\xreadup"

Write-Host "开始清理后端空文件夹..." -ForegroundColor Green

# 需要排除的文件夹模式
$excludePatterns = @(
    "*\.git*",
    "*\.idea*", 
    "*\target*",
    "*\logs*",
    "*\mysql*",
    "*\redis*",
    "*\nacos*"
)

# 获取所有空文件夹
$emptyFolders = @()
Get-ChildItem -Path $rootDir -Recurse -Directory | ForEach-Object {
    $folder = $_
    $folderPath = $folder.FullName
    
    # 检查是否应该排除这个文件夹
    $shouldExclude = $false
    foreach ($pattern in $excludePatterns) {
        if ($folderPath -like $pattern) {
            $shouldExclude = $true
            break
        }
    }
    
    # 如果不应该排除，检查是否为空
    if (-not $shouldExclude) {
        $items = Get-ChildItem -Path $folderPath -Force
        if ($items.Count -eq 0) {
            $emptyFolders += $folder
            Write-Host "发现空文件夹: $folderPath" -ForegroundColor Yellow
        }
    }
}

# 按路径长度倒序排序，确保先删除子文件夹
$emptyFolders = $emptyFolders | Sort-Object { $_.FullName.Length } -Descending

Write-Host "`n准备删除 $($emptyFolders.Count) 个空文件夹..." -ForegroundColor Cyan

$deletedCount = 0
foreach ($folder in $emptyFolders) {
    try {
        # 再次检查文件夹是否仍然存在且为空
        if (Test-Path $folder.FullName) {
            $items = Get-ChildItem -Path $folder.FullName -Force
            if ($items.Count -eq 0) {
                Remove-Item -Path $folder.FullName -Force
                Write-Host "已删除: $($folder.FullName)" -ForegroundColor Green
                $deletedCount++
            } else {
                Write-Host "跳过（不再为空）: $($folder.FullName)" -ForegroundColor Gray
            }
        } else {
            Write-Host "跳过（已不存在）: $($folder.FullName)" -ForegroundColor Gray
        }
    }
    catch {
        Write-Host "删除失败: $($folder.FullName) - $($_.Exception.Message)" -ForegroundColor Red
    }
}

Write-Host "`n清理完成！共删除了 $deletedCount 个空文件夹。" -ForegroundColor Green

# 最后再检查一遍剩余的空文件夹
Write-Host "`n检查剩余的空文件夹..." -ForegroundColor Cyan
$remainingEmpty = @()
Get-ChildItem -Path $rootDir -Recurse -Directory | ForEach-Object {
    $folder = $_
    $folderPath = $folder.FullName
    
    # 检查是否应该排除这个文件夹
    $shouldExclude = $false
    foreach ($pattern in $excludePatterns) {
        if ($folderPath -like $pattern) {
            $shouldExclude = $true
            break
        }
    }
    
    # 如果不应该排除，检查是否为空
    if (-not $shouldExclude) {
        $items = Get-ChildItem -Path $folderPath -Force
        if ($items.Count -eq 0) {
            $remainingEmpty += $folder
            Write-Host "剩余空文件夹: $folderPath" -ForegroundColor Yellow
        }
    }
}

if ($remainingEmpty.Count -eq 0) {
    Write-Host "✅ 所有可删除的空文件夹都已清理完成！" -ForegroundColor Green
} else {
    Write-Host "⚠️ 还有 $($remainingEmpty.Count) 个空文件夹因为各种原因未被删除（可能是系统保护或被占用）。" -ForegroundColor Yellow
}
