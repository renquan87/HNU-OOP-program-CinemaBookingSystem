<script setup lang="ts">
import { ref, onMounted } from "vue";
// 从 index.ts 引入场次相关接口
import {
  getShowList,
  addShow,
  deleteShow,
  getRoomList
} from "@/api/cinema/index";
// 从 movie.ts 引入电影相关接口
import { getMovieList } from "@/api/cinema/movie";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";

const showList = ref([]);
const movieList = ref([]);
const roomList = ref([]);
const loading = ref(false);

const dialogVisible = ref(false);
const form = ref({
  movieId: "",
  roomId: "",
  startTime: "",
  price: 50
});

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getShowList();
    showList.value = res.data;
  } finally {
    loading.value = false;
  }
};

const fetchOptions = async () => {
  const mRes = await getMovieList();
  movieList.value = mRes.data;
  const rRes = await getRoomList();
  roomList.value = rRes.data;
};

const handleSubmit = async () => {
  if (!form.value.movieId || !form.value.roomId || !form.value.startTime) {
    message("请填写完整信息", { type: "warning" });
    return;
  }
  await addShow(form.value);
  message("排片成功", { type: "success" });
  dialogVisible.value = false;
  fetchData();
};

const handleDelete = (id: string) => {
  ElMessageBox.confirm("确定要删除该场次吗?", "提示", { type: "warning" }).then(
    async () => {
      await deleteShow(id);
      message("删除成功", { type: "success" });
      fetchData();
    }
  );
};

onMounted(() => {
  fetchData();
  fetchOptions();
});
</script>

<template>
  <div class="main-content">
    <div class="toolbar">
      <el-button type="primary" @click="dialogVisible = true"
        >新增排片</el-button
      >
      <el-button @click="fetchData">刷新</el-button>
    </div>

    <el-table
      v-loading="loading"
      :data="showList"
      border
      style="width: 100%; margin-top: 20px"
    >
      <el-table-column prop="id" label="场次ID" width="180" />
      <el-table-column prop="movieTitle" label="电影名称" />
      <el-table-column prop="roomName" label="放映厅" width="120" />
      <el-table-column prop="startTime" label="放映时间" />
      <el-table-column prop="basePrice" label="票价" width="100">
        <template #default="{ row }">￥{{ row.basePrice }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link @click="handleDelete(row.id)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增排片" width="500px">
      <el-form label-width="80px">
        <el-form-item label="选择电影">
          <el-select v-model="form.movieId" placeholder="请选择电影">
            <el-option
              v-for="m in movieList"
              :key="m.id"
              :label="m.title"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择影厅">
          <el-select v-model="form.roomId" placeholder="请选择影厅">
            <el-option
              v-for="r in roomList"
              :key="r.id"
              :label="r.name"
              :value="r.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="放映时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            placeholder="选择日期时间"
            value-format="YYYY-MM-DD HH:mm"
          />
        </el-form-item>
        <el-form-item label="基础票价">
          <el-input-number v-model="form.price" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.main-content {
  padding: 20px;
  background: #fff;
}
</style>
