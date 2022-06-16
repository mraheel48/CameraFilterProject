package com.example.deafultproject

import android.content.Context
import android.os.Build
import android.os.Environment

object Util {

    @Suppress("DEPRECATION", "VARIABLE_WITH_REDUNDANT_INITIALIZER")
    @JvmStatic
    fun getRootPath(context: Context): String {
        var root: String? = null
        root = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.getExternalFilesDir(".pgu")?.absolutePath
        } else {
            Environment.getExternalStorageDirectory().absolutePath + "/.gpu"
        }
        return root.toString()
    }
}