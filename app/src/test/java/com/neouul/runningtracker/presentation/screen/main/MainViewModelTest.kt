package com.neouul.runningtracker.presentation.screen.main

import com.google.android.gms.maps.model.LatLng
import com.neouul.runningtracker.core.service.TrackingService
import com.neouul.runningtracker.data.repository.MainRepository
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Field

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    private lateinit var viewModel: MainViewModel
    private lateinit var mainRepository: MainRepository
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mainRepository = mockk(relaxed = true)
        
        // 상태 초기화
        resetTrackingServiceFlows()
        
        viewModel = MainViewModel(mainRepository)
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `TrackingService의 pathPoints가 업데이트되면 ViewModel의 state도 업데이트된다`() = runTest {
        // Given
        val testLatLng = LatLng(37.5, 127.0)
        val testPath = listOf(mutableListOf(testLatLng))
        
        // When
        updatePrivateFlowValue("_pathPoints", testPath)
        
        // Then
        val currentState = viewModel.state.value
        assertEquals(1, currentState.pathPoints.size)
        assertEquals(37.5, currentState.pathPoints[0][0].latitude, 0.0)
    }

    @Test
    fun `TrackingService의 timeRunInMillis가 업데이트되면 formattedTime이 올바르게 변환된다`() = runTest {
        // Given
        val tenSecondsInMs = 10000L
        
        // When
        updatePrivateFlowValue("_timeRunInMillis", tenSecondsInMs)
        
        // Then
        assertEquals("00:00:10", viewModel.state.value.formattedTime)
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
