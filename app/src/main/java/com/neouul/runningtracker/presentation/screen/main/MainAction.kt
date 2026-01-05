package com.neouul.runningtracker.presentation.screen.main

sealed interface MainAction {
    object OnToggleRun : MainAction
    object OnFinishRun : MainAction
    data class OnPermissionsResult(val granted: Boolean) : MainAction
}