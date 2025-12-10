@echo off
title 电影院购票系统
cd /d "%~dp0"

REM 设置控制台编码为UTF-8
chcp 936 >nul 2>&1

echo ========================================
echo        电影院购票系统
echo ========================================
echo.
echo 默认管理员账号: ADMIN-001
echo.

REM 检查lib目录
if not exist "lib" (
    echo [错误] lib目录不存在，正在下载依赖...
    call mvn dependency:copy-dependencies -DoutputDirectory=lib
    if errorlevel 1 (
        echo [错误] 下载依赖失败，请检查Maven配置
        pause
        exit /b 1
    )
)

REM 检查target/classes目录
if not exist "target\classes" (
    echo [信息] 正在编译项目...
    call mvn compile
    if errorlevel 1 (
        echo [错误] 编译失败
        pause
        exit /b 1
    )
)

REM 运行程序
if exist "lib\mysql-connector*.jar" (
    echo [信息] 使用MySQL数据库存储
    java -Dfile.encoding=GBK -cp "lib/*;target/classes" com.cinema.Main
) else (
    echo [信息] 使用文件存储
    java -Dfile.encoding=GBK -cp target/classes com.cinema.Main
)

pause