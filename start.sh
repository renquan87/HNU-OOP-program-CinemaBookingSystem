#!/usr/bin/env bash

set -e

echo "=================================="
echo " Cinema Project Startup Script"
echo "=================================="

# 1. 询问数据库密码（不回显）
read -s -p "请输入数据库密码: " DB_PASSWORD
echo

# 2. 通过环境变量传递数据库密码
export DB_PASSWORD
echo "✅ 数据库密码已通过 DB_PASSWORD 环境变量提供"

# 3. 拷贝 Maven 依赖
echo "📦 拷贝 Maven 依赖..."
mvn dependency:copy-dependencies -DoutputDirectory=lib

# 4. 编译
echo "🔨 编译项目..."
mvn clean compile

# 5. 判断 classpath 分隔符
case "$(uname -s)" in
  CYGWIN*|MINGW*|MSYS*)
    CP_SEP=";"
    ;;
  *)
    CP_SEP=":"
    ;;
esac

# 6. 初始化数据库
echo "🗄️ 初始化数据库..."
java -cp "lib/*${CP_SEP}target/classes" com.cinema.DatabaseInitializer "$DB_PASSWORD"

# 7. 启动后端（后台）
echo "🚀 启动后端 Spring Boot..."
(
  mvn spring-boot:run
) &

# 8. 启动前端
echo "🎨 启动前端..."

cd web

if ! command -v pnpm >/dev/null 2>&1; then
  echo "❌ 未检测到 pnpm，请先安装 pnpm"
  exit 1
fi

if [ ! -d "node_modules" ]; then
  echo "📥 未检测到依赖，执行 pnpm install..."
  pnpm install
else
  echo "✅ 已存在 node_modules，跳过安装"
fi

pnpm run dev
