<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { getMovieList } from "@/api/cinema/movie";
import {
  getShowList,
  getShowSeats,
  createOrder,
  payOrder,
  getUserOrders,
  refundOrder
} from "@/api/cinema/index";
// 统一使用 ElNotification 和 ElMessageBox
import { ElNotification, ElMessageBox } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";

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

// ================= 核心计算 (新增) =================
// 将扁平的座位数组转换为按行分组的结构: { 1: [Seat, Seat], 2: [Seat, Seat] }
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

// 计算总价（体现定价策略）
const totalPrice = computed(() => {
  let total = 0;
  selectedSeats.value.forEach(id => {
    const seat = seatList.value.find((s: any) => s.id === id);
    if (seat) total += seat.price;
  });
  // 🔴 关键修复：计算完成后，也进行四舍五入，避免前端累加浮点数误差
  const roundedPrice = Math.round(total * 100);
  return roundedPrice / 100.0;
});

// ================= 业务逻辑 =================

// 1. 加载电影
const loadMovies = async () => {
  loading.value = true;
  try {
    const res = await getMovieList();
    movies.value = res.data;
  } finally {
    loading.value = false;
  }
};

// 2. 打开购票选座
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
};

// 3. 切换场次
const handleShowChange = async (val: string) => {
  await loadSeats(val);
};

// 4. 加载座位
const loadSeats = async (showId: string) => {
  selectedSeats.value = [];
  const res = await getShowSeats(showId);
  seatList.value = res.data;
};

// 5. 选座交互
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
  // 'locked' 和 'sold' 都表示不可用，合并显示
  if (seat.status === "locked" || seat.status === "sold") return "seat-sold";
  if (seat.type === "vip") return "seat-vip";
  return "seat-available";
};

// 6. 确认下单 & 自动支付 (核心业务流程)
const confirmOrder = async () => {
  if (selectedSeats.value.length === 0) return;

  const userId = userStore.userId;
  if (!userId) {
    ElNotification({ title: "错误", message: "请重新登录", type: "error" });
    return;
  }

  try {
    // 步骤 1: 创建订单（锁座）
    const orderRes = await createOrder({
      userId,
      showId: currentShowId.value,
      seatIds: selectedSeats.value
    });

    if (orderRes.success && orderRes.code === 200) {
      // 模拟接收系统通知（锁座成功）
      ElNotification({
        title: "系统通知",
        message: "订单创建成功，座位已锁定，正在跳转支付...",
        type: "success",
        duration: 2000
      });

      // 步骤 2: 自动支付
      const payRes = await payOrder({ orderId: orderRes.data.orderId });
      if (payRes.success) {
        // 模拟接收系统通知（支付成功）
        ElNotification({
          title: "系统通知",
          message: `支付成功！扣款 ￥${totalPrice.value}。请在“我的订单”中查看。`,
          type: "success",
          duration: 4000
        });

        seatDialogVisible.value = false;
        loadMovies(); // 刷新电影列表
      } else {
        ElNotification({
          title: "支付失败",
          message: payRes.message,
          type: "error"
        });
        // 支付失败，座位在后端会自动解锁或过期取消
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
    ElNotification({
      title: "请求错误",
      message: "网络请求失败或服务器错误",
      type: "error"
    });
  }
};

// 7. 我的订单与退票
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
  } catch {
    ElNotification({ title: "错误", message: "加载订单失败", type: "error" });
  } finally {
    orderLoading.value = false;
  }
};

const handleRefund = (order: any) => {
  ElMessageBox.confirm(
    `确定要退掉 "${order.movieTitle}" 的票吗？`,
    "退票确认",
    {
      type: "warning",
      confirmButtonText: "确定退票",
      cancelButtonText: "再想想"
    }
  )
    .then(async () => {
      const res = await refundOrder({ orderId: order.orderId });
      if (res.success) {
        // 模拟接收退票通知
        ElNotification({
          title: "系统通知",
          message: "退票申请已通过，款项将原路返回",
          type: "success"
        });
        openMyOrders(); // 刷新
        loadSeats(currentShowId.value); // 刷新座位图，释放座位
      } else {
        ElNotification({
          title: "退票失败",
          message: res.message,
          type: "error"
        });
      }
    })
    .catch(() => {
      // 用户取消操作
    });
};

// 工具函数
const getStatusTag = (status: string) => {
  switch (status) {
    case "PAID":
      return "success";
    case "PENDING":
      return "warning";
    case "REFUNDED":
      return "info";
    case "CANCELLED":
      return "danger";
    case "EXPIRED":
      return "info";
    case "RESERVED":
      return "warning";
    default:
      return "info";
  }
};

const getStatusText = (status: string) => {
  switch (status) {
    case "PAID":
      return "已支付";
    case "PENDING":
      return "待支付";
    case "REFUNDED":
      return "已退票";
    case "CANCELLED":
      return "已取消";
    case "EXPIRED":
      return "已过期";
    case "RESERVED":
      return "预留中";
    default:
      return status;
  }
};

onMounted(() => loadMovies());
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
              ({{ show.roomName }}) ￥{{ show.basePrice }}
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
                  :content="`${seat.type === 'vip' ? 'VIP' : '普通'}座位 ￥${seat.price.toFixed(2)}`"
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
          <div class="legend-item"><span class="dot available" />可选</div>
          <div class="legend-item"><span class="dot selected" />已选</div>
          <div class="legend-item"><span class="dot sold" />已售/锁定</div>
          <div class="legend-item"><span class="dot vip" />VIP</div>
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
        <el-table-column prop="startTime" label="开场时间" width="160" />
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
  </div>
</template>

<style scoped>
/* 样式保留第二段代码的结构，并包含第一段代码的精简优化 */
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
  width: 40px;
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
} /* 包含已售和锁定 */

.legend {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 20px;
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
.dot.selected {
  background: #409eff;
  border-color: #409eff;
}
.dot.sold {
  background: #f56c6c;
  border-color: #f56c6c;
}
.dot.vip {
  background: #fdf6ec;
  border-color: #e6a23c;
}

.footer-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
