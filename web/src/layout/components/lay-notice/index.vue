<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { noticesData } from "./data"; // 如果这个引用报错，可以删掉，反正我们要用真数据
import NoticeList from "./noticeList.vue";
import { Bell } from "@element-plus/icons-vue";
import { getNoticeList } from "@/api/cinema/index";
import { useUserStoreHook } from "@/store/modules/user";

const notices = ref([]);
const activeKey = ref("通知");
const userStore = useUserStoreHook();

// 未读数量
const noticesNum = computed(() => {
  return notices.value.length;
});

// 加载真实通知数据
const fetchNotices = async () => {
  const userId = userStore.userId;
  if (!userId) return;

  try {
    const res = await getNoticeList(userId);
    if (res.success) {
      // 转换后端数据为组件需要的格式
      // 我们把所有数据都放在 "通知" 这个 Tab 下
      notices.value = res.data.map((item: any) => ({
        title: item.title,
        description: item.content, // 后端的 content 对应 description
        datetime: item.datetime,
        type: "通知", // 强制归类到第一个 Tab
        avatar:
          "https://gw.alipayobjects.com/zos/rmsportal/ThXAXghbEsBCCSDihZxY.png" // 默认图标
      }));
    }
  } catch (error) {
    console.error("获取通知失败", error);
  }
};

onMounted(() => {
  fetchNotices();
});

// 为了演示效果，每次点击铃铛时刷新一次数据
const handleVisibleChange = (val: boolean) => {
  if (val) {
    fetchNotices();
  }
};
</script>

<template>
  <el-dropdown
    trigger="click"
    placement="bottom-end"
    @visible-change="handleVisibleChange"
  >
    <span class="dropdown-badge navbar-bg-hover select-none">
      <el-badge :value="noticesNum" :max="99" :hidden="noticesNum === 0">
        <span class="header-notice-icon">
          <el-icon><Bell /></el-icon>
        </span>
      </el-badge>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-tabs
          v-model="activeKey"
          :stretch="true"
          class="dropdown-tabs"
          :style="{ width: notices.length === 0 ? '200px' : '330px' }"
        >
          <el-tab-pane label="消息通知" name="通知">
            <template v-if="notices.length > 0">
              <NoticeList :list="notices" />
            </template>
            <template v-else>
              <el-empty description="暂无消息" />
            </template>
          </el-tab-pane>
        </el-tabs>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="scss" scoped>
.dropdown-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  width: 40px;
  cursor: pointer;

  .header-notice-icon {
    font-size: 18px;
  }
}

.dropdown-tabs {
  .noticeList-container {
    padding: 15px 24px 0;
  }

  :deep(.el-tabs__header) {
    margin: 0;
  }

  :deep(.el-tabs__nav-wrap)::after {
    height: 1px;
  }

  :deep(.el-tabs__nav-wrap) {
    padding: 0 36px;
  }
}
</style>
