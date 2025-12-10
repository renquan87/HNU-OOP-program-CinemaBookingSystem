# 电影院购票系统启动脚本 (PowerShell版本)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "       电影院购票系统" -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "默认管理员账号: ADMIN-001" -ForegroundColor Green
Write-Host ""

# 设置环境变量
$env:JAVA_TOOL_OPTIONS = "-Dfile.encoding=GBK"

# 检查lib目录
if (-not (Test-Path "lib")) {
    Write-Host "[信息] 正在下载依赖..." -ForegroundColor Yellow
    mvn dependency:copy-dependencies -DoutputDirectory=lib
}

# 检查target/classes目录
if (-not (Test-Path "target\classes")) {
    Write-Host "[信息] 正在编译项目..." -ForegroundColor Yellow
    mvn compile
}

# 运行程序
$mysqlJars = Get-ChildItem -Path "lib" -Filter "mysql-connector*.jar" -ErrorAction SilentlyContinue
if ($mysqlJars) {
    Write-Host "[信息] 使用MySQL数据库存储" -ForegroundColor Green
    java -cp "lib/*;target/classes" com.cinema.Main
} else {
    Write-Host "[信息] 使用文件存储" -ForegroundColor Green
    java -cp target/classes com.cinema.Main
}

Write-Host ""
Write-Host "按任意键继续..." -ForegroundColor Cyan
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")