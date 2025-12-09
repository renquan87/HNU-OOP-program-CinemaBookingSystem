@echo off
cd /d "%~dp0"
echo 正在编译电影院购票系统...
echo 检测Java版本...
java -version 2>&1 | findstr "17" >nul
if %ERRORLEVEL% EQU 0 (
    echo 检测到Java 17，使用Java 17编译...
    call mvn compile -Djava.version=17
) else (
    echo 使用默认Java版本编译...
    call mvn compile
)

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