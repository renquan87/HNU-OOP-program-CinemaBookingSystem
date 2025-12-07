@echo off
cd /d "%~dp0"
echo 正在启动电影院购票系统...
echo.
echo 默认管理员账号:
echo 用户ID: ADMIN-001
echo.
echo 检查Java环境...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo 错误：未找到Java环境！请确保Java已正确安装并配置到PATH环境变量中。
    pause
    exit /b 1
)
echo.
echo 设置UTF-8编码...
chcp 65001 >nul
echo 启动程序...
java -cp target/classes com.cinema.Main
if errorlevel 1 (
    echo.
    echo 程序运行出错
)
pause