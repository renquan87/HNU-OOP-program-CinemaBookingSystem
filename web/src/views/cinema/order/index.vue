<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getAllOrders, refundOrder } from "@/api/cinema/index";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";

const tableData = ref([]);
const loading = ref(false);

// 加载所有订单
const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getAllOrders();
    if (res.success) {
      tableData.value = res.data;
    }
  } finally {
    loading.value = false;
  }
};

// 强制退款
const handleRefund = (row: any) => {
  ElMessageBox.confirm(
    `确定要强制退掉用户 ${row.userId} 的订单吗？此操作将触发系统通知。`,
    "管理员操作",
    { type: "warning" }
  ).then(async () => {
    const res = await refundOrder({ orderId: row.orderId });
    if (res.success) {
      message("退票成功，通知已发送", { type: "success" });
      fetchData(); // 刷新列表
    } else {
      message(res.message, { type: "error" });
    }
  });
};

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
    default:
      return "info";
  }
};

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="main-content">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>🧾 全平台订单管理</span>
          <el-button type="primary" @click="fetchData">刷新数据</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="orderId" label="订单号" width="180" />
        <el-table-column prop="userId" label="用户ID" width="120" />
        <el-table-column prop="movieTitle" label="电影" />
        <el-table-column prop="seats" label="座位" width="150" />
        <el-table-column prop="amount" label="金额" width="100">
          <template #default="{ row }">￥{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="time" label="下单时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ row.status }}</el-tag>
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
              强制退款
            </el-button>
            <span v-else style="color: #ccc; font-size: 12px">不可操作</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.main-content {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
