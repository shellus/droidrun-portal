# WebSocket 事件

Droidrun Portal 包含一个 WebSocket 服务器，可广播设备的实时事件，如通知。

## 设置

### 1. 启用 WebSocket 服务器

打开 Droidrun Portal 应用，在设置中启用 WebSocket 服务器。默认端口为 `8081`。

### 2. 获取认证令牌

本地 WebSocket 访问需要令牌。可从主界面复制或通过 ADB 查询：

```bash
adb shell content query --uri content://com.droidrun.portal/auth_token
```

### 3. 设置 ADB 端口转发

将设备的 WebSocket 端口转发到电脑：

```bash
adb forward tcp:8081 tcp:8081
```

### 4. 连接

使用任意 WebSocket 客户端连接 `ws://localhost:8081` 并传递令牌：

- 查询参数：`ws://localhost:8081/?token=YOUR_TOKEN`
- 或发送 `Authorization: Bearer YOUR_TOKEN` 头

确保已授予**通知访问**权限，并在设置中启用**通知**事件开关。

## 事件格式

所有事件遵循以下结构：

此 WebSocket 也支持 JSON-RPC 风格的命令；详见 [本地 API](local-api.md)。

```json
{
  "type": "EVENT_TYPE",
  "timestamp": 1234567890123,
  "payload": { ... }
}
```

## 事件类型

### PING / PONG

测试连接：

```json
// 发送
{"type": "PING", "timestamp": 123456789}

// 接收
{"type": "PONG", "timestamp": 1234567890123, "payload": "pong"}
```

### NOTIFICATION

当设备上有通知发布或移除时触发：

```json
{
  "type": "NOTIFICATION",
  "timestamp": 1234567890123,
  "payload": {
    "package": "com.example.app",
    "title": "新消息",
    "text": "你有一条新消息",
    "id": 12345,
    "tag": "",
    "is_ongoing": false,
    "post_time": 1234567890000,
    "key": "0|com.example.app|12345|null|10001"
  }
}
```

当通知被移除时，payload 包含 `removed` 标志：

```json
{
  "type": "NOTIFICATION",
  "timestamp": 1234567890123,
  "payload": {
    "package": "com.example.app",
    "id": 12345,
    "key": "0|com.example.app|12345|null|10001",
    "removed": true
  }
}
```

## Python 示例

使用附带的测试脚本连接并监听事件：

```bash
# 安装依赖
pip install websockets

# 运行测试脚本（自动设置 ADB 转发）
python test_websocket.py 8081 YOUR_TOKEN

# 或通过环境变量设置令牌
PORTAL_TOKEN=YOUR_TOKEN python test_websocket.py

# 或指定自定义端口
python test_websocket.py 8082 YOUR_TOKEN
```

示例输出：

```
Setting up ADB forward tcp:8081 -> tcp:8081...
✅ ADB forward established on port 8081
Connecting to ws://localhost:8081/?token=YOUR_TOKEN...
✅ Connected successfully!

Sending PING...
Waiting for response...
Received: {"type":"PONG","timestamp":1234567890123,"payload":"pong"}
✅ PING/PONG Test Passed

==================================================
Listening for events (Ctrl+C to stop)...
Trigger a notification on your phone to see it here!
==================================================

[NOTIFICATION] 1234567890123
  package: com.whatsapp
  title: John
  text: Hey, how are you?
  id: 12345
  is_ongoing: false
```

## 最简 Python 客户端

```python
import asyncio
import websockets
import json

async def listen():
    async with websockets.connect("ws://localhost:8081/?token=YOUR_TOKEN") as ws:
        while True:
            event = json.loads(await ws.recv())
            print(f"[{event['type']}] {event.get('payload', {})}")

asyncio.run(listen())
```

## 故障排除

| 问题 | 解决方案 |
|------|----------|
| 连接被拒绝 | 确保应用正在运行且设置中已启用 WebSocket 服务器 |
| 未收到事件 | 检查是否已为 Droidrun Portal 授予通知监听权限 |
| ADB 转发失败 | 通过 `adb devices` 确认设备已连接 |
