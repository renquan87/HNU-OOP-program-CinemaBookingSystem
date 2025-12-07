@echo off
cd /d "D:\MyHomework\OOP\SmallClass\Second\CinemaBookingSystem"
chcp 65001 >nul
echo 测试修复后的系统...
echo.
echo 1. 测试编码问题修复
echo 2. 测试电影列表排序
echo 3. 测试添加电影日期验证
echo 4. 测试用户数据保存
echo.
echo 启动程序进行测试...
java -cp target/classes com.cinema.Main
pause