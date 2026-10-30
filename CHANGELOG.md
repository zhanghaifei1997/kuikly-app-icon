# Changelog

## 0.1.0 (2026-10-30)

首个发布版本：Kuikly 动态更换 App 图标组件。

### 功能

- KMP 统一 API：`setIcon` / `setIconSilent` / `getCurrentIcon` / `getSupportedIcons` / `isSupported`
- Android：activity-alias 方案（约定命名 `<applicationId>.icon.<name>`），切换不杀进程
- iOS 10.3+：`CFBundleIcons` 规范嵌套声明 + 包内松散 PNG；`setIconSilent` 私有 API 静默切换（自动回退公开 API）
- 鸿蒙 API 26+：`app.json5` `alternateIcons` + `bundleManager.setAlternateIcon`，低版本自动降级
- 统一 JSON 回调契约与错误码（0-5），详见 README

### 测试

- 三端真机全链路验证通过：iPhone（iOS 26.2）/ 华为畅享 90 Pro Max（HarmonyOS API 26）/ OPPO PJZ110
- 验证项：能力检查 / 图标枚举 / 切换 / 互切 / 杀进程持久化 / 恢复默认 / 进程存活 / 单图标

### 真机测试期间发现并修复的缺陷

- iOS：宿主 Info.plist 必须使用规范 `CFBundleIcons → CFBundleAlternateIcons` 嵌套结构（顶层写法模拟器宽松、真机报 code=5 "feature is not supported"）；组件读取逻辑已兼容两种位置
- 鸿蒙：修复 ArkTS 模块未解析 `{"iconName":"xxx"}` JSON 参数导致 `setAlternateIcon` 报 17700308 的问题（Android/iOS 端原有解析不受影响）
