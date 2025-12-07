@echo off
cd /d "%~dp0"
echo 正在编译电影院购票系统...
call mvn compile

if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo 编译成功！正在启动系统...
echo.
echo 默认管理员账号: ADMIN-001
echo.
chcp 65001 >nul
java -cp target/classes com.cinema.Main

pause