import axios from 'axios'
import type { Seat, Session, SessionHistory, CheckInForm } from '@/types'

// 获取 API 基础地址
// 开发时：修改 .env.local 文件中的 VITE_API_BASE_URL 为你的电脑IP
// 例如：VITE_API_BASE_URL=http://192.168.1.5:8080/api
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// 创建 axios 实例 - 连接Java后端
const apiClient = axios.create({
  baseURL: baseURL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - Java后端返回格式处理
apiClient.interceptors.response.use(
  (response) => {
    // Java后端返回格式: { success: true, data: {...}, message: "..." }
    const data = response.data
    if (data.success) {
      return data.data || data
    } else {
      return Promise.reject(new Error(data.error || '请求失败'))
    }
  },
  (error) => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

// 座位 API
export const seatApi = {
  // 获取座位列表
  async getSeats(): Promise<{ seats: Seat[] }> {
    const res = await apiClient.get('/seats')
    return res as unknown as { seats: Seat[] }
  },

  // 创建座位
  async createSeat(seat: Omit<Seat, 'id'>): Promise<Seat> {
    const res = await apiClient.post('/seats', seat)
    return (res as any).seat as Seat
  },

  // 更新座位位置
  async updateSeatPosition(id: number, x: number, y: number): Promise<Seat> {
    const res = await apiClient.put(`/seats/${id}`, { x, y })
    return (res as any).seat as Seat
  },

  // 删除座位
  async deleteSeat(id: number): Promise<void> {
    await apiClient.delete(`/seats/${id}`)
  },
}

// 会话 API
export const sessionApi = {
  // 创建入座记录
  async createSession(seatId: number, form: CheckInForm): Promise<Session> {
    const res = await apiClient.post('/sessions', { seatId, ...form })
    return (res as any).session as Session
  },

  // 续时
  async renewSession(id: number, renewalHours: number): Promise<Session> {
    const res = await apiClient.put(`/sessions/${id}/renew`, { renewalHours })
    return (res as any).session as Session
  },

  // 结束体验
  async endSession(id: number): Promise<Session> {
    const res = await apiClient.put(`/sessions/${id}/end`)
    return (res as any).session as Session
  },

  // 获取历史记录（改为从座位API获取）
  async getHistory(seatId: number): Promise<{ history: SessionHistory[] }> {
    const res = await apiClient.get(`/seats/${seatId}/history`)
    return res as unknown as { history: SessionHistory[] }
  },

  // 获取今日统计
  async getTodayStats(): Promise<any> {
    return await apiClient.get('/sessions/stats/today')
  },

  // 更新会话备注
  async updateNote(id: number, note: string): Promise<Session> {
    const res = await apiClient.put(`/sessions/${id}/note`, { note })
    return (res as any).session as Session
  },

  // 更新会话信息（备注、付款状态、核销状态）
  async updateSessionInfo(id: number, info: { note: string; isPaid: boolean; isVerified: boolean }): Promise<Session> {
    const res = await apiClient.put(`/sessions/${id}/info`, info)
    return (res as any).session as Session
  },
}

export default apiClient
