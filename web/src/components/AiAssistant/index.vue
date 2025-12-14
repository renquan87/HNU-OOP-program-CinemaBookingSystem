<script setup lang="ts">
import { ref, nextTick } from "vue";
import { Service, Close, Promotion } from "@element-plus/icons-vue";
import { askAi } from "@/api/cinema/index";

const visible = ref(false);
const inputMsg = ref("");
const loading = ref(false);
const scrollRef = ref(null);

// 消息列表
const messages = ref([
  { role: "ai", content: "您好！我是影院智能助手。您可以问我：“最近有什么好看的电影？” 或者 “推荐一部科幻片”。" }
]);

// 发送消息
const handleSend = async () => {
  const text = inputMsg.value.trim();
  if (!text) return;

  // 1. 添加用户消息
  messages.value.push({ role: "user", content: text });
  inputMsg.value = "";
  scrollToBottom();

  loading.value = true;
  try {
    // 2. 调用后端 API
    const res = await askAi(text);
    if (res.success) {
      messages.value.push({ role: "ai", content: res.data });
    } else {
      messages.value.push({ role: "ai", content: "抱歉，我好像断线了..." });
    }
  } catch (e) {
    messages.value.push({ role: "ai", content: "网络连接失败。" });
  } finally {
    loading.value = false;
    scrollToBottom();
  }
};

const scrollToBottom = () => {
  nextTick(() => {
    if (scrollRef.value) {
      scrollRef.value.scrollTop = scrollRef.value.scrollHeight;
    }
  });
};
</script>

<template>
  <div class="ai-assistant">
    <div class="fab" @click="visible = !visible" :class="{ active: visible }">
      <el-icon size="24"><Service /></el-icon>
      <span v-if="!visible" class="fab-text">AI助手</span>
      <el-icon v-else size="24"><Close /></el-icon>
    </div>

    <transition name="el-zoom-in-bottom">
      <div v-show="visible" class="chat-window">
        <div class="chat-header">
          <span>🤖 智能推荐助手</span>
          <el-button link size="small" @click="messages = []">清空</el-button>
        </div>

        <div class="chat-body" ref="scrollRef">
          <div v-for="(msg, index) in messages" :key="index" class="message-row" :class="msg.role">
            <div class="avatar" v-if="msg.role === 'ai'">AI</div>
            <div class="bubble">
              {{ msg.content }}
            </div>
            <div class="avatar user-avatar" v-if="msg.role === 'user'">我</div>
          </div>
          <div v-if="loading" class="message-row ai">
            <div class="avatar">AI</div>
            <div class="bubble loading">正在思考...</div>
          </div>
        </div>

        <div class="chat-footer">
          <el-input
            v-model="inputMsg"
            placeholder="请输入您的问题..."
            @keyup.enter="handleSend"
            :disabled="loading"
          >
            <template #append>
              <el-button :icon="Promotion" @click="handleSend" />
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped lang="scss">
.ai-assistant {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 2000;
}

.fab {
  width: 56px;
  height: 56px;
  border-radius: 28px;
  background: linear-gradient(135deg, #409eff, #337ecc);
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0,0,0,0.3);
  cursor: pointer;
  transition: all 0.3s;

  &:hover { transform: scale(1.1); }
  &.active { background: #f56c6c; transform: rotate(90deg); }

  .fab-text { font-size: 10px; margin-top: 2px; }
}

.chat-window {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 350px;
  height: 500px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid #eee;
}

.chat-header {
  padding: 15px;
  background: #f5f7fa;
  border-bottom: 1px solid #eee;
  font-weight: bold;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-body {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background: #fff;
}

.message-row {
  display: flex;
  margin-bottom: 15px;
  align-items: flex-start;

  &.user { justify-content: flex-end; }

  .avatar {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #409eff;
    color: white;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    flex-shrink: 0;
  }

  .user-avatar { background: #67c23a; }

  .bubble {
    max-width: 70%;
    padding: 10px;
    border-radius: 8px;
    font-size: 14px;
    line-height: 1.5;
    margin: 0 10px;
    word-break: break-all;
  }

  &.ai .bubble { background: #f4f4f5; color: #333; border-top-left-radius: 0; }
  &.user .bubble { background: #ecf5ff; color: #409eff; border-top-right-radius: 0; }

  .loading { font-style: italic; color: #999; }
}

.chat-footer {
  padding: 10px;
  border-top: 1px solid #eee;
}
</style>
