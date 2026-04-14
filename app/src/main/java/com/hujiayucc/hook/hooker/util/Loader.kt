package com.hujiayucc.hook.hooker.util

import android.app.Activity
import com.hujiayucc.hook.ModuleMain
import com.hujiayucc.hook.author.Author
import io.github.libxposed.api.XposedModuleInterface

object Loader: Hooker() {
    override fun XposedModuleInterface.PackageReadyParam.onPackageReady() {
        Activity::class.java.method("onCreate")
            .hook {
                before {
                    Author(instance<Activity>(), true, ModuleMain.prefs)
                }
            }
    }
}