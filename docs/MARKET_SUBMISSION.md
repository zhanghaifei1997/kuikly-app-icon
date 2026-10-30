# Kuikly 组件市场上架指引

官方市场（<https://kuikly.tds.qq.com/third-party?tab=component>）的收录方式：向 [Tencent-TDS/KuiklyUI-third-party](https://github.com/Tencent-TDS/KuiklyUI-third-party) 仓库提交 PR，编辑根目录 `KuiklyUI-Libraries.json` 添加组件条目。

## 前置条件（全部完成后再提 PR）

- [ ] 四端产物已发布（Maven / CocoaPods / OHPM，见 PUBLISH.md）
- [ ] 组件源码已推送到你的公开 GitHub 仓库（独立仓库 `zhanghaifei1997/kuikly-app-icon`）
- [ ] README 完整：功能、API、各端预置配置、平台限制
- [ ] LICENSE 就位（MIT）

## PR 内容

### 1. KuiklyUI-Libraries.json 条目（2026-10-30 实际提交的格式）

⚠️ 提交前务必以仓库内现有条目为准（历史文档曾使用 name/type/platforms 等字段，实际格式是
componentName/componentType/githubUrl/developer + 平台布尔值）。0.1.0 实际提交的条目：

```json
{
  "githubUrl": "https://github.com/zhanghaifei1997/kuikly-app-icon",
  "examples": ["https://github.com/zhanghaifei1997/kuikly-app-icon/tree/main/demo"],
  "componentName": "kuikly-app-icon",
  "componentDescription": "Kuikly 动态更换 App 图标组件：Kotlin 侧一套统一 API（setIcon / getCurrentIcon / getSupportedIcons / isSupported）切换 Android（activity-alias）/ iOS（setAlternateIconName）/ 鸿蒙 API 26+（bundleManager.setAlternateIcon）三端桌面图标，图标由宿主预置声明，统一 JSON 回调契约与错误码，支持 iOS 静默切换。产物已发布 Maven Central / CocoaPods / OHPM。",
  "componentType": "KuiklyModule",
  "developer": "zhanghaifei1997",
  "Android": true,
  "iOS": true,
  "Ohos": true,
  "sDynamic": false
}
```

已提交 PR：<https://github.com/Tencent-TDS/KuiklyUI-third-party/pull/47>（fork 分支
`zhanghaifei1997: add-kuikly-app-icon`）

### 2. PR 说明模板

```markdown
### 组件名称
kuikly-app-icon

### 组件类型
KuiklyModule

### 功能说明
在 Kuikly(Kotlin) 侧用一套 API 动态更换 App 桌面图标：
- iOS 10.3+（CFBundleAlternateIcons + setAlternateIconName，支持静默切换）
- Android（activity-alias 启停，约定命名 .icon.<name>）
- 鸿蒙 API 26+（app.json5 alternateIcons + bundleManager.setAlternateIcon）
- 统一回调契约（success/code/icon/icons/error），支持能力检查与版本兼容

### 支持平台
Android / iOS / Ohos

### 产物地址
- Maven: <链接>
- CocoaPods: kuiklyAppIconIOS
- OHPM: kuikly-app-icon-ohos

### 演示
demo/ 目录含演示页 AppIconDemoPage 与三端宿主预置配置示例
```

### 3. 流程

1. Fork `Tencent-TDS/KuiklyUI-third-party`
2. 按 README 模板编辑 `KuiklyUI-Libraries.json` 提交 PR（一个组件一个 PR）
3. 等待官方审核合并后，组件会出现在官网组件市场
4. 组件需求/维护讨论：官方 GitHub Discussion「社区生态」版块

## 上架后的维护

- 版本更新：先发新产物，再更新 JSON 条目中的版本/链接信息（PR）
- 破坏性 API 变更在 description 中注明迁移说明
- Kuikly 框架大版本升级后及时回归验证（官方更新日志：<https://kuikly.tds.qq.com/ChangeLog/>）
