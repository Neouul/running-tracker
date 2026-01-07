package com.neouul.runningtracker.domain.model

import android.graphics.Bitmap

data class Run(
    val img: Bitmap? = null,
    val timestamp: Long = 0L,
    val avgSpeedInKMH: Float = 0f,
    val distanceInMeters: Int = 0,
    val timeInMillis: Long = 0L,
    val caloriesBurned: Int = 0,
    val id: Int? = null
)
