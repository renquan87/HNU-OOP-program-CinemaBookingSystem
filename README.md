# 电影院购票系统

## 项目概述

本项目是一个基于Java 17开发的电影院购票管理系统，采用面向对象设计模式，实现了完整的电影院业务流程。系统使用MySQL数据库存储，具备用户购票、管理员管理、座位选择、订单处理等核心功能。

### 系统特色

1. **MySQL数据库存储架构**：使用MySQL数据库存储所有数据，确保数据持久化和一致性
2. **完整的业务流程**：从电影管理到座位选择、订单支付的全流程支持
3. **灵活的定价策略**：支持标准定价和高级定价策略，可根据时段、座位类型动态调价
4. **安全的座位管理**：支持座位锁定、并发访问控制
5. **完善的异常处理**：自定义异常体系，确保系统稳定性

## 项目结构与依赖

### Maven依赖管理 (pom.xml)
```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <java.version>17</java.version>
</properties>

<dependencies>
    <!-- Spring Boot Web Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>2.7.5</version>
    </dependency>
    
    <!-- Spring Boot WebSocket Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- MySQL Connector -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- Connection Pool -->
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>5.0.1</version>
    </dependency>
    
    <!-- SLF4J Logging -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-api</artifactId>
        <version>1.7.36</version>
    </dependency>
    
    <!-- JUnit测试框架 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.9.2</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### 目录结构详解
```
CinemaBookingSystem/
├── src/
│   ├── main/
│   │   ├── java/com/cinema/
│   │   │   ├── Main.java                 # 程序入口
│   │   │   ├── DatabaseInitializer.java  # 数据库初始化工具
│   │   │   ├── MigrateToMySQL.java       # 数据迁移工具
│   │   │   ├── exception/                # 异常类定义
│   │   │   ├── model/                    # 实体类
│   │   │   ├── service/                  # 业务服务层
│   │   │   ├── storage/                  # 数据存储层
│   │   │   ├── strategy/                 # 策略模式实现
│   │   │   └── ui/                       # 用户界面层
│   │   └── resources/                    # 资源文件
│   │       ├── config.properties         # 数据库配置
│   │       └── schema.sql                # 数据库建表脚本
│   └── test/java/com/cinema/             # 测试代码
├── web/                                  # 前端项目目录
│   ├── src/                              # 前端源码
│   ├── public/                           # 静态资源
│   ├── package.json                      # 前端依赖配置
│   ├── vite.config.ts                    # Vite构建配置
│   └── README.md                         # 前端文档
├── data/                                 # 备份数据目录（仅用于备份）
│   └── backup_20251211_184401/            # 数据备份目录
├── lib/                                  # 依赖库目录
├── target/                               # Maven构建输出
├── pom.xml                               # Maven配置文件
├── run.bat                               # Windows运行脚本
├── test_mysql.bat                        # MySQL测试脚本
├── docs/                                 # 项目文档目录
│   ├── architecture.md                   # 系统架构详解
│   ├── database.md                       # 数据库详解
│   ├── business-process.md               # 业务流程详解
│   ├── extension-guide.md                # 扩展开发指南
│   ├── troubleshooting.md                # 故障排除
│   └── frontend.md                       # 前端文档
└── README.md                             # 项目文档
```

## 快速开始

```shell
mvn dependency:copy-dependencies -DoutputDirectory=lib # 依赖
mvn clean compile # 编译

# 初始化数据库（支持从命令行直接传入密码）
java -cp "lib/*;target/classes" com.cinema.DatabaseInitializer <your_password>

# 运行命令行版本系统
java -cp "lib/*;target/classes" com.cinema.Main <your_password>

# ====网页版====
# 开启后端
mvn spring-boot:run

# 再启动一个终端
cd web
pnpm run dev
```

最好直接使用系统的命令行，简单明了。

开发时用的是vscode的集成终端。

IDEA可能有终端中文乱码问题，可搜教程解决。

用MySQL Workbench或者Command Line可查看数据库存储情况。（具体查看[MySQLWorkbench.md](MySQLWorkbench.md)）

## 开发指南

### 1. 环境搭建

#### 安装Java 17
```bash
# Windows: 下载并安装Oracle JDK 17
# 设置环境变量JAVA_HOME
# 验证安装
java -version
javac -version
```

#### 安装MySQL 8.0
```bash
# Windows: 下载MySQL Installer
# 安装MySQL Server 8.0
# 配置root密码（后续需在项目相关位置将密码修改成你自己的）
# 启动MySQL服务
```

#### 安装Maven
```bash
# Windows: 下载Apache Maven
# 解压到指定目录
# 设置环境变量MAVEN_HOME和PATH
# 验证安装
mvn -version
```

#### 安装Node.js
```bash
# Windows: 下载并安装Node.js LTS版本 (推荐16.0.0或更高)
# 从 https://nodejs.org 下载安装包
# 安装时勾选自动添加PATH
# 验证安装
node -v
npm -v
```

#### 安装pnpm
```bash
# 使用npm全局安装pnpm (推荐)
npm install -g pnpm

# 或者使用其他安装方式
# Windows: 使用winget
winget install pnpm

# 验证安装
pnpm -v
```

### 2. 项目初始化

#### 克隆项目
```bash
git clone https://github.com/renquan87/HNU-OOP-program2-CinemaBookingSystem.git
cd CinemaBookingSystem
```

#### 下载依赖
```bash
# 下载所有依赖到lib目录
mvn dependency:copy-dependencies -DoutputDirectory=lib
```

#### 安装前端依赖
```bash
# 进入前端目录
cd web

# 安装前端依赖
pnpm install

# 返回根目录
cd ..
```

#### 编译项目
```bash
mvn clean compile
```

#### 初始化数据库
```bash
# 创建数据库表
java -cp "lib/*;target/classes" com.cinema.DatabaseInitializer
```

#### 测试MySQL连接（可选）
```bash
# 运行测试脚本验证MySQL连接
.\test_mysql.bat
```

### 3. 运行项目

#### 方式1：使用批处理文件（推荐）
```bash
# Windows
.\run.bat
```

#### 方式2：命令行运行
```bash
# 运行系统
java -cp "lib/*;target/classes" com.cinema.Main
```

#### 方式3：Maven运行
```bash
mvn exec:java -Dexec.mainClass="com.cinema.Main"
```

### 4. 测试指南(未测试测试指南)

#### 运行单元测试
```bash
mvn test
```

#### 运行特定测试类
```bash
mvn test -Dtest=CinemaManagerTest
```

#### 测试覆盖率
```bash
mvn jacoco:report
```

### 5. 代码规范

#### 命名规范
- 类名：PascalCase（如：CinemaManager）
- 方法名：camelCase（如：searchShows）
- 常量：UPPER_SNAKE_CASE（如：DEFAULT_PRICE）
- 包名：lowercase（如：com.cinema.service）

#### 注释规范
- 类级别：说明类的职责和用法
- 方法级别：说明方法的功能、参数和返回值
- 复杂逻辑：解释实现思路

#### 异常处理
- 使用自定义异常
- 异常信息要明确
- 避免捕获Exception而不处理

### 6. 调试技巧

#### 日志输出
```java
// 使用System.err输出错误信息
System.err.println("错误信息");

// 使用System.out输出调试信息
System.out.println("调试信息");
```

#### 数据库调试
```sql
-- 开启MySQL日志
SET GLOBAL general_log = 'ON';
SET GLOBAL general_log_file = '/path/to/mysql.log';

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
```
### AI 助手配置说明
本项目支持可选的 AI 智能助手功能。
如需启用，请在 `src/main/resources/` 下创建 `application-ai.properties`，
并填写你自己的 API Key（该文件已被 .gitignore 忽略）。

## 团队协作指南

### 1. 代码管理

#### Git工作流
```bash
# 创建功能分支
git checkout -b feature/new-pricing-strategy

# 提交代码
git add .
git commit -m "feat: 添加学生定价策略"

# 推送分支
git push origin feature/new-pricing-strategy

# 合并到主分支
git checkout main
git merge feature/new-pricing-strategy
```

#### 代码审查清单
- [x] 代码符合项目规范
- [ ] 异常处理完善
- [ ] 添加必要的注释
- [ ] 通过所有测试
- [x] 更新相关文档

### 2. 测试策略

#### 单元测试
- 每个服务类都要有对应的测试类
- 测试覆盖率不低于80%
- 使用Mock对象测试外部依赖

#### 集成测试
- 测试数据库操作
- 测试完整的业务流程
- 测试并发场景

#### 测试数据管理
```java
// 使用测试数据库
@Test
public void testCreateOrder() {
    // 准备测试数据
    Movie testMovie = new Movie("TEST-001", "测试电影", ...);
    // 执行测试
    // 验证结果
    // 清理测试数据
}
```

### 3. 文档维护

#### API文档
- 使用JavaDoc生成API文档
- 保持文档与代码同步
- 提供使用示例

#### 更新日志
```markdown
## 更新日志

### v1.2.0 (2025-12-11)
- 完全迁移到MySQL数据库存储
- 删除所有文件存储相关代码
- 更新JDK版本至17
- 添加数据库连接池支持
- 优化数据库表结构

### v1.1.0 (2025-12-10)
- 新增：MySQL数据库支持
- 修复：场次查询空指针异常
- 优化：座位锁定机制

### v1.0.0 (2025-12-01)
- 初始版本发布
```

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

- 项目维护者：renquan87
- 项目地址：https://github.com/renquan87/HNU-OOP-program2-CinemaBookingSystem
- 问题反馈：请提交Issue

---

*本文档最后更新时间：2025年12月14日*