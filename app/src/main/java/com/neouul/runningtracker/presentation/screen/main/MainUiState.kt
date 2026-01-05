package com.neouul.runningtracker.presentation.screen.main

import com.neouul.runningtracker.domain.model.TrackingPath

data class MainUiState(
    val isTracking: Boolean = false,
    val pathPoints: TrackingPath = mutableListOf(),
    val hasPermissions: Boolean = false,
    val distanceInMeters: Int = 0,
    val timeInMillis: Long = 0L,
    val caloriesBurned: Int = 0,
    val formattedTime: String = "00:00:00"
)