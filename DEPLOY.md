# 座位计时管理系统 - 部署指南

## 部署架构

```
用户请求 -> Nginx (80端口)
              ├── 静态文件 -> 前端 (Vue 3)
              └── /api/* -> 后端 (Spring Boot:8080)
```

## 一、服务器环境准备

### 1. 安装 Java 17+
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# CentOS/RHEL
sudo yum install java-17-openjdk-devel

# 验证
java -version
```

### 2. 安装 MySQL 8.0+
```bash
# Ubuntu/Debian
sudo apt install mysql-server

# CentOS/RHEL
sudo yum install mysql-server

# 启动并设置开机自启
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 创建数据库
mysql -u root -p
CREATE DATABASE seat_timer_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 安装 Nginx
```bash
# Ubuntu/Debian
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx

# 启动并设置开机自启
sudo systemctl start nginx
sudo systemctl enable nginx
```

### 4. 安装 Node.js 18+
```bash
# 使用 nvm 安装（推荐）
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc
nvm install 18
nvm use 18

# 验证
node -v
npm -v
```

## 二、后端部署

### 1. 修改生产环境配置
编辑 `backend/src/main/resources/application-prod.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/seat_timer_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_db_username      # 修改为你的数据库用户名
    password: your_db_password      # 修改为你的数据库密码

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

logging:
  level:
    root: INFO
    com.example.backend: INFO
```

### 2. 打包后端项目
```bash
cd backend

# 使用 Maven 打包
./mvnw clean package -DskipTests -P prod

# 或者使用 Gradle
./gradlew bootJar -Pprod

# 生成的 jar 文件在 target/ 或 build/libs/ 目录
```

### 3. 部署后端服务
```bash
# 创建应用目录和日志目录
sudo mkdir -p /opt/seat-timer
sudo mkdir -p /var/log/seat-timer
sudo cp target/backend-0.0.1-SNAPSHOT.jar /opt/seat-timer/app.jar

# 设置日志目录权限
sudo chown -R www-data:www-data /var/log/seat-timer
sudo chmod 755 /var/log/seat-timer

# 创建 systemd 服务文件
sudo tee /etc/systemd/system/seat-timer.service << 'EOF'
[Unit]
Description=Seat Timer Application
After=syslog.target network.target mysql.service

[Service]
Type=simple
User=www-data
Group=www-data
WorkingDirectory=/opt/seat-timer

# 启动前清空 app.log（可选：如不需要每次重启清空，请注释下行）
ExecStartPre=/bin/sh -c 'echo "=== Service Restarted at $(date) ===" > /var/log/seat-timer/app.log'

ExecStart=/usr/bin/java -jar -Xms512m -Xmx1024m app.jar --spring.profiles.active=prod
SuccessExitStatus=143
Restart=always
RestartSec=10

# 日志配置
StandardOutput=append:/var/log/seat-timer/app.log
StandardError=append:/var/log/seat-timer/error.log

# 文件描述符限制
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
EOF

# 启动服务
sudo systemctl daemon-reload
sudo systemctl enable seat-timer
sudo systemctl start seat-timer

# 查看状态
sudo systemctl status seat-timer

# 查看实时日志
sudo tail -f /var/log/seat-timer/app.log

# 查看错误日志
sudo tail -f /var/log/seat-timer/error.log
```

### 4. 配置日志轮转（按大小轮转，推荐）
```bash
# 创建 logrotate 配置（按大小轮转，保留5个备份，每个最大50MB）
sudo tee /etc/logrotate.d/seat-timer << 'EOF'
/var/log/seat-timer/*.log {
    # 按大小轮转（50MB）
    size 50M
    rotate 5
    compress
    delaycompress
    missingok
    notifempty
    create 0644 www-data www-data
    
    # 轮转后执行脚本（可选：发送通知等）
    postrotate
        # 可以在这里添加轮转后的操作，如发送邮件通知等
        /bin/kill -HUP $(cat /var/run/syslogd.pid 2> /dev/null) 2> /dev/null || true
    endscript
}
EOF

# 测试 logrotate 配置
sudo logrotate -d /etc/logrotate.d/seat-timer

# 手动执行一次轮转（用于测试）
sudo logrotate -f /etc/logrotate.d/seat-timer
```

**说明：**
- `size 50M` - 当日志达到50MB时自动轮转
- `rotate 5` - 保留5个备份文件
- `compress` - 压缩旧日志（节省空间）
- 这样配置后，日志文件会自动轮转，无需每次重启清空

## 三、前端部署

### 1. 配置生产环境 API 地址
编辑 `mobile-app/.env.production`:

```
# 生产环境使用相对路径，Nginx 会代理到后端
VITE_API_BASE_URL=/api
```

### 2. 构建前端项目
```bash
cd mobile-app

# 安装依赖
npm install

# 生产构建
npm run build

# 生成的静态文件在 dist/ 目录
```

### 3. 部署前端文件
```bash
# 备份旧版本
sudo mv /usr/share/nginx/html /usr/share/nginx/html.backup.$(date +%Y%m%d)

# 部署新版本
sudo cp -r dist/* /usr/share/nginx/html/
sudo chown -R www-data:www-data /usr/share/nginx/html

# 设置权限
sudo chmod -R 755 /usr/share/nginx/html
```

## 四、Nginx 配置

### 1. 复制配置文件
```bash
# 复制配置文件到 Nginx 配置目录
sudo cp nginx/seat-timer.conf /etc/nginx/conf.d/

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo systemctl reload nginx
```

### 2. （可选）配置 HTTPS
如果你有域名和 SSL 证书，编辑 `/etc/nginx/conf.d/seat-timer.conf`，取消 HTTPS server 块的注释并修改证书路径。

使用 Certbot 自动配置 SSL:
```bash
# 安装 Certbot
sudo apt install certbot python3-certbot-nginx

# 获取证书并自动配置
sudo certbot --nginx -d your-domain.com
```

## 五、验证部署

### 1. 检查各服务状态
```bash
# Nginx
sudo systemctl status nginx

# 后端服务
sudo systemctl status seat-timer

# MySQL
sudo systemctl status mysql
```

### 2. 访问测试
- 前端页面：http://your-server-ip/
- API 测试：http://your-server-ip/api/sessions/stats/today

### 3. 查看日志
```bash
# Nginx 访问日志
sudo tail -f /var/log/nginx/access.log

# Nginx 错误日志
sudo tail -f /var/log/nginx/error.log

# 后端应用日志（标准输出）
sudo tail -f /var/log/seat-timer/app.log

# 后端错误日志（标准错误）
sudo tail -f /var/log/seat-timer/error.log

# 查看最近100行应用日志
sudo tail -n 100 /var/log/seat-timer/app.log

# 搜索关键字（如错误信息）
sudo grep "ERROR" /var/log/seat-timer/app.log

# 查看某天所有日志
sudo grep "2026-08-20" /var/log/seat-timer/app.log

# 使用 journalctl（备用方式）
sudo journalctl -u seat-timer -f
```

## 六、更新部署

### 更新后端
```bash
# 上传新 jar 包
sudo cp target/backend-0.0.1-SNAPSHOT.jar /opt/seat-timer/app.jar.new
sudo mv /opt/seat-timer/app.jar.new /opt/seat-timer/app.jar

# 重启服务
sudo systemctl restart seat-timer
```

### 更新前端
```bash
cd mobile-app
npm run build
sudo rm -rf /usr/share/nginx/html/*
sudo cp -r dist/* /usr/share/nginx/html/
sudo chown -R www-data:www-data /usr/share/nginx/html
```

## 七、常见问题

### 1. 前端 404 错误
- 确认 `try_files $uri $uri/ /index.html;` 配置存在
- 确认 index.html 在 `/usr/share/nginx/html/` 目录

### 2. API 请求 502 错误
- 确认后端服务正在运行：`sudo systemctl status seat-timer`
- 确认后端端口 8080 被监听：`sudo netstat -tlnp | grep 8080`
- 检查 Nginx 错误日志

### 3. 跨域错误
- 确认 Nginx 配置中 CORS 头已添加
- 确认后端 `@CrossOrigin` 注解已移除（Nginx 已处理跨域）

### 4. 数据库连接失败
- 确认 MySQL 服务运行中
- 检查 `application-prod.yml` 中的数据库配置
- 确认数据库用户权限：`GRANT ALL PRIVILEGES ON seat_timer_db.* TO 'username'@'localhost';`

## 八、安全建议

1. **防火墙配置**：只开放 80/443 端口
2. **数据库安全**：不要使用 root 用户，创建专用应用用户
3. **定期备份**：配置数据库自动备份
4. **HTTPS**：生产环境必须使用 HTTPS
5. **日志清理**：配置 logrotate 定期清理日志
