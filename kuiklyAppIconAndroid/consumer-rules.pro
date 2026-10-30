# kuiklyAppIconAndroid 消费者混淆规则
# 本模块通过反射按类名查找 Kuikly Module（KRAppIconModule），宿主混淆时需保留：
-keep class com.kuikly.appicon.android.KRAppIconModule { *; }
