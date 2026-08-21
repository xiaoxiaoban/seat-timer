<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const activeTab = computed(() => route.name as string)

const menuItems = [
  { name: 'seats', label: '座位', icon: '🪑' },
  { name: 'history', label: '记录', icon: '📋' },
]

const navigateTo = (name: string) => {
  router.push({ name })
}
</script>

<template>
  <div class="mobile-app">
    <!-- 主内容区 -->
    <main class="main-content">
      <RouterView />
    </main>

    <!-- 底部导航 -->
    <nav class="tab-bar safe-area-bottom">
      <div
        v-for="item in menuItems"
        :key="item.name"
        class="tab-item"
        :class="{ active: activeTab === item.name }"
        @click="navigateTo(item.name)"
      >
        <div class="tab-icon">{{ item.icon }}</div>
        <div class="tab-name">{{ item.label }}</div>
      </div>
    </nav>
  </div>
</template>

<style>
/* 全局样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

body {
  background: #f5f5f5;
}

.mobile-app {
  max-width: 430px;
  margin: 0 auto;
  min-height: 100vh;
  background: #fff;
  position: relative;
}

.main-content {
  padding-bottom: calc(60px + env(safe-area-inset-bottom));
  min-height: 100vh;
}

/* 底部导航栏 */
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  max-width: 430px;
  margin: 0 auto;
  background: #fff;
  border-top: 1px solid #e5e5e5;
  display: flex;
  justify-content: space-around;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
}

.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 0;
  cursor: pointer;
  transition: color 0.2s;
  color: #999;
}

.tab-item.active {
  color: #667eea;
}

.tab-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.tab-name {
  font-size: 11px;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  body {
    background: #1a1a1a;
  }

  .mobile-app {
    background: #1a1a1a;
  }

  .tab-bar {
    background: #2a2a2a;
    border-top-color: #3a3a3a;
  }

  .tab-item {
    color: #666;
  }

  .tab-item.active {
    color: #a78bfa;
  }
}

/* 安全区域适配 */
.safe-area-top {
  padding-top: max(16px, env(safe-area-inset-top));
}

.safe-area-bottom {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
