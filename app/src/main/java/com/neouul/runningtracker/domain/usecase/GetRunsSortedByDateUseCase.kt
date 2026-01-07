package com.neouul.runningtracker.domain.usecase

import com.neouul.runningtracker.domain.model.Run
import com.neouul.runningtracker.domain.repository.RunRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRunsSortedByDateUseCase @Inject constructor(
    private val repository: RunRepository
) {
    operator fun invoke(): Flow<List<Run>> {
        return repository.getAllRunsSortedByDate()
    }
}
