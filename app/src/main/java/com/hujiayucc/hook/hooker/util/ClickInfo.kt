package com.hujiayucc.hook.hooker.util

import android.util.Log
import android.view.View
import android.widget.TextView
import com.hujiayucc.hook.ModuleMain
import com.hujiayucc.hook.utils.AppInfoUtil
import io.github.libxposed.api.XposedModuleInterface

object ClickInfo : Hooker() {
    override fun XposedModuleInterface.PackageReadyParam.onPackageReady() {
        View::class.java.method("performClick")
            .hook {
                before {
                    if (click) printInfo(instance as View)
                    if (stackTrack) printStackTrace(Throwable("堆栈信息"))
                }
            }

        "android.view.View.DeclaredOnClickListener".toClassOrNull()
            ?.method("onClick")
            ?.hook {
                before {
                    if (click) printInfo(instance as View)
                    if (stackTrack) printStackTrace(Throwable("堆栈信息"))
                }
            }
    }

    val click: Boolean get() = ModuleMain.prefs.getBoolean("clickInfo", false)
    val stackTrack: Boolean get() = ModuleMain.prefs.getBoolean("stackTrack", false)

    private fun printStackTrace(throwable: Throwable) {
        ModuleMain.module.log(Log.DEBUG, "Fuck AD", "", throwable)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun printInfo(view: View) {
        val id = view.id
        val resName: String = AppInfoUtil.getResourceName(view, id)
        val text = if (view is TextView) view.text.toString() else ""

        // 获取当前 Activity
        val activity = AppInfoUtil.getActivityFromView(view)
        val activityName = activity?.javaClass?.name ?: "Unknown"

        // 输出完整信息
        ModuleMain.module.log(
            Log.DEBUG, "Fuck AD",
            """
                ====== 点击事件详情 ======
                View 类: ${view::class.java.name}
                View ID: 0x${view.id.toHexString()} $resName
                View 文本: $text
                所在 Activity: $activityName
            """.trimIndent()
        )
    }
}