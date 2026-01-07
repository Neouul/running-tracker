package com.neouul.runningtracker.data.location

import android.location.Location
import com.neouul.runningtracker.domain.location.LocationClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class MockLocationClient @Inject constructor() : LocationClient {

    private val mockRoute = listOf(
        Pair(37.5665, 126.9780), // 서울시청
        Pair(37.5662, 126.9782), // 덕수궁 앞
        Pair(37.5658, 126.9775), // 덕수궁길 진입
        Pair(37.5651, 126.9768), // 서울시립미술관 부근
        Pair(37.5644, 126.9762), // 정동제일교회
        Pair(37.5648, 126.9754), // 정동길
        Pair(37.5656, 126.9749), // 예원학교 부근
        Pair(37.5664, 126.9755), // 국립정동극장
        Pair(37.5671, 126.9765), // 세종대로 교차로
        Pair(37.5678, 126.9772), // 서울광장 북측
        Pair(37.5672, 126.9788), // 서울신문사 앞
        Pair(37.5665, 126.9780)  // 다시 시청 (복귀)
    )

    override fun getLocationUpdates(interval: Long): Flow<Location> = flow {
        var index = 0
        while (true) {
            val (lat, lng) = mockRoute[index]
            val location = Location("mock").apply {
                latitude = lat
                longitude = lng
                time = System.currentTimeMillis()
                accuracy = 5.0f
            }
            emit(location)
            index = (index + 1) % mockRoute.size
            delay(interval)
        }
    }
}
