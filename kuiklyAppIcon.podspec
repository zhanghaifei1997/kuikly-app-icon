Pod::Spec.new do |s|
  s.name             = "kuiklyAppIcon"
  s.version          = ENV['KUIKLY_APP_ICON_VERSION'] || '0.1.1'
  s.summary          = "Kuikly dynamic app icon module (KMP shared framework)"
  s.description      = <<-DESC
                        Kuikly 动态更换 App 图标组件的 KMP 共享框架（Kotlin Multiplatform）：
                        提供跨端统一 API（setIcon / getCurrentIcon / getSupportedIcons / isSupported），
                        本 pod 为 iOS 静态 XCFramework 二进制分发，与 kuiklyAppIconIOS（原生实现）配合使用。
                        DESC
  s.homepage         = "https://github.com/zhanghaifei1997/kuikly-app-icon"
  s.license          = "MIT"
  s.author           = "Zhanghaifei"
  s.source           = { :http => "https://github.com/zhanghaifei1997/kuikly-app-icon/releases/download/#{s.version}/kuiklyAppIcon-#{s.version}.zip" }
  s.ios.deployment_target = '15.0'
  s.vendored_frameworks = 'kuiklyAppIcon.xcframework'
  s.libraries        = 'c++'
  s.pod_target_xcconfig = {
    'EXCLUDED_ARCHS[sdk=iphonesimulator*]' => 'i386'
  }
end
