package com.example.runningtracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): Flow<List<Run>>

    // 데이터 복구용: 현재 진행 중인 좌표들 가져오기
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackingPoint(point: TrackingPoint)

    @Query("SELECT * FROM tracking_points_table ORDER BY sequence ASC")
    fun getAllTrackingPoints(): Flow<List<TrackingPoint>>

    @Query("DELETE FROM tracking_points_table")
    suspend fun clearTrackingPoints()
}
