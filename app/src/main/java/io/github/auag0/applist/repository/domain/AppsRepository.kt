package io.github.auag0.applist.repository.domain

import io.github.auag0.applist.main.AppItem

interface AppsRepository {
    suspend fun getInstalledApp(): List<AppItem>
}