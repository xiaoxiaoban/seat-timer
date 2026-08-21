<script setup lang="ts">
import { onMounted } from 'vue'
import SeatCanvas from '@/components/SeatCanvas.vue'
import { useSeatStore } from '@/stores/seat'

const seatStore = useSeatStore()

onMounted(() => {
  seatStore.loadSeats()
})
</script>

<template>
  <div class="seat-manage-view">
    <div class="page-header safe-area-top">
      <h1 class="page-title">I豆座位计时</h1>
      <p class="page-subtitle">点击座位查看详情或登记入座</p>
    </div>
    <SeatCanvas />
  </div>
</template>

<style scoped>
.seat-manage-view {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f5f5;
}

.page-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 16px;
  padding-top: max(16px, env(safe-area-inset-top));
  color: white;
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.page-subtitle {
  font-size: 13px;
  opacity: 0.8;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .seat-manage-view {
    background: #1a1a1a;
  }
}
</style>
