# 本地 API

Droidrun Portal 通过 HTTP、WebSocket 和 ContentProvider 提供本地控制接口。适用于设备在同一网络或通过 ADB 连接的场景。

## 认证令牌

本地 HTTP 和 WebSocket 访问需要令牌（`GET /ping` 除外）。可从主界面复制或通过 ADB 查询：

```bash
adb shell content query --uri content://com.droidrun.portal/auth_token
```

响应示例：

```json
{"status":"success","result":"YOUR_TOKEN"}
```

## WebSocket（JSON-RPC 风格）

在设置中启用 **WebSocket 服务器**，默认端口 `8081`。

使用令牌连接：

```bash
wscat -c "ws://localhost:8081/?token=YOUR_TOKEN"
```

请求格式：

```json
{
  "id": "uuid-or-number",
  "method": "tap",
  "params": { "x": 200, "y": 400 }
}
```

响应格式：

```json
{
  "id": "uuid-or-number",
  "status": "success",
  "result": "..."
}
```

### 支持的方法

| 方法 | 参数 | 说明 |
| --- | --- | --- |
| `tap` | `x`, `y` | 点击屏幕坐标 |
| `swipe` | `startX`, `startY`, `endX`, `endY`, `duration` | duration 单位毫秒（可选） |
| `global` | `action` | 无障碍全局操作 ID |
| `app` | `package`, `activity` | `activity` 可选 |
| `keyboard/input` | `base64_text`, `clear` | `clear` 默认 `true` |
| `keyboard/clear` | - | 清空焦点输入框 |
| `keyboard/key` | `key_code` | 使用 Android 按键码 |
| `overlay_offset` | `offset` | 垂直偏移（像素） |
| `socket_port` | `port` | 更新 HTTP 服务器端口 |
| `screenshot` | `hideOverlay` | 默认 `true` |
| `packages` | - | 列出可启动应用 |
| `state` | `filter` | 完整状态；`filter=false` 保留小元素 |
| `version` | - | 应用版本 |
| `time` | - | Unix 毫秒时间戳 |
| `wake` | - | 唤醒屏幕 |
| `lock` | - | 锁屏（需要设备管理员权限） |
| `screen/off` | - | 显示黑色覆盖层（模拟息屏） |
| `screen/on` | - | 隐藏黑色覆盖层 |
| `install` | `urls`, `hideOverlay` | 仅 WebSocket；支持分包 APK |

流媒体方法（`stream/start`、`stream/stop`、`webrtc/*`）仅在反向连接中可用。

安装说明：

- 设备必须允许 Droidrun Portal **安装未知应用**
- 在设置中启用**自动确认安装**可自动点击安装确认

### 二进制截图响应

当 `screenshot` 返回二进制数据时，本地 WebSocket 发送二进制帧：

- 前 36 字节：请求 ID 字符串（UUID）
- 剩余字节：PNG 图片数据

如需 JSON 格式，使用 HTTP `/screenshot` 端点或反向连接（会对 PNG 进行 base64 编码）。

## HTTP 服务器

在设置中启用 **HTTP 服务器**，默认端口 `8080`。

认证头：

```
Authorization: Bearer YOUR_TOKEN
```

示例：

```bash
curl -H "Authorization: Bearer YOUR_TOKEN" http://localhost:8080/ping
```

### GET 端点

- `/ping`（无需认证）
- `/a11y_tree`
- `/a11y_tree_full?filter=true|false`
- `/state`
- `/state_full?filter=true|false`
- `/phone_state`
- `/version`
- `/packages`
- `/screenshot?hideOverlay=false`（二进制 PNG）

### POST 端点

POST 请求使用与 WebSocket 相同的方法名（如 `/tap`、`/action/tap`、`/keyboard/input`）。

示例：

```bash
curl -X POST \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "x=200&y=400" \
  http://localhost:8080/tap
```

## ContentProvider（ADB）

所有命令使用 `content://com.droidrun.portal/`。

### 查询

```bash
adb shell content query --uri content://com.droidrun.portal/ping
adb shell content query --uri content://com.droidrun.portal/version
adb shell content query --uri content://com.droidrun.portal/a11y_tree
adb shell content query --uri content://com.droidrun.portal/a11y_tree_full?filter=false
adb shell content query --uri content://com.droidrun.portal/phone_state
adb shell content query --uri content://com.droidrun.portal/state
adb shell content query --uri content://com.droidrun.portal/state_full?filter=false
adb shell content query --uri content://com.droidrun.portal/packages
adb shell content query --uri content://com.droidrun.portal/auth_token
```

### 插入

```bash
adb shell content insert --uri content://com.droidrun.portal/keyboard/input --bind base64_text:s:"SGVsbG8="
adb shell content insert --uri content://com.droidrun.portal/keyboard/clear
adb shell content insert --uri content://com.droidrun.portal/keyboard/key --bind key_code:i:66

adb shell content insert --uri content://com.droidrun.portal/overlay_offset --bind offset:i:100
adb shell content insert --uri content://com.droidrun.portal/overlay_visible --bind visible:b:false
adb shell content insert --uri content://com.droidrun.portal/socket_port --bind port:i:8090

adb shell content insert --uri content://com.droidrun.portal/toggle_websocket_server --bind enabled:b:true --bind port:i:8081

adb shell content insert --uri content://com.droidrun.portal/configure_reverse_connection --bind url:s:"wss://api.mobilerun.ai/v1/devices/{deviceId}/join" --bind token:s:"YOUR_TOKEN" --bind enabled:b:true
adb shell content insert --uri content://com.droidrun.portal/configure_reverse_connection --bind service_key:s:"YOUR_KEY"

adb shell content insert --uri content://com.droidrun.portal/toggle_production_mode --bind enabled:b:true
```
