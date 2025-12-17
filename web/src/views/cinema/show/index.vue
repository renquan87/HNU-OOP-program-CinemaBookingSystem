<script setup lang="ts">
import { ref, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getShowList } from "@/api/cinema/show";
import type { ShowItem } from "@/api/cinema/model/showModel";
import { ArrowLeft, Ticket } from "@element-plus/icons-vue";

<template>
  <div class="main-content">
    <el-card shadow="never" class="schedule-card">
      <template #header>
        <div class="card-header">
          <div class="title-group">
            <el-button :icon="ArrowLeft" circle @click="router.back()" />
            <div class="title-block">
              <p class="title">{{ displayTitle }}</p>
              <p class="subtitle">服务器时间：{{ serverTimeLabel }}</p>
            </div>
          </div>
          <el-button type="primary" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <section class="section">
        <header class="section-header">
          <div>
            <p class="section-title">当前排片</p>
            <small>尚未开场的场次可继续售票</small>
          </div>
          <el-tag type="success">{{ upcomingShows.length }} 场</el-tag>
        </header>

        <el-table
          v-loading="loading"
          :data="upcomingShows"
          border
          stripe
          class="table"
        >
          <el-table-column prop="roomName" label="放映厅" width="150" />
          <el-table-column prop="startTime" label="放映时间" min-width="180">
            <template #default="scope">
              <el-tag size="large">{{ formatTime(scope.row.startTime) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="basePrice" label="票价" width="120">
            <template #default="scope">
              <span class="price-tag">￥{{ scope.row.basePrice }}</span>
            </template>
          </el-table-column>
          <el-table-column label="座位态势" width="200">
            <template #default="scope">
              <el-progress
                :percentage="Math.round((scope.row.availableSeats / scope.row.totalSeats) * 100)"
                :status="scope.row.availableSeats === 0 ? 'exception' : 'success'"
              >
                {{ scope.row.availableSeats }}/{{ scope.row.totalSeats }}
              </el-progress>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="center">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                :icon="Ticket"
                :disabled="!canBook(scope.row)"
                >选座购票</el-button
              >
            </template>
          </el-table-column>
        </el-table>
      </section>

      <section class="section history-section">
        <header class="section-header">
          <div>
            <p class="section-title">历史场次</p>
            <small>服务器时间前的全部记录</small>
          </div>
          <el-tag type="info">{{ historyShows.length }} 场</el-tag>
        </header>

        <el-table
          v-loading="loading"
          :data="historyShows"
          border
          stripe
          class="table"
        >
          <el-table-column prop="roomName" label="放映厅" width="150" />
          <el-table-column prop="startTime" label="放映时间" min-width="180">
            <template #default="scope">
              <el-tag type="info" size="large">{{ formatTime(scope.row.startTime) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="basePrice" label="票价" width="120">
            <template #default="scope">
              <span class="price-tag">￥{{ scope.row.basePrice }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default>
              <el-tag type="warning">已结束</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </section>
    </el-card>
  </div>
</template>
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
  background: #f4f6fb;
}

.schedule-card {
  border-radius: 18px;
  padding-bottom: 24px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.title-group {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-block {
  display: flex;
  flex-direction: column;
}

.title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.subtitle {
  margin: 2px 0 0;
  font-size: 13px;
  color: #8c939f;
}

.section {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 18px;
  box-shadow: 0 10px 30px rgba(15, 20, 42, 0.05);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
  gap: 12px;
}

.section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.table {
  width: 100%;
}

.price-tag {
  font-size: 16px;
  font-weight: 600;
  color: #f56c6c;
}

.history-section {
  margin-top: 6px;
}

@media (max-width: 768px) {
  .section {
    padding: 16px;
  }
}
</style>
