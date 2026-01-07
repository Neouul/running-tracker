package com.neouul.runningtracker.data.mapper

import com.neouul.runningtracker.data.local.RunEntity
import com.neouul.runningtracker.domain.model.Run

fun RunEntity.toRun(): Run {
    return Run(
        img = img,
        timestamp = timestamp,
        avgSpeedInKMH = avgSpeedInKMH,
        distanceInMeters = distanceInMeters,
        timeInMillis = timeInMillis,
        caloriesBurned = caloriesBurned,
        id = id
    )
}

fun Run.toRunEntity(): RunEntity {
    return RunEntity(
        img = img,
        timestamp = timestamp,
        avgSpeedInKMH = avgSpeedInKMH,
        distanceInMeters = distanceInMeters,
        timeInMillis = timeInMillis,
        caloriesBurned = caloriesBurned
    ).also {
        it.id = id
    }
}
