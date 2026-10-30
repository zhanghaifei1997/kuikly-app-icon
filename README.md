# kuikly-app-icon

Kuikly 动态更换 App 图标组件 —— 在 Kuikly（Kotlin）侧一行代码切换 Android / iOS / 鸿蒙三端的桌面应用图标。

| 平台 | 系统要求 | 原生方案 | 限制 |
| --- | --- | --- | --- |
| iOS | 10.3+ | `UIApplication.setAlternateIconName` + Info.plist `CFBundleAlternateIcons` | 图标必须预置包内；切换默认有系统弹窗（可静默，见下） |
| Android | 全版本 | `activity-alias` + `PackageManager.setComponentEnabledSetting` | 图标必须预置；桌面刷新有延迟 |
| 鸿蒙 | API 26.0.0+ | `bundleManager.setAlternateIcon` + `app.json5` `alternateIcons` | 仅 bundleType=app；同时只能启用一个；分身应用不支持 |
| H5 / 小程序 | — | 不支持（`isSupported` 返回 false） | — |

## 快速使用

```kotlin
// Pager 中注册 Module
override fun createExternalModules(): Map<String, Module>? =
    mapOf(KuiklyAppIconModule.MODULE_NAME to KuiklyAppIconModule())

// 获取并调用
val module = acquireModule<KuiklyAppIconModule>(KuiklyAppIconModule.MODULE_NAME)

module.isSupported { r ->            // 平台能力检查
    val result = AppIconResult.from(r)
}
module.getSupportedIcons { r -> }    // 宿主预置的图标名列表
module.getCurrentIcon { r -> }       // 当前图标名（默认图标为空串）
module.setIcon("summer_theme") { r -> }  // 切换；传 "" 恢复默认
module.setIconSilent("summer_theme") { r -> } // iOS 静默切换（见下）
```

## API

### Kotlin 侧（`com.kuikly.appicon.KuiklyAppIconModule`）

| 方法 | 说明 |
| --- | --- |
| `setIcon(name, callback)` | 切换到指定备用图标；`name` 传空串恢复默认图标 |
| `setIconSilent(name, callback)` | 静默切换。仅 iOS 有差异（不弹系统提示框），其余端与 `setIcon` 一致 |
| `getCurrentIcon(callback)` | 当前启用的图标名；默认图标返回空串 |
| `getSupportedIcons(callback)` | 宿主预置的全部备用图标名（不含默认图标） |
| `isSupported(callback)` | 当前平台/系统是否支持，附带 platform / platformVersion |

### 统一回调契约（`AppIconResult` 封装）

```json
{ "success": true, "code": 0, "icon": "summer_theme", "icons": ["summer_theme", "winter_theme"],
  "error": "...", "supported": true, "platform": "ios", "platformVersion": "18.0" }
```

| 错误码 | 含义 |
| --- | --- |
| 0 | 成功 |
| 1 | 平台不支持（H5 / 小程序） |
| 2 | 图标不存在（未在宿主预置配置中声明） |
| 3 | 系统版本过低（iOS < 10.3 / 鸿蒙 < API 26） |
| 4 | 宿主预置配置缺失（Info.plist / Manifest / app.json5） |
| 5 | 系统切换失败或其他错误 |

## 关于 iOS 静默切换（setIconSilent）

iOS 公开 API `setAlternateIconName` 切换时会弹系统提示框。`setIconSilent` 通过私有 API
`_setAlternateIconName:isDiscardable:completionHandler:` 绕过弹窗，运行时检测不可用会自动回退公开 API。

> ⚠️ 私有 API 存在 App Store 审核风险，请自行评估。默认建议使用 `setIcon`。

## 工程结构

| 模块 | 说明 | 产物 |
| --- | --- | --- |
| `kuiklyAppIcon/` | KMP 跨端 Module（业务直接依赖） | Maven（Android/iOS 一次 + `-ohos` 后缀一次） |
| `kuiklyAppIconAndroid/` | Android 原生实现（activity-alias 方案） | Maven |
| `kuiklyAppIconIOS/` | iOS 原生实现（OC，双模式切换） | CocoaPods |
| `kuiklyAppIconOhos/` | 鸿蒙原生实现（ArkTS，bundleManager） | OHPM HAR |
| `demo/` | 演示页面 `AppIconDemoPage` + 三端宿主预置配置示例 | — |

## 宿主预置配置（关键前置条件）

三端的图标资源都必须**打包预置**在宿主工程中，切换只是启用已声明的图标：

- iOS：Info.plist `CFBundleAlternateIcons` → 见 `demo/host-config/iOS-Info.plist.snippet`
- Android：Manifest `activity-alias`（约定命名 `<applicationId>.icon.<name>`）→ 见 `demo/host-config/Android-Manifest.snippet`
- 鸿蒙：`AppScope/app.json5` 的 `alternateIcons` → 见 `demo/host-config/ohos-app.json5.snippet`

三端配置中的**图标名保持一致**（如 `summer_theme`），业务侧即可用同一个 name 无差别切换。

详细集成步骤见 [docs/INTEGRATION.md](docs/INTEGRATION.md)，发布与上架见 [docs/PUBLISH.md](docs/PUBLISH.md) 与 [docs/MARKET_SUBMISSION.md](docs/MARKET_SUBMISSION.md)。

## License

MIT
