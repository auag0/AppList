package io.github.auag0.applist

import android.content.pm.PackageInfo

data class AppItem(
    val packageInfo: PackageInfo,
    val name: CharSequence,
    val packageName: String,
    val isSystem: Boolean,
    val lastUpdateTime: Long
)