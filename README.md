# 途记 LuTu — AI 智能旅行规划应用

🌿 **途记** 是一个 AI 驱动的智能旅行规划 Web 应用。用户可以创建旅行计划、编排行程（交通/游玩/住宿），通过 AI 聊天助手智能管理行程，浏览/收藏热门旅行攻略，并将自己的计划分享给他人。

## ✨ 核心功能

- **AI 智能规划** — 接入通义千问大模型，通过对话式交互自动编排旅行行程，支持增删改查行程环节
- **行程管理** — 可视化管理每日行程（游玩/美食/住宿/交通），自动计算费用
- **攻略分享** — 将旅行计划生成为精美攻略分享给他人，支持浏览/点赞/收藏
- **热门目的地** — 精选目的地推荐，按季节匹配最佳旅行地点
- **用户画像** — 根据旅行偏好（预算/风格/节奏）提供个性化推荐

## 🛠 技术栈

| 层级 | 技术 |
|------|------|
| **后端** | Spring Boot 3.5.1 + Java 17 |
| **ORM** | MyBatis-Plus 3.5.5 |
| **数据库** | MySQL |
| **缓存** | Redis（认证、聊天记忆、邮箱验证码） |
| **认证** | Sa-Token + JWT（双 Token 体系 + Token 旋转 + 重用检测） |
| **AI** | Spring AI 1.0.0-M6 + 阿里云 DashScope（通义千问 qwen-plus） |
| **文件存储** | 阿里云 OSS |
| **API 文档** | Knife4j (OpenAPI 3) |
| **限流** | Guava RateLimiter + AOP |
| **前端** | Vue 3 + Vite 5 + Pinia + Vue Router 4 + Axios |

## 📁 项目结构

```
LuTu/
├── src/main/java/com/liu/lutu/
│   ├── LuTuApplication.java          # 入口
│   ├── config/                         # Spring 配置（Security/MyBatis/AI/Mail/OSS）
│   ├── Interceptor/                    # 认证拦截器（JWT + Redis 双重验证）
│   ├── controller/                     # REST API 控制器
│   ├── domain/
│   │   ├── po/                         # 实体类 + Result<T> 统一响应
│   │   ├── dto/                        # 数据传输对象
│   │   ├── vo/                         # 视图聚合对象
│   │   └── itf/                        # 自定义注解 @RateLimit
│   ├── service/                        # 服务接口 + 实现
│   ├── mapper/                         # MyBatis-Plus Mapper
│   ├── tools/                          # Spring AI @Tool（AI function calling）
│   ├── memory/                         # Redis 聊天记忆实现
│   ├── aop/                            # 切面（限流）
│   ├── util/                           # 工具类（JWT/ThreadLocal/邮件/安全过滤）
│   ├── constants/                      # AI 系统提示词
│   └── job/                            # 定时任务
├── src/main/resources/
│   ├── application.yml                  # 主配置（占位符）
│   └── application-config.yml           # 凭据配置（不提交）
├── frontend/                            # Vue 3 前端
│   ├── src/
│   │   ├── api/                         # Axios 封装 + 业务 API
│   │   ├── components/                  # 公共组件
│   │   ├── stores/                      # Pinia 状态管理
│   │   ├── router/                      # 路由配置 + 守卫
│   │   └── views/                       # 页面（首页/登录/AI聊天/行程/分享…）
│   └── vite.config.js
├── nginx.conf                           # Nginx 生产部署配置
└── pom.xml
```

## 🚀 快速开始

### 环境要求

- **JDK 17+**
- **Maven 3.9+**
- **Node.js 18+**
- **MySQL 8.0+**
- **Redis 6.0+**（必须运行，应用强依赖 Redis）

### 1. 配置

复制配置模板并填入实际凭据：

```bash
# src/main/resources/application-config.yml
```
```yaml
# 数据库
mysql:
  host: 127.0.0.1
  port: 3306
  database: lutu
  username: root
  password: your_password

# Redis
redis:
  host: 127.0.0.1
  port: 6379
  password:
  database: 0
  max-active: 8
  max-idle: 8
  min-idle: 0
  max-wait: -1

# AI（阿里云 DashScope）
ai:
  openapi:
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    api-key: your_dashscope_api_key
    chat:
      options:
        model: qwen-plus

# OSS
oss:
  endpoint: oss-cn-xxx.aliyuncs.com
  access-key-id: your_access_key
  access-key-secret: your_secret
  bucket-name: your_bucket

# Email（163 邮箱 SMTP SSL 465）
email:
  host: smtp.163.com
  port: 465
  username: your_email@163.com
  password: your_smtp_password
  nickname: 途记

# JWT
jwt:
  key: your_jwt_secret_key
```

### 2. 初始化数据库

```sql
CREATE DATABASE IF NOT EXISTS lutu DEFAULT CHARSET utf8mb4;
```

导入 LuTu.sql 这个SQL 脚本。

### 3. 启动后端

```bash
# 开发环境
mvn spring-boot:run

# 指定开发配置
mvn spring-boot:run -Dspring.profiles.active=dev
```

后端运行在 **http://localhost:8080**，API 文档见 **http://localhost:8080/doc.html**

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 **http://localhost:3000**，自动代理 `/api` 到后端 8080。

## 🔧 生产部署

### 构建

```bash
# 后端打包
mvn package -DskipTests

# 前端构建
cd frontend && npm run build
```

### Nginx 配置

```nginx
server {
    listen 80;
    server_name localhost;

    # 前端静态文件
    location / {
        root /path/to/frontend/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理（去除 /api 前缀，SSE 需关闭缓冲）
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_buffering off;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 启动

```bash
# 启动后端
java -jar target/LuTu-0.0.1-SNAPSHOT.jar &

# 启动 Nginx
nginx
```

## 📡 API 概览

所有接口返回统一格式 `Result<T>`：

```json
{"code": 200, "msg": "操作成功", "data": {...}}
```

| 模块 | 路径 | 说明 |
|------|------|------|
| 用户 | `/user` | 邮箱验证码登录、Token 刷新、头像管理 |
| 旅行计划 | `/travelplan` | 计划 CRUD、计划复制 |
| 行程项 | `/travelplanItem` | 行程环节 CRUD（游玩/美食/住宿/交通） |
| AI 聊天 | `/aichat` | SSE 流式聊天 `POST /stream`、普通聊天 `GET/POST /chat`、会话管理 |
| 分享 | `/share` | 创建/浏览/点赞/搜索分享 |
| 收藏 | `/favorite` | 收藏/取消收藏 |
| 首页 | `/home` | 热门目的地、季节推荐、统计数据 |
| 文件 | `/file` | OSS 文件上传 |
| 目的地 | `/destination` | 目的地列表/详情 |
| 攻略 | `/guide` | 攻略浏览 |

> 公开路径（无需认证）：登录/注册、分享浏览、首页数据、文件上传。

## 📝 开发注意事项

1. **配置文件**：`application-config.yml` 包含凭据，**不要提交到公共仓库**
2. **Redis 依赖**：应用强依赖 Redis，本地开发必须先启动 Redis
3. **AI 流式输出**：当前因 Spring AI M6 bug，流式聊天为先同步调用再模拟 SSE，非真正的 LLM 流式输出
4. **Nginx SSE**：部署时需配置 `proxy_buffering off`，否则 SSE 会卡住
5. **限流**：`@RateLimit` 基于本地 Guava 令牌桶，多实例部署需改为分布式限流
6. **CORS**：当前允许所有来源，生产环境需收窄

## 📄 License

MIT
