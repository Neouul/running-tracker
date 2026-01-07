package com.neouul.runningtracker.core.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object TrackingUtility {

    fun hasLocationPermissions(context: Context): Boolean {
        val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNotification = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            hasFineLocation && hasCoarseLocation && hasNotification
        } else {
            hasFineLocation && hasCoarseLocation
        }
    }

    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = milliseconds / 3600000
        milliseconds %= 3600000
        val minutes = milliseconds / 60000
        milliseconds %= 60000
        val seconds = milliseconds / 1000
        if (!includeMillis) {
            return "${if (hours < 10) "0" else ""}$hours:" +
                    "${if (minutes < 10) "0" else ""}$minutes:" +
                    "${if (seconds < 10) "0" else ""}$seconds"
        }
        milliseconds %= 1000
        milliseconds /= 10
        return "${if (hours < 10) "0" else ""}$hours:" +
                "${if (minutes < 10) "0" else ""}$minutes:" +
                "${if (seconds < 10) "0" else ""}$seconds:" +
                "${if (milliseconds < 10) "0" else ""}$milliseconds"
    }

    // 칼로리 계산 간단 공식: 거리(km) * 몸무게(kg) (사용자 입력 없을 시 기본 70kg 가정)
    fun calculateCalories(distanceInMeters: Int, weight: Float = 70f): Int {
        return ((distanceInMeters / 1000f) * weight).toInt()
    }

    fun getBatteryLevel(context: Context): Int {
        val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale.toFloat()).toInt()
        } else {
            -1
        }
    }
}

