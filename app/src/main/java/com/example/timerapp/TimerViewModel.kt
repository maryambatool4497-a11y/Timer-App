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

    // --- Stopwatch (counts up) ---
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

    // --- Countdown (counts down from a set time) ---
    var remainingSeconds by mutableStateOf(0)
        private set
    var countdownTotal by mutableStateOf(0)
        private set
    var isCountdownRunning by mutableStateOf(false)
        private set
    var isCountdownFinished by mutableStateOf(false)
        private set
    private var countdownJob: Job? = null

    fun startCountdown(totalSeconds: Int) {
        if (totalSeconds <= 0) return
        countdownTotal = totalSeconds
        remainingSeconds = totalSeconds
        isCountdownFinished = false
        resumeCountdown()
    }

    fun resumeCountdown() {
        if (remainingSeconds <= 0) return
        isCountdownRunning = true
        countdownJob = viewModelScope.launch {
            while (remainingSeconds > 0 && isCountdownRunning) {
                delay(1000L)
                remainingSeconds--
            }
            if (remainingSeconds <= 0) {
                isCountdownFinished = true
            }
            isCountdownRunning = false
        }
    }

    fun pauseCountdown() {
        isCountdownRunning = false
        countdownJob?.cancel()
    }

    fun resetCountdown() {
        isCountdownRunning = false
        countdownJob?.cancel()
        remainingSeconds = countdownTotal
        isCountdownFinished = false
    }
}