package com.neouul.runningtracker.domain.usecase

import com.neouul.runningtracker.domain.model.Run
import com.neouul.runningtracker.domain.repository.RunRepository
import javax.inject.Inject

class InsertRunUseCase @Inject constructor(
    private val repository: RunRepository
) {
    suspend operator fun invoke(run: Run) {
        repository.insertRun(run)
    }
}
