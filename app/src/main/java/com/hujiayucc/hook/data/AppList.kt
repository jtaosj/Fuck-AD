package com.hujiayucc.hook.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.drawable.Drawable
import android.icu.text.Collator
import com.hujiayucc.hook.R
import com.hujiayucc.hook.annotation.Run
import com.hujiayucc.hook.annotation.RunJiaGu
import com.hujiayucc.hook.utils.AnnotationScanner
import java.util.*

class AppList(
    private val context: Context
) {
    val appList: MutableList<Item> = mutableListOf()

    init {
        fun addItem(appName: String, packageName: String, versions: Array<String>, action: String) {
            val appIcon: Drawable = getAppIcon(packageName)
            val item = Item(appName, packageName, versions, action, appIcon)
            appList.add(item)
        }

        AnnotationScanner.scanClassesWithAnnotation(
            context, "com.hujiayucc.hook.hooker.app", arrayListOf(Run::class.java, RunJiaGu::class.java)
        ).forEach { clazz ->
            clazz.annotations.forEach { annotation ->
                when (annotation) {
                    is Run -> addItem(annotation.appName, annotation.packageName, annotation.versions, annotation.action)
                    is RunJiaGu -> addItem(
                        annotation.appName,
                        annotation.packageName,
                        annotation.versions,
                        annotation.action
                    )
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun getAppIcon(packageName: String): Drawable {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: NameNotFoundException) {
            context.resources.getDrawable(R.mipmap.ic_default, null)
        }
    }
}