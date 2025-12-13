<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getMovieList, deleteMovie } from "@/api/cinema/movie"; // 确保路径正确
import type { MovieItem } from "@/api/cinema/model/movieModel";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";

const tableData = ref<MovieItem[]>([]);
const loading = ref(true);

const fetchData = async () => {
  try {
    loading.value = true;
    const res = await getMovieList();
    if (res.success) {
      tableData.value = res.data;
    }
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 新增：删除电影逻辑
const handleDelete = (id: string) => {
  ElMessageBox.confirm("确定要删除这部电影吗?", "警告", {
    type: "warning"
  }).then(async () => {
    await deleteMovie(id);
    message("删除成功", { type: "success" });
    fetchData();
  });
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
          <span>🎥 电影管理</span>
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

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="danger"
              size="small"
              @click="handleDelete(scope.row.id)"
              >删除</el-button
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
</style>
