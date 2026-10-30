# kuikly-app-icon-ohos

Kuikly 动态更换 App 图标组件的 HarmonyOS（鸿蒙）原生实现，基于 `bundleManager.setAlternateIcon`（API 26+）。

通常不需要单独依赖本包——Kotlin 侧依赖 [kuiklyAppIcon](https://github.com/zhanghaifei1997/kuikly-app-icon)（KMP 模块）后，宿主鸿蒙工程再引入本模块注册 `KRAppIconModule` 即可。

## 安装

```bash
ohpm install kuikly-app-icon-ohos
```

## 前置条件

1. **设备 / 编译 SDK**：HarmonyOS API 26.0.0+（低于 26 运行时自动降级，所有接口回调 `code=3`，不崩溃）
2. **宿主预置备用图标**（关键）：在 `AppScope/app.json5` 声明，图标资源放 `AppScope/resources/base/media`：

```json5
{
  "app": {
    "alternateIcons": [
      { "name": "summer_theme", "icon": "$media:icon_summer" },
      { "name": "winter_theme", "icon": "$media:icon_winter" }
    ]
  }
}
```

3. **注册 Module**（实现 `IKuiklyViewDelegate` 的类）：

```ts
import { KRAppIconModule } from 'kuikly-app-icon-ohos';

// getCustomRenderModuleCreatorRegisterMap() 中：
map.set(KRAppIconModule.MODULE_NAME, () => new KRAppIconModule());
```

## 限制（官方行为）

- 仅 `bundleType=app` 生效
- 任一时刻最多启用一个备用图标（传空串恢复默认）
- 分身应用不支持设置和查询备用图标

## 统一回调契约

所有接口回调 JSON：`{ success, code, icon?, icons?, error?, supported?, platform?, platformVersion? }`。

| 错误码 | 含义 |
| --- | --- |
| 0 | 成功 |
| 2 | 图标未在 alternateIcons 声明 |
| 3 | 系统版本低于 API 26 |
| 4 | 宿主未配置 alternateIcons |
| 5 | 系统切换失败或其他错误 |

完整 API 与三端集成文档见主仓库：<https://github.com/zhanghaifei1997/kuikly-app-icon>

## License

MIT
