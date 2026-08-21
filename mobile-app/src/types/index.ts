// 座位状态
export type SeatStatus = 'idle' | 'occupied' | 'warning' | 'expired'

// 会话状态
export type SessionStatus = 'active' | 'completed' | 'cancelled'

// 座位
export interface Seat {
  id: number
  name: string
  x: number
  y: number
  status: SeatStatus
  currentSession?: Session
}

// 会话
export interface Session {
  id: number
  seatId: number
  customerName: string
  phone: string
  durationHours: number
  startTime: string
  endTime: string
  actualEndTime?: string
  note?: string
  couponInfo?: string
  isPaid: boolean
  isVerified: boolean
  renewalHours: number
  status: SessionStatus
}

// 历史记录
export interface SessionHistory {
  id: number
  seatId: number
  seatName: string
  customerName: string
  phone: string
  durationHours: number
  startTime: string
  endTime: string
  actualEndTime?: string
  note?: string
  couponInfo?: string
  isPaid: boolean
  isVerified: boolean
  renewalHours: number
  status: SessionStatus
  created_at?: string
  action?: string
}

// 入座表单
export interface CheckInForm {
  customerName: string
  phone: string
  durationHours: number
  startTime: string
  note: string
  couponInfo: string
  isPaid: boolean
  isVerified: boolean
}

// 续时表单
export interface RenewalForm {
  renewalHours: number
}
