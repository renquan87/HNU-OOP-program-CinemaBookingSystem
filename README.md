# 🎬 电影院购票管理系统 (Cinema Booking System)

> 基于 Java 17 + Spring Boot + MySQL 开发的综合性电影购票系统。
> 支持 **控制台(Console UI)** 交互与 **RESTful Web API** 双模式运行。

---

## 📖 项目简介

本项目是一个采用面向对象设计模式（OOP）开发的电影院业务管理系统。系统实现了完整的电影院业务流程，包括电影管理、影厅排片、用户购票、订单处理及复杂的定价策略。

**当前版本特色：**
* **混合运行模式**：既包含经典的命令行交互界面，也集成了 Spring Boot 提供 Web 接口。
* **数据持久化**：全面采用 MySQL 数据库存储，支持事务处理和连接池管理 (HikariCP)。
* **安全升级**：实现了基于密码的用户认证体系（登录/注册）。
* **定价策略**：实现了策略模式（Strategy Pattern），支持标准定价与高级动态定价（夜间/周末/VIP加价）。

---

## 🛠 技术栈

* **开发语言**：Java 17
* **构建工具**：Maven 3.x
* **框架**：Spring Boot 2.7.5 (Web模块)
* **数据库**：MySQL 8.0
* **连接池**：HikariCP
* **测试框架**：JUnit 5

---

## 📂 项目结构

```text
CinemaBookingSystem/
├── src/main/java/com/cinema/
│   ├── Main.java                 # 控制台主程序入口
│   ├── CinemaApplication.java    # Spring Boot Web应用入口
│   ├── DatabaseInitializer.java  # 数据库初始化工具
│   ├── controller/               # Web API 控制器 (Auth, Booking, Movie等)
│   ├── model/                    # 实体类 (User, Movie, Order, Seat等)
│   ├── service/                  # 业务逻辑层 (单例模式: CinemaManager, BookingService)
│   ├── storage/                  # 数据存储层 (MySQL实现)
│   ├── strategy/                 # 定价策略 (策略模式实现)
│   └── ui/                       # 控制台界面 (ConsoleUI, ANSI颜色支持)
├── src/main/resources/
│   ├── config.properties         # 数据库配置文件
│   ├── application.properties    # Spring Boot配置
│   └── schema.sql                # 数据库建表脚本
├── data/                         # 备份数据目录
└── pom.xml                       # Maven依赖配置
````

-----

## 🚀 快速开始

### 1\. 环境准备

* JDK 17 或更高版本
* MySQL 8.0 服务端
* Maven 环境

### 2\. 数据库配置

在运行前，请务必修改 `src/main/resources/config.properties` 文件，填入你的 MySQL 账号密码：

```properties
db.url=jdbc:mysql://localhost:3306/cinema_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
# 请修改为你的数据库密码
db.password=你的密码
db.driver=com.mysql.cj.jdbc.Driver
```

### 3\. 初始化数据库

首次运行需初始化表结构。我们提供了专门的初始化工具：

```bash
# 编译项目
mvn clean compile

# 运行初始化工具 (支持命令行传参密码，防止配置文件读取失败)
# Windows CMD
java -cp "target/classes;target/dependency/*" com.cinema.DatabaseInitializer 你的数据库密码

# 或 PowerShell
java -cp "target/classes;target/dependency/*" com.cinema.DatabaseInitializer
```

### 4\. 运行项目

本项目支持两种运行方式，请根据需求选择：

#### 方式 A：运行控制台界面 (Console UI)

适合体验基于命令行的交互系统。

```bash
# 运行 Main 类
java -cp "target/classes;target/dependency/*" com.cinema.Main
```

*操作说明：使用键盘输入数字选择菜单，支持彩色文本显示。*

#### 方式 B：运行 Web 后端 (Spring Boot)

适合作为后端服务，配合前端或 Postman 调用 API。

```bash
# 运行 Spring Boot 应用
mvn spring-boot:run
```

*服务默认端口：8080*

-----

## 🔌 API 接口说明 (Web模式)

启动 Web 模式后，可通过以下接口进行交互：

| 模块 | 方法 | 路径 | 描述 |
| :--- | :--- | :--- | :--- |
| **认证** | POST | `/api/login` | 用户登录 (需 JSON body) |
| | POST | `/api/register` | 用户注册 |
| **电影** | GET | `/api/movies` | 获取所有电影列表 |
| | POST | `/api/movies` | 添加电影 (JSON) |
| **场次** | GET | `/api/shows` | 获取场次列表 |
| | GET | `/api/shows/{id}/seats` | 获取某场次的座位图 |
| **订单** | POST | `/api/booking/create` | 创建订单并锁座 |
| | POST | `/api/booking/pay` | 支付订单 |

-----

## 🧩 核心功能详解

### 1\. 用户体系

* **普通用户**：注册、登录、浏览电影、选座购票、查看历史订单、退票。
* **管理员**：管理电影信息、管理影厅布局、排片管理、查看营收统计、数据备份。
* **密码安全**：用户注册时需设置密码（6位以上），登录时进行校验。

### 2\. 座位系统

* **多类型座位**：
  * `[D] 优惠座`：通常在第一排，基准价 80%。
  * `[V] VIP座`：位于黄金视野区域，基准价 +10元 (或高级策略下的2倍)。
  * `[O] 普通座`：标准价格。
* **状态管理**：支持 `AVAILABLE`(可用)、`LOCKED`(锁定中)、`SOLD`(已售) 状态。
* **并发控制**：预订时锁定座位 15 分钟，超时自动释放。

### 3\. 定价策略 (Strategy Pattern)

系统支持在运行时动态切换定价逻辑：

* **StandardPricing (标准策略)**：
  * 周末票价上浮 20%
  * 晚间 (18:00后) 上浮 15%
* **PremiumPricing (高级策略)**：
  * VIP 座位价格翻倍
  * 黄金时段 (19:00-21:00) 额外上浮 10%

-----

## ❓ 常见问题 (FAQ)

**Q: 启动时报错 `No suitable driver found`？**
A: 请确保 Maven 依赖已下载完整。如果手动运行 Java 命令，请确保 classpath 中包含了 `mysql-connector-j-8.0.33.jar`。建议使用 `mvn dependency:copy-dependencies` 将依赖复制到 `target/dependency` 目录后运行。

**Q: 控制台中文乱码？**
A: `Main.java` 中已内置了针对 Windows 环境的 UTF-8 自动切换代码 (`chcp 65001`)。如果仍乱码，请检查 IDE 或终端的默认编码设置。

**Q: 如何切换回旧的文件存储模式？**
A: 当前版本已强制升级为 MySQL 存储模式，移除了文件存储的降级支持，以确保数据一致性和支持复杂的查询需求。

-----

## 🤝 贡献指南

1.  Fork 本仓库
2.  创建特性分支 (`git checkout -b feature/AmazingFeature`)
3.  提交更改 (`git commit -m 'Add some AmazingFeature'`)
4.  推送到分支 (`git push origin feature/AmazingFeature`)
5.  提交 Pull Request

-----

*文档更新时间：2025年12月*

````

### 💡 针对你当前情况的额外建议

因为你之前是“手动合并”的，为了防止下次同学更新代码时你又乱了，建议你在生成这个 README 后，执行以下 Git 操作把你的环境整理好：

1.  **覆盖文件：** 用上面的内容替换项目里的 `README.md`。
2.  **提交更改：**
    ```bash
    git add README.md
    git commit -m "docs: update README with merged features and setup guide"
    ```
3.  **（可选）创建自己的分支：**
    如果你以后还要继续手动搬运同学的代码，建议你新建一个分支开发，保持主分支干净：
    ```bash
    git checkout -b my-dev
    ```
````