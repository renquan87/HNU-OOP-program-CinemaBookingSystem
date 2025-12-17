# ================================
# Cinema Project Startup Script
# ================================

# 1. 询问数据库密码（不回显）
Write-Host "请输入数据库密码：" -ForegroundColor Cyan
$dbPassword = Read-Host -AsSecureString
$dbPasswordPlain = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($dbPassword)
)

# 2. 通过环境变量传递数据库密码
$env:DB_PASSWORD = $dbPasswordPlain
Write-Host "数据库密码已通过 DB_PASSWORD 环境变量提供" -ForegroundColor Green

# 3. 拷贝依赖
Write-Host "正在拷贝 Maven 依赖..." -ForegroundColor Cyan
mvn dependency:copy-dependencies -DoutputDirectory=lib
if ($LASTEXITCODE -ne 0) { exit 1 }

# 4. 编译项目
Write-Host "正在编译项目..." -ForegroundColor Cyan
mvn clean compile
if ($LASTEXITCODE -ne 0) { exit 1 }

# 5. 初始化数据库
Write-Host "正在初始化数据库..." -ForegroundColor Cyan
java -cp "lib/*;target/classes" com.cinema.DatabaseInitializer "$dbPasswordPlain"
if ($LASTEXITCODE -ne 0) { exit 1 }

# 6. 启动后端（新终端）
Write-Host "启动后端 Spring Boot..." -ForegroundColor Green
Start-Process powershell -ArgumentList "mvn spring-boot:run"

# 7. 启动前端（新终端）
Start-Process powershell -ArgumentList {
    Set-Location web

    # 检查 pnpm 是否安装
    if (-not (Get-Command pnpm -ErrorAction SilentlyContinue)) {
        Write-Host "未检测到 pnpm，请先全局安装 pnpm" -ForegroundColor Red
        exit 1
    }

    # 检查 node_modules
    if (-Not (Test-Path "node_modules")) {
        Write-Host "未检测到前端依赖，正在执行 pnpm install..." -ForegroundColor Cyan
        pnpm install
        if ($LASTEXITCODE -ne 0) { exit 1 }
    } else {
        Write-Host "已存在 node_modules，跳过 pnpm install" -ForegroundColor Green
    }

    Write-Host "启动前端开发服务器..." -ForegroundColor Green
    pnpm run dev
}

Write-Host "🚀 全部服务已启动" -ForegroundColor Green
