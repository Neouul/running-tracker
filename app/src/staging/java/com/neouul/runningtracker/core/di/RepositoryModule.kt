package com.neouul.runningtracker.core.di

import com.neouul.runningtracker.data.location.LocationClientImpl
import com.neouul.runningtracker.data.repository.RunRepositoryImpl
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
        runRepositoryImpl: RunRepositoryImpl
    ): RunRepository

    @Binds
    @Singleton
    abstract fun bindLocationClient(
        locationClientImpl: LocationClientImpl
    ): LocationClient
}
