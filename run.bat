@echo off
echo 正在编译电影院购票系统...
javac -cp "src/main/java" src/main/java/com/cinema/Main.java

if %ERRORLEVEL% NEQ 0 (
    echo 编译失败！
    pause
    exit /b 1
)

echo 编译成功！正在启动系统...
java -cp "src/main/java" com.cinema.Main

pause