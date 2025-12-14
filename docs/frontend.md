# 前端文档

## 概述

电影院购票系统的前端基于Vue 3 + TypeScript + Vite开发，使用vue-pure-admin作为基础框架，提供现代化的用户界面和丰富的组件库。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **语言**: TypeScript
- **构建工具**: Vite
- **UI框架**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **样式**: Tailwind CSS
- **代码规范**: ESLint + Prettier + Stylelint

## 项目结构

```
web/
├── src/
│   ├── api/                    # API接口定义
│   │   ├── booking.ts         # 预订相关接口
│   │   ├── cinema.ts          # 影院相关接口
│   │   ├── user.ts            # 用户相关接口
│   │   └── system.ts          # 系统相关接口
│   ├── assets/                # 静态资源
│   │   ├── iconfont/          # 图标字体
│   │   ├── login/             # 登录页面资源
│   │   ├── status/            # 状态图标
│   │   └── svg/               # SVG图标
│   ├── components/            # 全局组件
│   │   ├── ReAnimateSelector/ # 动画选择器
│   │   ├── ReBarcode/         # 条形码组件
│   │   ├── ReCountTo/         # 数字动画
│   │   ├── ReDialog/          # 对话框
│   │   ├── ReDrawer/          # 抽屉
│   │   ├── ReIcon/            # 图标组件
│   │   ├── ReImageVerify/     # 图片验证码
│   │   ├── ReMap/             # 地图组件
│   │   ├── ReQrcode/          # 二维码
│   │   ├── ReSeamlessScroll/  # 无缝滚动
│   │   └── ReText/            # 文本组件
│   ├── config/                # 配置文件
│   ├── directives/            # 自定义指令
│   ├── layout/                # 布局组件
│   ├── plugins/               # 插件配置
│   ├── router/                # 路由配置
│   ├── store/                 # 状态管理
│   ├── style/                 # 全局样式
│   ├── types/                 # TypeScript类型定义
│   ├── utils/                 # 工具函数
│   ├── views/                 # 页面组件
│   │   ├── booking/           # 预订页面
│   │   ├── cinema/            # 影院管理页面
│   │   ├── dashboard/         # 仪表板
│   │   ├── login/             # 登录页面
│   │   ├── movie/             # 电影管理页面
│   │   └── user/              # 用户管理页面
│   ├── App.vue                # 根组件
│   └── main.ts                # 入口文件
├── public/                    # 公共静态资源
├── locales/                   # 国际化文件
├── mock/                      # 模拟数据
├── build/                     # 构建配置
├── types/                     # 全局类型定义
├── package.json               # 项目配置
├── vite.config.ts             # Vite配置
├── tsconfig.json              # TypeScript配置
└── README.md                  # 前端README
```

## 环境要求

- Node.js >= 16.0.0
- pnpm >= 7.0.0 (推荐) 或 npm/yarn

## 安装依赖

```bash
# 进入前端目录
cd web

# 安装依赖
pnpm install
```

## 开发环境运行

```bash
# 启动开发服务器
pnpm run dev

# 访问 http://localhost:5173
```

## 构建生产版本

```bash
# 构建生产版本
pnpm run build

# 预览构建结果
pnpm run preview
```

## 主要功能模块

### 1. 用户模块
- 用户登录/注册
- 个人信息管理
- 购票历史查询

### 2. 电影模块
- 电影列表浏览
- 电影详情查看
- 电影搜索筛选

### 3. 场次模块
- 场次信息展示
- 座位选择
- 票价计算

### 4. 预订模块
- 座位预订
- 订单管理
- 支付处理

### 5. 管理模块
- 电影管理
- 场次管理
- 用户管理
- 统计报表

## API接口

前端通过RESTful API与后端Spring Boot服务通信，主要接口包括：

### 电影相关
- `GET /api/movies` - 获取电影列表
- `GET /api/movies/{id}` - 获取电影详情
- `POST /api/movies` - 添加电影（管理员）
- `PUT /api/movies/{id}` - 更新电影（管理员）
- `DELETE /api/movies/{id}` - 删除电影（管理员）

### 场次相关
- `GET /api/shows` - 获取场次列表
- `GET /api/shows/{id}` - 获取场次详情
- `POST /api/shows` - 添加场次（管理员）
- `PUT /api/shows/{id}` - 更新场次（管理员）

### 预订相关
- `POST /api/bookings` - 创建预订
- `GET /api/bookings/{id}` - 获取预订详情
- `PUT /api/bookings/{id}/cancel` - 取消预订
- `POST /api/bookings/{id}/pay` - 支付预订

### 用户相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/users/profile` - 获取用户信息
- `PUT /api/users/profile` - 更新用户信息

## 开发指南

### 代码规范

项目采用严格的代码规范：

- 使用TypeScript编写所有业务代码
- 遵循Vue 3 Composition API最佳实践
- 使用ESLint + Prettier进行代码格式化
- 使用Stylelint检查CSS/SCSS代码
- 提交前必须通过所有lint检查

### 组件开发

```typescript
// 示例：电影卡片组件
<template>
  <div class="movie-card">
    <img :src="movie.poster" :alt="movie.title" />
    <h3>{{ movie.title }}</h3>
    <p>{{ movie.description }}</p>
    <ReButton @click="handleBook">预订</ReButton>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Movie } from '@/types/movie'

interface Props {
  movie: Movie
}

const props = defineProps<Props>()

const handleBook = () => {
  // 预订逻辑
}
</script>

<style scoped>
.movie-card {
  /* 样式 */
}
</style>
```

### 状态管理

使用Pinia进行状态管理：

```typescript
// stores/movie.ts
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Movie } from '@/types/movie'

export const useMovieStore = defineStore('movie', () => {
  const movies = ref<Movie[]>([])
  const loading = ref(false)

  const movieCount = computed(() => movies.value.length)

  const fetchMovies = async () => {
    loading.value = true
    try {
      const response = await api.getMovies()
      movies.value = response.data
    } finally {
      loading.value = false
    }
  }

  return {
    movies,
    loading,
    movieCount,
    fetchMovies
  }
})
```

### 路由配置

```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' }
  },
  {
    path: '/movies',
    name: 'Movies',
    component: () => import('@/views/movie/MovieList.vue'),
    meta: { title: '电影列表' }
  },
  {
    path: '/booking/:showId',
    name: 'Booking',
    component: () => import('@/views/booking/SeatSelection.vue'),
    meta: { title: '选择座位' }
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
```

## 部署说明

### 开发环境
```bash
# 启动后端服务
mvn spring-boot:run

# 启动前端开发服务器
cd web && pnpm run dev
```

### 生产环境
```bash
# 构建前端
cd web && pnpm run build

# 将dist目录部署到Web服务器
# 配置Nginx反向代理到Spring Boot后端
```

### Docker部署
```dockerfile
# Dockerfile示例
FROM node:18-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 常见问题

### 1. 依赖安装失败
```bash
# 清除缓存重新安装
rm -rf node_modules pnpm-lock.yaml
pnpm install
```

### 2. 热重载不生效
```bash
# 检查Vite配置
# 确保devServer配置正确
```

### 3. API请求失败
```bash
# 检查后端服务是否启动
# 检查API基础URL配置
# 检查CORS配置
```

## 贡献指南

1. Fork项目
2. 创建功能分支：`git checkout -b feature/new-feature`
3. 提交代码：`git commit -m 'feat: add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 提交Pull Request

## 许可证

本项目采用MIT许可证。