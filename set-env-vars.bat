@echo off
REM XReadUp 环境变量设置脚本 (批处理版本)
REM 将.env文件中的内容设置到系统环境变量中

echo 🚀 开始设置XReadUp环境变量...

REM 检查.env文件是否存在
if not exist ".env" (
    echo ❌ 错误: .env文件不存在！
    echo 请确保.env文件存在于当前目录中。
    pause
    exit /b 1
)

echo 📁 找到.env文件，开始读取配置...

REM 设置计数器
set /a count=0

REM 读取.env文件并设置环境变量
for /f "usebackq tokens=1,2 delims==" %%a in (".env") do (
    REM 跳过空行和注释行
    echo %%a | findstr /r "^#" >nul
    if not errorlevel 1 goto :skip
    
    echo %%a | findstr /r "^$" >nul
    if not errorlevel 1 goto :skip
    
    REM 设置环境变量
    set "%%a=%%b"
    setx "%%a" "%%b" >nul 2>&1
    if errorlevel 1 (
        echo ❌ 设置环境变量失败: %%a
    ) else (
        echo ✅ 设置环境变量: %%a = %%b
        set /a count+=1
    )
    :skip
)

echo.
echo 🎉 环境变量设置完成！
echo 📊 总共设置了 %count% 个环境变量

echo.
echo 💡 提示:
echo 1. 环境变量已设置到用户级别
echo 2. 需要重新启动命令行窗口或IDE才能生效
echo 3. 或者运行 'refreshenv' 命令刷新环境变量

echo.
echo 🔧 验证环境变量是否设置成功:
echo 运行以下命令验证:
echo echo %%MYSQL_PASSWORD%%
echo echo %%JWT_SECRET%%
echo echo %%GATEWAY_PORT%%

pause
