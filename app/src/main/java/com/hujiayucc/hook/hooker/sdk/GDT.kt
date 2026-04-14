package com.hujiayucc.hook.hooker.sdk

import com.hujiayucc.hook.hooker.util.Hooker
import io.github.libxposed.api.XposedModuleInterface

/** 广电通 */
object GDT : Hooker() {
    override fun XposedModuleInterface.PackageReadyParam.onPackageReady() {
        $$"com.qq.e.comm.managers.plugin.PM$a".toClassOrNull()?.method("a")?.hook {
            before {
                result = false
            }
        }
    }
}