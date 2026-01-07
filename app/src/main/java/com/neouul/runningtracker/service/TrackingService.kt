package com.neouul.runningtracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.model.LatLng
import com.neouul.runningtracker.MainActivity
import com.neouul.runningtracker.R
import com.neouul.runningtracker.core.util.Constants
import com.neouul.runningtracker.core.util.Constants.ACTION_PAUSE_SERVICE
import com.neouul.runningtracker.core.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.neouul.runningtracker.core.util.Constants.ACTION_STOP_SERVICE
import com.neouul.runningtracker.core.util.Constants.LOCATION_UPDATE_INTERVAL
import com.neouul.runningtracker.core.util.Constants.NOTIFICATION_CHANNEL_ID
import com.neouul.runningtracker.core.util.Constants.NOTIFICATION_CHANNEL_NAME
import com.neouul.runningtracker.core.util.Constants.NOTIFICATION_ID
import com.neouul.runningtracker.core.util.TrackingUtility
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import com.neouul.runningtracker.data.local.TrackingPoint
import com.neouul.runningtracker.domain.location.LocationClient
import com.neouul.runningtracker.domain.model.Run
import com.neouul.runningtracker.domain.repository.RunRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var runRepository: RunRepository

    @Inject
    lateinit var locationClient: LocationClient

    var isFirstRun = true
    var serviceKilled = false

    companion object {
        private val _isTracking = MutableStateFlow(false)
        val isTracking = _isTracking.asStateFlow()

        private val _pathPoints = MutableStateFlow<List<MutableList<LatLng>>>(mutableListOf())
        val pathPoints = _pathPoints.asStateFlow()

        private val _timeRunInMillis = MutableStateFlow(0L)
        val timeRunInMillis = _timeRunInMillis.asStateFlow()

        private val _timeRunInSeconds = MutableStateFlow(0L)
        val timeRunInSeconds = _timeRunInSeconds.asStateFlow()

        private val _distanceInMeters = MutableStateFlow(0)
        val distanceInMeters = _distanceInMeters.asStateFlow()

        private val _caloriesBurned = MutableStateFlow(0)
        val caloriesBurned = _caloriesBurned.asStateFlow()
    }

    private fun postInitialValues() {
        _isTracking.value = false
        _pathPoints.value = mutableListOf()
        _timeRunInSeconds.value = 0L
        _timeRunInMillis.value = 0L
        _distanceInMeters.value = 0
        _caloriesBurned.value = 0
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = level * 100 / scale.toFloat()

                if (batteryPct <= 20 && _isTracking.value) {
                    Timber.d("Battery low ($batteryPct%), force saving and stopping...")
                    forceSaveAndStop()
                }
            }
        }
    }

    private var isForceStopping = false

    private fun forceSaveAndStop() {
        if (isForceStopping) return
        isForceStopping = true
        
        // 즉시 운동 중지 상태로 변경 (타이머 및 위치 추적 중단)
        pauseService()
        
        lifecycleScope.launch(Dispatchers.IO) {
            val distanceMeters = _distanceInMeters.value
            val timeMillis = _timeRunInMillis.value
            val calories = _caloriesBurned.value
            val avgSpeed = if (timeMillis > 0) {
                (distanceMeters / 1000f) / (timeMillis / 3600000f)
            } else 0f

            val run = Run(
                img = null,
                timestamp = System.currentTimeMillis(),
                avgSpeedInKMH = avgSpeed,
                distanceInMeters = distanceMeters,
                timeInMillis = timeMillis,
                caloriesBurned = calories
            )
            
            try {
                // 데이터베이스 저장 완료 대기
                runRepository.insertRun(run)
                Timber.d("Workout saved successfully during force stop")
            } catch (e: Exception) {
                Timber.e(e, "Failed to save workout during force stop")
            } finally {
                // 저장 시도 후 반드시 서비스 종료
                launch(Dispatchers.Main) {
                    killService()
                    isForceStopping = false
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()

        lifecycleScope.launch {
            isTracking.collect {
                updateLocationTracking(it)
                updateNotificationState(it)
            }
        }

        loadPointsFromDb()
        registerReceiver(batteryReceiver, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(batteryReceiver)
        } catch (e: Exception) {
            Timber.e(e, "Error unregistering battery receiver")
        }
    }

    private fun loadPointsFromDb() {
        lifecycleScope.launch(Dispatchers.IO) {
            val points = runRepository.getAllTrackingPointsSync()
            if (points.isNotEmpty()) {
                val recoveredPolylines: MutableList<MutableList<LatLng>> = mutableListOf(mutableListOf())
                points.forEach { point ->
                    val latLng = LatLng(point.latitude, point.longitude)
                    recoveredPolylines.last().add(latLng)
                    if (point.isFinishPoint) {
                        recoveredPolylines.add(mutableListOf())
                    }
                }
                _pathPoints.value = recoveredPolylines
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
        _isTracking.value = false
        isTimerEnabled = false
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        isTimerEnabled = false
        pauseService()
        postInitialValues()
        lifecycleScope.launch(Dispatchers.IO) {
            runRepository.clearTrackingPoints()
        }
        stopForeground(true)
        stopSelf()
    }

    private var locationJob: Job? = null

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            locationJob?.cancel()
            locationJob = locationClient.getLocationUpdates(LOCATION_UPDATE_INTERVAL)
                .onEach { location ->
                    addPathPoint(location)
                    Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                }
                .launchIn(lifecycleScope)
        } else {
            locationJob?.cancel()
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            _pathPoints.update { currentPoints ->
                val newPoints = currentPoints.toMutableList()
                if (newPoints.isNotEmpty()) {
                    val lastList = newPoints.last()
                    if (lastList.isNotEmpty()) {
                        val lastLatLng = lastList.last()
                        val result = FloatArray(1)
                        Location.distanceBetween(
                            lastLatLng.latitude,
                            lastLatLng.longitude,
                            location.latitude,
                            location.longitude,
                            result
                        )
                        _distanceInMeters.update { it + result[0].toInt() }
                        _caloriesBurned.value = TrackingUtility.calculateCalories(_distanceInMeters.value)
                    }
                    newPoints.last().add(pos)
                } else {
                    newPoints.add(mutableListOf(pos))
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    runRepository.insertTrackingPoint(
                        TrackingPoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            sequence = newPoints.flatten().size
                        )
                    )
                }
                newPoints
            }
        }
    }

    private fun addEmptyPolyline() {
        _pathPoints.update { currentPoints ->
            val newPoints = currentPoints.toMutableList()
            newPoints.add(mutableListOf())
            newPoints
        }
    }

    private fun startForegroundService() {
        startTimer()
        _isTracking.value = true

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Running Tracker")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notificationBuilder.build(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notificationBuilder.build())
        }
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).apply {
            action = Constants.ACTION_SHOW_TRACKING_SCREEN
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

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        addEmptyPolyline()
        isTimerEnabled = true
        timeStarted = System.currentTimeMillis()
        lifecycleScope.launch {
            while (isTimerEnabled) {
                lapTime = System.currentTimeMillis() - timeStarted
                _timeRunInMillis.value = timeRun + lapTime
                if (_timeRunInMillis.value >= lastSecondTimestamp + 1000L) {
                    _timeRunInSeconds.value += 1
                    lastSecondTimestamp += 1000L
                }
                delay(Constants.TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun updateNotificationState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "일시정지" else "다시 시작"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT else FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT else FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        lifecycleScope.launch {
            timeRunInSeconds.collect {
                if (!serviceKilled) {
                    val notificationBuilder = NotificationCompat.Builder(this@TrackingService, NOTIFICATION_CHANNEL_ID)
                        .setAutoCancel(false)
                        .setOngoing(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("운동 중 🏃‍➡️")
                        .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                        .setContentIntent(getMainActivityPendingIntent())
                        .addAction(R.drawable.ic_launcher_foreground, notificationActionText, pendingIntent)

                    notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
                }
            }
        }
    }
}

