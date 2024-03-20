package io.github.auag0.applist.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.auag0.applist.repository.AppsRepositoryImpl
import io.github.auag0.applist.repository.domain.AppsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideAppsRepository(@ApplicationContext appContext: Context): AppsRepository {
        return AppsRepositoryImpl(appContext)
    }
}