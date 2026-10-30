#import <Foundation/Foundation.h>
#import "KRBaseModule.h"

NS_ASSUME_NONNULL_BEGIN

/**
 * Kuikly 动态更换 App 图标 Module（iOS 侧实现）。
 *
 * 依赖宿主在 Info.plist 的 CFBundleAlternateIcons 中预置备用图标（iOS 10.3+）。
 * 类名必须与 Kuikly 侧注册的 Module 名一致（KRAppIconModule），框架运行时按名创建。
 */
@interface KRAppIconModule : KRBaseModule

@end

NS_ASSUME_NONNULL_END
