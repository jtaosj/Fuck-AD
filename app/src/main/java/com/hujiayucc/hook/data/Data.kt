package com.hujiayucc.hook.data

import android.content.Context
import android.content.SharedPreferences
import com.hujiayucc.hook.application.XYApplication
import java.text.SimpleDateFormat
import java.util.*

object Data {
    val Context.prefsBridge: SharedPreferences
        get() = try {
            XYApplication.mService?.getRemotePreferences("config")
                ?: getSharedPreferences("config", Context.MODE_PRIVATE)
        } catch (_: Exception) {
            getSharedPreferences("config", Context.MODE_PRIVATE)
        }

    fun String.formatTime(pattern: String = "yyyy-MM-dd"): Date {
        val forMat = SimpleDateFormat(pattern)
        return forMat.parse(this)!!
    }
}