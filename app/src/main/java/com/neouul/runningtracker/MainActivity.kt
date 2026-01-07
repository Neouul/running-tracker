package com.neouul.runningtracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
                val context = androidx.compose.ui.platform.LocalContext.current
                val viewModel: com.neouul.runningtracker.presentation.screen.main.MainViewModel = androidx.hilt.navigation.compose.hiltViewModel()
                val state by viewModel.state.collectAsState()

                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    viewModel.onAction(com.neouul.runningtracker.presentation.screen.main.MainAction.OnPermissionsResult(permissions.values.all { it }))
                }

                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (!state.hasPermissions && !com.neouul.runningtracker.core.util.TrackingUtility.hasLocationPermissions(context)) {
                        val permissions = mutableListOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                            permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    } else if (!state.hasPermissions) {
                        viewModel.onAction(com.neouul.runningtracker.presentation.screen.main.MainAction.OnPermissionsResult(true))
                    }
                }

                MainRoot(viewModel = viewModel)
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
