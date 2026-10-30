package com.kuikly.appicon.demo

import com.kuikly.appicon.AppIconResult
import com.kuikly.appicon.KuiklyAppIconModule
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.directives.vforIndex
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/**
 * kuikly-app-icon 组件演示页。
 *
 * 展示能力：能力检查 / 当前图标 / 图标列表 / 切换图标 / 恢复默认 / 静默切换（iOS）。
 * 运行前需先完成宿主工程预配置（Info.plist / activity-alias / app.json5），
 * 见组件目录 docs/INTEGRATION.md。
 */
@Page("AppIconDemoPage")
internal class AppIconDemoPage : Pager() {

    private var currentIcon by observable("")
    private var statusText by observable("正在检查平台能力...")
    private var icons by observableList<String>()

    override fun created() {
        super.created()
        module().isSupported { r ->
            val result = AppIconResult.from(r)
            statusText = if (result.supported) "当前平台支持动态换图标（${result.platform} ${result.platformVersion}）"
            else "当前平台/系统不支持动态换图标：${result.error ?: ""}"
        }
        module().getCurrentIcon { r ->
            val result = AppIconResult.from(r)
            if (result.success) currentIcon = result.icon.ifEmpty { "默认图标" }
        }
        module().getSupportedIcons { r ->
            val result = AppIconResult.from(r)
            if (result.success) {
                icons.clear()
                icons.addAll(result.icons)
            }
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                padding(16f)
                backgroundColor(Color.WHITE)
            }
            View {
                attr { flexDirectionRow() }
                Text {
                    attr {
                        text("当前图标：")
                        fontSize(16f)
                        color(Color.BLACK)
                    }
                }
                Text {
                    attr {
                        text(ctx.currentIcon)
                        fontSize(16f)
                        color(Color.GRAY)
                    }
                }
            }
            Text {
                attr {
                    text(ctx.statusText)
                    fontSize(14f)
                    color(Color.GRAY)
                    marginTop(8f)
                }
            }
            View {
                attr {
                    flex(1f)
                    marginTop(16f)
                }
                vforIndex({ ctx.icons }) { name, _, _ ->
                    View {
                        attr {
                            height(48f)
                            justifyContentCenter()
                            paddingLeft(12f)
                            marginTop(8f)
                            backgroundColor(Color(0xFFF5F5F5))
                            borderRadius(8f)
                        }
                        Text {
                            attr {
                                text("切换到：$name")
                                fontSize(16f)
                                color(Color.BLACK)
                            }
                            event {
                                click {
                                    ctx.switchIcon(name)
                                }
                            }
                        }
                    }
                }
            }
            View {
                attr {
                    height(48f)
                    justifyContentCenter()
                    marginTop(12f)
                    backgroundColor(Color(0xFFEEEEEE))
                    borderRadius(8f)
                }
                Text {
                    attr {
                        text("恢复默认图标")
                        fontSize(16f)
                        color(Color.BLACK)
                    }
                    event {
                        click {
                            ctx.switchIcon("")
                        }
                    }
                }
            }
        }
    }

    override fun createExternalModules(): Map<String, Module>? {
        return mapOf(KuiklyAppIconModule.MODULE_NAME to KuiklyAppIconModule())
    }

    private fun module(): KuiklyAppIconModule =
        acquireModule<KuiklyAppIconModule>(KuiklyAppIconModule.MODULE_NAME)

    private fun switchIcon(name: String) {
        val display = name.ifEmpty { "默认图标" }
        module().setIcon(name) { r ->
            val result = AppIconResult.from(r)
            statusText = if (result.success) {
                currentIcon = result.icon.ifEmpty { "默认图标" }
                "已切换到 $display"
            } else {
                "切换失败 code=${result.code} ${result.error ?: ""}"
            }
        }
    }
}
