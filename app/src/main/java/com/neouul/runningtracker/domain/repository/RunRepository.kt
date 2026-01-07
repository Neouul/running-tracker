package com.neouul.runningtracker.domain.repository

import com.neouul.runningtracker.data.local.TrackingPoint
import com.neouul.runningtracker.domain.model.Run
import kotlinx.coroutines.flow.Flow

interface RunRepository {
    suspend fun insertRun(run: Run)

    suspend fun insertTrackingPoint(point: TrackingPoint)

    fun getAllRunsSortedByDate(): Flow<List<Run>>

    fun getAllTrackingPoints(): Flow<List<TrackingPoint>>

    suspend fun getAllTrackingPointsSync(): List<TrackingPoint>

    suspend fun clearTrackingPoints()
}
