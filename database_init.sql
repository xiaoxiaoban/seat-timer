-- 座位计时系统数据库初始化脚本
-- 数据库: seat_timer_db
-- 字符集: utf8mb4

-- 设置字符集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS seat_timer_db
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE seat_timer_db;

-- =============================================
-- 1. seats 表 - 座位信息
-- =============================================
DROP TABLE IF EXISTS seats;

CREATE TABLE seats (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  x INT NOT NULL DEFAULT 0,
  y INT NOT NULL DEFAULT 0,
  status ENUM('idle', 'occupied') DEFAULT 'idle',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_status (status),
  INDEX idx_coordinates (x, y)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 2. sessions 表 - 入座记录
-- =============================================
DROP TABLE IF EXISTS sessions;

CREATE TABLE sessions (
  id INT PRIMARY KEY AUTO_INCREMENT,
  seat_id INT NOT NULL,
  customer_name VARCHAR(100) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  duration_hours INT NOT NULL,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  actual_end_time DATETIME NULL,
  note TEXT,
  coupon_info VARCHAR(255),
  is_paid BOOLEAN DEFAULT FALSE,
  is_verified BOOLEAN DEFAULT FALSE,
  renewal_hours INT DEFAULT 0,
  total_amount DECIMAL(10, 2) DEFAULT 0,
  status ENUM('active', 'completed', 'cancelled') DEFAULT 'active',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  FOREIGN KEY (seat_id) REFERENCES seats(id) ON DELETE CASCADE,
  INDEX idx_seat_id (seat_id),
  INDEX idx_status (status),
  INDEX idx_end_time (end_time),
  INDEX idx_start_time (start_time),
  INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 3. renewals 表 - 续时记录
-- =============================================
DROP TABLE IF EXISTS renewals;

CREATE TABLE renewals (
  id INT PRIMARY KEY AUTO_INCREMENT,
  session_id INT NOT NULL,
  renewal_hours INT NOT NULL,
  renewal_amount DECIMAL(10, 2) DEFAULT 0,
  previous_end_time DATETIME NOT NULL,
  new_end_time DATETIME NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
  INDEX idx_session_id (session_id),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 4. session_history 表 - 操作历史
-- =============================================
DROP TABLE IF EXISTS session_history;

CREATE TABLE session_history (
  id INT PRIMARY KEY AUTO_INCREMENT,
  session_id INT NOT NULL,
  action VARCHAR(50) NOT NULL,
  details TEXT,
  operator VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
  INDEX idx_session_id (session_id),
  INDEX idx_action (action),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- 插入示例数据
-- =============================================

-- 插入默认座位 (3x3 布局)
INSERT INTO seats (name, x, y, status) VALUES
('A1', 50, 50, 'idle'),
('A2', 150, 50, 'idle'),
('A3', 250, 50, 'idle'),
('B1', 50, 150, 'idle'),
('B2', 150, 150, 'idle'),
('B3', 250, 150, 'idle'),
('C1', 50, 250, 'idle'),
('C2', 150, 250, 'idle'),
('C3', 250, 250, 'idle');

-- =============================================
-- 创建视图
-- =============================================

-- 座位当前状态视图
CREATE OR REPLACE VIEW seat_status_view AS
SELECT
  s.id,
  s.name,
  s.x,
  s.y,
  s.status AS seat_status,
  s.created_at,
  s.updated_at,
  ses.id AS session_id,
  ses.customer_name,
  ses.phone,
  ses.start_time,
  ses.end_time,
  ses.actual_end_time,
  ses.duration_hours,
  ses.renewal_hours,
  ses.is_paid,
  ses.is_verified,
  ses.note,
  ses.coupon_info,
  ses.status AS session_status,
  TIMESTAMPDIFF(SECOND, NOW(), ses.end_time) AS remaining_seconds
FROM seats s
LEFT JOIN sessions ses ON s.id = ses.seat_id AND ses.status = 'active'
WHERE s.status = 'idle' OR ses.id IS NOT NULL;

-- 今日统计视图
CREATE OR REPLACE VIEW today_stats_view AS
SELECT
  COUNT(*) AS total_sessions,
  COUNT(CASE WHEN status = 'active' THEN 1 END) AS active_sessions,
  COUNT(CASE WHEN status = 'completed' THEN 1 END) AS completed_sessions,
  SUM(duration_hours + renewal_hours) AS total_hours,
  SUM(CASE WHEN status = 'completed' THEN duration_hours + renewal_hours END) AS completed_hours
FROM sessions
WHERE DATE(created_at) = CURDATE();
