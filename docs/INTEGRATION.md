# 宿主工程集成指引

按官方《Kuikly扩展库创建指引》的组织方式，本组件需要集成到 Kuikly 模板工程（Android Studio Kuikly Project Template）中。核心原则：**三端图标资源全部预置在宿主工程，组件只负责运行时切换**。三端预置配置中的图标名保持一致（下文统一用 `summer_theme` / `winter_theme` 示例）。

## 1. 把组件模块加入工程

```kotlin
// settings.gradle.kts
include(":kuiklyAppIcon")
include(":kuiklyAppIconAndroid")

// settings.ohos.gradle.kts（鸿蒙侧编译）
include(":kuiklyAppIcon")

// shared 模块 commonMain 依赖（业务代码从这里 import KuiklyAppIconModule）
sourceSets {
    val commonMain by getting {
        dependencies {
            implementation(project(":kuiklyAppIcon"))
        }
    }
}
```

## 2. Android 宿主

**a. 依赖与注册**

```kotlin
// androidApp build.gradle.kts
implementation(project(":kuiklyAppIconAndroid"))
```

```kotlin
// 实现 KuiklyRenderViewBaseDelegatorDelegate 的类中
override fun registerExternalModule(kuiklyRenderExport: IKuiklyRenderExport) {
    super.registerExternalModule(kuiklyRenderExport)
    with(kuiklyRenderExport) {
        moduleExport(KRAppIconModule.MODULE_NAME) { KRAppIconModule() }
    }
}
```

**b. Manifest 预置 activity-alias**

完整示例见 `demo/host-config/Android-Manifest.snippet`，要点：

- 所有图标入口统一用别名，命名 `<applicationId>.icon.<name>`
- `.icon.default`（默认图标）初始 `enabled="true"`，其余初始 `enabled="false"`
- 真实启动 Activity 移除 MAIN/LAUNCHER intent-filter（避免双图标）
- 每个别名的 `android:icon` 指向预置的 mipmap 图标资源

**c. 混淆**

模块自带 consumer-rules.pro，无需额外配置。

## 3. iOS 宿主

**a. Podfile**

```ruby
pod 'kuiklyAppIconIOS', :path => '../kuiklyAppIconIOS'
```

KMP 框架（`kuiklyAppIcon`）由 cocoapods 插件自动生成 podspec，随 KMP 依赖引入。iOS 侧 Module 由框架运行时按类名 `KRAppIconModule` 创建，**无需手动注册**。

**b. Info.plist 预置备用图标**

完整示例见 `demo/host-config/iOS-Info.plist.snippet`：`CFBundleAlternateIcons` 字典，key 为图标名，对应 Assets.xcassets 中的图标。

**c. 图标规格**：1024x1024 PNG（Apple 会自动裁切各尺寸），或按官方多尺寸规范提供。

## 4. 鸿蒙宿主

**a. 依赖**

```json5
// ohosApp/oh-package.json5
"dependencies": {
  "kuikly-app-icon-ohos": "file:../../kuiklyAppIconOhos"
}
// 根目录 build-profile.json5 的 modules 中登记
{ "name": "kuiklyAppIconOhos", "srcPath": "../kuiklyAppIconOhos" }
```

**b. 注册 Module**

```ts
export class KuiklyViewDelegate extends IKuiklyViewDelegate {
  getCustomRenderModuleCreatorRegisterMap(): Map<string, KRRenderModuleExportCreator> {
    const map: Map<string, KRRenderModuleExportCreator> = new Map();
    map.set(KRAppIconModule.MODULE_NAME, () => new KRAppIconModule());
    return map;
  }
}
```

**c. app.json5 预置 alternateIcons**（API 26.0.0+）

完整示例见 `demo/host-config/ohos-app.json5.snippet`。注意仅 bundleType=app 生效、分身应用不支持。

## 5. 验证

1. 集成完成后运行 demo 页面：各端宿主加载 `AppIconDemoPage`（页面名 `AppIconDemoPage`）
2. 页面会依次执行 isSupported / getCurrentIcon / getSupportedIcons，点击列表项切换图标
3. 验证清单：
   - [ ] 切换后回到桌面确认图标变化（Android 桌面刷新有延迟属正常）
   - [ ] iOS 首次切换出现系统提示框（setIconSilent 不出现）
   - [ ] 恢复默认图标后各端回到默认外观
   - [ ] 鸿蒙 API 26 以下真机 isSupported 返回 false 且不崩溃

## 常见问题

| 现象 | 原因与处理 |
| --- | --- |
| Android 切换后图标未变 | 桌面刷新延迟；或别名未按 `<applicationId>.icon.<name>` 命名，检查 Manifest |
| Android 出现双图标 | 真实 Activity 仍带 LAUNCHER filter，移除后重装 |
| iOS 回调 code=2 | 图标名未在 CFBundleAlternateIcons 声明，核对 Info.plist |
| iOS 回调 code=4 | CFBundleAlternateIcons 未配置 |
| 鸿蒙 code=3 | 设备低于 API 26，用 isSupported 提前判断并隐藏入口 |
| 鸿蒙 code=4 | app.json5 未配置 alternateIcons |
