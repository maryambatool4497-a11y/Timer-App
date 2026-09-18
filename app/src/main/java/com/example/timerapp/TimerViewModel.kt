package com.example.timerapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerViewModel : ViewModel() {

    var elapsedSeconds by mutableStateOf(0)
        private set

    var isRunning by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    fun start() {
        if (isRunning) return
        isRunning = true
        timerJob = viewModelScope.launch {
            while (isRunning) {
                delay(1000L)
                elapsedSeconds++
            }
        }
    }

    fun stop() {
        isRunning = false
        timerJob?.cancel()
    }

    fun reset() {
        stop()
        elapsedSeconds = 0
    }
}