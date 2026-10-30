package com.kuikly.appicon

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * [KuiklyAppIconModule] 回调结果的类型化封装。
 *
 * 原生侧回调可能是 JSONObject（结构化通道）或 JSON 字符串，两者都兼容：
 *
 * ```
 * module.setIcon("summer") { result ->
 *     val r = AppIconResult.from(result)
 *     if (r.success) { /* r.code, r.icon, r.icons */ } else { /* r.error */ }
 * }
 * ```
 */
class AppIconResult private constructor(
    val success: Boolean,
    val code: Int,
    val icon: String,
    val icons: List<String>,
    val error: String?,
    val supported: Boolean,
    val platform: String,
    val platformVersion: String,
) {
    companion object {
        fun from(result: Any?): AppIconResult {
            val obj = when (result) {
                is JSONObject -> result
                is String -> JSONObject(result)
                else -> JSONObject()
            }
            val icons = ArrayList<String>()
            val arr = obj.optJSONArray(KuiklyAppIconModule.KEY_ICONS)
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val name = arr.optString(i) ?: ""
                    if (name.isNotEmpty()) {
                        icons.add(name)
                    }
                }
            }
            return AppIconResult(
                success = obj.optBoolean(KuiklyAppIconModule.KEY_SUCCESS),
                code = obj.optInt(KuiklyAppIconModule.KEY_CODE),
                icon = obj.optString(KuiklyAppIconModule.KEY_ICON),
                icons = icons,
                error = obj.optString(KuiklyAppIconModule.KEY_ERROR).takeIf { it.isNotEmpty() },
                supported = obj.optBoolean(KuiklyAppIconModule.KEY_SUPPORTED),
                platform = obj.optString(KuiklyAppIconModule.KEY_PLATFORM),
                platformVersion = obj.optString(KuiklyAppIconModule.KEY_PLATFORM_VERSION),
            )
        }
    }
}
