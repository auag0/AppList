package io.github.auag0.applist.core.utils.glide

import android.content.Context
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.module.AppGlideModule
import me.zhanghai.android.appiconloader.glide.AppIconModelLoader

@GlideModule
class MyGlide : AppGlideModule() {
    override fun isManifestParsingEnabled(): Boolean {
        return false
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val iconSize = context.resources.getDimensionPixelSize(android.R.dimen.app_icon_size)
        registry.prepend(
            PackageInfo::class.java,
            Bitmap::class.java,
            AppIconModelLoader.Factory(iconSize, false, context)
        )
    }
}