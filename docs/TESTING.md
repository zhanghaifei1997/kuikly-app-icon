# 本地测试指引（三端）

测试目标：验证 `kuiklyAppIcon`（KMP Module）+ 三端原生实现（`kuiklyAppIconAndroid` / `kuiklyAppIconIOS` / `kuiklyAppIconOhos`）在真实宿主工程中编译、注册、切换图标全链路正常。

推荐路线：**用官方 KuiklyUI 源码仓库做宿主工程**（四端宿主 androidApp / iosApp / ohosApp 已全部接好渲染器与适配器，避免从零接宿主）。也可用 Android Studio Kuikly 插件的模板工程，接线方式相同。

---

## 〇、环境准备（一次性）

| 项 | 要求 | 说明 |
| --- | --- | --- |
| JDK | 17 | AS: Settings → Build Tools → Gradle → Gradle JDK 选 17 |
| Android Studio | 2024.2.1+ | 装两个插件（Settings → Plugins → Marketplace）：**Kotlin Multiplatform**、**Kuikly**（鸿蒙工程需 Kuikly 插件 ≥ 1.1.0） |
| Xcode + CocoaPods | iOS 测试需要（仅 Mac） | Xcode 27+ 需把各库 iOS Deployment Target 调到 15.0、macOS 调到 12.0 |
| DevEco Studio | 鸿蒙测试需要 | **必须装 API 26 SDK**（Tools → SDK Manager 勾选），模拟器镜像也选 API 26+，否则动态图标接口不存在 |
| 真机/模拟器 | Android 任意；iOS 模拟器即可；鸿蒙需 API 26+ 设备 | iOS 模拟器桌面图标切换同样生效 |

获取宿主工程：

```bash
git clone https://github.com/Tencent-TDS/KuiklyUI.git kuikly-host
cd kuikly-host
```

---

## 一、组件接线（三端共用，做一次）

1. 把组件的 4 个模块目录拷入宿主工程根目录：
   `kuiklyAppIcon/`、`kuiklyAppIconAndroid/`、`kuiklyAppIconIOS/`、`kuiklyAppIconOhos/`
2. `settings.gradle.kts` 添加：
   ```kotlin
   include(":kuiklyAppIcon")
   include(":kuiklyAppIconAndroid")
   ```
   （鸿蒙侧编译另需 `settings.ohos.gradle.kts` 加 `include(":kuiklyAppIcon")`）
3. 业务模块（宿主的 `shared` 或 `demo` 模块）`build.gradle.kts` 的 commonMain 依赖：
   ```kotlin
   sourceSets {
       val commonMain by getting {
           dependencies {
               implementation(project(":kuiklyAppIcon"))
           }
       }
   }
   ```
4. 把组件 `demo/pages/AppIconDemoPage.kt` 拷入宿主 shared 模块的 pages 目录（任意包路径均可，`@Page("AppIconDemoPage")` 由 KSP 自动注册）。
5. 宿主工程 `gradle.properties` 确认有 `kuiklyVersion=<宿主使用的版本>`（KuiklyUI 仓库根目录已自带，保持一致即可）。
6. Android Studio 打开工程，等 Gradle Sync 通过（此步已验证 Kotlin 跨端 Module 编译无误）。

---

## 二、Android 端测试

**步骤 1｜依赖与注册（Android Studio）**

- `androidApp/build.gradle.kts` 添加 `implementation(project(":kuiklyAppIconAndroid"))`
- 找到宿主实现 `KuiklyRenderViewBaseDelegatorDelegate` 的类（KuiklyUI 仓库在 `androidApp/.../ContextCodeHandler.kt`），在 `registerExternalModule` 中追加：
  ```kotlin
  moduleExport(KRAppIconModule.MODULE_NAME) { KRAppIconModule() }
  ```

**步骤 2｜准备图标资源**

给 3 套图标（default / summer_theme / winter_theme），用肉眼易区分的纯色图即可：androidApp `res` 右键 → New → Image Asset → 生成 `ic_launcher_default` / `ic_launcher_summer` / `ic_launcher_winter` 到 mipmap。

**步骤 3｜Manifest 改造**

照抄组件 `demo/host-config/Android-Manifest.snippet`：
- 真实启动 Activity（模板的 Kuikly 承载 Activity）**移除 MAIN/LAUNCHER intent-filter**
- 添加 `.icon.default`（enabled=true）、`.icon.summer_theme`、`.icon.winter_theme`（enabled=false）三个 activity-alias，targetActivity 指向真实 Activity

**步骤 4｜打开测试页**

把宿主加载的初始 pageName 改为 `AppIconDemoPage`（即创建 `KuiklyRenderView` / 打开 KuiklyRenderActivity 时传入的页面名）。

**步骤 5｜运行验证**

Run `androidApp` 到模拟器/真机：
1. 页面顶部显示"当前平台支持动态换图标（android API xx）"、当前图标 = 默认图标
2. 列表出现 summer_theme / winter_theme → 点击"切换到：summer_theme"
3. 按 Home 回桌面 → 图标变为 summer 主题（部分 ROM 桌面刷新延迟数秒，或锁屏再解锁）
4. 点"恢复默认图标"→ 回桌面确认还原

预期异常路径：把 Manifest 里别名改名（破坏约定）→ 切换回调 `code=4`（配置缺失）；切换未配置的名字 → `code=2`。

---

## 三、iOS 端测试（Mac）

**步骤 1｜CocoaPods 依赖**

`iosApp/Podfile` 添加：

```ruby
pod 'kuiklyAppIconIOS', :path => '../kuiklyAppIconIOS'
```

终端在 iosApp 目录执行 `pod install --repo-update`。

**步骤 2｜图标资源**

`iosApp/Assets.xcassets` 中新建两个图片集 `icon_summer` / `icon_winter`（1024x1024 PNG，随意纯色图）。

**步骤 3｜Info.plist**

按 `demo/host-config/iOS-Info.plist.snippet` 添加 `CFBundleAlternateIcons`（key 与图标名一致：summer_theme / winter_theme）。

**步骤 4｜打开测试页 + 运行**

Xcode 打开 `iosApp.xcworkspace`，把宿主 `KuiklyRenderViewController` 加载的 pageName 改为 `AppIconDemoPage`，选 iPhone 模拟器 Run。

**步骤 5｜验证**

1. 点"切换到：summer_theme"→ **弹出系统提示框"您已更改此App的图标"** → 确认
2. 回主屏幕 → 图标已变（模拟器同样生效）
3. 在 demo 页临时把 `setIcon` 换成 `setIconSilent` 再试 → **无弹窗**直接切换（私有 API 生效）
4. 恢复默认 → 图标还原

---

## 四、鸿蒙端测试

**步骤 1｜依赖（DevEco Studio）**

- `ohosApp/entry/oh-package.json5`：
  ```json5
  "dependencies": {
    "kuikly-app-icon-ohos": "file:../../kuiklyAppIconOhos"
  }
  ```
- 根目录 `build-profile.json5` 的 `modules` 数组登记：`{ "name": "kuiklyAppIconOhos", "srcPath": "../kuiklyAppIconOhos" }`
- 右上角 Sync Now（或 entry 目录执行 `ohpm install`）

**步骤 2｜注册 Module**

找到宿主 `IKuiklyViewDelegate` 实现类（`getCustomRenderModuleCreatorRegisterMap`），追加：

```ts
import { KRAppIconModule } from 'kuikly-app-icon-ohos';
// map.set(KRAppIconModule.MODULE_NAME, () => new KRAppIconModule());
```

**步骤 3｜预置备用图标**

- `AppScope/resources/base/media` 放 `icon_summer.png` / `icon_winter.png`
- `AppScope/app.json5` 按 `demo/host-config/ohos-app.json5.snippet` 添加 `alternateIcons`

**步骤 4｜签名**

DevEco: File → Project Structure → Signing Configs → 勾选 **Automatically generate signature**（需华为开发者账号登录）。

**步骤 5｜设备与运行**

- 模拟器：Tools → Device Manager → 新建模拟器 → **系统镜像选 API 26+**
- 真机：需 HarmonyOS 版本对应 API 26+，开发者模式 + USB 连接
- 把宿主初始 pageName（`Kuikly()` 组件的 pageName 参数）改为 `AppIconDemoPage`，Run `entry`

**步骤 6｜验证**

1. API 26+ 设备：点"切换到：summer_theme"→ 回桌面图标变化（鸿蒙无系统弹窗）；恢复默认生效
2. 低版本设备/模拟器（可选反向验证）：页面顶部显示"不支持"（`isSupported=false`，`code=3`），不崩溃

---

## 五、验收清单

| # | 用例 | 预期 |
| --- | --- | --- |
| 1 | 三端 Gradle/pod/ohpm 编译 | 全部通过 |
| 2 | `isSupported` | 三端 true；H5/低版本鸿蒙 false |
| 3 | `getSupportedIcons` | 返回预置图标名（Android 含 default 的除外） |
| 4 | `setIcon(name)` | 三端桌面图标切换；iOS 有系统弹窗 |
| 5 | `setIcon("")` | 三端恢复默认 |
| 6 | `setIconSilent` | iOS 无弹窗；Android/鸿蒙与 setIcon 一致 |
| 7 | `setIcon("未配置名")` | 回调 code=2 |
| 8 | 拆除宿主预置配置再调用 | code=4 |
| 9 | Android 双图标检查 | 真实 Activity 移除 LAUNCHER 后无双图标 |

## 六、排障

见 [INTEGRATION.md](INTEGRATION.md) 的常见问题表（图标不变 / 双图标 / code 码含义 / 鸿蒙低版本处理）。错误码定义见 [README.md](../README.md)。

---

## 七、实测记录（2026-10-22，本机三端调试结论）

以官方 KuiklyUI 仓库为宿主完成实测，以下为实战踩坑与结论，按图索骥可少走弯路：

### Android ✅ 全部通过（Medium_Phone 模拟器 / API 37）

**真机复测（2026-10-30，OPPO PJZ110）：✅ 通过**——切换 / 互切 / 持久化 / 恢复默认 / 单图标全链路正常。环境坑：宿主 Gradle 7.6.3 只支持 Java 8-19，Android Studio 默认 JBR 21 报 "Incompatible Gradle JVM version"，在宿主 gradle.properties 固定 `org.gradle.java.home` 为本机 JDK 17 解决（连带消除 "prepareKotlinBuildScriptModel not found" 同步失败）。

| 用例 | 结果 |
| --- | --- |
| isSupported | ✅ "android API 37" |
| getSupportedIcons | ✅ [summer_theme, winter_theme]（activity-alias 自动发现） |
| setIcon("summer_theme") | ✅ 桌面图标变为测试橙色图标（系统级生效，探针验证组件启停正确） |
| setIcon("") | ✅ 桌面图标恢复默认 |
| getCurrentIcon | ✅ 反映真实启停状态 |

坑与修复（已同步进组件源）：
1. `cocoapods {}` 块**顶层不能写 baseName**（只能写在 framework{} 里），否则 gradle 脚本编译失败
2. KMP 模块被含 js/macos target 的宿主依赖时，需补齐 `js(IR)` + `macosX64()/macosArm64()` target
3. kuikly 的 `JSONArray.optString(index)` 返回 **String?**（与 org.json 不同），需判空
4. Android 的 `PackageInfo.activities` 是平台可空类型，需 `?.` 链
5. `vforIndex` 是 `com.tencent.kuikly.core.directives` 包的**扩展函数，必须显式 import**
6. 宿主 demo 模块同时是 KMP 模块时，Module 的 `moduleName()` 覆写必须存在（`Module` 为 abstract）
7. adb `input tap` 注入的点击事件对 Kuikly 自绘视图不可靠（系统 UI 正常）——自动化验证用 `setTimeout` 自动触发代替；人工手点正常

### iOS ✅ 全部通过（iPhone 17 Pro 模拟器 / iOS 27.1）

**真机复测（2026-10-30，iPhone iOS 26.2）：✅ 通过**——修复坑 5（plist 规范结构 + 松散 PNG）后，切换 / 系统弹窗 / 持久化 / 恢复默认全链路正常。

| 用例 | 结果 |
| --- | --- |
| isSupported | ✅ ios + 系统版本 |
| setIcon("summer_theme") | ✅ 冷启动 getCurrentIcon 读到 summer_theme（`UIApplication.alternateIconName` 系统持久化） |
| setIcon("") | ✅ 恢复默认（冷启动复核） |

坑与修复：
1. **Xcode 27** 要求所有库 Deployment Target ≥ 15.0 —— Podfile post_install 统一 `IPHONEOS_DEPLOYMENT_TARGET='15.0'`
2. pod install 卡在 libpag 从 GitHub 下载二进制（网络断连时）—— 用本地 podspec 把 source 指到 `https://gh-proxy.com/...` 镜像
3. `xcodebuild` 目的地匹配歧义时，用 `xcrun simctl list devices booted` 拿 UDID，`-destination "id=<UDID>"` 精确指定
4. 无头验证切换是否真正生效的最权威判据：**去掉自动切换逻辑后冷启动读 getCurrentIcon**，系统持久化的 alternateIconName 不会骗人
5. **真机 code=5 "feature is not supported"**（2026-10-30，iPhone 真机 iOS 26.2）：`CFBundleAlternateIcons` 写在 plist **顶层**时模拟器侥幸通过，真机 UIKit 只认规范嵌套 `CFBundleIcons → CFBundleAlternateIcons`，且备用图标需以**松散 PNG 打进包根**（仅放 Assets.xcassets 普通 imageset 真机不可寻址）。修复：宿主 Info.plist 改规范结构 + `icon_*.png` 作为 bundle 资源 + 组件读取逻辑兼容两种 plist 位置（KRAppIconModule.m 的 `alternateIconsConfig`），示例同步更新 `demo/host-config/iOS-Info.plist.snippet`

### 鸿蒙 ✅ 真机核心功能验证通过（附一项与组件无关的已知问题）

**实测环境**：华为畅享 90 Pro Max，HarmonyOS 7.0.0.109（**API 26**），DevEco 6.1.1（本地 SDK API 24）

**本地 API 24 SDK 编译 API 26 代码的三个补丁**（真机调试的关键解锁，均在调试现场 `~/KuiklyDebug` 完成）：
1. 给 SDK 的 `@ohos.bundle.bundleManager.d.ts` 追加 `setAlternateIcon`/`getAlternateIcons`/`AlternateIconInfo` 前向声明（编译期脚手架，运行时调真机真实接口）
2. `app.json5` 的 schema 有**两份**：`sdk/default/openharmony/toolchains/modulecheck/app.json` 与 `sdk/default/hms/toolchains/modulecheck/app.json`——HarmonyOS 工程走 **hms** 那份，两份的 `propertyNames.enum` 都要加 `alternateIcons`
3. 上游 `CrashReport.ets` 断点用桩文件修复（放 `entry/src/main/ets/ts-api/provider.ets`，注意 `../../ts-api` 解析到 `ets/ts-api` 不是 `src/main/ts-api`；ArkTS 桩必须用 class 实现，对象字面量违反 `arkts-no-untyped-obj-literals`）

**签名**（真机必须）：DevEco 打开 ohosApp → File → Project Structure → Signing Configs → Automatically generate signature（华为账号）→ hvigor 产出 `entry-default-signed.hap` → `hdc install`。

**真机验证结果**：

| 用例 | 结果 |
| --- | --- |
| isSupported | ✅ "ohos api26"（deviceInfo 守卫正确放行） |
| getSupportedIcons | ✅ [summer_theme, winter_theme]（真实读取 app.json5 alternateIcons） |
| getCurrentIcon | ✅ 默认图标 |
| setIcon("summer_theme") | ✅ **桌面图标真实变为橙色**（截图为证：ohos_home.jpeg） |
| 恢复默认 | ⚠️ 未及复测（见下） |

**已知问题 → 已解决（2026-10-22 深挖修复）**：切换成功后应用崩溃循环，根因与组件无关，是**宿主工程的产物配套问题（两层）**：

1. **ABI 混搭**：仓库预编译的 `libshared.so`（旧版 KRRenderCValue 按结构体指针编组）与本机新编译的 `libkuikly.so`（新版 uint64 单字 ABI）不配套。症状：Kotlin/Native `getByteArray` NPE → SIGABRT。
2. **结构偏移错位**：重建 libshared.so 后 ExportedSymbols 新增 2046 个导出条目（新页面符号），`initKuikly` 偏移后移；而 `napi_init.cpp` 引用的旧头文件按旧偏移取函数指针 → 调错函数 → SetCallKotlin 未注册 → C++ 侧 `callKotlin_ == nullptr` 断言。

**修复方法**（完整命令链保留在调试现场 `~/KuiklyDebug/KuiklyUI`）：
1. 用官方脚本流程重建 libshared.so：gradle 8.0（腾讯镜像下载）+ `KUIKLY_KOTLIN_VERSION=2.0.21-KBA-010`（腾讯定制 Kotlin/Native 工具链，自动落入 ~/.konan）+ ohos 变体 settings（注意 ohos settings 强制各模块使用 `build.2.0.ohos.gradle.kts`——组件模块需自建该文件并接入 ohos settings 与 demo 的 commonMain 依赖）
2. 将新 `libshared.so` 拷入 `ohosApp/entry/libs/arm64-v8a/`，并让 `napi_init.cpp` 包含**同一次构建产出**的 `libshared_api.h`（等价官方脚本第 26-29 行的拷贝步骤；两者偏移必须严格配套）

**修复后全流程复验（华为畅享 90 Pro Max / HarmonyOS 7.0.0.109 / API 26）**：

| 用例 | 结果 |
| --- | --- |
| 冷启动稳定性 | ✅ 进程稳定存活，崩溃循环消失 |
| setIcon("summer_theme") | ✅ 应用内状态更新 + **进程不退出**（setAlternateIcon 不重启应用） |
| 持久化 | ✅ 杀进程重启后 getCurrentIcon 读到 summer_theme（系统级持久化） |
| setIcon("") | ✅ 恢复默认图标，同一进程全程存活 |

> 附加结论：鸿蒙的 setAlternateIcon **不会杀进程**（与 Android activity-alias 行为不同），切换体验更优。降级路径（API<26 设备回 code=3）由 deviceInfo 守卫保证，无需设备实测即可推断。

> 组件的 API 26 编译声明：ArkTS 无法对命名空间做编译期安全的动态探测（`arkts-no-ns-as-obj`），最终实现为直接调用 + `deviceInfo.sdkApiVersion >= 26` 运行时守卫；在 **API 26 SDK** 下开箱即编译，在 API 24 SDK 下需 d.ts 前向声明补丁（或直接升级 SDK）。组件作为 Kuikly 扩展库发布时需按官方指引用 ohos 变体构建脚本（build.2.0.ohos 模式）编译，参考 `docs/PUBLISH.md`。

**2026-10-30 真机回归发现并修复（✅ 修复后复测通过：winter/summer 互切、恢复默认、杀进程持久化、进程稳定全链路正常）**：经 Kuikly 桥真实调用 `setIcon` 报 code=2 / 系统错误 17700308 "alternateIconName must match the name field under alternateIcons in the app.json5 file"。根因：**ArkTS 模块把 `{"iconName":"xxx"}` 原始 JSON 串直接传给了 `bundleManager.setAlternateIcon`**（Android 端 `JSONObject.optString("iconName")`、iOS 端 `dict[@"iconName"]` 均有解析，鸿蒙端遗漏）。10-22 的验证走的是绕桥直调路径，未暴露此缺陷；且当时只实际切换过 summer_theme。修复：`KRAppIconModule.ets` 增加 `parseIconName` 解析（JSON 值为空串/非 JSON 返回空串，保持恢复默认语义）。教训：**三端原生模块的参数解析必须逐端实现并逐端经真实桥接路径验证**。

### 其他实战记录

- GitHub 直连间歇性阻断时：`git clone https://gh-proxy.com/https://github.com/Tencent-TDS/KuiklyUI.git` 镜像可用
- libpag pod 卡 GitHub 下载时：本地 podspec 把 source 指向 `https://gh-proxy.com/...` 镜像
- 宿主工程位置：`~/KuiklyDebug/KuiklyUI`（本机调试现场，含全部接线改动，可直接复跑）
