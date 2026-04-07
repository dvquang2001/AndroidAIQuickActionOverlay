package com.qcp.aioverlay.di

import com.qcp.aioverlay.data.repository.AuthRepositoryImpl
import com.qcp.aioverlay.data.repository.HistoryRepositoryImpl
import com.qcp.aioverlay.data.repository.PermissionRepositoryImpl
import com.qcp.aioverlay.domain.repository.AuthRepository
import com.qcp.aioverlay.domain.repository.HistoryRepository
import com.qcp.aioverlay.domain.repository.PermissionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPermissionRepository(impl: PermissionRepositoryImpl): PermissionRepository
}