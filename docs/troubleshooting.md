# 故障排除

## 常见问题及解决方案

### MySQL连接失败
**问题**：`No suitable driver found for jdbc:mysql://`
**解决方案**：
```bash
# 确保MySQL驱动在lib目录中
ls lib/mysql-connector*.jar

# 重新下载依赖
mvn dependency:copy-dependencies -DoutputDirectory=lib

# 检查数据库服务是否启动
mysql -u root -p -e "SELECT 1"
```

### 中文乱码问题
**问题**：数据库中的中文显示为问号
**解决方案**：
```sql
-- 检查数据库字符集
SHOW VARIABLES LIKE 'character_set%';

-- 修改数据库字符集
ALTER DATABASE cinema_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 修改表字符集
ALTER TABLE movies CONVERT TO CHARACTER SET utf8mb4;
```

### PowerShell输入问题
**问题**：使用Scanner.nextLine()时抛出NoSuchElementException
**解决方案**：
- 使用项目中的`readLine()`方法代替`scanner.nextLine()`
- 或使用cmd代替PowerShell

### 座位锁定超时
**问题**：座位被锁定后无法自动释放
**解决方案**：
```java
// 在BookingService中添加定时检查
ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
scheduler.scheduleAtFixedRate(this::checkExpiredOrders, 0, 1, TimeUnit.MINUTES);
```

## 性能优化建议

### 数据库优化
- 为常用查询字段添加索引
- 使用连接池管理数据库连接
- 定期清理过期订单数据

### 内存优化
- 使用弱引用缓存不常用数据
- 及时释放大对象
- 避免内存泄漏

### 并发优化
- 使用线程安全的数据结构
- 合理使用同步机制
- 避免死锁

## 日志和监控

### 添加日志框架
```xml
<!-- pom.xml中添加logback依赖 -->
<dependency>
    <groupId>ch.qos.logback</groupId>
    <artifactId>logback-classic</artifactId>
    <version>1.4.7</version>
</dependency>
```

### 监控关键指标
- 数据库连接数
- 内存使用情况
- 订单处理速度
- 座位使用率