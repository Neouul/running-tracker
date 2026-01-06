package com.neouul.runningtracker.core.service

import android.location.Location
import android.os.Looper
import com.google.android.gms.maps.model.LatLng
import com.neouul.runningtracker.data.repository.MainRepository
import com.neouul.runningtracker.domain.location.LocationClient
import io.mockk.*
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field
import java.lang.reflect.Method

@OptIn(ExperimentalCoroutinesApi::class)
class TrackingServiceTest {

    private lateinit var service: TrackingService
    private lateinit var mainRepository: MainRepository
    private lateinit var locationClient: LocationClient
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // 최소한의 Looper 모킹 (나머지 로직을 위해)
        mockkStatic(Looper::class)
        val mockedLooper = mockk<Looper>(relaxed = true)
        every { Looper.getMainLooper() } returns mockedLooper
        every { Looper.myLooper() } returns mockedLooper
        
        mainRepository = mockk(relaxed = true)
        locationClient = mockk(relaxed = true)
        
        service = TrackingService()
        service.mainRepository = mainRepository
        service.locationClient = locationClient
        
        // 상태 초기화
        resetTrackingServiceFlows()
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `addPathPoint 호출 시 좌표가 pathPoints 리스트에 추가된다`() = runTest {
        // Given
        val location = mockk<Location>()
        every { location.latitude } returns 37.5
        every { location.longitude } returns 127.0

        // When: addPathPoint 호출 (Reflection)
        val method: Method = TrackingService::class.java.getDeclaredMethod("addPathPoint", Location::class.java)
        method.isAccessible = true
        method.invoke(service, location)

        // Then
        val currentPoints = TrackingService.pathPoints.value
        assertEquals(1, currentPoints.size)
        assertEquals(1, currentPoints[0].size)
        assertEquals(37.5, currentPoints[0][0].latitude, 0.0)
    }

    private fun resetTrackingServiceFlows() {
        updatePrivateFlowValue("_pathPoints", emptyList<MutableList<LatLng>>())
        updatePrivateFlowValue("_timeRunInMillis", 0L)
        updatePrivateFlowValue("_isTracking", false)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> updatePrivateFlowValue(fieldName: String, value: T) {
        val field = findField(TrackingService::class.java, fieldName) 
            ?: findField(TrackingService.Companion::class.java, fieldName)
            ?: throw NoSuchFieldException("Field $fieldName not found in TrackingService or Companion")
            
        field.isAccessible = true
        val flow = field.get(null) ?: field.get(TrackingService.Companion)
        (flow as MutableStateFlow<T>).value = value
    }

    private fun findField(clazz: Class<*>, name: String): Field? {
        return try {
            clazz.getDeclaredField(name)
        } catch (e: NoSuchFieldException) {
            null
        }
    }
}
