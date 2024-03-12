package io.github.auag0.applist

import android.app.Application
import com.google.android.material.color.DynamicColors

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        myApp = this
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    companion object {
        lateinit var myApp: MyApp
    }
}