# Spring Boot 2 后端工程

这是一个标准的 Spring Boot 2 后端项目，使用 Maven 构建，支持 JAR 打包部署。

## 技术栈

- **Spring Boot**: 2.7.18
- **JDK**: 1.8+
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **ORM**: Spring Data JPA (Hibernate)
- **构建工具**: Maven
- **其他**: Lombok, Validation, Jackson

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/example/backend/
│   │   │   ├── BackendApplication.java      # 启动类
│   │   │   ├── config/                      # 配置类
│   │   │   │   ├── RedisConfig.java
│   │   │   │   └── WebConfig.java
│   │   │   ├── controller/                  # 控制器层
│   │   │   │   ├── UserController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── dto/                         # 数据传输对象
│   │   │   │   ├── UserCreateDTO.java
│   │   │   │   ├── UserUpdateDTO.java
│   │   │   │   └── UserResponseDTO.java
│   │   │   ├── entity/                      # 实体类
│   │   │   │   └── User.java
│   │   │   ├── exception/                   # 异常处理
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── repository/                  # 数据访问层
│   │   │   │   └── UserRepository.java
│   │   │   ├── response/                    # 响应封装
│   │   │   │   └── ApiResponse.java
│   │   │   ├── service/                     # 服务层
│   │   │   │   ├── UserService.java
│   │   │   │   └── impl/
│   │   │   │       └── UserServiceImpl.java
│   │   │   └── util/                        # 工具类
│   │   │       ├── CommonUtils.java
│   │   │       └── RedisUtil.java
│   │   └── resources/
│   │       ├── application.yml              # 开发环境配置
│   │       └── application-prod.yml         # 生产环境配置
│   └── test/                                # 测试代码
├── pom.xml                                  # Maven 配置
└── README.md                                # 说明文档
```

## 快速开始

### 1. 环境准备

- JDK 1.8 或更高版本
- Maven 3.6+
- MySQL 8.0
- Redis

### 2. 数据库配置

修改 `src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/backend_db?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### 3. Redis配置

修改 `src/main/resources/application.yml` 中的Redis配置：

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password:
    database: 0
```

### 4. 构建项目

```bash
mvn clean compile
```

### 5. 运行项目

```bash
mvn spring-boot:run
```

或者

```bash
mvn clean package
java -jar target/backend-1.0.0.jar
```

### 6. 验证运行

访问健康检查接口：
```
http://localhost:8080/api/health
```

返回示例：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "UP",
    "timestamp": "2024-01-01T12:00:00",
    "service": "backend",
    "version": "1.0.0"
  },
  "success": true
}
```

## 打包部署

### 开发环境打包

```bash
mvn clean package
```

生成的 JAR 文件在 `target/backend-1.0.0.jar`

### 生产环境打包

```bash
mvn clean package -DskipTests -Pprod
```

### 服务器部署

1. 上传 JAR 文件到服务器
2. 使用环境变量配置数据库和Redis：

```bash
export MYSQL_HOST=your_mysql_host
export MYSQL_PORT=3306
export MYSQL_DB=backend_db
export MYSQL_USER=root
export MYSQL_PASSWORD=your_password
export REDIS_HOST=your_redis_host
export REDIS_PORT=6379
export REDIS_PASSWORD=

java -jar backend-1.0.0.jar --spring.profiles.active=prod
```

### 使用 systemd 服务部署

创建服务文件 `/etc/systemd/system/backend.service`：

```ini
[Unit]
Description=Spring Boot Backend Application
After=syslog.target

[Service]
User=appuser
ExecStart=/usr/bin/java -jar /opt/backend/backend-1.0.0.jar --spring.profiles.active=prod
SuccessExitStatus=143
Environment="MYSQL_HOST=localhost"
Environment="MYSQL_PORT=3306"
Environment="MYSQL_DB=backend_db"
Environment="MYSQL_USER=root"
Environment="MYSQL_PASSWORD=password"
Environment="REDIS_HOST=localhost"
Environment="REDIS_PORT=6379"

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl daemon-reload
sudo systemctl enable backend
sudo systemctl start backend
sudo systemctl status backend
```

## API 接口

### 用户管理

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/users | 创建用户 |
| GET | /api/users/{id} | 根据ID获取用户 |
| GET | /api/users | 分页获取用户列表 |
| PUT | /api/users/{id} | 更新用户 |
| DELETE | /api/users/{id} | 删除用户 |
| GET | /api/users/username/{username} | 根据用户名获取用户 |

### 健康检查

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/health | 健康检查 |
| GET | /api/health/ping | Ping测试 |

## 数据库初始化

执行以下 SQL 创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS backend_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;
```

表结构会自动通过 JPA 的 `ddl-auto: update` 创建。

## 测试

运行单元测试：

```bash
mvn test
```

## 许可证

MIT
