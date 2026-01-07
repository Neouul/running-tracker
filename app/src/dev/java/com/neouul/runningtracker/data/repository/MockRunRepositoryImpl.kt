package com.neouul.runningtracker.data.repository

import com.neouul.runningtracker.data.local.TrackingPoint
import com.neouul.runningtracker.domain.model.Run
import com.neouul.runningtracker.domain.repository.RunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MockRunRepositoryImpl @Inject constructor() : RunRepository {

    private val _mockRuns = MutableStateFlow<List<Run>>(
        listOf(
            Run(
                img = null,
                timestamp = System.currentTimeMillis() - 86400000,
                avgSpeedInKMH = 8.5f,
                distanceInMeters = 5000,
                timeInMillis = 2100000L,
                caloriesBurned = 350,
                id = 1
            ),
            Run(
                img = null,
                timestamp = System.currentTimeMillis() - 172800000,
                avgSpeedInKMH = 9.2f,
                distanceInMeters = 8000,
                timeInMillis = 3120000L,
                caloriesBurned = 580,
                id = 2
            )
        )
    )

    private val _mockPoints = MutableStateFlow<List<TrackingPoint>>(emptyList())

    override suspend fun insertRun(run: Run) {
        val newList = _mockRuns.value.toMutableList().apply {
            add(run.copy(id = (size + 1)))
        }
        _mockRuns.value = newList
    }

    override suspend fun insertTrackingPoint(point: TrackingPoint) {
        val newList = _mockPoints.value.toMutableList().apply {
            add(point)
        }
        _mockPoints.value = newList
    }

    override fun getAllRunsSortedByDate(): Flow<List<Run>> = _mockRuns.asStateFlow().map { list ->
        list.sortedByDescending { it.timestamp }
    }

    override fun getAllTrackingPoints(): Flow<List<TrackingPoint>> = _mockPoints.asStateFlow()

    override suspend fun getAllTrackingPointsSync(): List<TrackingPoint> = _mockPoints.value

    override suspend fun clearTrackingPoints() {
        _mockPoints.value = emptyList()
    }
}
