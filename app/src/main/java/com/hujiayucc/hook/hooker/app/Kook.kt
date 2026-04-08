package com.hujiayucc.hook.hooker.app

import android.widget.LinearLayout
import com.hujiayucc.hook.annotation.RunJiaGu
import com.hujiayucc.hook.hooker.util.Hooker
import io.github.libxposed.api.XposedModuleInterface

@RunJiaGu(
    appName = "KOOK",
    packageName = "cn.kaiheila",
    action = "开屏广告",
    versions = [
        "1.75.0"
    ]
)
object Kook : Hooker() {
    override fun XposedModuleInterface.PackageReadyParam.onPackageReady() {
        loadSdk(this, pangle = true)
        LinearLayout::class.java.method("onDraw")
            .hook {
                after {
                    val layout = instance as LinearLayout
                    if (layout.id == 0x7f0a0a51) layout.performClick()
                }
            }
    }
}