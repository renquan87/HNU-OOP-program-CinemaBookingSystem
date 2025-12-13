<script setup lang="ts">
import { PropType } from "vue";

// 定义 props 接收父组件传来的列表数据
const props = defineProps({
  list: {
    type: Array as PropType<Array<any>>,
    default: () => []
  }
});
</script>

<template>
  <div v-if="list.length === 0" class="empty-list">
    <el-empty description="暂无消息" :image-size="60" />
  </div>
  <div v-else class="notice-list-container">
    <div v-for="(item, index) in list" :key="index" class="notice-item">
      <div class="notice-avatar">
        <img :src="item.avatar" alt="avatar" />
      </div>
      <div class="notice-content">
        <div class="notice-title text-text_color_primary">
          {{ item.title }}
          <el-tag
            v-if="item.extra"
            size="small"
            :type="item.status"
            class="notice-tag"
          >
            {{ item.extra }}
          </el-tag>
        </div>
        <div class="notice-description text-text_color_regular">
          {{ item.description }}
        </div>
        <div class="notice-datetime text-text_color_secondary">
          {{ item.datetime }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.notice-list-container {
  max-height: 300px;
  overflow-y: auto;
}

.notice-item {
  display: flex;
  align-items: flex-start;
  padding: 12px 20px; // 增加左右内边距
  border-bottom: 1px solid var(--el-border-color-light);
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: var(--el-fill-color-light);
  }

  &:last-child {
    border-bottom: none;
  }

  .notice-avatar {
    margin-right: 16px;
    img {
      width: 32px;
      height: 32px;
      border-radius: 50%;
    }
  }

  .notice-content {
    flex: 1;

    .notice-title {
      font-size: 14px;
      font-weight: 500;
      margin-bottom: 4px;
      line-height: 1.5;
      display: flex;
      align-items: center;

      .notice-tag {
        margin-left: auto;
      }
    }

    .notice-description {
      font-size: 12px;
      color: #666; // 兜底颜色
      margin-bottom: 4px;
      line-height: 1.5;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      -webkit-line-clamp: 2; // 最多显示2行
      overflow: hidden;
    }

    .notice-datetime {
      font-size: 12px;
      color: #999; // 兜底颜色
    }
  }
}

.empty-list {
  padding: 20px 0;
  display: flex;
  justify-content: center;
}
</style>
