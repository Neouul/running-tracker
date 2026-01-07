package com.neouul.runningtracker.core.di

import com.neouul.runningtracker.data.location.MockLocationClient
import com.neouul.runningtracker.data.repository.MockRunRepositoryImpl
import com.neouul.runningtracker.domain.location.LocationClient
import com.neouul.runningtracker.domain.repository.RunRepository
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
    abstract fun bindRunRepository(
        mockRunRepositoryImpl: MockRunRepositoryImpl
    ): RunRepository

    @Binds
    @Singleton
    abstract fun bindLocationClient(
        mockLocationClient: MockLocationClient
    ): LocationClient
}
