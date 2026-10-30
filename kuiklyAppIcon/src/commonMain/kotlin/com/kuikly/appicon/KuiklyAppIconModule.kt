package com.kuikly.appicon

import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * Kuikly 动态更换 App 图标 Module。
 *
 * 三端能力：
 * - iOS 10.3+：Info.plist CFBundleAlternateIcons 预置备用图标，setAlternateIconName 切换（默认有系统弹窗）
 * - Android：宿主 Manifest 预置 activity-alias（约定命名 <applicationId>.icon.<name>），启停切换
 * - 鸿蒙 API 26+：app.json5 alternateIcons 预置，bundleManager.setAlternateIcon 切换
 * - H5 / 小程序：不支持（回调 code = CODE_UNSUPPORTED_PLATFORM）
 *
 * 所有方法均为异步调用（原生侧主线程执行），结果通过统一 JSON 契约回调，字段见 KEY_* 常量。
 */
class KuiklyAppIconModule : Module() {

    /**
     * 切换到指定备用图标。
     * @param name 备用图标名。各端预置配置中的图标标识（三方需保持一致）；传空串恢复默认图标。
     *             iOS 默认弹出系统"已更改图标"提示框。
     * @param callbackFn 结果回调：{success, code, icon, error?}
     */
    fun setIcon(name: String, callbackFn: CallbackFn) {
        asyncToNativeMethod(METHOD_SET_ICON, iconParams(name), callbackFn)
    }

    /**
     * 静默切换图标（不弹系统提示框）。
     * 仅 iOS 生效：使用私有 API `_setAlternateIconName:isDiscardable:completionHandler:`，
     * 运行时检测不可用会自动回退到公开 API（即等效 setIcon）。
     * 注意：私有 API 有 App Store 审核风险，默认建议使用 [setIcon]。
     * Android / 鸿蒙无系统弹窗概念，行为与 [setIcon] 完全一致。
     */
    fun setIconSilent(name: String, callbackFn: CallbackFn) {
        asyncToNativeMethod(METHOD_SET_ICON_SILENT, iconParams(name), callbackFn)
    }

    /**
     * 查询当前启用的备用图标名。
     * @param callbackFn 结果回调：icon 字段为图标名；当前为默认图标时 icon 为空串
     */
    fun getCurrentIcon(callbackFn: CallbackFn) {
        asyncToNativeMethod(METHOD_GET_CURRENT_ICON, null, callbackFn)
    }

    /**
     * 查询宿主预置的全部备用图标名列表（不含默认图标）。
     * @param callbackFn 结果回调：icons 字段为字符串数组
     */
    fun getSupportedIcons(callbackFn: CallbackFn) {
        asyncToNativeMethod(METHOD_GET_SUPPORTED_ICONS, null, callbackFn)
    }

    /**
     * 当前平台/系统是否支持动态换图标（含系统版本检查）。
     * @param callbackFn 结果回调：supported + platform + platformVersion 字段
     */
    fun isSupported(callbackFn: CallbackFn) {
        asyncToNativeMethod(METHOD_IS_SUPPORTED, null, callbackFn)
    }

    private fun iconParams(name: String): JSONObject =
        JSONObject().apply { put(KEY_ICON_NAME, name) }

    override fun moduleName(): String = MODULE_NAME

    companion object {
        const val MODULE_NAME = "KRAppIconModule"

        const val METHOD_SET_ICON = "setIcon"
        const val METHOD_SET_ICON_SILENT = "setIconSilent"
        const val METHOD_GET_CURRENT_ICON = "getCurrentIcon"
        const val METHOD_GET_SUPPORTED_ICONS = "getSupportedIcons"
        const val METHOD_IS_SUPPORTED = "isSupported"

        const val KEY_ICON_NAME = "iconName"

        // ---- 统一回调契约字段 ----
        const val KEY_SUCCESS = "success"
        const val KEY_CODE = "code"
        const val KEY_ICON = "icon"
        const val KEY_ICONS = "icons"
        const val KEY_ERROR = "error"
        const val KEY_SUPPORTED = "supported"
        const val KEY_PLATFORM = "platform"
        const val KEY_PLATFORM_VERSION = "platformVersion"

        // ---- 错误码 ----
        const val CODE_OK = 0
        const val CODE_UNSUPPORTED_PLATFORM = 1
        const val CODE_ICON_NOT_FOUND = 2
        const val CODE_SYSTEM_VERSION_TOO_LOW = 3
        const val CODE_HOST_CONFIG_MISSING = 4
        const val CODE_SYSTEM_REJECT = 5
    }
}
