# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# LuTu — AI 智能旅行规划应用

## 项目概述

LuTu（旅途）是一个 AI 驱动的智能旅行规划 Web 应用。用户可以创建旅行计划、编排行程（交通/游玩/住宿），通过 AI 聊天助手智能管理行程，浏览/收藏热门旅行攻略，并将自己的计划分享给他人。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| **后端框架** | Spring Boot | 3.5.1 |
| **Java** | | 17 |
| **ORM** | MyBatis-Plus | 3.5.5 |
| **数据库** | MySQL | — |
| **缓存** | Redis (Lettuce) | — |
| **认证** | Sa-Token + JWT | 1.37.0 |
| **AI** | Spring AI + DashScope (通义千问 qwen-plus) | 1.0.0-M6 |
| **AI SDK** | OpenAI Java SDK | 3.5.0 |
| **文件存储** | 阿里云 OSS | SDK 3.17.4 |
| **邮件** | Spring Mail (163.com SMTP SSL 465) | — |
| **API 文档** | Knife4j (OpenAPI 3) | 4.1.0 |
| **限流** | Guava RateLimiter | 32.1.3 |
| **工具库** | Hutool + Lombok | 5.8.38 |
| **构建** | Maven Wrapper | 3.9.14 |
| **前端** | Vue 3 + Vite 5 + Pinia + Vue Router 4 + Axios | — |

## 项目结构

```
LuTu/
├── src/main/java/com/liu/lutu/
│   ├── LuTuApplication.java          # 入口：@SpringBootApplication + @MapperScan
│   ├── config/                         # SecurityConfig, MybatisPlusConfig, SpringAiConfig, MailConfig, OssConfig, ScheduleConfig
│   ├── Interceptor/
│   │   └── UserInterceptor.java        # JWT 校验 + Redis 双层验证，ThreadLocal 存 userId
│   ├── controller/                     # REST API 控制器（11 个）
│   ├── domain/
│   │   ├── po/                         # 实体类（含 Result<T> 统一响应包装）
│   │   ├── dto/                        # 数据传输对象
│   │   ├── vo/                         # 视图对象（含 ShareDetailVO 等聚合 VO）
│   │   ├── emun/                       # 枚举：TravelTypeEnum, MessageTypeEnum, ChatEventTypeEnum
│   │   └── itf/                        # 自定义注解：@RateLimit
│   ├── service/
│   │   ├── I*.java / AiChatservice.java # 服务接口
│   │   └── impl/                       # 服务实现
│   ├── mapper/                         # MyBatis-Plus Mapper（继承 BaseMapper）
│   ├── tools/
│   │   └── TravePlanTool.java          # Spring AI @Tool — AI 通过 function calling 操作行程项
│   ├── memory/
│   │   ├── RedisChatMemory.java        # Spring AI ChatMemory 的 Redis 实现（30 天 TTL）
│   │   ├── RedisMessage.java           # Redis 消息序列化 DTO
│   │   └── MessageUtil.java            # 消息类型转换工具
│   ├── aop/
│   │   └── RateLimitAspect.java        # @RateLimit 切面实现（Guava 令牌桶，ConcurrentHashMap 缓存限流器）
│   ├── util/
│   │   ├── JwtUtil.java                # JWT HS256 创建/解析
│   │   ├── ThreadlocalUtil.java        # ThreadLocal（userId + sessionId）
│   │   ├── EmailUtil.java              # 邮件发送
│   │   ├── SecurityUtils.java          # XSS/SQL 注入过滤、输入清理
│   │   └── TravelType.java             # 旅行风格/预算/节奏常量
│   ├── constants/
│   │   └── constant.java               # AI 系统提示词（12 条行为规则 + 技能定义）
│   └── job/
│       └── ChatHistoryCleanJob.java    # 定时清理 Redis 中的过期/无效聊天历史
├── src/main/resources/
│   ├── application.yml                  # 主配置（端口 8080，占位符引用）
│   └── application-config.yml           # 默认配置值（含凭据，不提交到公共仓库）
├── frontend/                            # Vue 3 + Vite 前端项目
│   ├── src/
│   │   ├── api/                         # API 封装（request.js Axios 实例 + 各模块）
│   │   ├── components/                  # 公共组件
│   │   ├── stores/                      # Pinia 状态管理（手动读写 localStorage）
│   │   ├── utils/                       # 工具函数（idEncoder, security）
│   │   ├── styles/                      # 全局样式（theme.css）
│   │   └── views/                       # 页面组件（12 个视图）
│   ├── vite.config.js                   # Vite 配置（端口 3000，代理 /api 到 8080 并去除 /api 前缀）
│   └── dist/                            # 构建产物
├── nginx.conf                           # Nginx 配置（静态文件 + /api 反向代理到 8080）
└── pom.xml
```

## 数据库表（10 张）

| 表名 | 实体 | 说明 |
|------|------|------|
| `user` | User | 用户（id, username, password, email, avatar, status） |
| `travel_plan` | TravelPlan | 旅行计划（userId, title, startDate, endDate） |
| `travel_plan_item` | TravelPlanItem | 行程项（travelPlanId, itemType, startTime, endTime, fromLocation, toLocation, transportType, amount） |
| `travel_plan_share` | TravelPlanShare | 计划分享（shareCode, title, description, coverImage, tags, viewCount, likeCount） |
| `travel_plan_favorite` | TravelPlanFavorite | 计划收藏（userId, shareId, favoriteType） |
| `travel_user_portrait` | TravelUserPortrait | 用户画像（crowdType, budgetStyle, travelStyle, travelRhythm） |
| `ai_chat_session` | AiChatSession | AI 聊天会话（userId, travelPlanId, title） |
| `ai_chat_message` | AiChatMessage | AI 聊天消息（sessionId, userId, role, content） |
| `destination` | Destination | 目的地（name, description, imageUrl, bestSeason, avgCost, tags） |
| `travel_guide` | TravelGuide | 旅行攻略（title, summary, content, coverImage, destinationId, author） |

## 统一 API 响应格式

所有后端接口返回 `Result<T>` 包装：

```json
{"code": 200, "msg": "操作成功", "data": {...}}
```

- `code=200` 表示成功，`code=500` 或自定义值表示失败
- 前端 `request.js` 响应拦截器检查 `res.code !== 200` 判定业务错误
- 新增/修改资源时，`data` 通常返回操作后的完整对象

## 认证机制

双 Token 体系（Sa-Token + JWT + Redis）：
- **Access Token**：15 分钟有效，Bearer 头传递
- **Refresh Token**：7 天有效（勾选"记住我"则 30 天）
- **Token 旋转**：每次刷新生成新 Token Family，旧 Refresh Token 立即失效
- **重用攻击检测**：检测到过期 Refresh Token 被重用时，清除该用户所有 Token 强制重新登录
- **单设备登录**：新登录清除旧 Token
- **拦截器**：`UserInterceptor` 校验除公开路径外的所有请求（JWT 解析 + Redis 验证双重确认）
- **公开路径**（无需认证）：登录/注册、分享浏览（detail/hot/latest/search）、file/upload、home/**、/error

## `/api` 前缀处理（重要）

项目存在 **双重路径模式**，修改路由时需同时处理：

1. **后端控制器**：路径 **不带** `/api` 前缀（如 `@RequestMapping("/aichat")`）
2. **Vite 开发代理**：前端 `axios baseURL: '/api'`，Vite 代理将 `/api` 前缀去除后转发到后端
3. **Nginx 生产代理**：`proxy_pass http://localhost:8080/`（尾部斜杠去除 `/api/` 前缀）
4. **SecurityConfig**：拦截器排除路径同时列出带 `/api` 和不带 `/api` 两个版本

因此：前端请求 `/api/aichat/stream` → 代理转发的实际路径是 `/aichat/stream`。

## AI 聊天架构（两种模式）

### 模式 1：SSE 流式输出（`POST /aichat/stream`）

- 实现在 `AiChatServiceImpl.chatStream()`
- **不是真正的 LLM 流式输出**：由于 Spring AI M6 流式工具调用存在 bug，先同步调用 LLM（含 function calling 往返），获取完整回复后，再按句子分割模拟 SSE 打字效果
- 使用 `Mono.fromCallable` + `Schedulers.boundedElastic()` 在后台线程执行
- 通过 `ConcurrentHashMap<Long, Boolean> GENERATE_STATUS` 控制取消（单机版，分布式需改用 Redis）
- 支持中途停止（`POST /aichat/stop`）
- 错误处理：特殊处理 `data_inspection_failed`（阿里云内容安全）和 JSON 解析错误

### 模式 2：普通聊天（`GET/POST /aichat/chat`）

- 实现在 `AiChatServiceImpl.chat()`
- 应用了 `@RateLimit(permitsPerSecond=10, timeOut=100)` 限流
- 每次请求注入旅行计划背景信息（开始/结束日期等）和最近 10 条历史消息
- 支持 function calling（`TravePlanTool`）
- 返回完整回复文本

### 共享组件

- **ChatMemory**：`RedisChatMemory` 以 `userId_sessionId` 为 conversationId 存储对话历史，30 天 TTL
- **System Prompt**：定义在 `constant.java` 的 `SYSTEM_ROLE` 常量中，通过 `SpringAiConfig.chatClient()` 注入为默认系统消息
- **Function Calling**：`TravePlanTool` 暴露 4 个 `@Tool` 方法 — `getByTripId`（查询）、`add`（批量新增）、`updateByIds`（批量更新）、`delete`（批量删除）。工具通过 `ThreadlocalUtil.getSessionId()` 解析 travelPlanId

## 线程上下文传递

`ThreadlocalUtil` 存储两个值，跨层传递：
- **userId**：由 `UserInterceptor.preHandle()` 在请求进入时设置，`afterCompletion()` 清理
- **sessionId**：由 `AiChatServiceImpl` 在调用 AI 前设置（`ThreadlocalUtil.setSessionId(sessionId)`），供 `TravePlanTool` 工具方法读取

`TravePlanTool` 不直接接收 userId/travelPlanId 参数，而是从 ThreadLocal 和数据库（AiChatSession）推断，防止 AI 越权操作。

## 限流

自定义 `@RateLimit` 注解 + `RateLimitAspect` 切面：
- 基于 Guava `RateLimiter` 令牌桶
- 限流器按 key（默认方法名）缓存到 `ConcurrentHashMap`
- 超时获取不到令牌时抛出 RuntimeException("访问频繁，请稍后再试")
- 当前仅应用在聊天接口（`GET/POST /aichat/chat`）

## 前端架构

### 路由结构
- `/` — 首页（公开）
- `/login` — 登录（游客专属，已登录自动跳转 `/platform`）
- `/platform` — 主平台（需登录），子路由：`plans`、`ai-chat`、`profile`、`favorites`
- `/travel-plan/:id` — 计划详情（需登录）
- `/share/:code`、`/share-detail/:code`、`/shares` — 分享浏览（公开）
- 旧路由 `/dashboard`、`/ai-chat`、`/travel-plans`、`/profile` 均重定向到 `/platform/*`

### 路由守卫
- 检查 `localStorage.getItem('token')`（不是 Pinia store，因为在路由初始化时 store 可能未就绪）
- 需登录页面无 token 时跳转 `/login`
- 游客页面（guestOnly）有 token 时跳转 `/platform`

### API 层
- `api/request.js`：Axios 实例，baseURL `/api`，30s 超时
- 请求拦截器：自动附加 `Bearer` token + GET 请求加时间戳防缓存 + **重复请求去重**（cancelToken 机制）
- 响应拦截器：业务错误（code≠200）→ rejected Promise；HTTP 401 → 清除用户信息并跳转登录页
- 各业务模块 API 文件：`user.js`、`travel.js`、`aiChat.js`、`share.js`、`favorite.js`、`home.js`、`file.js`、`userPortrait.js`、`travelPlanShare.js`

### 状态管理
- `stores/user.js`：Pinia setup store，手动读写 localStorage（**未使用** pinia-plugin-persistedstate），管理 token 和 userInfo
- 登出时调用 `clearUser()` 清除 localStorage

## 配置管理

- `application.yml`：主配置，使用占位符（`${mysql.host}` 等），通过 `spring.config.import: classpath:application-config.yml` 引入默认值
- `application-config.yml`：包含实际凭据（数据库、Redis、OSS、邮件、AI API key、JWT key），**不应提交到公共仓库**
- `application-config-dev.yml`：开发环境覆盖（通过 `spring.profiles.active: dev` 激活）
- Sa-Token 配置在 `application.yml` 中（token 名 Authorization，前缀 Bearer，JWT 密钥来自占位符）

## 常用命令

```bash
# 启动后端（Spring Boot，端口 8080）
./mvnw spring-boot:run

# 启动前端开发服务器（端口 3000，代理 /api 到 localhost:8080）
cd frontend && npm run dev

# 构建前端
cd frontend && npm run build

# Maven 打包
./mvnw package

# 运行测试
./mvnw test

# 查看 API 文档（Knife4j）
# 启动后端后访问 http://localhost:8080/doc.html
```

## 开发注意事项

1. **配置文件**：`application-config.yml` 包含实际凭据，不应提交到公共仓库。
2. **CORS**：当前 `SecurityConfig` 允许所有来源（`allowedOriginPatterns("*")`），生产环境需收紧。
3. **Redis 依赖**：应用强依赖 Redis — 认证 Token、AI 聊天记忆、邮箱验证码都存储在 Redis 中。本地开发必须先启动 Redis。
4. **Spring AI**：使用 milestone 版本（1.0.0-M6），流式工具调用有已知 bug，当前用同步调用 + 模拟 SSE 绕开。
5. **AI Chat 流式输出**：Nginx 部署时 SSE 端点需配置 `proxy_buffering off`。
6. **前端 Token 管理**：`stores/user.js` 手动读写 localStorage；路由守卫直接读取 `localStorage.getItem('token')`（不是 store），确保 store 和 localStorage 保持一致。
7. **ThreadLocal**：`ThreadlocalUtil` 存储 userId 和 sessionId，拦截器的 `afterCompletion` 负责清理 userId；sessionId 在 AI 工具调用路径中由 service 设置。
8. **请求去重**：前端 `request.js` 使用 Axios CancelToken 防止重复提交，修改请求逻辑时注意不要破坏去重机制。
9. **@RateLimit**：注解基于 Guava 令牌桶，限流器是本地内存的（`ConcurrentHashMap`），多实例部署时需改为分布式限流。
10. **工具调用安全**：`TravePlanTool` 不从 AI 接收 userId/travelPlanId，而是从 ThreadLocal + 数据库推断，这是有意设计的安全边界。
