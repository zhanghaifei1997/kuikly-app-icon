# Kuikly 组件市场上架指引

官方市场（<https://kuikly.tds.qq.com/third-party?tab=component>）的收录方式：向 [Tencent-TDS/KuiklyUI-third-party](https://github.com/Tencent-TDS/KuiklyUI-third-party) 仓库提交 PR，编辑根目录 `KuiklyUI-Libraries.json` 添加组件条目。

## 前置条件（全部完成后再提 PR）

- [ ] 四端产物已发布（Maven / CocoaPods / OHPM，见 PUBLISH.md）
- [ ] 组件源码已推送到你的公开 GitHub 仓库（独立仓库 `zhanghaifei1997/kuikly-app-icon`）
- [ ] README 完整：功能、API、各端预置配置、平台限制
- [ ] LICENSE 就位（MIT）

## PR 内容

### 1. KuiklyUI-Libraries.json 条目草稿

按仓库中同类型条目（参考 gycrosskit 系列的 KuiklyModule 条目）的字段格式添加：

```json
{
  "name": "kuikly-app-icon",
  "type": "KuiklyModule",
  "description": "动态更换 App 图标组件：Kotlin 侧一套 API 切换三端桌面图标（iOS setAlternateIconName / Android activity-alias / 鸿蒙 API26+ bundleManager.setAlternateIcon），支持能力检查、当前图标查询、静默切换，统一回调契约。",
  "platforms": ["Android", "iOS", "Ohos"],
  "repo": "https://github.com/zhanghaifei1997/kuikly-app-icon",
  "owner": "zhanghaifei1997",
  "links": {
    "maven": "https://central.sonatype.com/artifact/io.github.zhanghaifei1997/kuiklyAppIcon",
    "cocoapods": "https://cocoapods.org/pods/kuiklyAppIconIOS",
    "ohpm": "https://ohpm.openharmony.cn/#/cn/detail/kuikly-app-icon-ohos"
  }
}
```

> 提交前先对照仓库内 JSON 的实际字段（不同时期模板字段有增减），与已有条目保持一致。

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
