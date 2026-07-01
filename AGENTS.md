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
│   ├── controller/                     # REST API 控制器（10 个）
│   │   ├── UserController.java         # /user — 登录/注册/Token/头像
│   │   ├── TravelPlanController.java   # /travelplan — 旅行计划 CRUD
│   │   ├── TravelItemController.java   # /travelplanItem — 行程项 CRUD（交通/游玩/住宿）
│   │   ├── AiChatController.java       # /aichat — AI 聊天（SSE 流式 + 普通）
│   │   ├── AiChatSessionController.java # /aichatsession — 会话管理
│   │   ├── AiChatMessageController.java # /aichatmessage — 消息管理
│   │   ├── TravelPlanShareController.java # /share — 计划分享/浏览/点赞/搜索
│   │   ├── TravelPlanFavoriteController.java # /favorite — 收藏/克隆计划
│   │   ├── TravelUserPortraitController.java # /traveluserportrait — 用户画像
│   │   ├── HomeController.java         # /home — 首页聚合数据
│   │   └── FileController.java         # /file — OSS 文件上传
│   ├── domain/
│   │   ├── po/                         # 实体类（10 个，@TableName 映射 MySQL 表）
│   │   ├── dto/                        # 数据传输对象
│   │   ├── vo/                         # 视图对象（含 ShareDetailVO 等聚合 VO）
│   │   ├── emun/                       # 枚举：TravelTypeEnum, MessageTypeEnum, ChatEventTypeEnum
│   │   └── itf/                        # 自定义注解：@RateLimit
│   ├── service/
│   │   ├── I*.java                     # 服务接口（10 个）
│   │   └── impl/                       # 服务实现（10 个）
│   ├── mapper/                         # MyBatis-Plus Mapper（10 个，继承 BaseMapper）
│   ├── tools/
│   │   └── TravePlanTool.java          # Spring AI @Tool — AI 可通过 function calling 操作行程项
│   ├── memory/
│   │   ├── RedisChatMemory.java        # Spring AI ChatMemory 的 Redis 实现（30 天 TTL）
│   │   ├── RedisMessage.java           # Redis 消息序列化 DTO
│   │   └── MessageUtil.java            # 消息类型转换工具
│   ├── aop/
│   │   └── RateLimitAspect.java        # @RateLimit 切面实现（Guava 令牌桶）
│   ├── util/
│   │   ├── JwtUtil.java                # JWT HS256 创建/解析
│   │   ├── ThreadlocalUtil.java        # ThreadLocal（userId + sessionId）
│   │   ├── EmailUtil.java              # 邮件发送
│   │   ├── SecurityUtils.java          # XSS/SQL 注入过滤、输入清理
│   │   └── TravelType.java             # 旅行风格/预算/节奏常量
│   ├── constants/
│   │   └── constant.java               # AI 系统提示词（12 条行为规则）
│   └── job/
│       └── ChatHistoryCleanJob.java    # 定时清理 Redis 中的过期/无效聊天历史
├── src/main/resources/
│   ├── application.yml                  # 主配置（端口 8080，占位符引用）
│   ├── application-config.yml           # 默认配置值（含完整数据库/AI/OSS/邮件凭据）
│   └── application-config-dev.yml       # 开发环境覆盖配置
├── frontend/                            # Vue 3 + Vite 前端项目
│   ├── src/
│   │   ├── App.vue
│   │   ├── api/                         # API 封装（request.js Axios 实例 + 各模块）
│   │   ├── components/                  # 公共组件
│   │   ├── stores/                      # Pinia 状态管理（user.js token 管理）
│   │   ├── utils/                       # 工具函数
│   │   └── views/                       # 页面组件
│   ├── vite.config.js                   # Vite 配置（端口 3000，代理 /api 到 8080）
│   └── dist/                            # 构建产物
├── nginx-1.26.3/                        # Nginx 服务器
├── nginx.conf                           # Nginx 配置（前端静态文件 + /api 反向代理到 8080）
├── pom.xml                              # Maven POM
└── db/                                  # 数据库脚本目录
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

## 认证机制

双 Token 体系（Sa-Token + JWT + Redis）：
- **Access Token**：15 分钟有效，Bearer 头传递
- **Refresh Token**：7 天有效（勾选"记住我"则 30 天）
- **Token 旋转**：每次刷新生成新 Token Family，旧 Refresh Token 立即失效
- **重用攻击检测**：检测到过期 Refresh Token 被重用时，清除该用户所有 Token 强制重新登录
- **单设备登录**：新登录清除旧 Token
- **拦截器**：`UserInterceptor` 校验除公开路径外的所有请求（JWT 解析 + Redis 验证双重确认）

## AI 聊天架构

- **模型**：阿里云 DashScope 通义千问 qwen-plus（兼容 OpenAI 格式）
- **聊天记忆**：`RedisChatMemory` 实现 Spring AI `ChatMemory` 接口，Redis List 存储，Key 前缀 `CHAT:`，30 天 TTL，读写时自动续期
- **Function Calling**：`TravePlanTool` 暴露 4 个 `@Tool` 方法（查询/添加/更新/删除行程项），AI 可直接操作旅行计划
- **流式输出**：SSE（Server-Sent Events），支持字符级模拟打字效果
- **系统提示词**：定义在 `constant.java` 中，包含 12 条行为规则（角色定位、信息收集、行程编排、时间合理性、预算管理、地点验证等）

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

# 查看 API 文档（Knife4j）
# 启动后访问 http://localhost:8080/doc.html
```

## 开发注意事项

1. **配置文件**：`application-config.yml` 包含实际凭据，不应提交到公共仓库。开发环境使用 `application-config-dev.yml` 覆盖。
2. **CORS**：当前 `SecurityConfig` 允许所有来源（`allowedOrigins("*")`），生产环境需收紧。
3. **Redis 依赖**：应用强依赖 Redis — 认证 Token、AI 聊天记忆、邮箱验证码、分享计数都存储在 Redis 中。
4. **Spring AI**：使用 milestone 版本（1.0.0-M6），API 可能在未来版本变更。
5. **AI Chat 流式输出**：SSE 端点可能受网关/代理缓冲影响，Nginx 需配置 `proxy_buffering off`。
6. **前端 Token 管理**：`stores/user.js` 使用 Pinia + `persistedstate` 持久化 Token，`api/request.js` 拦截器自动附加 Token 并处理 401。
7. **ThreadLocal**：`ThreadlocalUtil` 存储 userId 和 sessionId，用于跨层传递用户上下文（拦截器 → 控制器 → 服务 → AI Tool）。
