# 反向连接（云端模式）

Droidrun Portal 可以主动向指定主机发起 WebSocket 连接（用于 Mobilerun Cloud）。即使设备在 NAT 后或使用移动网络，也能保持可达。

## 在应用中启用

1. 打开 Portal 应用的**设置**
2. 在**反向连接**下输入主机 URL（ws/wss）
3. 可选：输入令牌（作为 Bearer token 发送）
4. 开启**连接到主机**

## 替代方法（最简单）

在 Portal 主页点击"连接到 Mobilerun"按钮。

---

Mobilerun 默认主机 URL：

```
wss://api.mobilerun.ai/v1/devices/{deviceId}/join
```

`{deviceId}` 占位符会自动替换。

## 设备发送的请求头

连接时，设备会包含以下元数据头：

- `Authorization: Bearer <token>`（如已设置）
- `X-User-ID`
- `X-Device-ID`
- `X-Device-Name`
- `X-Device-Country`
- `X-Remote-Device-Key`（如已配置）

## 命令格式

反向连接使用与本地 WebSocket API 相同的 JSON-RPC 风格消息：

```json
{
  "id": "uuid-or-number",
  "method": "tap",
  "params": { "x": 200, "y": 400 }
}
```

响应包含 `status` 和 `result` 或 `error`。

## 流媒体（WebRTC）

流媒体命令仅在反向连接中支持。

### 服务端 → 设备

- `stream/start`
  - 参数：`width`、`height`、`fps`、`sessionId`、`waitForOffer`、`iceServers`
- `stream/stop`
- `webrtc/answer`（用于设备生成的 offer）
  - 参数：`sdp`
- `webrtc/offer`（当 `waitForOffer=true` 时）
  - 参数：`sdp`、`sessionId`
- `webrtc/ice`
  - 参数：`candidate`、`sdpMid`、`sdpMLineIndex`、`sessionId`

`iceServers` 是包含 `urls` 和可选 `username`/`credential` 的对象数组。

### 设备 → 服务端

- `stream/ready`（采集就绪时发送）
  - 参数：`sessionId`
- `webrtc/offer`（设备生成的 offer）
  - 参数：`sdp`
- `webrtc/ice`
  - 参数：`candidate`、`sdpMid`、`sdpMLineIndex`、`sessionId`
- `stream/error`
  - 参数：`error`、`message`、`sessionId`
- `stream/stopped`
  - 参数：`reason`、`sessionId`

### 流媒体说明

- Android 13+ 需要通知权限才能显示后台推流提示
- 设置中的**自动接受屏幕共享**可自动点击 MediaProjection 对话框
- `stream/start` 在等待权限时可能返回 `prompting_user` 或 `waiting_for_user_notification_tap`

## 通过 ContentProvider 配置（可选）

```bash
adb shell content insert --uri content://com.droidrun.portal/configure_reverse_connection --bind url:s:"wss://api.mobilerun.ai/v1/devices/{deviceId}/join" --bind token:s:"YOUR_TOKEN" --bind enabled:b:true
```
