@echo off
cd /d "%~dp0"
echo ===== 测试电影院购票系统 =====
echo.
echo 1. 检查Java环境
java -version
if %ERRORLEVEL% NEQ 0 (
    echo 错误：未找到Java环境！
    pause
    exit /b 1
)
echo.
echo 2. 编译项目
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！
    pause
    exit /b 1
)
echo.
echo 3. 初始化数据
java -cp "target/classes;src/test/java" com.cinema.TestInitialization
echo.
echo 4. 验证管理员登录
java -cp "target/classes;src/test/java" com.cinema.TestLogin
echo.
echo 5. 启动系统
echo 默认管理员账号: ADMIN-001
echo.
chcp 65001 >nul
java -cp target/classes com.cinema.Main
pause