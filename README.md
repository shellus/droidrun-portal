<picture>
  <source media="(prefers-color-scheme: dark)" srcset="./static/droidrun-dark.png">
  <source media="(prefers-color-scheme: light)" srcset="./static/droidrun.png">
  <img src="./static/droidrun.png"  width="full">
</picture>

[![GitHub stars](https://img.shields.io/github/stars/droidrun/droidrun-portal?style=social)](https://github.com/droidrun/droidrun-portal/stargazers)
[![Discord](https://img.shields.io/discord/1360219330318696488?color=7289DA&label=Discord&logo=discord&logoColor=white)](https://discord.gg/ZZbKEZZkwK)
[![Documentation](https://img.shields.io/badge/Documentation-📕-blue)](https://docs.droidrun.ai)
[![Twitter Follow](https://img.shields.io/twitter/follow/droid_run?style=social)](https://x.com/droid_run)

<a href="https://github.com/droidrun/droidrun-portal/releases" target="_blank">
    <img src="https://raw.githubusercontent.com/Kunzisoft/Github-badge/main/get-it-on-github.png" alt="Get it on GitHub" style="width:200px;height:auto;">
</a>

## 概述

Droidrun Portal 是一个 Android 无障碍服务应用，提供屏幕 UI 元素的实时可视化反馈和数据采集。它创建一个交互式覆盖层，高亮显示可点击、可勾选、可编辑、可滚动和可聚焦的元素，是 UI 测试、自动化开发和无障碍评估的实用工具。

## 功能特性

- 交互式覆盖层，高亮显示可点击、可勾选、可编辑、可滚动和可聚焦的元素
- 本地控制 API（HTTP 服务器、WebSocket JSON-RPC、ContentProvider）
- 反向 WebSocket 连接，支持云端控制
- WebRTC 屏幕推流，支持自动接受
- 从 URL 安装 APK（支持分包 APK），可选自动确认
- 通知事件推送，支持按事件类型开关

## 使用方法

### 初始设置

1. 在 Android 设备上安装应用
2. 在系统设置 → 无障碍 → Droidrun Portal 中启用无障碍服务
3. 根据提示授予悬浮窗权限
4. （可选）在应用内打开**设置**，启用本地服务器或反向连接

### 认证令牌（本地 API）

Droidrun Portal 为 HTTP 和 WebSocket 访问生成本地认证令牌。

- 在应用内：从主界面复制令牌
- 通过 ADB：
  ```bash
  adb shell content query --uri content://com.droidrun.portal/auth_token
  ```

### 本地 API

Droidrun Portal 提供三种本地接口：

- HTTP 服务器（默认端口 8080）
- WebSocket 服务器（默认端口 8081）
- ContentProvider（ADB 命令）

详见 [本地 API 文档](docs/local-api.md)。

### WebSocket 事件

在设置中启用后，Droidrun Portal 通过 WebSocket 推送通知事件。

详见 [WebSocket 事件文档](docs/websocket-events.md)。

### 反向连接（云端模式）

启用反向连接后，设备会主动向指定主机发起 WebSocket 连接（用于 Mobilerun Cloud）。

详见 [反向连接文档](docs/reverse-connection.md)。

### ADB 命令（ContentProvider）

所有命令使用 ContentProvider authority `content://com.droidrun.portal/`。

#### 查询命令（读取数据）

```bash
# 测试连接（ping）
adb shell content query --uri content://com.droidrun.portal/ping

# 获取应用版本
adb shell content query --uri content://com.droidrun.portal/version

# 获取无障碍树 JSON（可见元素及覆盖层索引）
adb shell content query --uri content://com.droidrun.portal/a11y_tree

# 获取完整无障碍树（包含所有属性）
adb shell content query --uri content://com.droidrun.portal/a11y_tree_full

# 获取完整树，不过滤小元素（< 1% 可见度）
adb shell content query --uri 'content://com.droidrun.portal/a11y_tree_full?filter=false'

# 获取手机状态 JSON（当前应用、焦点元素、键盘可见性）
adb shell content query --uri content://com.droidrun.portal/phone_state

# 获取组合状态（无障碍树 + 手机状态）
adb shell content query --uri content://com.droidrun.portal/state

# 获取完整组合状态（完整树 + 手机状态 + 设备上下文）
adb shell content query --uri content://com.droidrun.portal/state_full

# 获取完整状态，不过滤
adb shell content query --uri 'content://com.droidrun.portal/state_full?filter=false'

# 获取已安装可启动应用列表
adb shell content query --uri content://com.droidrun.portal/packages

# 获取本地认证令牌
adb shell content query --uri content://com.droidrun.portal/auth_token
```

#### 插入命令（操作与配置）

```bash
# 键盘文本输入（base64 编码，默认先清空输入框）
adb shell content insert --uri content://com.droidrun.portal/keyboard/input --bind base64_text:s:"SGVsbG8gV29ybGQ="

# 键盘文本输入，不清空输入框
adb shell content insert --uri content://com.droidrun.portal/keyboard/input --bind base64_text:s:"SGVsbG8=" --bind clear:b:false

# 清空当前焦点输入框的文本
adb shell content insert --uri content://com.droidrun.portal/keyboard/clear

# 发送按键事件（如 Enter = 66，Backspace = 67）
adb shell content insert --uri content://com.droidrun.portal/keyboard/key --bind key_code:i:66

# 设置覆盖层垂直偏移（像素）
adb shell content insert --uri content://com.droidrun.portal/overlay_offset --bind offset:i:100

# 切换覆盖层可见性
adb shell content insert --uri content://com.droidrun.portal/overlay_visible --bind visible:b:true
adb shell content insert --uri content://com.droidrun.portal/overlay_visible --bind visible:b:false
```

```bash
# 配置 HTTP 服务器端口（默认 8080）
adb shell content insert --uri content://com.droidrun.portal/socket_port --bind port:i:8090

# 启用/禁用本地 WebSocket 服务器（默认端口 8081）
adb shell content insert --uri content://com.droidrun.portal/toggle_websocket_server --bind enabled:b:true --bind port:i:8081

# 配置反向连接（主机 URL + 可选令牌/服务密钥）
adb shell content insert --uri content://com.droidrun.portal/configure_reverse_connection --bind url:s:"wss://api.mobilerun.ai/v1/devices/{deviceId}/join" --bind token:s:"YOUR_TOKEN" --bind enabled:b:true
adb shell content insert --uri content://com.droidrun.portal/configure_reverse_connection --bind service_key:s:"YOUR_KEY"

# 切换生产模式 UI
adb shell content insert --uri content://com.droidrun.portal/toggle_production_mode --bind enabled:b:true
```

#### 常用按键码

| 按键 | 码值 | 按键 | 码值 |
|-----|------|-----|------|
| Enter | 66 | Backspace | 67 |
| Tab | 61 | Escape | 111 |
| Home | 3 | Back | 4 |
| Up | 19 | Down | 20 |
| Left | 21 | Right | 22 |

### 数据输出

元素数据通过 ContentProvider 查询以 JSON 格式返回。响应包含 status 字段和请求的数据。所有响应遵循以下结构：

```json
{
  "status": "success",
  "result": "..."
}
```

错误响应：
```json
{
  "status": "error",
  "error": "错误信息"
}
```

## 技术细节

- 最低 Android API 级别：30（Android 11.0）
- 使用 Android 无障碍服务 API
- 通过 Window Manager 实现自定义绘制覆盖层
- 支持多窗口环境
- 使用 Kotlin 开发

## 持续集成

本项目使用 GitHub Actions 实现自动构建和发布。

### 自动构建

每次推送到 main 分支或创建 Pull Request 都会触发构建工作流：
- 构建 Android 应用
- 生成 APK
- 将 APK 上传为 GitHub Actions 构件
