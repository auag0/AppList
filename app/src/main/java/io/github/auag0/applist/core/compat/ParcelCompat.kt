package io.github.auag0.applist.core.compat

import android.os.Build
import android.os.Parcel
import android.os.Parcelable

object ParcelCompat {
    inline fun <reified T : Parcelable> Parcel.readParcelable(): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            readParcelable(T::class.java.classLoader, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            readParcelable(T::class.java.classLoader)
        }
    }
}