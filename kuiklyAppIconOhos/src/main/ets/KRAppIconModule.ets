import { bundleManager } from '@kit.AbilityKit';
import { BusinessError } from '@kit.BasicServicesKit';
import deviceInfo from '@ohos.deviceInfo';
import { KuiklyRenderBaseModule, KuiklyRenderCallback, KRAny } from '@kuikly-open/render';

const CODE_OK = 0;
const CODE_ICON_NOT_FOUND = 2;
const CODE_SYSTEM_VERSION_TOO_LOW = 3;
const CODE_HOST_CONFIG_MISSING = 4;
const CODE_SYSTEM_REJECT = 5;

// alternateIcons 动态图标自 API 26 起提供（官方文档：配置应用图标和名称）
const REQUIRED_API_VERSION = 26;

/**
 * Kuikly 动态更换 App 图标 Module（鸿蒙侧实现）。
 *
 * 依赖宿主工程在 AppScope/app.json5 的 alternateIcons 标签中预置备用图标（API 26+）：
 * {
 *   "app": {
 *     "alternateIcons": [
 *       { "name": "summer_theme", "icon": "$media:summer_icon" }
 *     ]
 *   }
 * }
 *
 * 运行时通过 bundleManager.setAlternateIcon(name) 切换，空串恢复默认图标；
 * bundleManager.getAlternateIcons() 查询列表（iconName/iconId/enabled）。
 *
 * 编译要求：compileSdkVersion >= API 26（alternateIcons 接口自 API 26 起出现在 SDK 中）。
 * 低版本设备运行时自动降级：所有接口回调 code=3，不会崩溃。
 */
export class KRAppIconModule extends KuiklyRenderBaseModule {

  static readonly MODULE_NAME: string = 'KRAppIconModule';

  // bundleManager 接口为 Promise 异步，模块走异步模式（运行在 UI 线程）
  syncMode(): boolean {
    return false;
  }

  call(method: string, params: KRAny, callback: KuiklyRenderCallback | null): KRAny {
    switch (method) {
      case 'setIcon':
      case 'setIconSilent': // 鸿蒙切换无系统弹窗，静默与普通切换行为一致
        this.setIcon(this.parseIconName(params), callback);
        return null;
      case 'getCurrentIcon':
        this.getCurrentIcon(callback);
        return null;
      case 'getSupportedIcons':
        this.getSupportedIcons(callback);
        return null;
      case 'isSupported':
        this.isSupported(callback);
        return null;
      default:
        return null;
    }
  }

  // Kotlin 侧传入 {"iconName":"xxx"} JSON 串（与 Android/iOS 端一致），需解出 iconName；
  // 空 JSON 值 / 非 JSON 一律返回空串（恢复默认图标语义）
  private parseIconName(params: KRAny): string {
    if (typeof params !== 'string') {
      return '';
    }
    try {
      const parsed = JSON.parse(params) as Record<string, Object>;
      const name = parsed['iconName'];
      return typeof name === 'string' ? name : '';
    } catch (e) {
      return '';
    }
  }

  private setIcon(name: string, callback: KuiklyRenderCallback | null): void {
    if (!this.supported()) {
      callback?.(this.resultJson(false, CODE_SYSTEM_VERSION_TOO_LOW, '',
        `需要 HarmonyOS API ${REQUIRED_API_VERSION}+（当前 ${deviceInfo.sdkApiVersion}）`));
      return;
    }
    bundleManager.setAlternateIcon(name).then(() => {
      callback?.(this.resultJson(true, CODE_OK));
    }).catch((err: BusinessError) => {
      callback?.(this.iconNameError(err, name));
    });
  }

  private getCurrentIcon(callback: KuiklyRenderCallback | null): void {
    if (!this.supported()) {
      callback?.(this.resultJson(false, CODE_SYSTEM_VERSION_TOO_LOW));
      return;
    }
    bundleManager.getAlternateIcons().then((icons: Array<bundleManager.AlternateIconInfo>) => {
      let current = '';
      if (icons) {
        for (const info of icons) {
          if (info.enabled) {
            current = info.iconName;
            break;
          }
        }
      }
      callback?.(this.resultJson(true, CODE_OK, current));
    }).catch((err: BusinessError) => {
      callback?.(this.resultJson(false, CODE_SYSTEM_REJECT, '', err.message));
    });
  }

  private getSupportedIcons(callback: KuiklyRenderCallback | null): void {
    if (!this.supported()) {
      callback?.(this.resultJson(false, CODE_SYSTEM_VERSION_TOO_LOW));
      return;
    }
    bundleManager.getAlternateIcons().then((icons: Array<bundleManager.AlternateIconInfo>) => {
      const names: string[] = [];
      if (icons) {
        for (const info of icons) {
          names.push(info.iconName);
        }
      }
      if (names.length === 0) {
        callback?.(this.resultJson(false, CODE_HOST_CONFIG_MISSING, '',
          '宿主未在 AppScope/app.json5 配置 alternateIcons'));
        return;
      }
      const quoted = names.map((n: string): string => `"${n}"`).join(',');
      callback?.(`{"success":true,"code":0,"icons":[${quoted}]}`);
    }).catch((err: BusinessError) => {
      callback?.(this.resultJson(false, CODE_SYSTEM_REJECT, '', err.message));
    });
  }

  private isSupported(callback: KuiklyRenderCallback | null): void {
    const supported = this.supported();
    callback?.(`{"success":true,"supported":${supported},"code":0,` +
      `"platform":"ohos","platformVersion":"api${deviceInfo.sdkApiVersion}"}`);
  }

  private supported(): boolean {
    return deviceInfo.sdkApiVersion >= REQUIRED_API_VERSION;
  }

  // setAlternateIcon 失败最常见原因是 name 未在 alternateIcons 中声明；错误信息原样透出，code 归为图标不存在/系统拒绝
  private iconNameError(err: BusinessError, name: string): string {
    const msg = `setAlternateIcon failed, name=${name}, code=${err.code}, ${err.message}`;
    const code = name.length > 0 ? CODE_ICON_NOT_FOUND : CODE_SYSTEM_REJECT;
    return this.resultJson(false, code, '', msg);
  }

  private resultJson(success: boolean, code: number, icon: string = '', error: string = ''): string {
    let json = `{"success":${success},"code":${code}`;
    if (icon.length > 0) {
      json += `,"icon":"${icon}"`;
    }
    if (error.length > 0) {
      json += `,"error":"${error.replace(/"/g, '\\"')}"`;
    }
    return json + '}';
  }
}
