package com.neouul.runningtracker.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@SmallTest
class RunDaoTest {

    private lateinit var database: RunningDatabase
    private lateinit var dao: RunDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RunningDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.getRunDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertRun_shouldReturnInsertedRun() = runTest {
        // Given
        val run = Run(
            timestamp = 123L,
            avgSpeedInKMH = 10f,
            distanceInMeters = 100,
            timeInMillis = 1000L,
            caloriesBurned = 50
        )

        // When
        dao.insertRun(run)

        // Then
        // Flow에서 첫 번째 방출된 값 수집
        val allRuns = dao.getAllRunsSortedByDate().first()
        assertEquals(1, allRuns.size)
        
        val insertedRun = allRuns[0]
        assertEquals(run.timestamp, insertedRun.timestamp)
        assertEquals(run.avgSpeedInKMH, insertedRun.avgSpeedInKMH)
        assertEquals(run.distanceInMeters, insertedRun.distanceInMeters)
    }

    @Test
    fun insertTrackingPoint_shouldReturnInsertedPoint() = runTest {
        // Given
        val point = TrackingPoint(
            latitude = 37.5665,
            longitude = 126.9780,
            sequence = 0
        )

        // When
        dao.insertTrackingPoint(point)

        // Then
        val storedPoints = dao.getAllTrackingPointsSync()
        assertEquals(1, storedPoints.size)
        assertEquals(point.latitude, storedPoints[0].latitude, 0.0)
        assertEquals(point.longitude, storedPoints[0].longitude, 0.0)
    }

    @Test
    fun clearTrackingPoints_shouldDeleteAllPoints() = runTest {
        // Given
        val point1 = TrackingPoint(37.0, 127.0, 0)
        val point2 = TrackingPoint(37.1, 127.1, 1)
        dao.insertTrackingPoint(point1)
        dao.insertTrackingPoint(point2)

        // When
        dao.clearTrackingPoints()

        // Then
        val storedPoints = dao.getAllTrackingPointsSync()
        assertEquals(0, storedPoints.size)
    }
}
