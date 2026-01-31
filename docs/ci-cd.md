# CI/CD 自动构建

本项目使用 GitHub Actions 实现自动构建和发布。

## 工作流程

### 1. PR 测试构建

**触发条件**：Pull Request 创建或更新

**文件**：`.github/workflows/pr-test-build.yml`

**流程**：
1. 检出 PR 代码
2. 配置 JDK 17
3. 解码签名密钥
4. 构建 Release APK
5. 上传 APK 到 Artifacts（保留 30 天）
6. 在 PR 中评论下载链接

### 2. 正式发布构建

**触发条件**：推送 `v*` 格式的 Tag（如 `v1.0.0`）

**文件**：`.github/workflows/build-and-release.yml`

**流程**：
1. 检出代码
2. 配置 JDK 17
3. 解码签名密钥
4. 从 Tag 提取版本号构建 Release APK
5. 创建 GitHub Release 并上传 APK

## APK 签名配置

所有构建使用统一的 Release 签名，确保 APK 可以覆盖安装。

### 必需的 GitHub Secrets

在仓库 **Settings → Secrets and variables → Actions** 中配置：

| Secret 名称 | 说明 |
|------------|------|
| `KEYSTORE_BASE64` | Keystore 文件的 Base64 编码 |
| `KEYSTORE_PASSWORD` | Keystore 密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 |

### 生成 Keystore

使用 Docker 生成（无需本地安装 Java）：

```bash
# 使用 Docker JDK 镜像生成 keystore
docker run --rm -v $(pwd):/work -w /work eclipse-temurin:17-jdk \
  keytool -genkey -v -keystore release.keystore \
  -alias your-alias -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass your-password -keypass your-password \
  -dname "CN=YourApp, OU=YourOrg, O=YourOrg, L=City, ST=State, C=CN"

# 转换为 base64
base64 -w 0 release.keystore
```

## 发布新版本

```bash
# 创建并推送 tag
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions 会自动构建并创建 Release。

## 本地编译

项目根目录下的 `release.keystore` 用于本地编译（已在 .gitignore 中忽略）。

```bash
# 设置环境变量
export KEYSTORE_FILE=$(pwd)/release.keystore
export KEYSTORE_PASSWORD=droidrun123
export KEY_ALIAS=droidrun
export KEY_PASSWORD=droidrun123

# 编译 Release APK
./gradlew assembleRelease
```

APK 输出路径：`app/build/outputs/apk/release/app-release.apk`
