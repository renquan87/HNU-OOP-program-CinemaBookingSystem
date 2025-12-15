<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute } from "vue-router";
import { getMovieDetail, addComment } from "@/api/cinema/movie";
import { useUserStoreHook } from "@/store/modules/user";
import { message } from "@/utils/message";
import ReBookingDialog from "@/components/ReBookingDialog/index.vue"; // 确保引入了组件

const route = useRoute();
const userStore = useUserStoreHook();
const movieId = route.params.id as string;

const movie = ref<any>(null);
const loading = ref(true);
const bookingVisible = ref(false); // 控制购票弹窗

// 评论相关
const commentContent = ref("");
const userRating = ref(0); // 这里存的是 0-5 的星级
const submitting = ref(false);

const fetchData = async () => {
  try {
    loading.value = true;
    const res = await getMovieDetail(movieId);
    if (res.success) {
      movie.value = res.data;
    }
  } finally {
    loading.value = false;
  }
};

// 打开购票弹窗
const handleBooking = () => {
  bookingVisible.value = true;
};

// 提交评论
const handlePostComment = async () => {
  if (userRating.value === 0) return message("请点击星星进行评分", { type: "warning" });
  if (!commentContent.value.trim()) return message("请填写评论内容", { type: "warning" });

  submitting.value = true;
  try {
    // 🔴 核心逻辑：前端 5 星 -> 后端 10 分
    // 用户选 4.5 星，传给后端 9.0 分
    const scoreToSend = userRating.value * 2;

    const res = await addComment(movieId, {
      userId: userStore.userId,
      content: commentContent.value,
      rating: scoreToSend
    });

    if (res.success) {
      message("评论发布成功", { type: "success" });
      commentContent.value = "";
      userRating.value = 0;
      fetchData(); // 刷新数据，重新计算平均分
    } else {
      message(res.message, { type: "error" });
    }
  } finally {
    submitting.value = false;
  }
};

// 显示用的平均分 (0-10)
const displayRatingScore = computed(() => movie.value?.rating || 0);

// 显示用的星级 (0-5)
// 后端 9.0 分 -> 前端 4.5 星
const displayRatingStars = computed(() => (movie.value?.rating || 0) / 2);

onMounted(() => {
  fetchData();
});
</script>

<template>
  <div class="detail-container" v-loading="loading">
    <div v-if="movie" class="content-wrapper">

      <div class="top-section">
        <div class="video-area">
          <video
              v-if="movie.trailerUrl"
              :src="movie.trailerUrl"
              controls
              :poster="movie.coverUrl"
              style="width: 100%; height: 100%; object-fit: contain; background: #000; border-radius: 8px;"
          >
            您的浏览器不支持视频标签。
          </video>
          <el-image
              v-else
              :src="movie.coverUrl"
              fit="cover"
              style="width: 100%; height: 100%; border-radius: 8px;"
          >
            <template #error>
              <div class="no-video">暂无预告片</div>
            </template>
          </el-image>
        </div>

        <div class="info-area">
          <h1 class="title">{{ movie.title }}</h1>

          <div class="tags">
            <el-tag effect="dark">{{ movie.genre }}</el-tag>
            <el-tag type="info" effect="plain">{{ movie.duration }}分钟</el-tag>
            <el-tag type="success" effect="plain">{{ movie.releaseTime }} 上映</el-tag>
          </div>

          <div class="rating-box">
            <span class="score">{{ displayRatingScore }}</span>
            <span class="label">分</span>

            <el-rate
                :model-value="displayRatingStars"
                disabled
                allow-half
                text-color="#ff9900"
            />
          </div>

          <div class="meta-info">
            <p><strong>导演：</strong>{{ movie.director }}</p>
            <p><strong>主演：</strong>{{ Array.isArray(movie.actors) ? movie.actors.join(" / ") : movie.actors }}</p>
            <p class="desc"><strong>简介：</strong>{{ movie.description }}</p>
          </div>

          <div class="action-btn">
            <el-button
                type="primary"
                size="large"
                color="#f56c6c"
                :dark="false"
                @click="handleBooking"
                style="width: 200px; height: 50px; font-size: 18px; font-weight: bold; box-shadow: 0 4px 12px rgba(245, 108, 108, 0.4);"
            >
              选座购票
            </el-button>
          </div>
        </div>
      </div>

      <el-divider />

      <div class="comment-section">
        <h3>🎥 用户评论 ({{ movie.comments ? movie.comments.length : 0 }})</h3>

        <div class="post-box">
          <div class="rate-row">
            <span class="rate-label">点击打分：</span>
            <el-rate
                v-model="userRating"
                allow-half
                show-text
                :texts="['极差', '失望', '一般', '满意', '神作!']"
            />
          </div>

          <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              placeholder="这个电影好看吗？快来分享你的看法吧..."
              maxlength="200"
              show-word-limit
              class="comment-input"
          />

          <div class="post-actions">
            <el-button type="primary" @click="handlePostComment" :loading="submitting">发布评论</el-button>
          </div>
        </div>

        <div class="comment-list">
          <div v-for="comment in movie.comments" :key="comment.id" class="comment-item">
            <div class="avatar">{{ comment.userName.charAt(0).toUpperCase() }}</div>
            <div class="content">
              <div class="header">
                <span class="name">{{ comment.userName }}</span>
                <el-rate
                    :model-value="comment.rating / 2"
                    disabled
                    allow-half
                    size="small"
                />
                <span class="time">{{ comment.createTime.replace('T', ' ').substring(0, 16) }}</span>
              </div>
              <p class="text">{{ comment.content }}</p>
            </div>
          </div>

          <el-empty v-if="!movie.comments || movie.comments.length === 0" description="暂无评论，快来抢沙发！" />
        </div>
      </div>
    </div>

    <ReBookingDialog
        v-if="movie"
        v-model:visible="bookingVisible"
        :movie="movie"
        @success="fetchData"
    />
  </div>
</template>

<style scoped>
.detail-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
  min-height: 100vh;
}

.content-wrapper {
  animation: fadeIn 0.5s ease;
}

/* 顶部布局 */
.top-section {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  align-items: flex-start;
}

.video-area {
  flex: 1.5;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  height: 400px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.no-video {
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 14px;
}

.info-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
  height: 400px; /* 与视频高度一致 */
}

.title {
  font-size: 28px;
  margin: 0;
  color: #333;
  line-height: 1.2;
}

.tags {
  display: flex;
  gap: 10px;
}

.rating-box {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 10px 0;
}

.score {
  font-size: 36px;
  color: #ff9900;
  font-weight: bold;
  line-height: 1;
}

.label {
  font-size: 14px;
  color: #999;
  margin-right: 10px;
  align-self: flex-end;
  margin-bottom: 5px;
}

.meta-info {
  flex: 1; /* 撑开中间空间 */
}

.meta-info p {
  margin: 8px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.desc {
  color: #888;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.action-btn {
  margin-top: auto; /* 按钮沉底 */
}

/* 评论区 */
.comment-section {
  padding-top: 10px;
}

.post-box {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 30px;
  border: 1px solid #eee;
}

.rate-row {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.rate-label {
  font-size: 14px;
  color: #606266;
  margin-right: 10px;
}

.comment-input {
  margin-bottom: 15px;
}

.post-actions {
  text-align: right;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.comment-item {
  display: flex;
  gap: 15px;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.avatar {
  width: 40px;
  height: 40px;
  background: #409eff;
  color: #fff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  flex-shrink: 0;
}

.content {
  flex: 1;
}

.header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.name {
  font-weight: bold;
  font-size: 14px;
  color: #333;
}

.time {
  color: #999;
  font-size: 12px;
  margin-left: auto;
}

.text {
  color: #555;
  font-size: 14px;
  line-height: 1.6;
  margin: 0;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
