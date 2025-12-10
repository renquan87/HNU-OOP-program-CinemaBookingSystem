@echo off
title 电影院购票系统 - MySQL版本
cd /d "%~dp0"

REM 设置UTF-8编码
chcp 65001 >nul 2>&1

echo ========================================
echo        电影院购票系统 - MySQL版本
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

REM 检查MySQL驱动
if not exist "lib\mysql-connector*.jar" (
    echo [错误] MySQL驱动未找到，将使用文件存储模式
    echo.
    java -cp target/classes com.cinema.Main
) else (
    echo [信息] 使用MySQL数据库存储
    echo.
    java -cp "lib/*;target/classes" com.cinema.Main
)

pause