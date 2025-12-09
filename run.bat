@echo off
title 电影院购票系统
cd /d "%~dp0"
echo 正在启动电影院购票系统...
echo.
echo 默认管理员账号: ADMIN-001
echo.
chcp 65001 >nul
java -cp target/classes com.cinema.Main
pause