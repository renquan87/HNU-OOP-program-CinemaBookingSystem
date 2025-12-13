<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getShowList } from "@/api/cinema/show";
import type { ShowItem } from "@/api/cinema/model/showModel";
import { ArrowLeft, Ticket } from "@element-plus/icons-vue";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const tableData = ref<ShowItem[]>([]);
const currentMovieTitle = ref("");

// 获取 URL 中的 movieId 参数
const movieId = route.query.movieId as string;

const fetchData = async () => {
  try {
    loading.value = true;
    // 传入 movieId 进行筛选
    const res = await getShowList({ movieId });
    if (res.success) {
      tableData.value = res.data;
      if (tableData.value.length > 0) {
        currentMovieTitle.value = tableData.value[0].movieTitle;
      }
    }
  } finally {
    loading.value = false;
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
          <div class="title-group">
            <el-button :icon="ArrowLeft" circle @click="router.back()" />
            <span class="title">
              {{
                currentMovieTitle
                  ? `《${currentMovieTitle}》的排片`
                  : "所有排片"
              }}
            </span>
          </div>
          <el-button type="primary" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        border
        stripe
        style="width: 100%"
      >
        <el-table-column prop="roomName" label="放映厅" width="150" />

        <el-table-column prop="startTime" label="放映时间" min-width="180">
          <template #default="scope">
            <el-tag size="large">{{
              scope.row.startTime.replace("T", " ")
            }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="basePrice" label="票价" width="100">
          <template #default="scope">
            <span style="color: #f56c6c; font-weight: bold"
              >￥{{ scope.row.basePrice }}</span
            >
          </template>
        </el-table-column>

        <el-table-column label="座位情况" width="180">
          <template #default="scope">
            <el-progress
              :percentage="
                Math.round(
                  (scope.row.availableSeats / scope.row.totalSeats) * 100
                )
              "
              :status="scope.row.availableSeats === 0 ? 'exception' : 'success'"
            >
              {{ scope.row.availableSeats }}/{{ scope.row.totalSeats }}
            </el-progress>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default>
            <el-button type="primary" size="small" :icon="Ticket"
              >选座购票</el-button
            >
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
.title-group {
  display: flex;
  align-items: center;
  gap: 15px;
}
.title {
  font-size: 18px;
  font-weight: bold;
}
</style>
