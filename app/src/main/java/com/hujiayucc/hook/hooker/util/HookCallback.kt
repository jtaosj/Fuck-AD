package com.hujiayucc.hook.hooker.util

import io.github.libxposed.api.XposedInterface

interface HookCallback {
    val chain: XposedInterface.Chain
    var result: Any?
}