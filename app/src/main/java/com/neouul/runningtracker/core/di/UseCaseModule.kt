package com.neouul.runningtracker.core.di

import com.neouul.runningtracker.domain.repository.RunRepository
import com.neouul.runningtracker.domain.usecase.GetRunsSortedByDateUseCase
import com.neouul.runningtracker.domain.usecase.InsertRunUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetRunsSortedByDateUseCase(repository: RunRepository): GetRunsSortedByDateUseCase {
        return GetRunsSortedByDateUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideInsertRunUseCase(repository: RunRepository): InsertRunUseCase {
        return InsertRunUseCase(repository)
    }
}
