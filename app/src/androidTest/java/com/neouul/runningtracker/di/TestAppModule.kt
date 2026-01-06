package com.neouul.runningtracker.di

import android.content.Context
import androidx.room.Room
import com.neouul.runningtracker.core.di.AppModule
import com.neouul.runningtracker.data.local.RunningDatabase
import com.neouul.runningtracker.discovery.FakeLocationClient
import com.neouul.runningtracker.domain.location.LocationClient
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object TestAppModule {

    @Singleton
    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app: Context
    ): RunningDatabase {
        return Room.inMemoryDatabaseBuilder(
            app,
            RunningDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Singleton
    @Provides
    fun provideRunDao(db: RunningDatabase) = db.getRunDao()

    @Singleton
    @Provides
    fun provideFakeLocationClient(): FakeLocationClient {
        return FakeLocationClient()
    }

    @Singleton
    @Provides
    fun provideLocationClient(fakeLocationClient: FakeLocationClient): LocationClient {
        return fakeLocationClient
    }
    
    // FusedLocationProviderClient가 필요하다면 여기서도 Mock 혹은 Fake를 제공해야 하지만, 
    // LocationClientImpl을 대체하므로 FakeLocationClient만 제공하면 됨.
}
