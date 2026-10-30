package com.kuikly.appicon.android

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import com.tencent.kuikly.core.render.android.export.KuiklyRenderBaseModule
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import org.json.JSONArray
import org.json.JSONObject

/**
 * Kuikly 动态更换 App 图标 Module（Android 侧实现）。
 *
 * 采用 activity-alias 约定方案：宿主在 Manifest 中为每套图标声明一个入口别名，
 * 统一命名 `<applicationId>.icon.<name>`，其中 `.icon.default` 为默认图标别名（初始 enabled），
 * 其余别名初始 disabled。切换图标 = 启停对应别名，永不触碰真实 Activity，避免进程被杀。
 *
 * 宿主 Manifest 配置示例见组件 docs/INTEGRATION.md。
 */
class KRAppIconModule : KuiklyRenderBaseModule() {

    override fun call(method: String, params: Any?, callback: KuiklyRenderCallback?): Any? {
        when (method) {
            METHOD_SET_ICON, METHOD_SET_ICON_SILENT -> // Android 无系统弹窗，两种切换行为一致
                setIcon(parseIconName(params), callback)
            METHOD_GET_CURRENT_ICON ->
                callback?.invoke(currentIconResult())
            METHOD_GET_SUPPORTED_ICONS ->
                callback?.invoke(supportedIconsResult())
            METHOD_IS_SUPPORTED ->
                callback?.invoke(isSupportedResult())
            else -> return super.call(method, params, callback)
        }
        return null
    }

    private fun setIcon(name: String, callback: KuiklyRenderCallback?) {
        val ctx = context
        if (ctx == null) {
            callback?.invoke(resultJson(false, CODE_SYSTEM_REJECT, error = "module context 不可用"))
            return
        }
        val pm = ctx.packageManager
        val aliases = aliasInfos(pm, ctx.packageName)
        if (aliases.isEmpty()) {
            callback?.invoke(resultJson(false, CODE_HOST_CONFIG_MISSING,
                error = "宿主未配置 activity-alias（命名约定 <applicationId>.icon.<name>）"))
            return
        }
        val targetName = if (name.isEmpty()) DEFAULT_ALIAS_NAME else name
        val target = aliases.firstOrNull { aliasName(it, ctx.packageName) == targetName }
        if (target == null) {
            callback?.invoke(resultJson(false, CODE_ICON_NOT_FOUND, error = "未找到图标别名：$targetName"))
            return
        }
        for (alias in aliases) {
            val state = if (alias.name == target.name) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(
                ComponentName(ctx, alias.name), state,
                PackageManager.DONT_KILL_APP
            )
        }
        // 返回给业务侧的语义与其他端一致：默认图标用空串表示
        val displayIcon = if (targetName == DEFAULT_ALIAS_NAME) "" else targetName
        callback?.invoke(resultJson(true, CODE_OK, icon = displayIcon))
    }

    private fun currentIconResult(): String {
        val ctx = context
            ?: return resultJson(false, CODE_SYSTEM_REJECT, error = "module context 不可用")
        val pm = ctx.packageManager
        val aliases = aliasInfos(pm, ctx.packageName)
        val current = aliases.firstOrNull {
            pm.getComponentEnabledSetting(ComponentName(ctx, it.name)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        val name = current?.let { aliasName(it, ctx.packageName) } ?: ""
        return resultJson(true, CODE_OK, icon = if (name == DEFAULT_ALIAS_NAME) "" else name)
    }

    private fun supportedIconsResult(): String {
        val ctx = context
            ?: return resultJson(false, CODE_SYSTEM_REJECT, error = "module context 不可用")
        val aliases = aliasInfos(ctx.packageManager, ctx.packageName)
        val names = aliases.map { aliasName(it, ctx.packageName) }
            .filter { it != DEFAULT_ALIAS_NAME }
        if (names.isEmpty()) {
            return resultJson(false, CODE_HOST_CONFIG_MISSING,
                error = "宿主未配置 activity-alias（命名约定 <applicationId>.icon.<name>）")
        }
        val json = JSONObject()
            .put("success", true)
            .put("code", CODE_OK)
            .put("icons", JSONArray(names))
        return json.toString()
    }

    private fun isSupportedResult(): String {
        val json = JSONObject()
            .put("success", true)
            .put("supported", true)
            .put("code", CODE_OK)
            .put("platform", "android")
            .put("platformVersion", "API ${Build.VERSION.SDK_INT}")
        return json.toString()
    }

    private fun parseIconName(params: Any?): String {
        val p = params as? String ?: return ""
        return try {
            JSONObject(p).optString(KEY_ICON_NAME)
        } catch (e: Exception) {
            ""
        }
    }

    /** 读取全部 icon 别名（含禁用状态，需要 MATCH_DISABLED_COMPONENTS） */
    private fun aliasInfos(pm: PackageManager, packageName: String): List<ActivityInfo> {
        return try {
            val info = pm.getPackageInfo(
                packageName,
                PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
            )
            info.activities
                ?.filter { it.targetActivity != null && it.name.startsWith("$packageName$ALIAS_PREFIX") }
                ?.sortedBy { it.name }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun aliasName(info: ActivityInfo, packageName: String): String =
        info.name.removePrefix("$packageName$ALIAS_PREFIX")

    private fun resultJson(
        success: Boolean,
        code: Int,
        icon: String = "",
        error: String = ""
    ): String {
        val json = JSONObject()
            .put("success", success)
            .put("code", code)
        if (icon.isNotEmpty()) {
            json.put("icon", icon)
        }
        if (error.isNotEmpty()) {
            json.put("error", error)
        }
        return json.toString()
    }

    companion object {
        const val MODULE_NAME = "KRAppIconModule"

        const val ALIAS_PREFIX = ".icon."
        const val DEFAULT_ALIAS_NAME = "default"

        private const val METHOD_SET_ICON = "setIcon"
        private const val METHOD_SET_ICON_SILENT = "setIconSilent"
        private const val METHOD_GET_CURRENT_ICON = "getCurrentIcon"
        private const val METHOD_GET_SUPPORTED_ICONS = "getSupportedIcons"
        private const val METHOD_IS_SUPPORTED = "isSupported"

        private const val KEY_ICON_NAME = "iconName"

        private const val CODE_OK = 0
        private const val CODE_ICON_NOT_FOUND = 2
        private const val CODE_HOST_CONFIG_MISSING = 4
        private const val CODE_SYSTEM_REJECT = 5
    }
}
