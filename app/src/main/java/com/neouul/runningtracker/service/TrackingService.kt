package com.neouul.runningtracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.neouul.runningtracker.R
import com.neouul.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
import com.neouul.runningtracker.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.neouul.runningtracker.util.Constants.ACTION_STOP_SERVICE
import com.neouul.runningtracker.util.Constants.FASTEST_LOCATION_INTERVAL
import com.neouul.runningtracker.util.Constants.LOCATION_UPDATE_INTERVAL
import com.neouul.runningtracker.util.Constants.NOTIFICATION_CHANNEL_ID
import com.neouul.runningtracker.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.neouul.runningtracker.util.Constants.NOTIFICATION_ID
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var mainRepository: com.neouul.runningtracker.data.repository.MainRepository

    var isFirstRun = true
    var serviceKilled = false

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    companion object {
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        // Hilt 주입 확인 (LifecycleService는 @AndroidEntryPoint 필요)
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        isTracking.observe(this) {
            updateLocationTracking(it)
        }
        
        // 데이터 복구 로직
        loadPointsFromDb()
    }

    private fun loadPointsFromDb() {
        // 간단한 복구 로직: 서비스를 시작할 때 DB에 저장된 포인트가 있다면 불러옴
        // (실제로는 정교한 상태 관리가 필요함)
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            val points = mainRepository.getAllTrackingPointsSync() 
            if (points.isNotEmpty()) {
                val recoveredPolylines: Polylines = mutableListOf(mutableListOf())
                points.forEach { point ->
                    val latLng = LatLng(point.latitude, point.longitude)
                    recoveredPolylines.last().add(latLng)
                    if (point.isFinishPoint) {
                        recoveredPolylines.add(mutableListOf())
                    }
                }
                pathPoints.postValue(recoveredPolylines)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startForegroundService()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                    // 일시정지 시 끝점 표시 후 새 라인 준비
                    addEmptyPolyline() 
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun pauseService() {
        isTracking.postValue(false)
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            mainRepository.clearTrackingPoints()
        }
        stopForeground(true)
        stopSelf()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                .build()
            
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
                
                // DB에 실시간 저장 (복구용)
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    mainRepository.insertTrackingPoint(
                        com.neouul.runningtracker.data.local.TrackingPoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            sequence = (pathPoints.value?.flatten()?.size ?: 0)
                        )
                    )
                }
            }
        }
    }


    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        addEmptyPolyline()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher) // 실제 앱에서는 적절한 아이콘으로 교체 필요
            .setContentTitle("Running Tracker")
            .setContentText("00:00:00") // 타이머 연동 예정
            .setContentIntent(getMainActivityPendingIntent())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notificationBuilder.build(),
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, com.neouul.runningtracker.MainActivity::class.java).apply {
            action = com.neouul.runningtracker.util.Constants.ACTION_SHOW_TRACKING_SCREEN
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT else FLAG_UPDATE_CURRENT
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}

