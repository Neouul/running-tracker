package com.neouul.runningtracker.core.di

import android.content.Context
import androidx.room.Room
import com.neouul.runningtracker.data.local.RunningDatabase
import com.neouul.runningtracker.core.util.Constants.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(app)

    @Singleton
    @Provides
    fun provideLocationClient(
        client: com.google.android.gms.location.FusedLocationProviderClient
    ): com.neouul.runningtracker.domain.location.LocationClient = 
        com.neouul.runningtracker.data.location.LocationClientImpl(client)
}

