package com.neouul.runningtracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracking_points_table")
data class TrackingPoint(
    val latitude: Double,
    val longitude: Double,
    val sequence: Int,
    val isFinishPoint: Boolean = false // 경로의 끝점인지 여부 (다시 시작할 때 선 끊김 방지)
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}

