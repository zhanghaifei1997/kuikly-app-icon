# 发布指引

四个模块发布到各自仓库。发布坐标与账号信息已定稿（`io.github.zhanghaifei1997` / GitHub `zhanghaifei1997`），配置在 `gradle.properties` 与各模块 `build.gradle.kts` 中，无需再改。

## 0. 环境要求

- JDK 17（本机 `~/.gradle/gradle.properties` 已全局固定 `org.gradle.java.home`）
- 本仓库可**独立构建**：`settings.gradle.kts` + Gradle wrapper（7.6.3）已就位，`local.properties` 指向本机 Android SDK（不提交）
- iOS：Xcode + CocoaPods（trunk 已注册：2428669833@qq.com / Zhanghaifei）
- 鸿蒙：DevEco Studio；ohpm 用 `/Applications/DevEco-Studio.app/Contents/tools/ohpm/bin/ohpm`

## 1. 本地快速验证（mavenLocal）

```bash
# Android/iOS 产物（独立构建）
./gradlew :kuiklyAppIcon:publish :kuiklyAppIconAndroid:publish

# 鸿蒙产物（官方指引：用 -c settings.ohos.gradle.kts，版本自动带 -ohos 后缀；
# 需先在宿主 KuiklyUI 工程的 ohos 变体 settings 中 include 该模块）
./gradlew -c settings.ohos.gradle.kts :kuiklyAppIcon:publish
```

宿主工程用 `mavenLocal()` 仓库引入验证功能后再走正式发布。

## 2. Maven Central（KMP 模块 + Android 模块）

前置（已完成）：central.sonatype.com 账号（GitHub 登录）、namespace `io.github.zhanghaifei1997` 已验证、GPG 密钥已生成（Key ID `B2EE9DFD`，无口令，`useGpgCmd` 签名）。

`~/.gradle/gradle.properties` 已配置 `signing.keyId`；**填入 User Token**（portal → View Account → Generate User Token）：

```properties
mavenCentralUsername=<token 用户名>
mavenCentralPassword=<token 密码>
```

发布流程（Central Portal API，无需 OSSRH）：

```bash
# 1. 构建 + 签名到暂存目录（产物含 sources/javadoc jar 与 .asc 签名，POM 元数据齐全）
./gradlew publishAllPublicationsToCentralStagingRepository

# 2. 打 bundle zip（Central 上传格式：根级即 maven 仓库布局）
cd kuiklyAppIcon/build/staging-repo && zip -r ../../bundle.zip . && cd ../..
cd kuiklyAppIconAndroid/build/staging-repo && zip -r ../../bundle.zip . && cd ../..

# 3. 上传并自动发布（--publish 会走完校验直接发布）
curl -u "$mavenCentralUsername:$mavenCentralPassword" \
  -X POST "https://central.sonatype.com/api/v1/publisher/upload?publishingType=AUTOMATIC" \
  -H "Content-Type: multipart/form-data" -F "bundle=@kuiklyAppIcon/build/bundle.zip"
# kuiklyAppIconAndroid 同理（或合并为一个 bundle 一次上传）

# 4. 首次发布前：公钥需上传 keyserver（一次性）
gpg --keyserver keyserver.ubuntu.com --send-keys B2EE9DFD
```

> 鸿蒙产物发布时版本会追加 `-ohos` 后缀（官方约定），与 Android/iOS 产物区分。

## 3. CocoaPods（iOS 模块）

> ⚠️ 本机 Xcode 27 的 SDK 最低支持 iOS 15.0，而依赖 `OpenKuiklyIOSRender` 线上全部版本声明 12.0，
> 本机 lint/push 必然编译失败。**用 GitHub Actions 的 macos-15 runner（默认 Xcode 16）发布**：
> 仓库已内置 `.github/workflows/trunk-push.yml`（workflow_dispatch 手动触发，trunk 会话凭据存于
> GitHub Secret `COCOAPODS_TRUNK_NETRC`，从本机 `~/.netrc` 经管道导入，不落任何仓库文件）。

流程：

1. 更新 podspec 版本并提交推送、打对应 tag（trunk 按 tag 拉源码）
2. Actions 页面手动触发 `trunk-push`（或 `gh workflow run trunk-push.yml`）
3. `pod trunk info kuiklyAppIconIOS` 确认版本已收录

注意：podspec 位于**仓库根**（trunk 拉取 tag 后文件模式按仓库根解析，`source_files` 需带
`kuiklyAppIconIOS/` 目录前缀）；本地 `:path` 开发时同样以仓库根为 pod 根，两种方式一致。

## 4. OHPM（鸿蒙模块）

> 实战踩坑记录（2026-10-30 首次发布，全部为注册中心强制校验，缺一项报一项）：

**包内容要求**（均需打包进 HAR 根级，参照官方 `@kuikly-open/render` 布局）：
- `oh-package.json5` 的 `author` 必须是**对象**且带 `email` 或 `url`（纯名字字符串报 400）
- `oh-package.json5` 必须有 `repository`（URL 需以 http/https 开头），建议同补 `homepage`、`keywords`
- 模块目录需包含非空 `LICENSE`、`README.md`、`CHANGELOG.md`（hvigor 打 HAR 时自动收入包根）

**认证要求**：
- 私钥必须 **PEM 格式 + 非空口令**（`ssh-keygen -m PEM`；OpenSSH 格式/空口令均报"private key not supported"）
- 公钥需先上传 ohpm 个人中心；`ohpm config set publish_id <发布码>` + `ohpm config set key_path <私钥路径>`
- 发布时口令为**交互式隐藏输入**（readline setRawMode，管道/expect 伪终端均不可用，必须真实终端）

**构建与发布**：

```bash
# 构建 HAR（宿主 ohosApp 工程根）
export DEVECO_SDK_HOME=/Applications/DevEco-Studio.app/Contents/sdk
cd ~/KuiklyDebug/KuiklyUI/ohosApp
/Applications/DevEco-Studio.app/Contents/tools/hvigor/bin/hvigorw --mode module \
  -p module=kuiklyAppIconOhos@default assembleHar --no-daemon

# 发布（⚠️ PATH 里 DevEco 的 node 必须排最前，否则 ohpm 用错 node 直接崩溃；
#    需在真实终端执行，会提示输入私钥口令）
export PATH=/Applications/DevEco-Studio.app/Contents/tools/node/bin:$PATH:/Applications/DevEco-Studio.app/Contents/tools/ohpm/bin
ohpm publish ~/KuiklyDebug/KuiklyUI/kuiklyAppIconOhos/build/default/outputs/default/kuiklyAppIconOhos.har
```

提交后进入运营审核（个人中心 → 我的包 查看状态）。官方指引：<https://ohpm.openharmony.cn/#/cn/help/createandpublish>

## 5. 版本约定

- 首版 `0.1.0`（`gradle.properties` 中 `kuiklyAppIcon.version`，改一处全模块生效）
- 鸿蒙产物自动 `-ohos` 后缀，无需手动区分
- 发版前更新 README 平台支持表与 CHANGELOG
