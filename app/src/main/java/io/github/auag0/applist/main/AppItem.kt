package io.github.auag0.applist.main

import android.content.pm.PackageInfo
import android.os.Parcel
import android.os.Parcelable
import io.github.auag0.applist.core.compat.ParcelCompat.readParcelable

data class AppItem(
    val packageInfo: PackageInfo,
    val name: CharSequence,
    val packageName: String,
    val isSystem: Boolean,
    val lastUpdateTime: Long
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readParcelable()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(packageInfo, flags)
        parcel.writeString(name.toString())
        parcel.writeString(packageName)
        parcel.writeByte(if (isSystem) 1 else 0)
        parcel.writeLong(lastUpdateTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AppItem> {
        override fun createFromParcel(parcel: Parcel): AppItem {
            return AppItem(parcel)
        }

        override fun newArray(size: Int): Array<AppItem?> {
            return arrayOfNulls(size)
        }
    }
}