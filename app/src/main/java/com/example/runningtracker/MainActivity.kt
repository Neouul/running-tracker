package com.example.runningtracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.runningtracker.ui.MainScreen
import com.example.runningtracker.ui.MainViewModel
import com.example.runningtracker.ui.theme.RunningTrackerTheme
import com.example.runningtracker.util.Constants.ACTION_SHOW_TRACKING_SCREEN
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RunningTrackerTheme {
                MainScreen(viewModel)
            }
        }

        navigateToTrackingFragmentIfNeeded(intent)
    }

    override fun onNewIntent(intent: Intent?) {
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