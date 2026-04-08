package com.hujiayucc.hook.hooker.app

import android.view.View
import com.hujiayucc.hook.annotation.RunJiaGu
import com.hujiayucc.hook.hooker.util.Hooker
import io.github.libxposed.api.XposedModuleInterface

@RunJiaGu(
    appName = "酷安",
    packageName = "com.coolapk.market",
    action = "禁用SDK, 信息流广告"
)
object CoolMarket : Hooker() {
    override fun XposedModuleInterface.PackageReadyParam.onPackageReady() {
        loadSdk(this, pangle = true)
        "androidx.appcompat.widget.AppCompatImageView".toClassOrNull()
            ?.method("hasOverlappingRendering")
            ?.hook {
                after {
                    val view = instance<View>()
                    if (view.id == 0x7f0b0424) {
                        view.isClickable && view.performClick()
                    }
                }
            }
    }
}