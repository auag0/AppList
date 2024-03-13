package io.github.auag0.applist.utils

import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import io.github.auag0.applist.MyApp.Companion.myApp

object AppPrefsManager {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(myApp)

    enum class AppFilter {
        UserApps,
        SystemApps,
        AllApps
    }

    enum class AppSort {
        ByName,
        ByLastUpdateTime
    }

    var appFilter: AppFilter = AppFilter.UserApps
        get() = AppFilter.valueOf(prefs.getString("appFilter", AppFilter.UserApps.name)!!)
        set(value) {
            prefs.edit().putString("appFilter", value.name).apply()
            field = value
        }

    var appSort: AppSort = AppSort.ByName
        get() = AppSort.valueOf(prefs.getString("appSort", AppSort.ByName.name)!!)
        set(value) {
            prefs.edit().putString("appSort", value.name).apply()
            field = value
        }
}