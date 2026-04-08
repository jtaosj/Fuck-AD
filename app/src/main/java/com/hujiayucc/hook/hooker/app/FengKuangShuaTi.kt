package com.hujiayucc.hook.hooker.app

import com.hujiayucc.hook.annotation.RunJiaGu
import com.hujiayucc.hook.hooker.util.Hooker
import io.github.libxposed.api.XposedModuleInterface

@RunJiaGu(
    appName = "疯狂刷题",
    packageName = "com.yaerxing.fkst",
    action = "开屏广告"
)
object FengKuangShuaTi: Hooker() {
    override fun XposedModuleInterface.PackageReadyParam.onPackageReady() {
        loadSdk(this, pangle = true)
    }
}