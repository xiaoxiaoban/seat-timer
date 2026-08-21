import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Seat, Session, SessionHistory, CheckInForm } from '@/types'
import { seatApi, sessionApi } from '@/services/api'

export const useSeatStore = defineStore('seat', () => {
  // State
  const seats = ref<Seat[]>([])
  const currentSession = ref<Session | null>(null)
  const sessionHistory = ref<SessionHistory[]>([])
  const isLoading = ref(false)
  const error = ref<string | null>(null)

  // Getters
  const idleSeats = computed(() => seats.value.filter((s) => s.status === 'idle'))
  const occupiedSeats = computed(() =>
    seats.value.filter((s) => s.status === 'occupied')
  )
  const warningSeats = computed(() =>
    seats.value.filter((s) => s.status === 'warning')
  )
  const expiredSeats = computed(() =>
    seats.value.filter((s) => s.status === 'expired')
  )

  // 计算座位的剩余秒数
  const getRemainingSeconds = (seat: Seat): number => {
    if (!seat.currentSession?.endTime) return 0
    const end = new Date(seat.currentSession.endTime).getTime()
    const now = Date.now()
    return Math.max(0, Math.floor((end - now) / 1000))
  }

  // 计算座位的状态
  const getSeatStatus = (seat: Seat): Seat['status'] => {
    if (!seat.currentSession) return 'idle'
    const remaining = getRemainingSeconds(seat)
    if (remaining === 0) return 'expired'
    if (remaining < 15 * 60) return 'warning'
    return 'occupied'
  }

  // 格式化时间 hh:mm:ss
  const formatTime = (seconds: number): string => {
    const hours = Math.floor(seconds / 3600)
    const mins = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    return `${hours.toString().padStart(2, '0')}:${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }

  // 计算剩余时间比例 (用于进度条)
  const getRemainingPercent = (session: Session): number => {
    const start = new Date(session.startTime).getTime()
    const end = new Date(session.endTime).getTime()
    const now = Date.now()
    const total = end - start
    const remaining = end - now
    return Math.max(0, Math.min(100, (remaining / total) * 100))
  }

  // Actions
  // 加载座位列表
  const loadSeats = async () => {
    isLoading.value = true
    error.value = null
    try {
      const data = await seatApi.getSeats()
      seats.value = data.seats || []
    } catch (err) {
      error.value = err instanceof Error ? err.message : '加载座位失败'
      console.error('加载座位失败:', err)
    } finally {
      isLoading.value = false
    }
  }

  // 创建座位
  const createSeat = async (seat: Omit<Seat, 'id'>) => {
    try {
      const newSeat = await seatApi.createSeat(seat)
      seats.value.push(newSeat)
      return newSeat
    } catch (err) {
      console.error('创建座位失败:', err)
      throw err
    }
  }

  // 更新座位位置
  const updateSeatPosition = async (id: number, x: number, y: number) => {
    try {
      const updated = await seatApi.updateSeatPosition(id, x, y)
      const index = seats.value.findIndex((s) => s.id === id)
      if (index !== -1) {
        seats.value[index] = { ...seats.value[index], x, y }
      }
      return updated
    } catch (err) {
      console.error('更新座位位置失败:', err)
      throw err
    }
  }

  // 删除座位
  const deleteSeat = async (id: number) => {
    try {
      await seatApi.deleteSeat(id)
      seats.value = seats.value.filter((s) => s.id !== id)
    } catch (err) {
      console.error('删除座位失败:', err)
      throw err
    }
  }

  // 入座
  const checkIn = async (seatId: number, form: CheckInForm) => {
    try {
      const session = await sessionApi.createSession(seatId, form)
      const seat = seats.value.find((s) => s.id === seatId)
      if (seat) {
        seat.currentSession = session
        seat.status = 'occupied'
      }
      currentSession.value = session
      return session
    } catch (err) {
      console.error('入座失败:', err)
      throw err
    }
  }

  // 续时
  const renewSession = async (sessionId: number, renewalHours: number) => {
    try {
      const session = await sessionApi.renewSession(sessionId, renewalHours)
      currentSession.value = session
      // 更新座位中的会话
      const seat = seats.value.find(
        (s) => s.currentSession?.id === sessionId
      )
      if (seat) {
        seat.currentSession = session
        seat.status = 'occupied'
      }
      return session
    } catch (err) {
      console.error('续时失败:', err)
      throw err
    }
  }

  // 结束体验
  const endSession = async (sessionId: number) => {
    try {
      const session = await sessionApi.endSession(sessionId)
      currentSession.value = session
      // 更新座位状态
      const seat = seats.value.find(
        (s) => s.currentSession?.id === sessionId
      )
      if (seat) {
        seat.currentSession = undefined
        seat.status = 'idle'
      }
      return session
    } catch (err) {
      console.error('结束体验失败:', err)
      throw err
    }
  }

  // 加载历史记录
  const loadHistory = async (seatId: number) => {
    try {
      const data = await sessionApi.getHistory(seatId)
      sessionHistory.value = data.history || []
      return data.history || []
    } catch (err) {
      console.error('加载历史记录失败:', err)
      throw err
    }
  }

  // 更新会话备注
  const updateSessionNote = async (sessionId: number, note: string) => {
    try {
      const session = await sessionApi.updateNote(sessionId, note)
      // 更新座位中的会话
      const seat = seats.value.find((s) => s.currentSession?.id === sessionId)
      if (seat) {
        seat.currentSession = session
      }
      return session
    } catch (err) {
      console.error('更新备注失败:', err)
      throw err
    }
  }

  // 更新会话信息（备注、付款状态、核销状态）
  const updateSessionInfo = async (sessionId: number, info: { note: string; isPaid: boolean; isVerified: boolean }) => {
    try {
      const session = await sessionApi.updateSessionInfo(sessionId, info)
      // 更新座位中的会话
      const seat = seats.value.find((s) => s.currentSession?.id === sessionId)
      if (seat) {
        seat.currentSession = session
      }
      return session
    } catch (err) {
      console.error('更新信息失败:', err)
      throw err
    }
  }

  // 设置当前会话
  const setCurrentSession = (session: Session | null) => {
    currentSession.value = session
  }

  // 更新所有座位状态 (用于定时器)
  const updateAllSeatStatus = () => {
    seats.value.forEach((seat) => {
      if (seat.currentSession) {
        seat.status = getSeatStatus(seat)
      }
    })
  }

  return {
    // State
    seats,
    currentSession,
    sessionHistory,
    isLoading,
    error,

    // Getters
    idleSeats,
    occupiedSeats,
    warningSeats,
    expiredSeats,
    getRemainingSeconds,
    formatTime,
    getRemainingPercent,

    // Actions
    loadSeats,
    createSeat,
    updateSeatPosition,
    deleteSeat,
    checkIn,
    renewSession,
    endSession,
    loadHistory,
    setCurrentSession,
    updateAllSeatStatus,
    updateSessionNote,
    updateSessionInfo,
  }
})
