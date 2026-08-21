<script setup lang="ts">
import { ref, computed } from 'vue'
import { useSeatStore } from '@/stores/seat'
import type { Seat, CheckInForm } from '@/types'

const props = defineProps<{
  seat: Seat
}>()

const emit = defineEmits<{
  close: []
  success: []
}>()

const seatStore = useSeatStore()
const isSubmitting = ref(false)

// 表单数据
const form = ref<CheckInForm>({
  customerName: '',
  phone: '',
  durationHours: 2,
  startTime: new Date().toISOString().slice(0, 16),
  note: '',
  couponInfo: '',
  isPaid: false,
  isVerified: false,
})

// 计算结束时间
const endTime = computed(() => {
  const start = new Date(form.value.startTime)
  const end = new Date(start.getTime() + form.value.durationHours * 60 * 60 * 1000)
  return end.toLocaleString('zh-CN')
})

// 表单验证 - 只保留必填项：时长和开始时间
const isValid = computed(() => {
  return form.value.durationHours > 0 && form.value.startTime
})

// 提交表单
const handleSubmit = async () => {
  if (!isValid.value) return
  isSubmitting.value = true
  try {
    await seatStore.checkIn(props.seat.id, form.value)
    emit('success')
  } catch (error) {
    console.error('入座失败:', error)
    alert('入座失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

// 设置为当前时间（使用本地时间格式）
const setNow = () => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  form.value.startTime = `${year}-${month}-${day}T${hours}:${minutes}`
}

// 设置时长
const setDuration = (hours: number) => {
  form.value.durationHours = hours
}
</script>

<template>
  <div class="modal-overlay" @click.self="emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>入座登记 - {{ seat.name }}</h3>
        <button class="btn-close" @click="emit('close')">&times;</button>
      </div>

      <form class="modal-body" @submit.prevent="handleSubmit">
        <!-- 客户姓名（可选） -->
        <div class="form-group">
          <label>客户姓名</label>
          <input
            v-model="form.customerName"
            type="text"
            placeholder="选填"
          />
        </div>

        <!-- 手机号（可选） -->
        <div class="form-group">
          <label>手机号</label>
          <input
            v-model="form.phone"
            type="tel"
            placeholder="选填"
          />
        </div>

        <!-- 体验时长 -->
        <div class="form-group">
          <label>体验时长 <span class="required">*</span></label>
          <div class="duration-selector">
            <button
              v-for="h in [1, 2, 3, 4, 6, 8]"
              :key="h"
              type="button"
              class="duration-btn"
              :class="{ active: form.durationHours === h }"
              @click="setDuration(h)"
            >
              {{ h }}小时
            </button>
          </div>
          <input
            v-model.number="form.durationHours"
            type="number"
            min="1"
            max="24"
            class="duration-input"
          />
        </div>

        <!-- 开始时间 -->
        <div class="form-group">
          <label>开始时间 <span class="required">*</span></label>
          <div class="time-input-wrapper">
            <input v-model="form.startTime" type="datetime-local" required />
            <button type="button" class="btn-now" @click="setNow">
              现在
            </button>
          </div>
        </div>

        <!-- 结束时间预览 -->
        <div class="form-group">
          <label>预计结束时间</label>
          <div class="end-time-preview">{{ endTime }}</div>
        </div>

        <!-- 团购券信息 -->
        <div class="form-group">
          <label>团购券信息</label>
          <input
            v-model="form.couponInfo"
            type="text"
            placeholder="请输入券码（选填）"
          />
        </div>

        <!-- 备注 -->
        <div class="form-group">
          <label>备注</label>
          <textarea
            v-model="form.note"
            rows="2"
            placeholder="添加备注信息..."
          ></textarea>
        </div>

        <!-- 付款/核销开关 -->
        <div class="form-group switches">
          <label class="switch-label">
            <input v-model="form.isPaid" type="checkbox" />
            <span class="switch-text">已付款</span>
          </label>
          <label class="switch-label">
            <input v-model="form.isVerified" type="checkbox" />
            <span class="switch-text">已核销</span>
          </label>
        </div>

        <!-- 提交按钮 -->
        <div class="form-actions">
          <button type="button" class="btn-cancel" @click="emit('close')">
            取消
          </button>
          <button
            type="submit"
            class="btn-submit"
            :disabled="!isValid || isSubmitting"
          >
            {{ isSubmitting ? '提交中...' : '确认入座' }}
          </button>
        </div>
      </form>
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

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.required {
  color: #f5222d;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1890ff;
}

.duration-selector {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
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
  text-align: center;
}

/* 时间输入框包装器 */
.time-input-wrapper {
  display: flex;
  gap: 8px;
  align-items: center;
}

.time-input-wrapper input {
  flex: 1;
}

.btn-now {
  padding: 8px 14px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-now:hover {
  background: #40a9ff;
}

.btn-now:active {
  background: #096dd9;
  transform: scale(0.98);
}

.end-time-preview {
  padding: 10px 12px;
  background: #f5f5f5;
  border-radius: 8px;
  font-size: 14px;
  color: #666;
}

.switches {
  display: flex;
  gap: 24px;
}

.switch-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.switch-text {
  font-size: 14px;
  color: #333;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn-cancel,
.btn-submit {
  flex: 1;
  padding: 12px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #f5f5f5;
  border: none;
  color: #666;
}

.btn-submit {
  background: #1890ff;
  border: none;
  color: #fff;
}

.btn-submit:disabled {
  background: #d9d9d9;
  cursor: not-allowed;
}

.btn-cancel:active,
.btn-submit:active {
  transform: scale(0.98);
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

  .btn-close:hover {
    background: #3a3a3a;
    color: #fff;
  }

  .form-group label {
    color: #fff;
  }

  .form-group input,
  .form-group textarea {
    background: #3a3a3a;
    border-color: #4a4a4a;
    color: #fff;
  }

  .duration-btn {
    background: #3a3a3a;
    border-color: #4a4a4a;
    color: #ccc;
  }

  .end-time-preview {
    background: #3a3a3a;
    color: #ccc;
  }

  .switch-text {
    color: #fff;
  }

  .btn-cancel {
    background: #3a3a3a;
    color: #ccc;
  }

  .btn-now:hover {
    background: #40a9ff;
  }
}
</style>
