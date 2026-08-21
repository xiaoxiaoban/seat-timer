import { createRouter, createWebHistory } from 'vue-router'
import SeatManageView from '@/views/SeatManageView.vue'
import HistoryView from '@/views/HistoryView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/seats',
    },
    {
      path: '/seats',
      name: 'seats',
      component: SeatManageView,
      meta: {
        title: '座位管理',
        icon: '🪑',
      },
    },
    {
      path: '/history',
      name: 'history',
      component: HistoryView,
      meta: {
        title: '历史记录',
        icon: '📋',
      },
    },
  ],
})

export default router
