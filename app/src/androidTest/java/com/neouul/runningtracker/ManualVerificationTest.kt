package com.neouul.runningtracker

import android.location.Location
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.neouul.runningtracker.di.TestAppModule
import com.neouul.runningtracker.discovery.FakeLocationClient
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ManualVerificationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()
    
    @get:Rule(order = 2)
    val permissionRule: androidx.test.rule.GrantPermissionRule = androidx.test.rule.GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.POST_NOTIFICATIONS
    )

    @Inject
    lateinit var fakeLocationClient: FakeLocationClient

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun testRunningFlowWithVisualDelay() = runBlocking {
        // 이 테스트는 사용자가 화면을 직접 확인하기 위한 용도입니다.
        // runTest 대신 runBlocking을 사용하여 실제 시간 흐름대로 실행되도록 합니다.
        // (runTest는 TestDispatcher를 사용하여 Dispatchers.Main을 제어하려 하므로 
        // 실제 서비스의 타이머나 UI 업데이트가 멈출 수 있습니다.)
        
        // 1. 초기 대기 (앱 실행 확인)
        Thread.sleep(2000)

        // 2. 운동 시작
        composeRule.onNodeWithText("운동 시작").performClick()
        
        // UI가 갱신될 시간을 줌
        composeRule.waitForIdle()
        Thread.sleep(2000)

        // 3. 위치 이동 시뮬레이션 (서울 시청 주변 경로)
        val centerLat = 37.5665
        val centerLng = 126.9780
        
        // 사각형 경로 생성
        val locations = listOf(
            Pair(centerLat, centerLng), // 시작점
            Pair(centerLat + 0.001, centerLng), // 북쪽으로 이동
            Pair(centerLat + 0.001, centerLng + 0.001), // 동쪽으로 이동
            Pair(centerLat, centerLng + 0.001), // 남쪽으로 이동
            Pair(centerLat, centerLng) // 원점 복귀
        )

        locations.forEachIndexed { index, (lat, lng) ->
            val location = Location("test").apply {
                latitude = lat
                longitude = lng
                time = System.currentTimeMillis() + (index * 1000)
            }
            fakeLocationClient.emitLocation(location)
            
            // UI 업데이트 및 사용자 확인 대기
            composeRule.waitForIdle()
            Thread.sleep(2000) 
        }

        // 4. 운동 종료
        // "일시 정지" 버튼이 보여야 함
        composeRule.onNodeWithText("일시 정지").assertIsDisplayed()
        composeRule.onNodeWithText("일시 정지").performClick()
        
        composeRule.waitForIdle()
        Thread.sleep(2000)
        
        composeRule.onNodeWithText("운동 종료").performClick()
        
        composeRule.waitForIdle()
        Thread.sleep(2000)
    }
}
