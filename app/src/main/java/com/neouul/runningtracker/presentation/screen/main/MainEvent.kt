package com.neouul.runningtracker.presentation.screen.main

sealed interface MainEvent {
    data class Error(val message: String) : MainEvent
    object RunFinished : MainEvent
    data class TriggerService(val action: String) : MainEvent
}
