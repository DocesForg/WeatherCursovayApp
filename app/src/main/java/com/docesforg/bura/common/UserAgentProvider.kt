package com.docesforg.bura.common

import android.content.Context

class UserAgentProvider(private val context: Context) {
    val userAgent: String get() {
        val packageName = context.packageName
        val appVersion = context.packageManager.getPackageInfo(packageName, 0).versionName
        val source = "https://github.com/docesforg/bura"
        return "Bura/$packageName/$appVersion ($source)"
    }
}