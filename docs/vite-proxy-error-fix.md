# Vite HTTP 代理错误修复指南

## 问题描述
```
22:38:10 [vite] http proxy error: /api/movies
Error: Parse Error: Expected LF after chunk data
```

## 问题根本原因
这个错误通常由以下原因引起：
1. **后端响应的分块传输编码（chunked transfer encoding）格式不正确**
2. **响应头信息不匹配实际响应体**
3. **Content-Length 和 Transfer-Encoding 头设置冲突**
4. **代理配置缺少必要的缓冲和超时参数**

## 实施的修复方案

### 1️⃣ 后端配置修复 (application.properties)

```properties
# 禁用 HTTP/2（可能导致分块编码问题）
server.http2.enabled=false

# 禁用压缩（压缩可能导致分块编码问题）
server.compression.enabled=false

# 字符编码配置
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
server.servlet.encoding.force-request=true
server.servlet.encoding.force-response=true

# 连接池配置
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
server.tomcat.connection-timeout=20000
server.tomcat.accept-count=100
```

### 2️⃣ Java 配置类修复

创建了以下配置类：

#### HttpResponseConfig.java
- 配置内容协商
- 确保默认返回 JSON 格式

#### HttpResponseInterceptor.java
- 拦截所有 API 响应
- 移除可能导致问题的响应头
- 设置正确的 CORS 和 Content-Type 头
- 禁用 gzip 压缩

#### WebMvcConfig.java
- 注册 HTTP 响应拦截器
- 应用到所有 `/api/**` 路径

### 3️⃣ Vite 代理配置修复 (vite.config.ts)

关键改进：
```typescript
proxy: {
  "/api": {
    target: "http://localhost:8081",
    changeOrigin: true,
    timeout: 30000,
    proxyTimeout: 30000,
    ws: false,
    
    // 使用 Agent 配置来控制连接参数
    agent: new http.Agent({
      keepAlive: true,
      timeout: 30000,
      maxSockets: 100,
      maxFreeSockets: 10
    }),
    
    // 重要：处理响应头以修复分块编码问题
    onProxyRes: (proxyRes) => {
      // 删除 transfer-encoding，使用 content-length 代替
      delete proxyRes.headers['transfer-encoding'];
      // 确保响应头正确
      proxyRes.headers['content-type'] = 'application/json; charset=utf-8';
    }
  }
}
```

## 需要执行的步骤

### 后端修复（Java）
1. ✅ 修改 `src/main/resources/application.properties` - 已完成
2. ✅ 创建 `src/main/java/com/cinema/config/HttpResponseConfig.java` - 已完成
3. ✅ 创建 `src/main/java/com/cinema/config/HttpResponseInterceptor.java` - 已完成
4. ✅ 创建 `src/main/java/com/cinema/config/WebMvcConfig.java` - 已完成
5. 🔨 重新编译并启动后端服务：
   ```bash
   mvn clean package
   mvn spring-boot:run
   ```

### 前端修复（Node.js）
1. ✅ 修改 `web/vite.config.ts` - 已完成
2. 🔨 安装依赖（如果还没有）：
   ```bash
   cd web
   npm install http-proxy
   ```
3. 🔨 重新启动 Vite 开发服务器：
   ```bash
   npm run dev
   ```

## 验证修复

1. 启动后端服务：`mvn spring-boot:run`
2. 启动前端服务：`npm run dev`
3. 在浏览器中打开前端应用
4. 打开开发者工具（F12）-> 网络选项卡
5. 加载电影列表页面
6. 检查 `/api/movies` 请求是否成功（应该显示 200 状态码）
7. 检查浏览器控制台是否没有错误信息

## 如果问题仍然存在

尝试以下额外的调试步骤：

1. **检查后端日志**
   ```bash
   # 查看 Spring Boot 启动日志
   tail -f logs/spring.log
   ```

2. **直接测试后端 API**
   ```bash
   curl http://localhost:8081/api/movies
   ```

3. **检查响应头**
   ```bash
   curl -i http://localhost:8081/api/movies
   ```

4. **清除缓存并重启**
   ```bash
   # 清除 npm 缓存
   npm cache clean --force
   
   # 清除 Maven 缓存
   mvn clean
   
   # 重新启动所有服务
   ```

5. **临时禁用代理进行测试**
   - 直接访问 `http://localhost:8081` 来验证后端是否正常工作

## 相关知识点

### 分块传输编码（Chunked Transfer Encoding）
- 用于当响应大小未知或很大时
- 格式：`chunk-size [chunk-extensions] CRLF chunk-data CRLF`
- 错误："Expected LF after chunk data" 意味着解析器在期望 CRLF 时收到了意外的数据

### 代理配置的最佳实践
- 总是设置 `timeout` 和 `proxyTimeout`
- 使用 `onProxyRes` 钩子来处理响应头
- 禁用不必要的编码（如 gzip）来简化调试
- 使用 `keepAlive` 来优化连接性能

## 相关文件位置

- [web/vite.config.ts](../../web/vite.config.ts) - Vite 配置
- [src/main/resources/application.properties](../../src/main/resources/application.properties) - Spring Boot 配置
- [src/main/java/com/cinema/config/](../../src/main/java/com/cinema/config/) - Java 配置类
