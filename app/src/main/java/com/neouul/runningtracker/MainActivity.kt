package com.neouul.runningtracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.neouul.runningtracker.ui.theme.RunningTrackerTheme
import com.neouul.runningtracker.core.util.Constants.ACTION_SHOW_TRACKING_SCREEN
import com.neouul.runningtracker.presentation.screen.main.MainRoot
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RunningTrackerTheme {
                MainRoot()
            }
        }

        navigateToTrackingFragmentIfNeeded(intent)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }


    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {
        if (intent?.action == ACTION_SHOW_TRACKING_SCREEN) {
            // 이미 Content가 MainScreen이므로 별도 네비게이션은 필요 없으나
            // 추후 다중 화면 구성 시 여기서 처리
        }
    }
}
