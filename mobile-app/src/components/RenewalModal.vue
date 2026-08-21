<script setup lang="ts">
import type { SessionHistory } from '@/types'

const props = defineProps<{
  history: SessionHistory[]
}>()
</script>

<template>
  <div class="renewal-modal">
    <div class="modal-header">
      <h3 class="modal-title">续时</h3>
      <button class="modal-close">&times;</button>
    </div>
    <div class="modal-body">
      <div class="form-group">
        <label class="form-label">续时时长</label>
        <select class="form-select">
          <option :value="1">1 小时</option>
          <option :value="2">2 小时</option>
          <option :value="3">3 小时</option>
          <option :value="4">4 小时</option>
        </select>
      </div>
    </div>
    <div class="modal-footer">
      <button class="btn-secondary">取消</button>
      <button class="btn-primary">确认续时</button>
    </div>
  </div>
</template>

<style scoped>
.renewal-modal {
  background: white;
  border-radius: 16px;
  width: 100%;
  max-width: 320px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
}

.modal-body {
  padding: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.form-select {
  padding: 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  background: white;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.btn-secondary,
.btn-primary {
  flex: 1;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary {
  background: #f5f5f5;
  color: #666;
}

.btn-primary {
  background: #1890ff;
  color: white;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .renewal-modal {
    background: #2a2a2a;
  }

  .modal-title {
    color: #fff;
  }

  .form-label {
    color: #ddd;
  }

  .form-select {
    background: #333;
    border-color: #444;
    color: #fff;
  }

  .btn-secondary {
    background: #333;
    color: #ddd;
  }
}
</style>
