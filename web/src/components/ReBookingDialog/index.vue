<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from "vue";
import { getShowList, getShowSeats, createOrder, payOrder } from "@/api/cinema/index";
import { ElNotification } from "element-plus";
import { useUserStoreHook } from "@/store/modules/user";

const props = defineProps({
  visible: Boolean,
  movie: Object
});

const emit = defineEmits(["update:visible", "success"]);

const userStore = useUserStoreHook();
const showList = ref([]);
const currentShowId = ref("");
const seatList = ref([]);
const selectedSeats = ref<string[]>([]);
const seatsByRow = computed(() => {
  const rows: Record<number, any[]> = {};
  seatList.value.forEach((seat: any) => {
    if (!rows[seat.row]) rows[seat.row] = [];
    rows[seat.row].push(seat);
  });
  for (const r in rows) rows[r].sort((a, b) => a.col - b.col);
  return rows;
});

const totalPrice = computed(() => {
  let total = 0;
  selectedSeats.value.forEach(id => {
    const seat = seatList.value.find((s: any) => s.id === id);
    if (seat) total += seat.price;
  });
  return Math.round(total * 100) / 100;
});

let ws: WebSocket | null = null;

const initWebSocket = (showId: string) => {
  if (ws) ws.close();
  ws = new WebSocket(`ws://localhost:8081/ws/seats/${showId}`);
  ws.onmessage = (event) => {
    if (event.data === "UPDATE") loadSeats(showId);
  };
};

const loadSeats = async (showId: string) => {
  const res = await getShowSeats(showId);
  if (res.success) seatList.value = res.data;
};

const handleShowChange = async (val: string) => {
  selectedSeats.value = [];
  await loadSeats(val);
  initWebSocket(val);
};

const toggleSeat = (seat: any) => {
  if (seat.status !== "available") return;
  const index = selectedSeats.value.indexOf(seat.id);
  if (index !== -1) selectedSeats.value.splice(index, 1);
  else {
    if (selectedSeats.value.length >= 4) return ElNotification({ title: "提示", message: "限购4张", type: "warning" });
    selectedSeats.value.push(seat.id);
  }
};

const getSeatClass = (seat: any) => {
  if (selectedSeats.value.includes(seat.id)) return "seat-selected";
  if (seat.status !== "available") return "seat-sold";
  if (seat.type === "vip") return "seat-vip";
  return "seat-available";
};

const confirmOrder = async () => {
  if (selectedSeats.value.length === 0) return;
  try {
    const orderRes = await createOrder({
      userId: userStore.userId,
      showId: currentShowId.value,
      seatIds: selectedSeats.value
    });
    if (orderRes.success) {
      ElNotification({ title: "锁定成功", message: "正在支付...", type: "success" });
      const payRes = await payOrder({ orderId: orderRes.data.orderId });
      if (payRes.success) {
        ElNotification({ title: "支付成功", type: "success" });
        emit("success");
        closeDialog();
      } else {
        ElNotification({ title: "支付失败", message: payRes.message, type: "error" });
      }
    } else {
      ElNotification({ title: "下单失败", message: orderRes.message, type: "error" });
    }
  } catch (e) {
    console.error(e);
  }
};

const closeDialog = () => {
  if (ws) ws.close();
  emit("update:visible", false);
};

// 监听弹窗打开，加载数据
watch(() => props.visible, async (val) => {
  if (val && props.movie) {
    const res = await getShowList(props.movie.id);
    if (res.data && res.data.length > 0) {
      showList.value = res.data;
      currentShowId.value = res.data[0].id;
      await loadSeats(currentShowId.value);
      initWebSocket(currentShowId.value);
    } else {
      ElNotification({ title: "提示", message: "暂无排片", type: "warning" });
      emit("update:visible", false);
    }
  }
});

onUnmounted(() => { if (ws) ws.close(); });
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="'购票 - ' + (movie ? movie.title : '')"
    width="850px"
    append-to-body
    @close="closeDialog"
  >
    <div class="booking-content" v-if="showList.length > 0">
      <div class="show-select">
        <span>选择场次：</span>
        <el-radio-group v-model="currentShowId" @change="handleShowChange">
          <el-radio-button v-for="show in showList" :key="show.id" :label="show.id">
            {{ show.startTime.substring(5, 16) }} ({{ show.roomName }})
          </el-radio-button>
        </el-radio-group>
      </div>

      <el-divider />

      <div class="screen-container"><div class="screen">银幕中央</div></div>

      <div class="seat-map-wrapper">
        <div class="seat-map-container">
          <div v-for="(seats, rowNum) in seatsByRow" :key="rowNum" class="seat-row">
            <div class="row-label">{{ rowNum }}排</div>
            <div class="row-seats">
              <el-tooltip v-for="seat in seats" :key="seat.id" :content="`￥${seat.price}`" placement="top">
                <div class="seat-item" :class="getSeatClass(seat)" @click="toggleSeat(seat)">{{ seat.col }}</div>
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>

      <div class="legend">
        <div class="legend-item"><span class="dot available" />普通</div>
        <div class="legend-item"><span class="dot vip" />VIP</div>
        <div class="legend-item"><span class="dot selected" />已选</div>
        <div class="legend-item"><span class="dot sold" />售/锁</div>
      </div>
    </div>

    <template #footer>
      <div class="footer-info">
        <span>已选：{{ selectedSeats.length }} 座 | 总价：<span style="color:#f56c6c;font-weight:bold">￥{{ totalPrice.toFixed(2) }}</span></span>
        <el-button type="primary" :disabled="selectedSeats.length===0" @click="confirmOrder">确认支付</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style scoped>
/* 复制原 index.vue 的 CSS 样式到这里 */
.show-select { text-align: center; margin-bottom: 20px; }
.screen-container { display: flex; justify-content: center; margin-bottom: 20px; }
.screen { background: #e0e0e0; color: #999; text-align: center; padding: 4px; border-radius: 0 0 40px 40px; width: 50%; }
.seat-map-wrapper { max-height: 400px; overflow-y: auto; padding: 10px; border: 1px solid #f0f0f0; border-radius: 4px; }
.seat-map-container { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.seat-row { display: flex; align-items: center; gap: 15px; }
.row-label { width: 30px; text-align: right; color: #999; font-size: 12px; }
.row-seats { display: flex; gap: 8px; }
.seat-item { width: 32px; height: 32px; line-height: 32px; text-align: center; font-size: 12px; border-radius: 4px; cursor: pointer; border: 1px solid #dcdfe6; user-select: none; }
.seat-available { background: #fff; color: #606266; }
.seat-vip { background: #fdf6ec; border-color: #e6a23c; color: #e6a23c; font-weight: bold; }
.seat-selected { background: #409eff; color: #fff; border-color: #409eff; }
.seat-sold { background: #f56c6c; color: #fff; border-color: #f56c6c; cursor: not-allowed; opacity: 0.6; }
.legend { margin-top: 15px; display: flex; justify-content: center; gap: 15px; }
.legend-item { display: flex; align-items: center; font-size: 12px; }
.dot { width: 12px; height: 12px; margin-right: 4px; border-radius: 2px; border: 1px solid #ccc; }
.dot.available { background: #fff; } .dot.vip { background: #fdf6ec; border-color: #e6a23c; } .dot.selected { background: #409eff; border-color: #409eff; } .dot.sold { background: #f56c6c; border-color: #f56c6c; }
.footer-info { display: flex; justify-content: space-between; align-items: center; }
</style>
