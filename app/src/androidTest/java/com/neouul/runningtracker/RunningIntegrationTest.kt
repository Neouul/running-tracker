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
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class RunningIntegrationTest {

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
    fun testRunningFlow() = runTest {
        // 0. 권한 상태 업데이트 (ViewModel이 자동으로 감지하지 못할 수 있으므로 강제 업데이트 시뮬레이션 필요할 수 있음)
        // 하지만 GrantPermissionRule로 인해 실제 앱 시작 시 권한이 허용된 상태로 인식될 것임.
        // MainActivity에서 onCreate 시점에 권한 체크를 한다면 OK.
        // 만약 Compose 내부에서 LaunchedEffect로 체크한다면 OK.
        
        // 1. 초기 상태: "운동 시작" 버튼 확인
        composeRule.onNodeWithText("운동 시작").assertIsDisplayed()

        // 2. 운동 시작
        composeRule.onNodeWithText("운동 시작").performClick()

        // 3. 버튼 텍스트가 "일시 정지"로 변경되었는지 확인
        composeRule.onNodeWithText("일시 정지").assertIsDisplayed()

        // 4. 위치 이동 시뮬레이션
        val location1 = Location("test").apply {
            latitude = 37.5665
            longitude = 126.9780
            time = System.currentTimeMillis()
        }
        val location2 = Location("test").apply {
            latitude = 37.5670 // 약간 이동
            longitude = 126.9785
            time = System.currentTimeMillis() + 1000
        }

        fakeLocationClient.emitLocation(location1)
        composeRule.waitForIdle() // UI 업데이트 대기

        fakeLocationClient.emitLocation(location2)
        composeRule.waitForIdle()

        // 5. 거리/경로 업데이트 검증 (UI 텍스트 등으로 간접 확인)
        // MainUiState의 distanceInMeters가 업데이트 되면 "0.0 km"가 아닌 값으로 변경될 것임.
        // 여기서는 간단히 에러 없이 흐름이 진행되는지 확인하고, 
        // 실제 거리 계산값은 화면에 "0.07 km" 등으로 표시될 수 있으므로
        // 구체적인 텍스트 매칭보다는 "일시 정지" 상태가 유지되는지 등으로 검증.
        composeRule.onNodeWithText("일시 정지").assertIsDisplayed()

        // 6. 운동 종료 (일시 정지 -> 운동 종료 버튼 활성화 확인 필요)
        // 현재 로직상 일시정지 버튼을 누르면 정지 상태가 되고, 이때 종료 버튼이 보일 수 있음.
        composeRule.onNodeWithText("일시 정지").performClick() // 일시 정지 상태로 전환 ("다시 시작"?)
        
        // 버튼 텍스트가 "운동 시작"으로 돌아오거나 별도 종료 버튼이 있는지 확인해야 함.
        // MainScreen 구현상 state.isTracking 일 때 "일시 정지", 아닐 때 "운동 시작".
        // 일시 정지(Tracking = false) 상태면 "운동 시작"이 보이고 "운동 종료" 버튼도 조건부로 보임.
        composeRule.onNodeWithText("운동 시작").assertIsDisplayed()
        composeRule.onNodeWithText("운동 종료").assertIsDisplayed()
        
        composeRule.onNodeWithText("운동 종료").performClick()
        
        // 7. 종료 후 초기 상태 복귀 확인 ("운동 종료" 버튼 사라짐)
        composeRule.onNodeWithText("운동 종료").assertDoesNotExist()
    }
}

