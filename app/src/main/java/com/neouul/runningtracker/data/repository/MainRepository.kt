package com.neouul.runningtracker.data.repository

import com.neouul.runningtracker.data.local.Run
import com.neouul.runningtracker.data.local.RunDao
import com.neouul.runningtracker.data.local.TrackingPoint
import javax.inject.Inject

class MainRepository @Inject constructor(
    private val runDao: RunDao
) {
    suspend fun insertRun(run: Run) = runDao.insertRun(run)

    suspend fun insertTrackingPoint(point: TrackingPoint) = runDao.insertTrackingPoint(point)

    fun getAllRunsSortedByDate() = runDao.getAllRunsSortedByDate()

    fun getAllTrackingPoints() = runDao.getAllTrackingPoints()

    suspend fun getAllTrackingPointsSync() = runDao.getAllTrackingPointsSync()

    suspend fun clearTrackingPoints() = runDao.clearTrackingPoints()
}

