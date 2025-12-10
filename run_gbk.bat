@echo off
title 电影院购票系统
cd /d "%~dp0"

echo ========================================
echo        电影院购票系统
echo ========================================
echo.
echo 默认管理员账号: ADMIN-001
echo.

REM 检查lib目录
if not exist "lib" (
    echo [信息] 正在下载依赖...
    call mvn dependency:copy-dependencies -DoutputDirectory=lib
)

REM 检查target/classes目录
if not exist "target\classes" (
    echo [信息] 正在编译项目...
    call mvn compile
)

REM 使用GBK编码运行
if exist "lib\mysql-connector*.jar" (
    echo [信息] 使用MySQL数据库存储
    java -Dfile.encoding=GBK -cp "lib/*;target/classes" com.cinema.Main
) else (
    echo [信息] 使用文件存储
    java -Dfile.encoding=GBK -cp target/classes com.cinema.Main
)

pause