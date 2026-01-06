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
import com.neouul.runningtracker.core.service.TrackingService
import com.neouul.runningtracker.core.util.Constants.ACTION_STOP_SERVICE
import com.neouul.runningtracker.core.util.TrackingUtility
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainRoot(
    viewModel: MainViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        viewModel.onAction(MainAction.OnPermissionsResult(permissions.values.all { it }))
    }

    LaunchedEffect(Unit) {
        if (!state.hasPermissions && !TrackingUtility.hasLocationPermissions(context)) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            permissionLauncher.launch(permissions.toTypedArray())
        } else if (!state.hasPermissions) {
            viewModel.onAction(MainAction.OnPermissionsResult(true))
        }
    }

    // 1회성 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is MainEvent.Error -> {
                    // Toast 표시 등
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

    MainScreen(
        state = state,
        onAction = viewModel::onAction
    )
}