package com.neouul.runningtracker.discovery

import android.location.Location
import com.neouul.runningtracker.domain.location.LocationClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class FakeLocationClient : LocationClient {

    private val _locationFlow = MutableSharedFlow<Location>(replay = 1)
    
    // 테스트에서 외부적으로 위치를 주입하기 위한 함수
    suspend fun emitLocation(location: Location) {
        _locationFlow.emit(location)
    }

    override fun getLocationUpdates(interval: Long): Flow<Location> {
        return _locationFlow
    }
}
