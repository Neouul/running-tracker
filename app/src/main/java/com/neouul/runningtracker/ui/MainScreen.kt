package com.neouul.runningtracker.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neouul.runningtracker.service.TrackingService
import com.neouul.runningtracker.util.Constants.ACTION_PAUSE_SERVICE
import com.neouul.runningtracker.util.Constants.ACTION_START_OR_RESUME_SERVICE
import com.neouul.runningtracker.util.Constants.ACTION_STOP_SERVICE
import com.neouul.runningtracker.util.TrackingUtility
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MainScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isTracking by TrackingService.isTracking.observeAsState(false)
    val pathPoints by TrackingService.pathPoints.observeAsState(mutableListOf())

    var hasPermissions by remember { mutableStateOf(TrackingUtility.hasLocationPermissions(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        if (!hasPermissions) {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            // 주의: ACCESS_BACKGROUND_LOCATION은 여기서 함께 요청하면 시스템에서 무시할 수 있습니다.
            // 우선 서비스 구동을 위해 필수적인 포그라운드 권한만 먼저 받습니다.
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 지도 영역 (60%)
        Box(modifier = Modifier.weight(0.6f)) {
            if (hasPermissions) {
                val lastLatLng = pathPoints.lastOrNull()?.lastOrNull() ?: LatLng(37.5665, 126.9780)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(lastLatLng, 15f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true)
                ) {
                    pathPoints.forEach { polyline ->
                        Polyline(points = polyline, color = Color.Blue, width = 10f)
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("위치 권한이 필요합니다.")
                }
            }
        }

        // 하단 컨트롤 및 정보 영역 (40%)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 실시간 정보 대시보드
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    InfoItem(label = "거리", value = "0.0 km")
                    InfoItem(label = "시간", value = "00:00:00")
                    InfoItem(label = "페이스", value = "0'00\"")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 컨트롤 버튼 (노랑/초록 그라데이션)
                val gradientBrush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFD4FC79), Color(0xFF96E6A1)) // 트렌디한 그린/옐로우 그라데이션
                )

                Button(
                    onClick = {
                        if (TrackingUtility.hasLocationPermissions(context)) {
                            val action = if (isTracking) ACTION_PAUSE_SERVICE else ACTION_START_OR_RESUME_SERVICE
                            Intent(context, TrackingService::class.java).also {
                                it.action = action
                                androidx.core.content.ContextCompat.startForegroundService(context, it)
                            }
                        } else {
                            // 권한이 없으면 다시 요청
                            val permissions = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            permissionLauncher.launch(permissions.toTypedArray())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(60.dp),
                    shape = RoundedCornerShape(30.dp),
                    contentPadding = PaddingValues(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(gradientBrush),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isTracking) "일시정지" else "운동 시작",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (isTracking || pathPoints.isNotEmpty()) {
                    TextButton(onClick = {
                        Intent(context, TrackingService::class.java).also {
                            it.action = ACTION_STOP_SERVICE
                            context.startService(it)
                        }
                    }) {
                        Text("운동 종료", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color.Gray, fontSize = 14.sp)
        Text(text = value, color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    }
}

