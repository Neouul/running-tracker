package com.neouul.runningtracker.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.neouul.runningtracker.service.TrackingService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.channels.Channel
import com.neouul.runningtracker.domain.model.LocationPoint
import com.neouul.runningtracker.core.util.Constants.ACTION_PAUSE_SERVICE
import com.neouul.runningtracker.core.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.neouul.runningtracker.core.util.Constants.ACTION_STOP_SERVICE
import com.neouul.runningtracker.core.util.TrackingUtility
import com.neouul.runningtracker.domain.model.Run
import com.neouul.runningtracker.domain.usecase.GetRunsSortedByDateUseCase
import com.neouul.runningtracker.domain.usecase.InsertRunUseCase

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getRunsSortedByDateUseCase: GetRunsSortedByDateUseCase,
    private val insertRunUseCase: InsertRunUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(MainUiState())
    val state = _state.asStateFlow()

    private val _event = Channel<MainEvent>()
    val event = _event.receiveAsFlow()

    init {
        // TrackingService의 StateFlow를 관찰하여 상태 업데이트
        combine(
            TrackingService.isTracking,
            TrackingService.pathPoints,
            TrackingService.timeRunInMillis,
            TrackingService.distanceInMeters,
            TrackingService.caloriesBurned
        ) { isTracking, pathPoints, timeInMillis, distance, calories ->
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(timeInMillis)
            _state.update { 
                it.copy(
                    isTracking = isTracking,
                    pathPoints = pathPoints.map { polyline ->
                        polyline.map { latLng ->
                            LocationPoint(latLng.latitude, latLng.longitude)
                        }
                    },
                    timeInMillis = timeInMillis,
                    formattedTime = formattedTime,
                    distanceInMeters = distance,
                    caloriesBurned = calories
                )
            }
        }.launchIn(viewModelScope)
    }

    fun onAction(action: MainAction) {
        when (action) {
            is MainAction.OnPermissionsResult -> {
                _state.update { it.copy(hasPermissions = action.granted) }
            }
            MainAction.OnToggleRun -> {
                viewModelScope.launch {
                    if (state.value.hasPermissions) {
                        val serviceAction = if (state.value.isTracking) {
                            ACTION_PAUSE_SERVICE
                        } else {
                            ACTION_START_OR_RESUME_SERVICE
                        }
                        _event.send(MainEvent.TriggerService(serviceAction))
                    } else {
                        _event.send(MainEvent.Error("위치 권한이 없습니다."))
                    }
                }
            }
            MainAction.OnFinishRun -> {
                viewModelScope.launch {
                    _event.send(MainEvent.TriggerService(ACTION_STOP_SERVICE))
                    _event.send(MainEvent.RunFinished)
                }
            }
        }
    }

    val runsSortedByDate = getRunsSortedByDateUseCase()

    fun insertRun(run: Run) = viewModelScope.launch {
        insertRunUseCase(run)
    }
}
