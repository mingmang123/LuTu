# 途记前端 - Vue 3 项目

## 项目简介
途记旅行规划助手的前端应用，使用 Vue 3 + Vite + Pinia 构建。

## 技术栈
- Vue 3 - 渐进式 JavaScript 框架
- Vue Router - 官方路由管理器
- Pinia - 状态管理库
- Axios - HTTP 客户端
- Vite - 构建工具

## 项目结构
```
frontend/
├── public/              # 静态资源
├── src/
│   ├── api/            # API 接口封装
│   │   ├── request.js  # Axios 请求配置
│   │   ├── user.js     # 用户相关接口
│   │   ├── travel.js   # 行程相关接口
│   │   └── aiChat.js   # AI 对话接口
│   ├── components/     # 公共组件
│   │   └── AppHeader.vue
│   ├── router/         # 路由配置
│   │   └── index.js
│   ├── stores/         # Pinia 状态管理
│   │   └── user.js
│   ├── views/          # 页面视图
│   │   ├── LoginView.vue
│   │   ├── DashboardView.vue
│   │   ├── AiChatView.vue
│   │   ├── TravelPlansView.vue
│   │   └── TravelPlanDetailView.vue
│   ├── App.vue         # 根组件
│   └── main.js         # 入口文件
├── index.html
├── package.json
└── vite.config.js
```

## 功能模块
- **登录页** - 用户登录认证
- **首页** - 功能导航入口
- **AI 助手** - 智能旅行规划对话
- **行程管理** - 旅行计划 CRUD
- **行程详情** - 行程项目详细管理

## 开发环境配置

### 安装依赖
```bash
cd frontend
npm install
```

### 启动开发服务器
```bash
npm run dev
```
开发服务器默认运行在 http://localhost:3000

### 构建生产环境
```bash
npm run build
```
构建产物将输出到 `dist` 目录

## 部署说明

### 1. 构建项目
```bash
cd frontend
npm install
npm run build
```

### 2. 部署到 Nginx
将 `dist` 目录复制到 Nginx 的 html 目录下：
```bash
# Windows
xcopy /E /I dist ..\nginx-1.26.3\html\dist

# 或手动复制 dist 文件夹到 nginx-1.26.3/html/ 目录下
```

### 3. 配置 Nginx
确保 nginx.conf 已配置好 Vue 路由支持：
```nginx
location / {
    root   html/dist;
    index  index.html index.htm;
    try_files $uri $uri/ /index.html;  # 关键：支持 Vue Router
}
```

### 4. 启动服务
1. 启动后端服务（端口 8080）
2. 启动 Nginx
3. 访问 http://localhost

## API 代理配置
开发环境下，Vite 配置已设置代理：
```javascript
// vite.config.js
server: {
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      rewrite: (path) => path.replace(/^\/api/, '')
    }
  }
}
```

生产环境由 Nginx 反向代理处理。

## 注意事项
1. 确保后端服务运行在 8080 端口
2. Nginx 配置中的 `try_files` 指令是 Vue Router 正常工作的关键
3. 静态资源已配置长期缓存（1年）
