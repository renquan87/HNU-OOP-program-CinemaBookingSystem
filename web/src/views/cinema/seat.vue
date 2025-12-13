<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getShowSeats, createOrder } from "@/api/booking"; // 引入刚才写的API
import { message } from "@/utils/message"; // vue-pure-admin 的消息提示

const route = useRoute();
const router = useRouter();

// 从路由参数获取 showId，或者你可以写死一个 ID 测试: "SHOW-001"
const showId = (route.query.id as string) || "SHOW-001";
// 假设当前用户ID，实际应从 userStore 获取
const currentUserId = "renquan";

const seatList = ref([]);
const loading = ref(true);
const selectedSeats = ref<string[]>([]); // 选中的座位ID数组

// 初始化加载座位
const initSeats = async () => {
  try {
    loading.value = true;
    const res = await getShowSeats(showId);
    if (res.success) {
      seatList.value = res.data;
    } else {
      message("获取座位失败", { type: "error" });
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 点击座位逻辑
const handleSeatClick = (seat: any) => {
  if (seat.status !== "available") return; // 已售出或锁定不能点

  const index = selectedSeats.value.indexOf(seat.id);
  if (index !== -1) {
    // 已选 -> 取消
    selectedSeats.value.splice(index, 1);
  } else {
    // 未选 -> 选中 (限制最多选4个)
    if (selectedSeats.value.length >= 4) {
      message("一次最多选择4个座位", { type: "warning" });
      return;
    }
    selectedSeats.value.push(seat.id);
  }
};

// 计算总价
const totalPrice = computed(() => {
  let sum = 0;
  selectedSeats.value.forEach(id => {
    const seat = seatList.value.find((s: any) => s.id === id);
    if (seat) sum += seat.price;
  });
  return sum;
});

// 提交订单
const submitOrder = async () => {
  if (selectedSeats.value.length === 0) {
    message("请先选择座位", { type: "warning" });
    return;
  }

  try {
    const res = await createOrder({
      userId: currentUserId,
      showId: showId,
      seatIds: selectedSeats.value
    });

    if (res.success) {
      message(`下单成功！订单号: ${res.data.orderId}`, { type: "success" });
      // 跳转到支付页或刷新页面
      // router.push({ name: 'Pay', query: { orderId: res.data.orderId } });

      // 临时：刷新座位图看效果
      selectedSeats.value = [];
      initSeats();
    } else {
      message(res.message, { type: "error" });
    }
  } catch (error) {
    console.error(error);
  }
};

onMounted(() => {
  initSeats();
});
</script>

<template>
  <div v-loading="loading" class="seat-container">
    <div class="screen">银幕</div>

    <div class="seats-grid">
      <div
        v-for="seat in seatList"
        :key="seat.id"
        class="seat-item"
        :class="[
          seat.status,
          seat.type,
          { selected: selectedSeats.includes(seat.id) }
        ]"
        @click="handleSeatClick(seat)"
      >
        <span class="seat-icon" />
        <span class="seat-info">{{ seat.row }}排{{ seat.col }}座</span>
        <span class="seat-price">¥{{ seat.price }}</span>
      </div>
    </div>

    <div class="footer-bar">
      <div class="info">
        已选：{{ selectedSeats.length }} 张 | 总价：<span class="price"
          >¥{{ totalPrice }}</span
        >
      </div>
      <button class="buy-btn" @click="submitOrder">立即购票</button>
    </div>
  </div>
</template>

<style scoped>
.seat-container {
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.screen {
  width: 80%;
  height: 30px;
  background: #e0e0e0;
  text-align: center;
  line-height: 30px;
  margin-bottom: 40px;
  border-radius: 0 0 40px 40px;
  color: #666;
  box-shadow: 0 10px 10px -5px rgba(0, 0, 0, 0.1);
}

.seats-grid {
  display: flex;
  flex-wrap: wrap;
  width: 600px; /* 根据实际调整 */
  justify-content: center;
  gap: 10px;
}

.seat-item {
  width: 50px;
  height: 50px;
  border: 1px solid #ccc;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 10px;
  background: white;
  transition: all 0.2s;
}

/* 状态样式 */
.seat-item.available {
  background: #fff;
}
.seat-item.selected {
  background: #409eff;
  color: white;
  border-color: #409eff;
}
.seat-item.sold {
  background: #f56c6c;
  color: white;
  cursor: not-allowed;
}
.seat-item.locked {
  background: #e6a23c;
  color: white;
  cursor: not-allowed;
}

/* 类型样式 (边框区分) */
.seat-item.vip {
  border: 2px solid #e6a23c;
} /* VIP 金边 */
.seat-item.discount {
  border: 2px solid #67c23a;
} /* 优惠 绿边 */

.footer-bar {
  position: fixed;
  bottom: 0;
  width: 100%;
  height: 60px;
  background: white;
  border-top: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
}

.buy-btn {
  padding: 10px 30px;
  background: #f56c6c;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 16px;
}
.buy-btn:hover {
  background: #f78989;
}
</style>
