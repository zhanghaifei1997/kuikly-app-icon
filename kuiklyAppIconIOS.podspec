Pod::Spec.new do |s|
  s.name             = "kuiklyAppIconIOS"
  s.version          = ENV['KUIKLY_APP_ICON_VERSION'] || '0.1.0'
  s.summary          = "Kuikly dynamic app icon module (iOS side)"
  s.description      = <<-DESC
                        Kuikly 动态更换 App 图标组件的 iOS 原生实现：
                        基于 UIApplication.setAlternateIconName 与 Info.plist CFBundleAlternateIcons。
                        DESC
  s.homepage         = "https://github.com/zhanghaifei1997/kuikly-app-icon"
  s.license          = { :type => "MIT", :file => "LICENSE" }
  s.author           = { "Zhanghaifei" => "2428669833@qq.com" }
  s.source           = { :git => "https://github.com/zhanghaifei1997/kuikly-app-icon.git", :tag => "#{s.version}" }
  s.ios.deployment_target = '15.0'
  s.requires_arc     = true
  # podspec 位于仓库根：trunk 拉取 tag 时文件模式按仓库根解析，因此带模块目录前缀；
  # 本地 :path 开发时同样以仓库根为 pod 根，两种方式解析一致
  s.source_files     = "kuiklyAppIconIOS/Classes/**/*.{h,m}"
  s.public_header_files = "kuiklyAppIconIOS/Classes/KRAppIconModule.h"
  s.frameworks       = 'UIKit', 'Foundation'
  s.dependency "OpenKuiklyIOSRender"
end
