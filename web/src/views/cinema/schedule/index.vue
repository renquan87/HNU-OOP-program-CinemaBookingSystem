<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
// 从 index.ts 引入场次相关接口
import {
  getShowList,
  addShow,
  deleteShow,
  getRoomList
} from "@/api/cinema/index";
// 从 movie.ts 引入电影相关接口
import { getMovieList } from "@/api/cinema/movie";
import type { ShowItem } from "@/api/cinema/model/showModel";
import { message } from "@/utils/message";
import { ElMessageBox } from "element-plus";

const upcomingShows = ref<ShowItem[]>([]);
const historyShows = ref<ShowItem[]>([]);
const serverTime = ref("");
const activeTab = ref("upcoming");
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

const formattedServerTime = computed(() =>
  serverTime.value ? serverTime.value.replace("T", " ") : "尚未同步"
);

const formatTime = (value: string) => (value ? value.replace("T", " ") : "-");

const fetchData = async () => {
  loading.value = true;
  try {
    const res = await getShowList();
    if (res.success && res.data) {
      upcomingShows.value = res.data.upcomingShows;
      historyShows.value = res.data.historyShows;
      serverTime.value = res.data.serverTime;
    }
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
  form.value = { movieId: "", roomId: "", startTime: "", price: 50 };
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
      <div class="toolbar-actions">
        <el-button type="primary" @click="dialogVisible = true">新增排片</el-button>
        <el-button @click="fetchData">刷新</el-button>
      </div>
      <div class="server-time">服务器时间: {{ formattedServerTime }}</div>
    </div>

    <el-tabs v-model="activeTab" type="card" class="tab-card">
      <el-tab-pane label="当前排片" name="upcoming">
        <el-table
          v-loading="loading"
          :data="upcomingShows"
          border
          style="width: 100%; margin-top: 20px"
        >
          <el-table-column prop="id" label="场次ID" width="180" />
          <el-table-column prop="movieTitle" label="电影名称" />
          <el-table-column prop="roomName" label="放映厅" width="120" />
          <el-table-column prop="startTime" label="放映时间" min-width="180">
            <template #default="{ row }">
              {{ formatTime(row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="basePrice" label="票价" width="100">
            <template #default="{ row }">￥{{ row.basePrice }}</template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template #default="{ row }">
              <el-tag type="success">即将上映</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="历史场次" name="history">
        <el-table
          v-loading="loading"
          :data="historyShows"
          border
          style="width: 100%; margin-top: 20px"
        >
          <el-table-column prop="id" label="场次ID" width="180" />
          <el-table-column prop="movieTitle" label="电影" />
          <el-table-column prop="roomName" label="放映厅" width="120" />
          <el-table-column prop="startTime" label="放映时间" min-width="180">
            <template #default="{ row }">
              {{ formatTime(row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="basePrice" label="票价" width="100">
            <template #default="{ row }">￥{{ row.basePrice }}</template>
          </el-table-column>
          <el-table-column label="状态" width="140">
            <template #default="{ row }">
              <el-tag type="info">已结束</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  gap: 16px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.server-time {
  font-size: 14px;
  color: #606266;
}

.tab-card {
  border-radius: 12px;
  padding: 10px;
  background: #fff;
  box-shadow: 0 20px 50px rgba(15, 20, 42, 0.08);
}
</style>
