<script setup lang="ts">
import { computed } from "vue";
import { useUserStoreHook } from "@/store/modules/user";
import { useRouter } from "vue-router";
import dayjs from "dayjs";

const userStore = useUserStoreHook();
const router = useRouter();

// 获取当前时间段问候语
const timeRange = computed(() => {
  const hour = dayjs().hour();
  if (hour < 6) return "凌晨好";
  if (hour < 9) return "早上好";
  if (hour < 12) return "上午好";
  if (hour < 14) return "中午好";
  if (hour < 17) return "下午好";
  if (hour < 19) return "傍晚好";
  return "晚上好";
});

// 快捷跳转
const goto = (path: string) => {
  router.push(path);
};
</script>

<template>
  <div class="welcome-container">
    <el-card shadow="hover" class="header-card">
      <div class="header-content">
        <div class="avatar">
          <img
            src="https://avatars.githubusercontent.com/u/44761321?v=4"
            alt="avatar"
          />
        </div>
        <div class="greeting">
          <div class="title">
            {{ timeRange }}，<span class="username">{{
              userStore.username
            }}</span
            >，祝你开心每一天！
          </div>
          <div class="subtitle">
            当前角色：
            <el-tag v-if="userStore.roles.includes('admin')" type="danger"
              >系统管理员</el-tag
            >
            <el-tag v-else type="success">普通用户</el-tag>
            <span style="margin-left: 20px; color: #888">
              用户ID: {{ userStore.userId || "未获取" }}
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <div v-if="userStore.roles.includes('admin')" class="dashboard-grid">
      <el-card shadow="hover" class="grid-item" @click="goto('/cinema/order')">
        <template #header>
          <div class="card-header">
            <span>📦 订单管理</span>
          </div>
        </template>
        <div class="card-desc">查看全平台所有订单，处理退款申请。</div>
      </el-card>

      <el-card
        shadow="hover"
        class="grid-item"
        @click="goto('/cinema/schedule')"
      >
        <template #header>
          <div class="card-header">
            <span>📅 排片管理</span>
          </div>
        </template>
        <div class="card-desc">新增电影场次，调整放映时间与票价。</div>
      </el-card>

      <el-card shadow="hover" class="grid-item" @click="goto('/cinema/movie')">
        <template #header>
          <div class="card-header">
            <span>🎥 电影列表</span>
          </div>
        </template>
        <div class="card-desc">上架新电影，删除下架电影。</div>
      </el-card>
    </div>

    <div v-else class="dashboard-grid">
      <el-card shadow="hover" class="grid-item" @click="goto('/cinema/portal')">
        <template #header>
          <div class="card-header">
            <span>🎬 购票大厅</span>
          </div>
        </template>
        <div class="card-desc">浏览正在热映的电影，在线选座购票。</div>
      </el-card>

      <el-card shadow="hover" class="grid-item" @click="goto('/cinema/portal')">
        <template #header>
          <div class="card-header">
            <span>🎫 我的订单</span>
          </div>
        </template>
        <div class="card-desc">查看购票记录，进行退票操作。</div>
      </el-card>
    </div>

    <el-card shadow="never" class="system-info">
      <template #header>
        <span>💡 系统公告</span>
      </template>
      <div class="info-content">
        <p>1. 欢迎使用影院在线购票系统。</p>
        <p>2. 系统已接入实时定价策略，VIP座位与普通座位价格不同。</p>
        <p>3. 订单支付后如需退票，请在放映前 30 分钟操作。</p>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.welcome-container {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  align-items: center;
}

.avatar img {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  margin-right: 20px;
}

.greeting .title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.username {
  color: #409eff;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.grid-item {
  cursor: pointer;
  transition: all 0.3s;
}

.grid-item:hover {
  transform: translateY(-5px);
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.card-desc {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.info-content p {
  line-height: 28px;
  color: #555;
  margin: 5px 0;
}
</style>
