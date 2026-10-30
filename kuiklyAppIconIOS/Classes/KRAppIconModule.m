#import "KRAppIconModule.h"
#import "NSObject+KR.h"
#import "KRUIKit.h" // [macOS]
#import <objc/message.h>

static NSString *const kAlternateIconsKey = @"CFBundleAlternateIcons";

@implementation KRAppIconModule

#pragma mark - Kuikly Module 方法（方法名与 Kotlin 侧 toNative 的 methodName 一致，参数固定 NSDictionary）

- (void)setIcon:(NSDictionary *)args {
    [self applyIconName:[self iconNameFromArgs:args] silent:NO callback:args[KR_CALLBACK_KEY]];
}

- (void)setIconSilent:(NSDictionary *)args {
    [self applyIconName:[self iconNameFromArgs:args] silent:YES callback:args[KR_CALLBACK_KEY]];
}

- (void)getCurrentIcon:(NSDictionary *)args {
    KuiklyRenderCallback callback = args[KR_CALLBACK_KEY];
    if (@available(iOS 10.3, *)) {
        NSString *current = [UIApplication sharedApplication].alternateIconName ?: @"";
        callback ? callback(@{@"success": @YES, @"code": @0, @"icon": current}) : nil;
    } else {
        callback ? callback([self failure:@3 error:@"需要 iOS 10.3+"]) : nil;
    }
}

- (void)getSupportedIcons:(NSDictionary *)args {
    KuiklyRenderCallback callback = args[KR_CALLBACK_KEY];
    NSDictionary *alternateIcons = [self alternateIconsConfig];
    if (![alternateIcons isKindOfClass:[NSDictionary class]] || alternateIcons.count == 0) {
        callback ? callback([self failure:@4 error:@"宿主未在 Info.plist 配置 CFBundleAlternateIcons"]) : nil;
        return;
    }
    NSArray<NSString *> *names = [alternateIcons.allKeys sortedArrayUsingSelector:@selector(compare:)];
    callback ? callback(@{@"success": @YES, @"code": @0, @"icons": names}) : nil;
}

- (void)isSupported:(NSDictionary *)args {
    KuiklyRenderCallback callback = args[KR_CALLBACK_KEY];
    BOOL supported = @available(iOS 10.3, *);
    NSString *version = [UIDevice currentDevice].systemVersion ?: @"";
    callback ? callback(@{@"success": @YES, @"code": @0,
                          @"supported": @(supported),
                          @"platform": @"ios",
                          @"platformVersion": version}) : nil;
}

#pragma mark - Private

// 规范结构为 CFBundleIcons -> CFBundleAlternateIcons（真机生效要求）；同时兼容顶层 CFBundleAlternateIcons 旧结构
- (NSDictionary *)alternateIconsConfig {
    NSDictionary *config = NSBundle.mainBundle.infoDictionary[kAlternateIconsKey];
    if (![config isKindOfClass:[NSDictionary class]]) {
        NSDictionary *bundleIcons = NSBundle.mainBundle.infoDictionary[@"CFBundleIcons"];
        if ([bundleIcons isKindOfClass:[NSDictionary class]]) {
            id nested = bundleIcons[kAlternateIconsKey];
            if ([nested isKindOfClass:[NSDictionary class]]) {
                config = nested;
            }
        }
    }
    return [config isKindOfClass:[NSDictionary class]] ? config : nil;
}

- (void)applyIconName:(NSString *)name silent:(BOOL)silent callback:(KuiklyRenderCallback)callback {
    if (![NSThread isMainThread]) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self applyIconName:name silent:silent callback:callback];
        });
        return;
    }
    if (!@available(iOS 10.3, *)) {
        callback ? callback([self failure:@3 error:@"需要 iOS 10.3+"]) : nil;
        return;
    }
    // 备用图标必须已在 CFBundleAlternateIcons 声明，否则切换必然失败，提前给出明确错误
    if (name.length > 0) {
        NSDictionary *alternateIcons = [self alternateIconsConfig];
        if (![alternateIcons isKindOfClass:[NSDictionary class]] ||
            alternateIcons[name] == nil) {
            callback ? callback([self failure:@2
                                      error:[NSString stringWithFormat:@"图标未在 CFBundleAlternateIcons 声明：%@", name]]) : nil;
            return;
        }
    }
    void (^completion)(NSError * _Nullable) = ^(NSError * _Nullable error) {
        if (error) {
            callback ? callback([self failure:@5 error:error.localizedDescription]) : nil;
        } else {
            callback ? callback(@{@"success": @YES, @"code": @0, @"icon": name ?: @""}) : nil;
        }
    };
    if (silent) {
        // 私有 API 静默切换（无系统弹窗）。审核风险见 README；运行时不可用则回退公开 API。
        SEL silentSelector = NSSelectorFromString(@"_setAlternateIconName:isDiscardable:completionHandler:");
        if ([UIApplication respondsToSelector:silentSelector]) {
            ((void (*)(id, SEL, NSString *, BOOL, void (^)(NSError * _Nullable)))objc_msgSend)(
                [UIApplication sharedApplication], silentSelector, name, YES, completion);
            return;
        }
    }
    // name 传 nil 恢复默认图标
    [[UIApplication sharedApplication] setAlternateIconName:name.length > 0 ? name : nil
                                          completionHandler:completion];
}

- (NSString *)iconNameFromArgs:(NSDictionary *)args {
    id param = args[KR_PARAM_KEY];
    if ([param isKindOfClass:NSString.class]) {
        NSDictionary *dict = [param hr_stringToDictionary];
        return dict[@"iconName"] ?: @"";
    }
    return @"";
}

- (NSDictionary *)failure:(NSNumber *)code error:(NSString *)error {
    return @{@"success": @NO, @"code": code, @"error": error ?: @""};
}

@end
