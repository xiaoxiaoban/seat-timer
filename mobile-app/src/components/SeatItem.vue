<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import type { Seat } from '@/types'

const props = defineProps<{
  seat: Seat
  isEditMode: boolean
  scale?: number
}>()

const emit = defineEmits<{
  click: [seat: Seat]
  dragstart: [seat: Seat, event: MouseEvent | TouchEvent]
  delete: [seat: Seat]
}>()

// 本地倒计时状态
const now = ref(Date.now())
let timer: number | null = null

// 每秒更新时间
onMounted(() => {
  timer = window.setInterval(() => {
    now.value = Date.now()
  }, 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

// 座位状态颜色
const statusColor = computed(() => {
  switch (props.seat.status) {
    case 'idle':
      return '#52c41a'
    case 'occupied':
      return '#1890ff'
    case 'warning':
      return '#faad14'
    case 'expired':
      return '#f5222d'
    default:
      return '#52c41a'
  }
})

// 计算剩余秒数
const remainingSeconds = computed(() => {
  if (!props.seat.currentSession?.endTime) return 0
  const end = new Date(props.seat.currentSession.endTime).getTime()
  return Math.max(0, Math.floor((end - now.value) / 1000))
})

// 格式化时间 hh:mm:ss
const remainingTime = computed(() => {
  if (props.seat.status === 'idle') return ''
  const secs = remainingSeconds.value
  const hours = Math.floor(secs / 3600)
  const mins = Math.floor((secs % 3600) / 60)
  const s = secs % 60
  return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

// 进度百分比
const progressPercent = computed(() => {
  if (!props.seat.currentSession) return 100
  const start = new Date(props.seat.currentSession.startTime).getTime()
  const end = new Date(props.seat.currentSession.endTime).getTime()
  const total = end - start
  const remain = end - now.value
  return Math.max(0, Math.min(100, (remain / total) * 100))
})

// 根据剩余时间计算状态
const actualStatus = computed(() => {
  if (props.seat.status === 'idle') return 'idle'
  const secs = remainingSeconds.value
  if (secs === 0) return 'expired'
  if (secs < 15 * 60) return 'warning'
  return 'occupied'
})

// 是否显示脉冲动画
const showPulse = computed(() => {
  return actualStatus.value === 'warning' || actualStatus.value === 'expired'
})

// 处理点击 - 编辑模式下空闲座位可删除
const handleClick = () => {
  if (props.isEditMode && props.seat.status === 'idle') {
    emit('delete', props.seat)
  } else {
    emit('click', props.seat)
  }
}

// 处理拖拽开始
const handleMouseDown = (event: MouseEvent) => {
  if (props.isEditMode) {
    emit('dragstart', props.seat, event)
  }
}

// 处理触摸开始
const handleTouchStart = (event: TouchEvent) => {
  if (props.isEditMode) {
    emit('dragstart', props.seat, event)
  }
}
</script>

<template>
  <div
    class="seat-item"
    :class="{
      'is-edit-mode': isEditMode,
      'is-idle': actualStatus === 'idle',
      'is-occupied': actualStatus === 'occupied',
      'is-warning': actualStatus === 'warning',
      'is-expired': actualStatus === 'expired',
      'has-pulse': showPulse,
    }"
    :style="{
      left: `${seat.x}px`,
      top: `${seat.y}px`,
      '--seat-color': statusColor,
    }"
    @click="handleClick"
    @mousedown="handleMouseDown"
    @touchstart="handleTouchStart"
  >
    <div class="seat-content">
      <div class="seat-number">{{ seat.name }}</div>
      <div v-if="actualStatus !== 'idle'" class="seat-timer-wrapper">
        <div class="seat-timer" :class="{ 'timer-urgent': showPulse }">
          {{ remainingTime }}
        </div>
        <div class="seat-badges">
          <span v-if="!seat.currentSession?.isPaid" class="badge-tag unpaid">未付款</span>
          <span v-if="!seat.currentSession?.isVerified" class="badge-tag unverified">未核销</span>
        </div>
      </div>
      <div v-else-if="isEditMode" class="seat-delete-hint">点击删除</div>
    </div>
    <div v-if="actualStatus !== 'idle'" class="seat-progress" :style="{ width: `${progressPercent}%` }"></div>

    <!-- 状态指示器 -->
    <div v-if="actualStatus !== 'idle'" class="status-indicator" :class="actualStatus"></div>
  </div>
</template>

<style scoped>
.seat-item {
  position: absolute;
  width: 60px;
  height: 60px;
  border-radius: 10px;
  background: white;
  border: 2px solid var(--seat-color, #52c41a);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s, border-color 0.3s;
  overflow: hidden;
  user-select: none;
  touch-action: none;
  z-index: 10;
}

.seat-item:hover {
  transform: scale(1.05);
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.15);
}

.seat-item:active {
  transform: scale(0.95);
}

.seat-item.is-edit-mode {
  cursor: move;
}

.seat-item.is-edit-mode.is-idle {
  cursor: pointer;
  border-style: dashed;
}

/* 脉冲动画 - 用于即将到期和已超时 */
@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(250, 173, 20, 0.4);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(250, 173, 20, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(250, 173, 20, 0);
  }
}

@keyframes pulse-red {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 34, 45, 0.4);
  }
  70% {
    box-shadow: 0 0 0 8px rgba(245, 34, 45, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 34, 45, 0);
  }
}

.seat-item.is-warning {
  animation: pulse 1.5s infinite;
}

.seat-item.is-expired {
  animation: pulse-red 1s infinite;
}

.seat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: flex-start;
  height: 100%;
  padding: 3px 2px;
  position: relative;
  z-index: 2;
}

.seat-number {
  font-size: 13px;
  font-weight: 600;
  color: #333;
  line-height: 1.2;
  margin-bottom: 0px;
}

/* 时间包装器 - 包含时间和标签 */
.seat-timer-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  flex: 1;
}

/* 倒计时样式 */
.seat-timer {
  font-size: 12px;
  font-weight: 600;
  color: var(--seat-color, #333);
  font-family: 'SF Mono', monospace;
  letter-spacing: -0.5px;
  line-height: 1.2;
}

.seat-timer.timer-urgent {
  animation: timer-blink 1s infinite;
}

@keyframes timer-blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 标签容器 - 放在时间下方 */
.seat-badges {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 2px;
  margin-top: 4px;
}

/* 标签样式 */
.badge-tag {
  padding: 1px 2px;
  font-size: 7px;
  font-weight: 600;
  border-radius: 2px;
  line-height: 1;
  color: #fff;
  white-space: nowrap;
}

.badge-tag.unpaid {
  background: #fa8c16;
}

.badge-tag.unverified {
  background: #ff4d4f;
}

.seat-delete-hint {
  font-size: 8px;
  color: #999;
  margin-top: auto;
}

.seat-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  height: 4px;
  background: var(--seat-color, #52c41a);
  transition: width 0.5s linear;
}

/* 状态指示器 */
.status-indicator {
  position: absolute;
  top: 3px;
  right: 3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
}

.status-indicator.occupied {
  background: #1890ff;
}

.status-indicator.warning {
  background: #faad14;
  animation: blink 1s infinite;
}

.status-indicator.expired {
  background: #f5222d;
  animation: blink 0.5s infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.3;
  }
}

/* 状态样式 */
.is-idle {
  background: #f6ffed;
}

.is-occupied {
  background: #e6f7ff;
}

.is-warning {
  background: #fffbe6;
}

.is-expired {
  background: #fff2f0;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .seat-item {
    background: #2a2a2a;
    border-color: var(--seat-color, #52c41a);
  }

  .seat-number {
    color: #fff;
  }

  .seat-delete-hint {
    color: #666;
  }

  .is-idle {
    background: #1a2e1a;
  }

  .is-occupied {
    background: #1a2e3a;
  }

  .is-warning {
    background: #2e2a1a;
  }

  .is-expired {
    background: #2e1a1a;
  }
}
</style>
