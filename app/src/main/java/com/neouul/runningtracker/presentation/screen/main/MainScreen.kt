package com.neouul.runningtracker.presentation.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState



@Composable
fun MainScreen(
    state: MainUiState,
    onAction: (MainAction) -> Unit
) {

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단 지도 영역 (60%)
        Box(modifier = Modifier.weight(0.6f)) {
            if (state.hasPermissions) {
                val lastLatLng = state.pathPoints.lastOrNull()?.lastOrNull()?.let {
                    LatLng(it.latitude, it.longitude)
                } ?: LatLng(37.5665, 126.9780)

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(lastLatLng, 15f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true)
                ) {
                    state.pathPoints.forEach { polyline ->
                        Polyline(
                            points = polyline.map { LatLng(it.latitude, it.longitude) },
                            color = Color.Blue,
                            width = 10f
                        )
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
                    InfoItem(label = "거리", value = "${state.distanceInMeters / 1000f} km")
                    InfoItem(label = "시간", value = state.formattedTime)
                    InfoItem(label = "칼로리", value = "${state.caloriesBurned} kcal")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 컨트롤 버튼 (노랑/초록 그라데이션)
                val gradientBrush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFD4FC79), Color(0xFF96E6A1))
                )

                Button(
                    onClick = { onAction(MainAction.OnToggleRun) },
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
                            text = if (state.isTracking) "일시 정지" else "운동 시작",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (state.isTracking || state.pathPoints.flatten().isNotEmpty()) {
                    TextButton(onClick = { onAction(MainAction.OnFinishRun) }) {
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

@Preview(showBackground = true)
@Composable
private fun PreviewMainScreen() {
    MainScreen(
        state = MainUiState(),
        onAction = {},
    )
}
