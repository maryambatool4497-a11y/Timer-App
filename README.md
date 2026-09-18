# Timer App

A simple Android timer app built with Kotlin and Jetpack Compose. Supports start, stop, and reset, and keeps counting correctly even when the screen rotates.

## Screenshot

![Timer App Screenshot](screenshot.png)

## Project Structure

This app has two main files. `TimerViewModel.kt` holds all the timer logic and state (elapsed seconds, running status) — using a ViewModel here means the state survives configuration changes like screen rotation, since Android keeps the ViewModel instance alive even when the Activity is destroyed and recreated. `MainActivity.kt` contains the Compose UI: a `TimerScreen` composable that displays the formatted time, a heading, and three buttons (Start/Stop/Reset) wired to functions on the ViewModel. The UI observes the ViewModel's state directly using Compose's `mutableStateOf`, so the screen automatically recomposes whenever the timer updates.