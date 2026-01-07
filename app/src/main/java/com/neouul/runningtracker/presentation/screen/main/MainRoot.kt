package com.neouul.runningtracker.presentation.screen.main

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.MapsInitializer
import com.neouul.runningtracker.service.TrackingService
import com.neouul.runningtracker.core.util.TrackingUtility
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.padding

@Composable
fun MainRoot(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 1회성 이벤트 처리
    LaunchedEffect(Unit) {
        // CameraUpdateFactory 초기화를 보장하기 위해 한 번 호출
        MapsInitializer.initialize(context)
        
        viewModel.event.collect { event ->
            when (event) {
                is MainEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                MainEvent.RunFinished -> {
                    // 운동 종료 후 처리
                }
                is MainEvent.TriggerService -> {
                    Intent(context, TrackingService::class.java).also {
                        it.action = event.action
                        ContextCompat.startForegroundService(context, it)
                    }
                }
            }
        }
    }

    // 카메라 위치 상태 관리
    val lastLatLng = state.pathPoints.flatten().lastOrNull()?.let {
        LatLng(it.latitude, it.longitude)
    } ?: LatLng(37.5665, 126.9780)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(lastLatLng, 15f)
    }

    // 카메라 팔로우: 최신 포인트가 추가될 때마다 카메라 이동
    LaunchedEffect(lastLatLng) {
        try {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLng(lastLatLng)
            )
        } catch (e: Exception) {
            // CameraUpdateFactory가 아직 준비되지 않았을 경우 무시
            e.printStackTrace()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        MainScreen(
            state = state,
            cameraPositionState = cameraPositionState,
            onAction = viewModel::onAction,
            modifier = androidx.compose.ui.Modifier.padding(paddingValues)
        )
    }
}
