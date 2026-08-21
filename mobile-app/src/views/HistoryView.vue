<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useSeatStore } from '@/stores/seat'
import type { SessionHistory } from '@/types'

const seatStore = useSeatStore()
const loading = ref(false)
const historyList = ref<SessionHistory[]>([])
const filterStatus = ref<'all' | 'completed' | 'cancelled'>('all')
const filterDate = ref(getTodayDate()) // 默认为今天

// 获取今天日期字符串 (YYYY-MM-DD)
function getTodayDate(): string {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 加载历史记录
const loadHistory = async () => {
  loading.value = true
  try {
    // 从所有座位获取历史记录
    const allHistory: SessionHistory[] = []
    for (const seat of seatStore.seats) {
      try {
        const history = await seatStore.loadHistory(seat.id)
        allHistory.push(...history)
      } catch (e) {
        // 忽略错误
      }
    }
    // 按时间倒序排列
    historyList.value = allHistory.sort(
      (a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
    )
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  seatStore.loadSeats().then(() => loadHistory())
})

// 过滤历史记录
const filteredHistory = computed(() => {
  let result = historyList.value

  // 按状态筛选
  if (filterStatus.value !== 'all') {
    result = result.filter((item) => item.status === filterStatus.value)
  }

  // 按日期筛选
  if (filterDate.value) {
    result = result.filter((item) => {
      const itemDate = new Date(item.startTime).toISOString().split('T')[0]
      return itemDate === filterDate.value
    })
  }

  return result
})

// 按日期分组
const groupedHistory = computed(() => {
  const groups: Record<string, SessionHistory[]> = {}
  filteredHistory.value.forEach((item) => {
    const date = new Date(item.startTime).toLocaleDateString('zh-CN')
    if (!groups[date]) groups[date] = []
    groups[date].push(item)
  })
  return groups
})

// 格式化时间
// const formatTime = (dateStr: string) => {
//   return new Date(dateStr).toLocaleTimeString('zh-CN', {
//     hour: '2-digit',
//     minute: '2-digit',
//   })
// }

const getStatusText = (status: string) => {
  switch (status) {
    case 'completed':
      return '已完成'
    case 'cancelled':
      return '已取消'
    case 'active':
      return '进行中'
    default:
      return status
  }
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'completed':
      return 'status-completed'
    case 'cancelled':
      return 'status-cancelled'
    case 'active':
      return 'status-active'
    default:
      return ''
  }
}

// 选中的历史记录详情
const selectedHistory = ref<SessionHistory | null>(null)
const showDetailModal = ref(false)

// 打开详情弹窗
const openDetail = (item: SessionHistory) => {
  selectedHistory.value = item
  showDetailModal.value = true
}

// 关闭详情弹窗
const closeDetail = () => {
  selectedHistory.value = null
  showDetailModal.value = false
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

// 计算使用时长
const getDuration = (item: SessionHistory) => {
  const start = new Date(item.startTime)
  const end = item.actualEndTime
    ? new Date(item.actualEndTime)
    : item.endTime
      ? new Date(item.endTime)
      : new Date()

  const diffMs = end.getTime() - start.getTime()
  const hours = Math.floor(diffMs / (1000 * 60 * 60))
  const mins = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60))

  if (hours > 0) {
    return `${hours}小时${mins}分钟`
  }
  return `${mins}分钟`
}
</script>

<template>
  <div class="history-view">
    <!-- 页面头部 -->
    <header class="page-header safe-area-top">
      <h1 class="page-title">历史记录</h1>
      <!-- 日期选择器 -->
      <div class="date-filter">
        <input
          v-model="filterDate"
          type="date"
          class="date-input"
          :max="getTodayDate()"
        />
        <button class="btn-today" @click="filterDate = getTodayDate()">
          今天
        </button>
      </div>
      <!-- 状态筛选 -->
      <div class="filter-tabs">
        <button
          class="filter-tab"
          :class="{ active: filterStatus === 'all' }"
          @click="filterStatus = 'all'"
        >
          全部
        </button>
        <button
          class="filter-tab"
          :class="{ active: filterStatus === 'completed' }"
          @click="filterStatus = 'completed'"
        >
          已完成
        </button>
        <button
          class="filter-tab"
          :class="{ active: filterStatus === 'cancelled' }"
          @click="filterStatus = 'cancelled'"
        >
          已取消
        </button>
      </div>
    </header>

    <!-- 内容区域 -->
    <main class="content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-state">
        <div class="spinner"></div>
        <p>加载中...</p>
      </div>

      <!-- 空状态 -->
      <div v-else-if="filteredHistory.length === 0" class="empty-state">
        <div class="empty-icon">📋</div>
        <div class="empty-text">暂无历史记录</div>
      </div>

      <!-- 历史列表 -->
      <div v-else class="history-list">
        <div
          v-for="(items, date) in groupedHistory"
          :key="date"
          class="date-group"
        >
          <div class="date-header">{{ date }}</div>
          <div class="history-items">
            <div
              v-for="item in items"
              :key="item.id"
              class="history-item"
              @click="openDetail(item)"
            >
              <div class="item-left">
                <span class="status-badge" :class="getStatusClass(item.status)">
                  {{ getStatusText(item.status) }}
                </span>
                <div class="item-info">
                  <div class="item-title">
                    座位 {{ item.seatName }} - {{ item.customerName || '未命名' }}
                  </div>
                  <div class="item-time">
                    <span class="time-label">开始:</span>{{ formatDateTime(item.startTime) }}
                    <span class="time-divider">|</span>
                    <span class="time-label">结束:</span>{{ item.actualEndTime ? formatDateTime(item.actualEndTime) : (item.endTime ? formatDateTime(item.endTime) : '-') }}
                  </div>
                </div>
              </div>
              <div class="item-phone">{{ item.phone || '-' }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 刷新按钮 -->
      <button class="btn-refresh" @click="loadHistory" :disabled="loading">
        刷新记录
      </button>
    </main>

    <!-- 详情弹窗 -->
    <div v-if="showDetailModal && selectedHistory" class="detail-modal" @click.self="closeDetail">
      <div class="detail-content">
        <div class="detail-header">
          <h3>记录详情</h3>
          <button class="btn-close" @click="closeDetail">&times;</button>
        </div>
        <div class="detail-body">
          <div class="detail-row">
            <span class="detail-label">座位</span>
            <span class="detail-value">{{ selectedHistory.seatName }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">客户姓名</span>
            <span class="detail-value">{{ selectedHistory.customerName || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">手机号</span>
            <span class="detail-value">{{ selectedHistory.phone || '-' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">状态</span>
            <span class="detail-value status" :class="getStatusClass(selectedHistory.status)">
              {{ getStatusText(selectedHistory.status) }}
            </span>
          </div>
          <div class="detail-row">
            <span class="detail-label">开始时间</span>
            <span class="detail-value">{{ formatDateTime(selectedHistory.startTime) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">结束时间</span>
            <span class="detail-value">{{ selectedHistory.actualEndTime ? formatDateTime(selectedHistory.actualEndTime) : (selectedHistory.endTime ? formatDateTime(selectedHistory.endTime) : '-') }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">使用时长</span>
            <span class="detail-value">{{ getDuration(selectedHistory) }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">付款状态</span>
            <span class="detail-value">{{ selectedHistory.isPaid ? '已付款' : '未付款' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">核销状态</span>
            <span class="detail-value">{{ selectedHistory.isVerified ? '已核销' : '未核销' }}</span>
          </div>
          <div v-if="selectedHistory.couponInfo" class="detail-row">
            <span class="detail-label">团购券</span>
            <span class="detail-value">{{ selectedHistory.couponInfo }}</span>
          </div>
          <div v-if="selectedHistory.note" class="detail-row">
            <span class="detail-label">备注</span>
            <span class="detail-value note">{{ selectedHistory.note }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.history-view {
  min-height: 100vh;
  background: #f5f5f5;
}

/* 页面头部 */
.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  padding-top: max(16px, env(safe-area-inset-top));
  color: white;
  position: sticky;
  top: 0;
  z-index: 10;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
}

/* 日期筛选 */
.date-filter {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.date-input {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.9);
  font-size: 14px;
  color: #333;
  cursor: pointer;
  outline: none;
}

.date-input::-webkit-calendar-picker-indicator {
  cursor: pointer;
  opacity: 0.6;
}

.date-input::-webkit-calendar-picker-indicator:hover {
  opacity: 1;
}

.btn-today {
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.3);
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 8px;
  font-size: 13px;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-today:hover {
  background: rgba(255, 255, 255, 0.5);
}

.btn-today:active {
  transform: scale(0.95);
}

.filter-tabs {
  display: flex;
  gap: 8px;
}

.filter-tab {
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 20px;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.2s;
}

.filter-tab.active {
  background: white;
  color: #667eea;
}

/* 内容区域 */
.content {
  padding: 16px;
  padding-bottom: calc(80px + env(safe-area-inset-bottom));
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  color: #999;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top-color: #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 16px;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-state {
  text-align: center;
  padding: 60px;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 16px;
  color: #999;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.date-group {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
}

.date-header {
  padding: 12px 16px;
  background: #f5f5f5;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.history-items {
  padding: 8px;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.history-item:last-child {
  border-bottom: none;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-completed {
  background: #f6ffed;
  color: #52c41a;
}

.status-cancelled {
  background: #fff2f0;
  color: #f5222d;
}

.status-active {
  background: #e6f7ff;
  color: #1890ff;
}

.item-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.item-time {
  font-size: 12px;
  color: #999;
}

.item-phone {
  font-size: 13px;
  color: #666;
}

.btn-refresh {
  width: 100%;
  margin-top: 16px;
  padding: 12px;
  background: #667eea;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-refresh:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.btn-refresh:active {
  transform: scale(0.98);
}

/* 历史记录项 - 可点击 */
.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.history-item:hover {
  background: #f5f5f5;
}

.history-item:last-child {
  border-bottom: none;
}

/* 时间显示样式 */
.item-time {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
}

.time-label {
  color: #999;
}

.time-divider {
  color: #ccc;
  margin: 0 4px;
}

/* 详情弹窗 */
.detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.detail-content {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 400px;
  max-height: 80vh;
  overflow: hidden;
  animation: modal-in 0.2s ease-out;
}

@keyframes modal-in {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.btn-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  border-radius: 8px;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #f5f5f5;
  color: #333;
}

.detail-body {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(80vh - 60px);
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  font-size: 14px;
  color: #666;
  flex-shrink: 0;
}

.detail-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  text-align: right;
  word-break: break-all;
}

.detail-value.status {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.detail-value.status-completed {
  background: #f6ffed;
  color: #52c41a;
}

.detail-value.status-cancelled {
  background: #fff2f0;
  color: #f5222d;
}

.detail-value.status-active {
  background: #e6f7ff;
  color: #1890ff;
}

.detail-value.note {
  text-align: left;
  background: #f5f5f5;
  padding: 8px 12px;
  border-radius: 8px;
  margin-top: 4px;
  line-height: 1.5;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .history-view {
    background: #1a1a1a;
  }

  .date-input {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }

  .btn-today {
    background: rgba(255, 255, 255, 0.1);
    border-color: rgba(255, 255, 255, 0.3);
  }

  .btn-today:hover {
    background: rgba(255, 255, 255, 0.2);
  }

  .date-group {
    background: #2a2a2a;
  }

  .date-header {
    background: #3a3a3a;
    color: #fff;
  }

  .history-item {
    border-bottom-color: #3a3a3a;
  }

  .item-title {
    color: #fff;
  }

  .item-phone {
    color: #ccc;
  }

  .status-completed {
    background: #1a2e1a;
  }

  .status-cancelled {
    background: #2e1a1a;
  }

  .status-active {
    background: #1a2e3a;
  }

  .history-item:hover {
    background: #3a3a3a;
  }

  .time-label {
    color: #999;
  }

  .time-divider {
    color: #666;
  }

  .detail-content {
    background: #2a2a2a;
  }

  .detail-header {
    border-bottom-color: #3a3a3a;
  }

  .detail-header h3 {
    color: #fff;
  }

  .btn-close:hover {
    background: #3a3a3a;
    color: #fff;
  }

  .detail-row {
    border-bottom-color: #3a3a3a;
  }

  .detail-label {
    color: #999;
  }

  .detail-value {
    color: #fff;
  }

  .detail-value.status-completed {
    background: #1a2e1a;
    color: #52c41a;
  }

  .detail-value.status-cancelled {
    background: #2e1a1a;
    color: #f5222d;
  }

  .detail-value.status-active {
    background: #1a2e3a;
    color: #1890ff;
  }

  .detail-value.note {
    background: #3a3a3a;
    color: #ccc;
  }
}
</style>
