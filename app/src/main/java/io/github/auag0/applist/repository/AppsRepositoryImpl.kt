package io.github.auag0.applist.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.auag0.applist.main.AppItem
import io.github.auag0.applist.repository.domain.AppsRepository
import javax.inject.Inject

class AppsRepositoryImpl @Inject constructor(
    @ApplicationContext appContext: Context
) : AppsRepository {
    private val pm = appContext.packageManager

    override suspend fun getInstalledApp(): List<AppItem> {
        val installedPackages = pm.getInstalledPackages(0)

        val apps = mutableListOf<AppItem>()

        installedPackages.forEach { packageInfo ->
            val appInfo = packageInfo.applicationInfo
            val appItem = AppItem(
                packageInfo = packageInfo,
                name = appInfo.loadLabel(pm),
                packageName = packageInfo.packageName,
                isSystem = appInfo.flags and ApplicationInfo.FLAG_SYSTEM == ApplicationInfo.FLAG_SYSTEM,
                lastUpdateTime = packageInfo.lastUpdateTime
            )
            apps.add(appItem)
        }
        return apps
    }
}