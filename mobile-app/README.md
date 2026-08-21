# Vue Mobile App

一个基于 Vue 3 + Vite 构建的移动端适配项目。

## 特性

- ⚡️ **Vue 3** - 组合式 API，更好的 TypeScript 支持
- 🚀 **Vite** - 极速的开发构建工具
- 📱 **移动端适配** - 刘海屏、安全区、横屏提示
- 🎨 **响应式设计** - 自适应布局，深色模式支持
- 💪 **TypeScript** - 类型安全的开发体验

## 快速开始

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview
```

## 移动端预览

### 方式一：Chrome DevTools
1. 运行 `npm run dev`
2. 在 Chrome 中按 F12 打开开发者工具
3. 点击左上角设备切换图标（或按 Ctrl+Shift+M）
4. 选择 iPhone 或 Android 设备尺寸预览

### 方式二：局域网访问
1. 运行 `npm run dev`（默认会显示局域网地址如 `http://192.168.x.x:5173`）
2. 确保手机和电脑在同一 WiFi 网络
3. 手机浏览器访问显示的局域网地址

### 方式三：Vite 的 `--host` 参数
```bash
npm run dev -- --host
```

## 项目结构

```
mobile-app/
├── src/
│   ├── App.vue          # 主组件（含移动端 UI）
│   ├── main.ts          # 入口文件
│   ├── style.css        # 全局样式（含移动端适配）
│   └── vite-env.d.ts    # Vite 类型声明
├── index.html           # HTML 模板（含 viewport 配置）
├── vite.config.ts       # Vite 配置
├── tsconfig.json        # TypeScript 配置
└── package.json         # 项目依赖
```

## 移动端适配说明

### Viewport 配置
在 `index.html` 中已配置：
```html
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0, user-scalable=no, viewport-fit=cover">
```

### 安全区适配
支持刘海屏、灵动岛等异形屏幕：
```css
/* 顶部安全区 */
padding-top: env(safe-area-inset-top);

/* 底部安全区 */
padding-bottom: env(safe-area-inset-bottom);
```

### 横屏提示
当设备横屏且高度小于 500px 时，显示提示用户竖屏使用。

## 推荐 VS Code 插件

- [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) - Vue 官方插件
- [TypeScript Vue Plugin](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin)

## 相关文档

- [Vue 3 文档](https://cn.vuejs.org/)
- [Vite 文档](https://cn.vitejs.dev/)
