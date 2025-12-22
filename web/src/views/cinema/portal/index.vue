<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { getMovieList } from "@/api/cinema/movie";
import {
  getShowSeats,
  createOrder,
  payOrder,
  getUserOrders,
  refundOrder
} from "@/api/cinema/index";
import { ElNotification, ElMessageBox } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import AiAssistant from "@/components/AiAssistant/index.vue";
import ReBookingDialog from "@/components/ReBookingDialog/index.vue";

// ================= 数据定义 =================
const router = useRouter();
const userStore = useUserStoreHook();
const movies = ref([]);
const loading = ref(false);

// 购票弹窗控制
const seatDialogVisible = ref(false);
const currentMovie = ref<any>({});
// 以下 seat/show 状态主要由 ReBookingDialog 内部使用或维护，但在外部仍需声明

// 订单弹窗
const orderDialogVisible = ref(false);
const myOrders = ref([]);
const orderLoading = ref(false);

// ================= 业务逻辑 =================

const loadMovies = async () => {
  loading.value = true;
  try {
    const res = await getMovieList();
    if (res && res.success) {
      movies.value = res.data || [];
    } else {
      console.error("获取电影列表失败:", res);
      movies.value = [];
    }
  } catch (error) {
    console.error("获取电影列表异常:", error);
    movies.value = [];
    // 如果是权限错误，会被http拦截器处理并跳转到登录页
  } finally {
    loading.value = false;
  }
};

const goToDetail = (movie: any) => {
  router.push({ name: "MovieDetail", params: { id: movie.id } });
};

const handleQuickBuy = (movie: any, e: Event) => {
  if (e) e.stopPropagation(); // 防止冒泡触发跳转
  currentMovie.value = movie;
  seatDialogVisible.value = true;
};

// 我的订单逻辑
const openMyOrders = async () => {
  const userId = userStore.userId;
  if (!userId) return ElNotification({ title: "警告", message: "请先登录", type: "warning" });
  orderDialogVisible.value = true;
  orderLoading.value = true;
  try {
    const res = await getUserOrders(userId);
    if (res.success) myOrders.value = res.data;
  } finally {
    orderLoading.value = false;
  }
};

// 退票
const handleRefund = (order: any) => {
  ElMessageBox.confirm(
    `确定要退掉 "${order.movieTitle}" 的票吗？`,
    "退票确认",
    {
      type: "warning",
      confirmButtonText: "确定退票",
      cancelButtonText: "再想想"
    }
  ).then(async () => {
    const res = await refundOrder({ orderId: order.orderId });
    if (res.success) {
      ElNotification({
        title: "系统通知",
        message: "退票成功，款项已原路退回",
        type: "success"
      });
      openMyOrders();
      // 如果退的是当前正在看的场次，刷新座位图（需要 ReBookingDialog 暴露事件或状态）
    } else {
      ElNotification({
        title: "退票失败",
        message: res.message,
        type: "error"
      });
    }
  });
};

const getStatusTag = (status: string) => {
  const map: any = {
    PAID: "success",
    PENDING: "warning",
    REFUNDED: "info",
    CANCELLED: "danger",
    EXPIRED: "info"
  };
  return map[status] || "info";
};

const getStatusText = (status: string) => {
  const map: any = {
    PAID: "已支付",
    PENDING: "待支付",
    REFUNDED: "已退票",
    CANCELLED: "已取消",
    EXPIRED: "已过期",
    RESERVED: "预留中"
  };
  return map[status] || status;
};


onMounted(() => {
  loadMovies();
});
</script>

<template>
  <div class="portal-container">
    <div class="header-banner">
      <div class="header-content">
        <h2>👋 欢迎回来，{{ userStore.username }}</h2>
        <p>今日热映电影推荐，点击卡片查看详情与预告片</p>
      </div>
      <el-button type="primary" size="large" icon="el-icon-tickets" @click="openMyOrders" round>
        查看我的订单
      </el-button>
    </div>

    <div v-loading="loading" class="movie-grid">
      <el-card
        v-for="item in movies"
        :key="item.id"
        class="movie-card"
        :body-style="{ padding: '0px' }"
        shadow="hover"
        @click="goToDetail(item)"
      >
        <div class="poster-wrapper">
          <el-image
            v-if="item.coverUrl"
            :src="item.coverUrl"
            fit="cover"
            class="poster-image"
            lazy
          >
            <template #placeholder>
              <div class="image-slot loading">加载中...</div>
            </template>
            <template #error>
              <div class="image-slot error">
                <span>{{ item.title.substring(0, 1) }}</span>
              </div>
            </template>
          </el-image>
          <div v-else class="image-slot default">
            <span>{{ item.title.substring(0, 1) }}</span>
          </div>

          <div class="poster-mask">
            <span class="play-icon">▶</span>
          </div>

          <div class="rating-tag">
            <span>{{ item.rating }}</span> <span class="unit">分</span>
          </div>
        </div>

        <div class="card-content">
          <h3 class="movie-title" :title="item.title">{{ item.title }}</h3>
          <div class="movie-meta">
            <el-tag size="small" effect="plain">{{ item.genre }}</el-tag>
            <span class="duration">{{ item.duration }}分钟</span>
          </div>
          <p class="director">导演：{{ item.director }}</p>

          <div class="card-actions">
            <el-button
              type="primary"
              block
              color="#f56c6c"
              @click="(e) => handleQuickBuy(item, e)"
              style="font-weight: bold; width: 100%;"
              class="buy-btn"
            >
              选座购票
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <ReBookingDialog v-model:visible="seatDialogVisible" :movie="currentMovie" />

    <el-dialog v-model="orderDialogVisible" title="我的订单" width="900px" append-to-body>
      <el-table
        v-loading="orderLoading"
        :data="myOrders"
        border
        style="width: 100%"
      >
        <el-table-column prop="orderId" label="订单号" width="180" />
        <el-table-column prop="movieTitle" label="电影" />
        <el-table-column prop="startTime" label="时间" width="160" />
        <el-table-column prop="seats" label="座位" />
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }">￥{{ row.totalAmount }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{
                getStatusText(row.status)
              }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PAID'"
              type="danger"
              size="small"
              link
              @click="handleRefund(row)"
            >
              退票
            </el-button>
            <span v-else style="color: #999; font-size: 12px">不可操作</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <AiAssistant />
  </div>
</template>

<style scoped>
.portal-container {
  padding: 20px;
  background-color: #f6f8fa;
  min-height: 100vh;
}

.header-banner {
  background: white;
  padding: 20px 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-content h2 { margin: 0 0 8px 0; color: #303133; }
.header-content p { margin: 0; color: #909399; font-size: 14px; }

/* 网格布局优化 */
.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 24px;
}

.movie-card {
  border: none;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  overflow: hidden;
  background: #fff;
  position: relative;
}

.movie-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 20px rgba(0, 0, 0, 0.1);
}

/* 海报区域 */
.poster-wrapper {
  position: relative;
  height: 340px; /* 固定高度，保持海报比例 */
  background-color: #f0f2f5;
  overflow: hidden;
}

.poster-image {
  width: 100%;
  height: 100%;
  display: block;
  transition: transform 0.5s ease;
}

.movie-card:hover .poster-image {
  transform: scale(1.05);
}

/* 图片加载失败或无图时的占位 */
.image-slot {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background: #eef2f7;
  color: #909399;
  font-size: 14px;
}
.image-slot.error span, .image-slot.default span {
  font-size: 80px;
  font-weight: bold;
  color: #dcdfe6;
}

/* 评分标签 */
.rating-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  background: rgba(0, 0, 0, 0.7);
  color: #ffcc00;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: bold;
  font-size: 16px;
  backdrop-filter: blur(4px);
}
.rating-tag .unit { font-size: 12px; color: #fff; margin-left: 2px; }

/* 悬停遮罩 */
.poster-mask {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.movie-card:hover .poster-mask { opacity: 1; }
.play-icon {
  font-size: 40px;
  color: white;
  background: rgba(255, 255, 255, 0.2);
  width: 60px; height: 60px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  backdrop-filter: blur(2px);
}

/* 内容区域 */
.card-content {
  padding: 16px;
}

.movie-title {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.movie-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.duration { color: #909399; font-size: 13px; }

.director {
  font-size: 13px;
  color: #606266;
  margin: 0 0 16px 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-actions {
  display: flex;
}

/* 统一购票按钮样式 */
.buy-btn {
  width: 100%;
  /* 移除原有的线性渐变，使用 color="#f56c6c" */
  font-weight: bold;
  height: 36px;
  border: none;
}
</style>
