package com.neouul.runningtracker.data.repository

import com.neouul.runningtracker.data.local.RunDao
import com.neouul.runningtracker.data.local.TrackingPoint
import com.neouul.runningtracker.data.mapper.toRun
import com.neouul.runningtracker.data.mapper.toRunEntity
import com.neouul.runningtracker.domain.model.Run
import com.neouul.runningtracker.domain.repository.RunRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RunRepositoryImpl @Inject constructor(
    private val runDao: RunDao
) : RunRepository {
    override suspend fun insertRun(run: Run) = runDao.insertRun(run.toRunEntity())

    override suspend fun insertTrackingPoint(point: TrackingPoint) = runDao.insertTrackingPoint(point)

    override fun getAllRunsSortedByDate(): Flow<List<Run>> = 
        runDao.getAllRunsSortedByDate().map { entities ->
            entities.map { it.toRun() }
        }

    override fun getAllTrackingPoints(): Flow<List<TrackingPoint>> = runDao.getAllTrackingPoints()

    override suspend fun getAllTrackingPointsSync(): List<TrackingPoint> = runDao.getAllTrackingPointsSync()

    override suspend fun clearTrackingPoints() = runDao.clearTrackingPoints()
}
