// src/router/modules/cinema.ts

// 1. 引入系统布局组件 (这是关键，否则页面没有侧边栏框架)
const Layout = () => import("@/layout/index.vue");

// 假设 RouteConfigsTable 是一个预定义的类型
// export interface RouteConfigsTable { ... }

export default {
  path: "/cinema",
  // 2. 指定父级组件为 Layout
  component: Layout,
  redirect: "/cinema/portal",
  meta: {
    icon: "ep:video-camera-filled",
    title: "影院系统",
    rank: 1
  },
  children: [
    // ================== 管理员可见 ==================
    {
      path: "/cinema/movie",
      name: "MovieManagement",
      component: () => import("@/views/cinema/movie/index.vue"),
      meta: {
        title: "电影列表",
        roles: ["admin"] // 仅管理员可见
      }
    },
    {
      path: "/cinema/schedule",
      name: "CinemaSchedule",
      component: () => import("@/views/cinema/schedule/index.vue"),
      meta: {
        title: "排片管理",
        roles: ["admin"] // 仅管理员可见
      }
    },
    // 🔴 订单管理 (管理员)
    {
      path: "/cinema/order",
      name: "OrderManagement",
      component: () => import("@/views/cinema/order/index.vue"),
      meta: {
        title: "订单管理",
        roles: ["admin"] // 仅管理员可见
      }
    },

    // ================== 普通用户可见 ==================
    {
      path: "/cinema/portal",
      name: "CinemaPortal",
      component: () => import("@/views/cinema/portal/index.vue"),
      meta: {
        title: "购票大厅",
        roles: ["common"] // 仅普通用户可见
      }
    },

    // 🔴 新增：电影详情页
    {
      path: "/cinema/movie-detail/:id",
      name: "MovieDetail",
      component: () => import("@/views/cinema/portal/detail.vue"),
      meta: {
        title: "电影详情",
        showLink: false, // 隐藏菜单
        activePath: "/cinema/portal", // 侧边栏高亮购票大厅
        roles: ["common", "admin"] // 普通用户和管理员均可访问
      }
    }
  ]
} as RouteConfigsTable;
