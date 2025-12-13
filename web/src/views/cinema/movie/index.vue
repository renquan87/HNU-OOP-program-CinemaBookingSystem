<script setup lang="ts">
import { ref, onMounted } from "vue";
import { getMovieList, addMovie, deleteMovie } from "@/api/cinema/movie";
import type { MovieItem } from "@/api/cinema/model/movieModel";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";

// 表格数据
const tableData = ref<MovieItem[]>([]);
const loading = ref(true);

// 🟢 新增：添加电影弹窗控制
const dialogVisible = ref(false);
const form = ref({
  title: "",
  director: "",
  actors: "", // 输入时用逗号分隔
  duration: 120,
  rating: 8.0,
  description: "",
  genre: "剧情",
  releaseTime: "" // YYYY-MM-DD
});

// 获取数据
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

// 🟢 新增：提交添加电影
const handleSubmit = async () => {
  if (!form.value.title || !form.value.director || !form.value.releaseTime) {
    message("请填写完整的电影信息", { type: "warning" });
    return;
  }

  try {
    const res = await addMovie(form.value);
    if (res.success) {
      message("电影添加成功", { type: "success" });
      dialogVisible.value = false;
      // 重置表单
      form.value = {
        title: "",
        director: "",
        actors: "",
        duration: 120,
        rating: 8.0,
        description: "",
        genre: "剧情",
        releaseTime: ""
      };
      fetchData(); // 刷新列表
    } else {
      message(res.message || "添加失败", { type: "error" });
    }
  } catch (e) {
    console.error(e);
  }
};

// 删除电影
const handleDelete = (id: string) => {
  ElMessageBox.confirm("确定要删除这部电影吗? 删除后相关的排片也会被移除。", "警告", {
    type: "warning",
    confirmButtonText: "确定删除",
    cancelButtonText: "取消"
  })
    .then(async () => {
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
          <div class="header-btns">
            <el-button type="success" @click="dialogVisible = true">添加新电影</el-button>
            <el-button type="primary" @click="fetchData">刷新列表</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="tableData" style="width: 100%" border stripe>
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
            {{ Array.isArray(scope.row.actors) ? scope.row.actors.join(" / ") : scope.row.actors }}
          </template>
        </el-table-column>

        <el-table-column prop="genre" label="类型" width="100" />

        <el-table-column prop="releaseTime" label="上映日期" width="120" />

        <el-table-column prop="duration" label="时长" width="100">
          <template #default="scope"> {{ scope.row.duration }} 分钟 </template>
        </el-table-column>

        <el-table-column label="操作" width="100" fixed="right">
          <template #default="scope">
            <el-button link type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="上架新电影" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="电影名称" required>
          <el-input v-model="form.title" placeholder="例如：阿凡达2" />
        </el-form-item>

        <el-form-item label="导演" required>
          <el-input v-model="form.director" placeholder="例如：卡梅隆" />
        </el-form-item>

        <el-form-item label="主演" required>
          <el-input v-model="form.actors" placeholder="多个演员请用逗号分隔，如：张三,李四" />
        </el-form-item>

        <el-row>
          <el-col :span="12">
            <el-form-item label="时长(分钟)">
              <el-input-number v-model="form.duration" :min="1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="评分">
              <el-input-number v-model="form.rating" :min="0" :max="10" :precision="1" :step="0.1" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="类型">
          <el-select v-model="form.genre" placeholder="请选择类型" style="width: 100%">
            <el-option label="剧情" value="剧情" />
            <el-option label="科幻" value="科幻" />
            <el-option label="动作" value="动作" />
            <el-option label="喜剧" value="喜剧" />
            <el-option label="动画" value="动画" />
            <el-option label="悬疑" value="悬疑" />
            <el-option label="爱情" value="爱情" />
          </el-select>
        </el-form-item>

        <el-form-item label="上映日期" required>
          <el-date-picker
            v-model="form.releaseTime"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定上架</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.main-content { padding: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-btns { display: flex; gap: 10px; }
</style>
