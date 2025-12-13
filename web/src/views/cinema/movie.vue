<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getMovieList } from "@/api/cinema/movie";
import type { MovieItem } from "@/api/cinema/model/movieModel";
import { message } from "@/utils/message";

// 定义表格数据
const tableData = ref<MovieItem[]>([]);
const loading = ref(true);

// 获取数据的方法
const fetchData = async () => {
  try {
    loading.value = true;
    const res = await getMovieList();
    if (res.success) {
      tableData.value = res.data;
    }
  } catch (error) {
    console.error(error);
    // Vue Pure Admin 自带的消息提示
    message("获取电影数据失败", { type: "error" });
  } finally {
    loading.value = false;
  }
};

// 页面加载时调用
onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="main-content">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>🎥 正在热映电影</span>
          <el-button type="primary" @click="fetchData">刷新列表</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%"
        border
      >
        <el-table-column prop="title" label="电影名称" width="180">
          <template #default="scope">
            <span style="font-weight: bold">{{ scope.row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="rating" label="评分" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.rating >= 8 ? 'success' : 'warning'">
              {{ scope.row.rating }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="director" label="导演" width="120" />

        <el-table-column label="主演" min-width="200">
          <template #default="scope">
            {{ scope.row.actors.join(" / ") }}
          </template>
        </el-table-column>

        <el-table-column prop="duration" label="时长" width="100">
          <template #default="scope"> {{ scope.row.duration }} 分钟 </template>
        </el-table-column>

        <el-table-column label="操作" width="150" fixed="right">
          <template #default>
            <el-button link type="primary" size="small">查看场次</el-button>
            <el-button link type="primary" size="small">购票</el-button>
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
