<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useSeatStore } from '@/stores/seat'
import type { Seat } from '@/types'

const props = defineProps<{
  seat: Seat
}>()

const emit = defineEmits<{
  close: []
  success: []
  delete: []
}>()

const seatStore = useSeatStore()
const showRenewalForm = ref(false)
const renewalHours = ref(1)
const isSubmitting = ref(false)
const isSavingInfo = ref(false)

// 可编辑字段
const editableNote = ref('')
const editableIsPaid = ref(false)
const editableIsVerified = ref(false)

// 初始化可编辑字段
onMounted(() => {
  editableNote.value = props.seat.currentSession?.note || ''
  editableIsPaid.value = props.seat.currentSession?.isPaid || false
  editableIsVerified.value = props.seat.currentSession?.isVerified || false
})

// 倒计时
const remainingSeconds = ref(0)
let timerInterval: number | null = null

// 初始化倒计时
const updateTimer = () => {
  remainingSeconds.value = seatStore.getRemainingSeconds(props.seat)
}

onMounted(() => {
  updateTimer()
  timerInterval = window.setInterval(updateTimer, 1000)
})

onUnmounted(() => {
  if (timerInterval) clearInterval(timerInterval)
})

// 倒计时显示 (hh:mm:ss)
const countdownDisplay = computed(() => {
  const secs = remainingSeconds.value
  const hours = Math.floor(secs / 3600)
  const mins = Math.floor((secs % 3600) / 60)
  const s = secs % 60
  return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
})

// 进度百分比
const progressPercent = computed(() => {
  if (!props.seat.currentSession) return 0
  return seatStore.getRemainingPercent(props.seat.currentSession)
})

// 状态颜色
const statusColor = computed(() => {
  switch (props.seat.status) {
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

// 续时
const handleRenew = async () => {
  if (!props.seat.currentSession || renewalHours.value <= 0) return
  isSubmitting.value = true
  try {
    await seatStore.renewSession(props.seat.currentSession.id, renewalHours.value)
    showRenewalForm.value = false
    renewalHours.value = 1
    updateTimer()
  } catch (error) {
    console.error('续时失败:', error)
    alert('续时失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 结束体验
const handleEnd = async () => {
  if (!props.seat.currentSession) return
  if (!confirm('确定要结束该客户的体验吗？')) return
  isSubmitting.value = true
  try {
    await seatStore.endSession(props.seat.currentSession.id)
    emit('success')
  } catch (error) {
    console.error('结束失败:', error)
    alert('结束失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 格式化日期时间
const formatDateTime = (dateStr: string) => {
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 保存信息（备注、付款状态、核销状态）
const handleSaveInfo = async () => {
  if (!props.seat.currentSession) return
  isSavingInfo.value = true
  try {
    await seatStore.updateSessionInfo(props.seat.currentSession.id, {
      note: editableNote.value,
      isPaid: editableIsPaid.value,
      isVerified: editableIsVerified.value,
    })
    // 更新本地数据
    if (props.seat.currentSession) {
      props.seat.currentSession.note = editableNote.value
      props.seat.currentSession.isPaid = editableIsPaid.value
      props.seat.currentSession.isVerified = editableIsVerified.value
    }
  } catch (error) {
    console.error('保存失败:', error)
    alert('保存失败，请重试')
  } finally {
    isSavingInfo.value = false
  }
}
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>{{ seat.name }} - 座位详情</h3>
        <button class="btn-close" @click="emit('close')">&times;</button>
      </div>

      <div class="modal-body">
        <!-- 倒计时显示 -->
        <div class="countdown-section" :style="{ '--status-color': statusColor }">
          <div class="countdown-label">剩余时间</div>
          <div class="countdown-value">{{ countdownDisplay }}</div>
          <div class="progress-bar">
            <div
              class="progress-fill"
              :style="{ width: `${progressPercent}%`, background: statusColor }"
            ></div>
          </div>
        </div>

        <template v-if="seat.currentSession">
          <!-- 客户信息 -->
          <div class="info-section">
            <h4>客户信息</h4>
            <div class="info-row">
              <span class="info-label">姓名</span>
              <span class="info-value">{{ seat.currentSession.customerName }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">手机</span>
              <span class="info-value">{{ seat.currentSession.phone }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">时长</span>
              <span class="info-value">
                {{ seat.currentSession.durationHours }}小时
                <span v-if="seat.currentSession.renewalHours > 0" class="renewal-tag">
                  +{{ seat.currentSession.renewalHours }}小时续时
                </span>
              </span>
            </div>
          </div>

          <!-- 时间信息 -->
          <div class="info-section">
            <h4>时间信息</h4>
            <div class="info-row">
              <span class="info-label">开始</span>
              <span class="info-value">{{ formatDateTime(seat.currentSession.startTime) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">结束</span>
              <span class="info-value">{{ formatDateTime(seat.currentSession.endTime) }}</span>
            </div>
          </div>

          <!-- 支付信息（只读显示当前状态） -->
          <div class="info-section">
            <h4>支付信息</h4>
            <div class="info-row">
              <span class="info-label">付款状态</span>
              <span class="info-value" :class="{ 'status-paid': seat.currentSession.isPaid }">
                {{ seat.currentSession.isPaid ? '已付款' : '未付款' }}
              </span>
            </div>
            <div v-if="seat.currentSession.couponInfo" class="info-row">
              <span class="info-label">团购券</span>
              <span class="info-value">{{ seat.currentSession.couponInfo }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">核销状态</span>
              <span class="info-value" :class="{ 'status-verified': seat.currentSession.isVerified }">
                {{ seat.currentSession.isVerified ? '已核销' : '未核销' }}
              </span>
            </div>
          </div>

          <!-- 备注和状态编辑 -->
          <div class="info-section edit-section">
            <h4>信息编辑</h4>

            <!-- 付款状态开关 -->
            <div class="edit-row">
              <span class="edit-label">付款状态</span>
              <label class="switch-toggle">
                <input v-model="editableIsPaid" type="checkbox" />
                <span class="switch-slider"></span>
                <span class="switch-text">{{ editableIsPaid ? '已付款' : '未付款' }}</span>
              </label>
            </div>

            <!-- 核销状态开关 -->
            <div class="edit-row">
              <span class="edit-label">核销状态</span>
              <label class="switch-toggle">
                <input v-model="editableIsVerified" type="checkbox" />
                <span class="switch-slider"></span>
                <span class="switch-text">{{ editableIsVerified ? '已核销' : '未核销' }}</span>
              </label>
            </div>

            <!-- 备注 -->
            <div class="edit-row stacked">
              <span class="edit-label">备注</span>
              <textarea
                v-model="editableNote"
                rows="2"
                placeholder="添加备注信息..."
                class="edit-textarea"
              ></textarea>
            </div>

            <!-- 保存按钮 -->
            <button
              class="btn-save-info"
              :disabled="isSavingInfo"
              @click="handleSaveInfo"
            >
              {{ isSavingInfo ? '保存中...' : '保存信息' }}
            </button>
          </div>

          <!-- 续时表单 -->
          <div v-if="showRenewalForm" class="renewal-section">
            <h4>续时</h4>
            <div class="renewal-form">
              <div class="duration-selector">
                <button
                  v-for="h in [1, 2, 3, 4]"
                  :key="h"
                  type="button"
                  class="duration-btn"
                  :class="{ active: renewalHours === h }"
                  @click="renewalHours = h"
                >
                  {{ h }}小时
                </button>
              </div>
              <input
                v-model.number="renewalHours"
                type="number"
                min="1"
                max="24"
                class="duration-input"
              />
              <div class="renewal-actions">
                <button class="btn-cancel-small" @click="showRenewalForm = false">
                  取消
                </button>
                <button
                  class="btn-confirm"
                  :disabled="renewalHours <= 0 || isSubmitting"
                  @click="handleRenew"
                >
                  {{ isSubmitting ? '处理中...' : '确认续时' }}
                </button>
              </div>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div v-else class="action-buttons">
            <button class="btn-renew" @click="showRenewalForm = true">
              ⏱️ 续时
            </button>
            <button class="btn-end" @click="handleEnd">
              ✓ 结束体验
            </button>
          </div>
        </template>

        <div v-else class="empty-state">
          <p>该座位当前空闲</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
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

.modal-content {
  background: #fff;
  border-radius: 16px;
  width: 100%;
  max-width: 400px;
  max-height: 90vh;
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

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
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

.modal-body {
  padding: 20px;
  overflow-y: auto;
  max-height: calc(90vh - 60px);
}

/* 倒计时区域 */
.countdown-section {
  text-align: center;
  padding: 24px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  border-radius: 12px;
  margin-bottom: 20px;
}

.countdown-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.countdown-value {
  font-size: 48px;
  font-weight: 700;
  color: var(--status-color, #1890ff);
  font-family: 'SF Mono', monospace;
  line-height: 1;
  margin-bottom: 16px;
}

.progress-bar {
  height: 6px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 1s linear;
}

/* 信息区域 */
.info-section {
  margin-bottom: 20px;
}

.info-section h4 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.info-label {
  font-size: 14px;
  color: #666;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.renewal-tag {
  display: inline-block;
  padding: 2px 6px;
  background: #faad14;
  color: #fff;
  border-radius: 4px;
  font-size: 11px;
  margin-left: 4px;
}

.status-paid,
.status-verified {
  color: #52c41a;
}

.note-text {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  background: #f5f5f5;
  padding: 12px;
  border-radius: 8px;
}

/* 备注编辑区域 */
.note-edit-wrapper {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.note-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  min-height: 60px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.note-textarea:focus {
  outline: none;
  border-color: #1890ff;
}

.btn-save-note {
  align-self: flex-end;
  padding: 6px 16px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save-note:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-save-note:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

/* 编辑区域样式 */
.edit-section {
  background: #f8f9fa;
  border-radius: 12px;
  padding: 16px;
}

.edit-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #e8e8e8;
}

.edit-row:last-of-type {
  border-bottom: none;
}

.edit-row.stacked {
  flex-direction: column;
  align-items: stretch;
  gap: 8px;
}

.edit-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

/* 开关样式 */
.switch-toggle {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.switch-toggle input {
  display: none;
}

.switch-slider {
  width: 44px;
  height: 24px;
  background: #d9d9d9;
  border-radius: 12px;
  position: relative;
  transition: background 0.3s;
}

.switch-slider::after {
  content: '';
  position: absolute;
  width: 20px;
  height: 20px;
  background: #fff;
  border-radius: 50%;
  top: 2px;
  left: 2px;
  transition: transform 0.3s;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
}

.switch-toggle input:checked + .switch-slider {
  background: #52c41a;
}

.switch-toggle input:checked + .switch-slider::after {
  transform: translateX(20px);
}

.switch-text {
  font-size: 13px;
  color: #666;
  min-width: 48px;
}

/* 编辑文本框 */
.edit-textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.5;
  resize: vertical;
  min-height: 60px;
  font-family: inherit;
  transition: border-color 0.2s;
  background: #fff;
}

.edit-textarea:focus {
  outline: none;
  border-color: #1890ff;
}

/* 保存按钮 */
.btn-save-info {
  width: 100%;
  margin-top: 12px;
  padding: 10px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-save-info:hover:not(:disabled) {
  background: #40a9ff;
}

.btn-save-info:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .edit-section {
    background: #2a2a2a;
  }

  .edit-row {
    border-bottom-color: #3a3a3a;
  }

  .edit-label {
    color: #fff;
  }

  .switch-slider {
    background: #4a4a4a;
  }

  .switch-text {
    color: #ccc;
  }

  .edit-textarea {
    background: #3a3a3a;
    border-color: #4a4a4a;
    color: #fff;
  }

  .edit-textarea:focus {
    border-color: #1890ff;
  }

  .note-textarea {
    background: #3a3a3a;
    border-color: #4a4a4a;
    color: #fff;
  }

  .note-textarea:focus {
    border-color: #1890ff;
  }
}

/* 续时表单 */
.renewal-section {
  padding: 16px;
  background: #f5f5f5;
  border-radius: 12px;
  margin-bottom: 16px;
}

.renewal-section h4 {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}

.duration-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.duration-btn {
  flex: 1;
  padding: 8px;
  border: 1px solid #d9d9d9;
  background: #fff;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.duration-btn:hover {
  border-color: #1890ff;
  color: #1890ff;
}

.duration-btn.active {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

.duration-input {
  width: 100%;
  padding: 10px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  margin-bottom: 12px;
  text-align: center;
}

.renewal-actions {
  display: flex;
  gap: 8px;
}

.btn-cancel-small,
.btn-confirm {
  flex: 1;
  padding: 10px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel-small {
  background: #fff;
  border: 1px solid #d9d9d9;
  color: #666;
}

.btn-confirm {
  background: #1890ff;
  border: none;
  color: #fff;
}

.btn-confirm:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  gap: 12px;
}

.btn-renew,
.btn-end {
  flex: 1;
  padding: 14px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-renew {
  background: #faad14;
  color: #fff;
}

.btn-end {
  background: #52c41a;
  color: #fff;
}

.btn-renew:active,
.btn-end:active {
  transform: scale(0.98);
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #999;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .modal-content {
    background: #2a2a2a;
  }

  .modal-header {
    border-bottom-color: #3a3a3a;
  }

  .modal-header h3 {
    color: #fff;
  }

  .countdown-section {
    background: linear-gradient(135deg, #2a2a2a 0%, #1a1a1a 100%);
  }

  .info-section h4 {
    color: #fff;
    border-bottom-color: #3a3a3a;
  }

  .info-label {
    color: #999;
  }

  .info-value {
    color: #fff;
  }

  .note-text {
    background: #3a3a3a;
    color: #ccc;
  }

  .renewal-section {
    background: #3a3a3a;
  }

  .duration-btn {
    background: #3a3a3a;
    border-color: #4a4a4a;
    color: #ccc;
  }

  .duration-input {
    background: #3a3a3a;
    border-color: #4a4a4a;
    color: #fff;
  }

  .btn-cancel-small {
    background: #3a3a3a;
    border-color: #4a4a4a;
    color: #ccc;
  }
}
</style>
