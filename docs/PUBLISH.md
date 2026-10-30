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

```bash
cd kuiklyAppIconIOS
pod lib lint --allow-warnings
KUIKLY_APP_ICON_VERSION=0.1.0 pod trunk push kuiklyAppIconIOS.podspec --allow-warnings
```

前置：源码已推 GitHub 且打了 `0.1.0` tag（podspec source 按 tag 拉取）。

## 4. OHPM（鸿蒙模块）

```bash
export PATH=$PATH:/Applications/DevEco-Studio.app/Contents/tools/ohpm/bin
cd kuiklyAppIconOhos
ohpm login   # ohpm.openharmony.cn 注册的华为账号
ohpm publish
```

官方指引：<https://ohpm.openharmony.cn/#/cn/help/createandpublish>

## 5. 版本约定

- 首版 `0.1.0`（`gradle.properties` 中 `kuiklyAppIcon.version`，改一处全模块生效）
- 鸿蒙产物自动 `-ohos` 后缀，无需手动区分
- 发版前更新 README 平台支持表与 CHANGELOG
