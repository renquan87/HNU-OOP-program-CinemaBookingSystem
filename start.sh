#!/usr/bin/env bash

set -e

echo "=================================="
echo " Cinema Project Startup Script"
echo "=================================="

# 1. 询问数据库密码（不回显）
read -s -p "请输入数据库密码: " DB_PASSWORD
echo

CONFIG_FILE="src/main/resources/config.properties"

if [ ! -f "$CONFIG_FILE" ]; then
  echo "❌ 未找到 $CONFIG_FILE"
  exit 1
fi

# 2. 修改 db.password
if grep -q "^db.password=" "$CONFIG_FILE"; then
  sed -i.bak "s|^db.password=.*|db.password=$DB_PASSWORD|" "$CONFIG_FILE"
else
  echo "db.password=$DB_PASSWORD" >> "$CONFIG_FILE"
fi

echo "✅ 数据库密码已写入 config.properties"

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
java -cp "lib/*${CP_SEP}target/classes" com.cinema.DatabaseInitializer

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
