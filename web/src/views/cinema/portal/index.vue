<script setup lang="ts">
import { ref, onMounted, computed, onUnmounted, watch } from "vue";
import { getMovieList } from "@/api/cinema/movie";
import {
  getShowList,
  getShowSeats,
  createOrder,
  payOrder,
  getUserOrders,
  refundOrder
} from "@/api/cinema/index";
import { ElNotification, ElMessageBox } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";
import AiAssistant from "@/components/AiAssistant/index.vue";

// ================= 数据定义 =================
const movies = ref([]);
const loading = ref(false);
const userStore = useUserStoreHook();

// 购票弹窗相关
const seatDialogVisible = ref(false);
const currentMovie = ref<any>({});
const showList = ref([]);
const currentShowId = ref("");
const seatList = ref([]); // 原始座位列表
const selectedSeats = ref<string[]>([]);

// 订单弹窗相关
const orderDialogVisible = ref(false);
const myOrders = ref([]);
const orderLoading = ref(false);

// WebSocket 实例
let ws: WebSocket | null = null;

// ================= 核心计算 =================

// 1. 修复座位布局：按行分组
const seatsByRow = computed(() => {
  const rows: Record<number, any[]> = {};
  seatList.value.forEach((seat: any) => {
    if (!rows[seat.row]) {
      rows[seat.row] = [];
    }
    rows[seat.row].push(seat);
  });
  // 确保列排序
  for (const r in rows) {
    rows[r].sort((a, b) => a.col - b.col);
  }
  return rows;
});

// 2. 计算总价（防止浮点数精度问题）
const totalPrice = computed(() => {
  let total = 0;
  selectedSeats.value.forEach(id => {
    const seat = seatList.value.find((s: any) => s.id === id);
    if (seat) total += seat.price;
  });
  return Math.round(total * 100) / 100;
});

// ================= WebSocket 实时逻辑 =================

const initWebSocket = (showId: string) => {
  // 断开旧连接
  if (ws) ws.close();

  // 建立新连接 (注意端口号需与后端一致，这里假设是 8081)
  ws = new WebSocket(`ws://localhost:8081/ws/seats/${showId}`);

  ws.onopen = () => {
    console.log(`[WebSocket] 已连接场次: ${showId}`);
  };

  ws.onmessage = (event) => {
    if (event.data === "UPDATE") {
      console.log("[WebSocket] 收到座位更新通知");
      refreshSeatStatus(showId);
    }
  };

  ws.onclose = () => {
    console.log("[WebSocket] 连接已断开");
  };
};

// 静默刷新座位状态
const refreshSeatStatus = async (showId: string) => {
  const res = await getShowSeats(showId);
  if (res.success) {
    seatList.value = res.data;
    // 检查已选座位是否被抢
    const takenSeats = res.data.filter(
      (s: any) =>
        selectedSeats.value.includes(s.id) && s.status !== "available"
    );

    if (takenSeats.length > 0) {
      takenSeats.forEach((s: any) => {
        const idx = selectedSeats.value.indexOf(s.id);
        if (idx !== -1) selectedSeats.value.splice(idx, 1);
      });
      ElNotification({
        title: "手慢了",
        message: "您选择的部分座位已被其他人锁定",
        type: "warning"
      });
    }
  }
};

// 监听弹窗关闭，断开连接
watch(seatDialogVisible, newVal => {
  if (!newVal && ws) {
    ws.close();
    ws = null;
  }
});

onUnmounted(() => {
  if (ws) ws.close();
});

// ================= 业务逻辑 =================

// 加载电影
const loadMovies = async () => {
  loading.value = true;
  try {
    const res = await getMovieList();
    movies.value = res.data;
  } finally {
    loading.value = false;
  }
};

// 打开购票选座
const handleBuyTicket = async (movie: any) => {
  currentMovie.value = movie;
  const res = await getShowList(movie.id);
  if (!res.data || res.data.length === 0) {
    ElNotification({
      title: "通知",
      message: "该电影暂无排片",
      type: "warning"
    });
    return;
  }
  showList.value = res.data;
  // 默认选中第一个场次
  currentShowId.value = res.data[0].id;
  await loadSeats(res.data[0].id);
  seatDialogVisible.value = true;

  // 启动 WebSocket
  initWebSocket(currentShowId.value);
};

// 切换场次
const handleShowChange = async (val: string) => {
  await loadSeats(val);
  // 切换 WebSocket
  initWebSocket(val);
};

// 加载座位
const loadSeats = async (showId: string) => {
  selectedSeats.value = [];
  const res = await getShowSeats(showId);
  seatList.value = res.data;
};

// 选座交互
const toggleSeat = (seat: any) => {
  if (seat.status !== "available") return;
  const index = selectedSeats.value.indexOf(seat.id);
  if (index !== -1) {
    selectedSeats.value.splice(index, 1);
  } else {
    if (selectedSeats.value.length >= 4) {
      ElNotification({
        title: "提示",
        message: "一次最多选择4个座位",
        type: "warning"
      });
      return;
    }
    selectedSeats.value.push(seat.id);
  }
};

const getSeatClass = (seat: any) => {
  if (selectedSeats.value.includes(seat.id)) return "seat-selected";
  if (seat.status === "locked" || seat.status === "sold") return "seat-sold";
  if (seat.type === "vip") return "seat-vip";
  return "seat-available";
};

// 确认下单
const confirmOrder = async () => {
  if (selectedSeats.value.length === 0) return;

  const userId = userStore.userId;
  if (!userId) {
    ElNotification({ title: "错误", message: "请重新登录", type: "error" });
    return;
  }

  try {
    const orderRes = await createOrder({
      userId,
      showId: currentShowId.value,
      seatIds: selectedSeats.value
    });

    if (orderRes.success && orderRes.code === 200) {
      ElNotification({
        title: "系统通知",
        message: "锁定成功，正在支付...",
        type: "success",
        duration: 1500
      });

      const payRes = await payOrder({ orderId: orderRes.data.orderId });
      if (payRes.success) {
        ElNotification({
          title: "支付成功",
          message: `扣款 ￥${totalPrice.value}，请在“我的订单”查看`,
          type: "success",
          duration: 3000
        });
        seatDialogVisible.value = false;
        loadMovies();
      } else {
        ElNotification({
          title: "支付失败",
          message: payRes.message,
          type: "error"
        });
      }
    } else {
      ElNotification({
        title: "下单失败",
        message: orderRes.message,
        type: "error"
      });
    }
  } catch (error: any) {
    console.error(error);
    ElNotification({ title: "错误", message: "系统异常", type: "error" });
  }
};

// 我的订单
const openMyOrders = async () => {
  const userId = userStore.userId;
  if (!userId) {
    ElNotification({ title: "警告", message: "请先登录", type: "warning" });
    return;
  }
  orderDialogVisible.value = true;
  orderLoading.value = true;
  try {
    const res = await getUserOrders(userId);
    if (res.success) {
      myOrders.value = res.data;
    }
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
      // 如果退的是当前正在看的场次，刷新座位图
      if (currentShowId.value) {
        loadSeats(currentShowId.value);
      }
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
    <div class="header-actions">
      <span class="welcome-text">👋 欢迎，{{ userStore.username }}</span>
      <el-button
        type="primary"
        plain
        icon="el-icon-tickets"
        @click="openMyOrders"
      >
        我的订单 / 退票
      </el-button>
    </div>

    <div v-loading="loading" class="movie-grid">
      <el-card
        v-for="item in movies"
        :key="item.id"
        class="movie-card"
        :body-style="{ padding: '0px' }"
      >
        <div class="movie-info">
          <h3>{{ item.title }}</h3>
          <p>导演：{{ item.director }}</p>
          <p>类型：{{ item.genre }}</p>
          <div class="rating">
            评分：<span>{{ item.rating }}</span>
          </div>
        </div>
        <div class="bottom-btn">
          <el-button type="primary" block @click="handleBuyTicket(item)"
          >选座购票</el-button
          >
        </div>
      </el-card>
    </div>

    <el-dialog
      v-model="seatDialogVisible"
      :title="'购票 - ' + currentMovie.title"
      width="850px"
      append-to-body
    >
      <div class="booking-content">
        <div class="show-select">
          <span>选择场次：</span>
          <el-radio-group
            v-model="currentShowId"
            size="default"
            @change="handleShowChange"
          >
            <el-radio-button
              v-for="show in showList"
              :key="show.id"
              :label="show.id"
            >
              {{ show.startTime.substring(5, 16) }}
              ({{ show.roomName }})
            </el-radio-button>
          </el-radio-group>
        </div>

        <el-divider />

        <div class="screen-container">
          <div class="screen">银幕中央</div>
        </div>

        <div class="seat-map-wrapper">
          <div class="seat-map-container">
            <div
              v-for="(seats, rowNum) in seatsByRow"
              :key="rowNum"
              class="seat-row"
            >
              <div class="row-label">{{ rowNum }}排</div>
              <div class="row-seats">
                <el-tooltip
                  v-for="seat in seats"
                  :key="seat.id"
                  effect="dark"
                  :content="`${seat.type === 'vip' ? 'VIP' : '普通'} ￥${seat.price}`"
                  placement="top"
                >
                  <div
                    class="seat-item"
                    :class="getSeatClass(seat)"
                    @click="toggleSeat(seat)"
                  >
                    {{ seat.col }}
                  </div>
                </el-tooltip>
              </div>
            </div>
          </div>
        </div>

        <div class="legend">
          <div class="legend-item"><span class="dot available" />普通</div>
          <div class="legend-item"><span class="dot vip" />VIP</div>
          <div class="legend-item"><span class="dot selected" />已选</div>
          <div class="legend-item"><span class="dot sold" />已售/锁定</div>
        </div>
      </div>

      <template #footer>
        <div class="footer-info">
          <span
          >已选：{{ selectedSeats.length }} 座 | 总价：<span
            style="color: #f56c6c; font-weight: bold; font-size: 18px"
          >
              ￥{{ totalPrice.toFixed(2) }}
            </span>
          </span>
          <el-button
            type="primary"
            size="large"
            :disabled="selectedSeats.length === 0"
            @click="confirmOrder"
          >
            确认支付
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="orderDialogVisible"
      title="我的订单"
      width="900px"
      append-to-body
    >
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
}
.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.welcome-text {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.movie-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}
.movie-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}
.movie-info {
  padding: 15px;
}
.rating span {
  color: #ff9900;
  font-weight: bold;
}
.bottom-btn {
  padding: 10px;
  border-top: 1px solid #eee;
}

/* 选座区域 */
.show-select {
  text-align: center;
  margin-bottom: 20px;
}
.screen-container {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.screen {
  background: #e0e0e0;
  color: #999;
  text-align: center;
  padding: 4px;
  border-radius: 0 0 40px 40px;
  width: 50%;
  font-size: 12px;
}

.seat-map-wrapper {
  max-height: 400px;
  overflow-y: auto;
  padding: 10px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
}
.seat-map-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}
.seat-row {
  display: flex;
  align-items: center;
  gap: 15px;
}
.row-label {
  width: 30px;
  text-align: right;
  color: #999;
  font-size: 12px;
}
.row-seats {
  display: flex;
  gap: 8px;
}

.seat-item {
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  user-select: none;
}
.seat-item:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 状态颜色 */
.seat-available {
  background: #fff;
  color: #606266;
}
.seat-vip {
  background: #fdf6ec;
  border-color: #e6a23c;
  color: #e6a23c;
  font-weight: bold;
}
.seat-selected {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}
.seat-sold {
  background: #f56c6c;
  color: #fff;
  border-color: #f56c6c;
  cursor: not-allowed;
  opacity: 0.6;
}

.legend {
  margin-top: 15px;
  display: flex;
  justify-content: center;
  gap: 15px;
}
.legend-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
}
.dot {
  width: 12px;
  height: 12px;
  margin-right: 4px;
  border-radius: 2px;
  border: 1px solid #ccc;
}
.dot.available {
  background: #fff;
}
.dot.vip {
  background: #fdf6ec;
  border-color: #e6a23c;
}
.dot.selected {
  background: #409eff;
  border-color: #409eff;
}
.dot.sold {
  background: #f56c6c;
  border-color: #f56c6c;
}

.footer-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
