<script setup lang="ts">
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useSeatStore } from '@/stores/seat'
import type { Seat } from '@/types'
import SeatItem from '@/components/SeatItem.vue'
import CheckInModal from '@/components/CheckInModal.vue'
import SeatDetailModal from '@/components/SeatDetailModal.vue'

const seatStore = useSeatStore()
const selectedSeat = ref<Seat | null>(null)
const showCheckInModal = ref(false)
const showDetailModal = ref(false)
const isEditMode = ref(false)
const dragSeat = ref<Seat | null>(null)
const dragOffset = ref({ x: 0, y: 0 })
const canvasRef = ref<HTMLDivElement | null>(null)
const canvasWrapperRef = ref<HTMLDivElement | null>(null)

// 缩放相关
const scale = ref(1)
const MIN_SCALE = 0.5
const MAX_SCALE = 2
const SCALE_STEP = 0.1

// 画布偏移（用于拖拽画布）
const canvasOffset = ref({ x: 0, y: 0 })
const isDraggingCanvas = ref(false)
const canvasDragStart = ref({ x: 0, y: 0 })

// 定时更新座位状态
let timerInterval: number | null = null

onMounted(() => {
  seatStore.loadSeats()
  timerInterval = window.setInterval(() => {
    seatStore.updateAllSeatStatus()
  }, 1000)

  // 添加滚轮缩放
  const wrapper = canvasWrapperRef.value
  if (wrapper) {
    wrapper.addEventListener('wheel', handleWheel, { passive: false })
  }
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
  const wrapper = canvasWrapperRef.value
  if (wrapper) {
    wrapper.removeEventListener('wheel', handleWheel)
  }
})

// 画布尺寸
const CANVAS_WIDTH = 800
const CANVAS_HEIGHT = 1200

// 计算画布边界
const constrainCanvasOffset = () => {
  // 获取画布容器的实际尺寸
  const wrapperRect = canvasWrapperRef.value?.getBoundingClientRect()
  if (!wrapperRect) return

  const visibleWidth = wrapperRect.width
  const visibleHeight = wrapperRect.height

  // 计算缩放后的画布尺寸
  const scaledWidth = CANVAS_WIDTH * scale.value
  const scaledHeight = CANVAS_HEIGHT * scale.value

  // 计算边界限制
  // 当画布比可视区域大时，可以拖动查看不同区域
  // 当画布比可视区域小时，限制在居中或边缘对齐

  // X轴边界：
  // 最大值：0（画布左边缘不超出可视区域左边缘）
  // 最小值：visibleWidth - scaledWidth（画布右边缘不超出可视区域右边缘）
  // 但要确保至少显示一半画布
  const minVisibleRatio = 0.5
  const maxOffsetX = Math.min(0, visibleWidth * minVisibleRatio)
  const minOffsetX = Math.max(visibleWidth - scaledWidth, -scaledWidth * (1 - minVisibleRatio))

  // Y轴边界：
  const maxOffsetY = Math.min(0, visibleHeight * minVisibleRatio)
  const minOffsetY = Math.max(visibleHeight - scaledHeight, -scaledHeight * (1 - minVisibleRatio))

  canvasOffset.value = {
    x: Math.max(minOffsetX, Math.min(maxOffsetX, canvasOffset.value.x)),
    y: Math.max(minOffsetY, Math.min(maxOffsetY, canvasOffset.value.y)),
  }
}

// 缩放控制
const zoomIn = () => {
  scale.value = Math.min(MAX_SCALE, scale.value + SCALE_STEP)
  constrainCanvasOffset()
}

const zoomOut = () => {
  scale.value = Math.max(MIN_SCALE, scale.value - SCALE_STEP)
  constrainCanvasOffset()
}

const resetZoom = () => {
  scale.value = 1
  canvasOffset.value = { x: 0, y: 0 }
}

// 滚轮缩放
const handleWheel = (event: WheelEvent) => {
  if (event.ctrlKey || event.metaKey) {
    event.preventDefault()
    const delta = event.deltaY > 0 ? -SCALE_STEP : SCALE_STEP
    scale.value = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale.value + delta))
    constrainCanvasOffset()
  }
}

// 捏合缩放相关
const touchDistance = ref(0)
const isPinching = ref(false)

const getTouchDistance = (touches: TouchList) => {
  const dx = touches[0].clientX - touches[1].clientX
  const dy = touches[0].clientY - touches[1].clientY
  return Math.sqrt(dx * dx + dy * dy)
}

// 点击座位
const handleSeatClick = (seat: Seat) => {
  if (isDraggingCanvas.value) return
  selectedSeat.value = seat
  if (seat.status === 'idle') {
    showCheckInModal.value = true
  } else {
    showDetailModal.value = true
  }
}

// 关闭弹窗
const closeModals = () => {
  showCheckInModal.value = false
  showDetailModal.value = false
  selectedSeat.value = null
}

// 拖拽开始
const handleDragStart = (seat: Seat, event: MouseEvent | TouchEvent) => {
  if (!isEditMode.value) return

  event.stopPropagation()
  dragSeat.value = seat

  const clientX = event instanceof MouseEvent ? event.clientX : event.touches[0].clientX
  const clientY = event instanceof MouseEvent ? event.clientY : event.touches[0].clientY

  // 考虑缩放后的偏移计算
  const wrapperRect = canvasWrapperRef.value?.getBoundingClientRect()
  if (wrapperRect) {
    const canvasX = (clientX - wrapperRect.left - canvasOffset.value.x) / scale.value
    const canvasY = (clientY - wrapperRect.top - canvasOffset.value.y) / scale.value
    dragOffset.value = {
      x: canvasX - seat.x,
      y: canvasY - seat.y,
    }
  }

  if (event instanceof MouseEvent) {
    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleDragEnd)
  } else {
    document.addEventListener('touchmove', handleTouchMove, { passive: false })
    document.addEventListener('touchend', handleDragEnd)
  }
}

// 鼠标移动
const handleMouseMove = (event: MouseEvent) => {
  handleDragMove(event.clientX, event.clientY)
}

// 触摸移动（单指拖拽）
const handleTouchMove = (event: TouchEvent) => {
  if (event.touches.length === 1 && dragSeat.value) {
    event.preventDefault()
    handleDragMove(event.touches[0].clientX, event.touches[0].clientY)
  } else if (event.touches.length === 2) {
    // 双指捏合缩放
    event.preventDefault()
    const newDistance = getTouchDistance(event.touches)
    if (isPinching.value && touchDistance.value > 0) {
      const scaleChange = (newDistance - touchDistance.value) / 200
      scale.value = Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale.value + scaleChange))
    }
    touchDistance.value = newDistance
    isPinching.value = true
  }
}

// 拖拽移动
const handleDragMove = (clientX: number, clientY: number) => {
  if (!dragSeat.value) return

  const wrapperRect = canvasWrapperRef.value?.getBoundingClientRect()
  if (!wrapperRect) return

  // 计算相对于画布的位置（考虑缩放）
  const canvasX = (clientX - wrapperRect.left - canvasOffset.value.x) / scale.value
  const canvasY = (clientY - wrapperRect.top - canvasOffset.value.y) / scale.value

  // 限制在画布范围内
  dragSeat.value.x = Math.max(0, Math.min(CANVAS_WIDTH - 60, canvasX - dragOffset.value.x))
  dragSeat.value.y = Math.max(0, Math.min(CANVAS_HEIGHT - 60, canvasY - dragOffset.value.y))
}

// 拖拽结束
const handleDragEnd = () => {
  if (dragSeat.value) {
    seatStore.updateSeatPosition(dragSeat.value.id, dragSeat.value.x, dragSeat.value.y)
    dragSeat.value = null
  }
  isPinching.value = false
  touchDistance.value = 0

  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('mouseup', handleDragEnd)
  document.removeEventListener('touchmove', handleTouchMove)
  document.removeEventListener('touchend', handleDragEnd)
}

// 画布拖拽（移动视图）
const handleCanvasMouseDown = (event: MouseEvent) => {
  const target = event.target as HTMLElement
  const isSeat = target.closest('.seat-item')

  if (!isSeat) {
    isDraggingCanvas.value = true
    canvasDragStart.value = {
      x: event.clientX - canvasOffset.value.x,
      y: event.clientY - canvasOffset.value.y,
    }
    document.addEventListener('mousemove', handleCanvasDrag)
    document.addEventListener('mouseup', handleCanvasDragEnd)
  }
}

const handleCanvasDrag = (event: MouseEvent) => {
  if (!isDraggingCanvas.value) return
  canvasOffset.value = {
    x: event.clientX - canvasDragStart.value.x,
    y: event.clientY - canvasDragStart.value.y,
  }
  constrainCanvasOffset()
}

const handleCanvasDragEnd = () => {
  isDraggingCanvas.value = false
  document.removeEventListener('mousemove', handleCanvasDrag)
  document.removeEventListener('mouseup', handleCanvasDragEnd)
}

// 画布级别的双指缩放处理
let wrapperTouchDistance = ref(0)
let wrapperIsPinching = ref(false)

// 画布单指拖动
let isTouchDraggingCanvas = ref(false)
let touchDragStart = ref({ x: 0, y: 0 })

const getWrapperTouchDistance = (touches: TouchList) => {
  const dx = touches[0].clientX - touches[1].clientX
  const dy = touches[0].clientY - touches[1].clientY
  return Math.sqrt(dx * dx + dy * dy)
}

const handleWrapperTouchStart = (event: TouchEvent) => {
  if (event.touches.length === 2) {
    // 双指开始缩放
    event.preventDefault()
    wrapperIsPinching.value = true
    wrapperTouchDistance.value = getWrapperTouchDistance(event.touches)
  } else if (event.touches.length === 1) {
    // 单指开始拖动画布
    const target = event.target as HTMLElement
    if (target === canvasRef.value || target === canvasWrapperRef.value || target.classList.contains('grid-background')) {
      isTouchDraggingCanvas.value = true
      touchDragStart.value = {
        x: event.touches[0].clientX - canvasOffset.value.x,
        y: event.touches[0].clientY - canvasOffset.value.y,
      }
    }
  }
}

const handleWrapperTouchMove = (event: TouchEvent) => {
  if (wrapperIsPinching.value && event.touches.length === 2) {
    event.preventDefault()
    const newDistance = getWrapperTouchDistance(event.touches)
    if (wrapperTouchDistance.value > 0) {
      const scaleChange = (newDistance - wrapperTouchDistance.value) / 300
      const newScale = scale.value + scaleChange
      scale.value = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScale))
      constrainCanvasOffset()
    }
    wrapperTouchDistance.value = newDistance
  } else if (isTouchDraggingCanvas.value && event.touches.length === 1) {
    event.preventDefault()
    canvasOffset.value = {
      x: event.touches[0].clientX - touchDragStart.value.x,
      y: event.touches[0].clientY - touchDragStart.value.y,
    }
    constrainCanvasOffset()
  }
}

const handleWrapperTouchEnd = () => {
  wrapperIsPinching.value = false
  wrapperTouchDistance.value = 0
  isTouchDraggingCanvas.value = false
}

// 添加新座位
const addSeat = async () => {
  const count = seatStore.seats.length
  const cols = 4
  const spacing = 80
  const x = 40 + (count % cols) * spacing
  const y = 40 + Math.floor(count / cols) * spacing
  const row = Math.floor(count / cols)
  const col = count % cols
  const seatName = `${String.fromCharCode(65 + row)}${col + 1}`

  try {
    await seatStore.createSeat({
      name: seatName,
      x,
      y,
      status: 'idle',
    })
  } catch (error) {
    console.error('添加座位失败:', error)
    alert('添加座位失败，请重试')
  }
}

// 删除座位
const deleteSeat = async (seat: Seat) => {
  if (seat.status !== 'idle') {
    alert('只能删除空闲状态的座位')
    return
  }
  if (!confirm(`确定要删除座位 ${seat.name} 吗？`)) {
    return
  }
  try {
    await seatStore.deleteSeat(seat.id)
  } catch (error) {
    console.error('删除座位失败:', error)
    alert('删除座位失败，请重试')
  }
}

// 缩放百分比显示
const scalePercent = computed(() => Math.round(scale.value * 100) + '%')
</script>

<template>
  <div class="seat-canvas-container">
    <!-- 缩放控制栏 -->
    <div class="zoom-toolbar">
      <button class="zoom-btn" @click="zoomOut" :disabled="scale <= MIN_SCALE">
        <span class="btn-icon">−</span>
      </button>
      <span class="zoom-text">{{ scalePercent }}</span>
      <button class="zoom-btn" @click="zoomIn" :disabled="scale >= MAX_SCALE">
        <span class="btn-icon">+</span>
      </button>
      <button class="zoom-btn reset" @click="resetZoom">
        <span class="btn-icon">⌘</span>
      </button>
    </div>

    <div
      ref="canvasWrapperRef"
      class="canvas-wrapper"
      @mousedown="handleCanvasMouseDown"
      @touchstart="handleWrapperTouchStart"
      @touchmove="handleWrapperTouchMove"
      @touchend="handleWrapperTouchEnd"
    >
      <!-- 画布 -->
      <div
        ref="canvasRef"
        class="seat-canvas"
        :style="{
          transform: `translate(${canvasOffset.x}px, ${canvasOffset.y}px) scale(${scale})`,
          width: `${CANVAS_WIDTH}px`,
          height: `${CANVAS_HEIGHT}px`,
        }"
      >
        <SeatItem
          v-for="seat in seatStore.seats"
          :key="seat.id"
          :seat="seat"
          :is-edit-mode="isEditMode"
          :scale="scale"
          @click="handleSeatClick"
          @dragstart="handleDragStart"
          @delete="deleteSeat"
        />

        <!-- 空状态 -->
        <div v-if="seatStore.seats.length === 0" class="empty-state">
          <div class="empty-icon">🪑</div>
          <div class="empty-text">暂无座位</div>
        </div>

        <!-- 网格背景 -->
        <div class="grid-background"></div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <button class="btn-add-seat" @click="addSeat">
        <span class="btn-icon">+</span>
        <span>添加座位</span>
      </button>
      <button
        class="btn-edit"
        :class="{ active: isEditMode }"
        @click="isEditMode = !isEditMode"
      >
        <span class="btn-icon">{{ isEditMode ? '✓' : '✏️' }}</span>
        <span>{{ isEditMode ? '完成编辑' : '编辑位置' }}</span>
      </button>
    </div>

    <!-- 提示信息 -->
    <div class="tip-text">
      {{ isEditMode ? '拖拽座位调整位置，捏合双指缩放画布' : '点击座位进行入座管理' }}
    </div>

    <!-- 状态图例 -->
    <div class="status-legend">
      <div class="legend-item">
        <span class="dot" style="background: #52c41a;"></span>
        <span>空闲</span>
      </div>
      <div class="legend-item">
        <span class="dot" style="background: #1890ff;"></span>
        <span>使用中</span>
      </div>
      <div class="legend-item">
        <span class="dot" style="background: #faad14;"></span>
        <span>即将到期</span>
      </div>
      <div class="legend-item">
        <span class="dot" style="background: #f5222d;"></span>
        <span>已超时</span>
      </div>
    </div>

    <!-- 弹窗 -->
    <CheckInModal
      v-if="showCheckInModal && selectedSeat"
      :seat="selectedSeat"
      @close="closeModals"
      @success="closeModals"
    />

    <SeatDetailModal
      v-if="showDetailModal && selectedSeat"
      :seat="selectedSeat"
      @close="closeModals"
      @success="closeModals"
    />
  </div>
</template>

<style scoped>
.seat-canvas-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 8px;
  gap: 8px;
  height: 100%;
  overflow: hidden;
}

/* 缩放工具栏 */
.zoom-toolbar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 8px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.zoom-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 6px;
  background: #f5f5f5;
  color: #333;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.zoom-btn:hover:not(:disabled) {
  background: #e0e0e0;
}

.zoom-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.zoom-btn.reset {
  font-size: 14px;
}

.zoom-text {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  min-width: 50px;
  text-align: center;
}

/* 画布容器 */
.canvas-wrapper {
  flex: 1;
  position: relative;
  background: #f0f0f0;
  border-radius: 12px;
  overflow: hidden;
  cursor: grab;
}

.canvas-wrapper:active {
  cursor: grabbing;
}

/* 画布 */
.seat-canvas {
  position: relative;
  background: #fff;
  transform-origin: 0 0;
  transition: transform 0.1s ease-out;
}

/* 网格背景 */
.grid-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    linear-gradient(#e0e0e0 1px, transparent 1px),
    linear-gradient(90deg, #e0e0e0 1px, transparent 1px);
  background-size: 20px 20px;
  pointer-events: none;
  z-index: 0;
}

/* 空状态 */
.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
  z-index: 1;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 16px;
  color: #999;
}

/* 工具栏 */
.toolbar {
  display: flex;
  gap: 10px;
  padding: 0 4px;
}

.btn-add-seat,
.btn-edit {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-icon {
  font-size: 16px;
}

.btn-add-seat {
  background: #52c41a;
  color: #fff;
}

.btn-add-seat:active {
  background: #389e0d;
  transform: scale(0.98);
}

.btn-edit {
  background: #f0f0f0;
  color: #666;
}

.btn-edit.active {
  background: #1890ff;
  color: #fff;
}

.btn-edit:active {
  transform: scale(0.98);
}

/* 提示文字 */
.tip-text {
  text-align: center;
  font-size: 12px;
  color: #999;
  padding: 0 4px;
}

/* 状态图例 */
.status-legend {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding: 10px;
  background: #fff;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #666;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .zoom-toolbar,
  .status-legend {
    background: #2a2a2a;
  }

  .zoom-btn {
    background: #3a3a3a;
    color: #ccc;
  }

  .zoom-text {
    color: #fff;
  }

  .canvas-wrapper {
    background: #1a1a1a;
  }

  .seat-canvas {
    background: #2a2a2a;
  }

  .grid-background {
    background-image:
      linear-gradient(#3a3a3a 1px, transparent 1px),
      linear-gradient(90deg, #3a3a3a 1px, transparent 1px);
  }

  .btn-edit {
    background: #3a3a3a;
    color: #ccc;
  }

  .legend-item {
    color: #ccc;
  }

  .tip-text {
    color: #666;
  }
}
</style>
