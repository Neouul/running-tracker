package com.neouul.runningtracker.di

import android.content.Context
import androidx.room.Room
import com.neouul.runningtracker.core.di.RepositoryModule
import com.neouul.runningtracker.data.local.RunningDatabase
import com.neouul.runningtracker.discovery.FakeLocationClient
import com.neouul.runningtracker.domain.location.LocationClient
import com.neouul.runningtracker.domain.repository.RunRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import io.mockk.mockk
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class, RepositoryModule::class]
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

    @Singleton
    @Provides
    fun provideRunRepository(): RunRepository {
        return mockk(relaxed = true)
    }
}
