package com.example.timerapp

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppRoot(viewModel)
        }
    }
}

@Composable
fun AppRoot(viewModel: TimerViewModel) {
    var currentScreen by remember { mutableStateOf("stopwatch") }

    when (currentScreen) {
        "stopwatch" -> TimerScreen(
            viewModel = viewModel,
            onSetTimerClick = { currentScreen = "setTimer" }
        )
        "setTimer" -> SetTimerScreen(
            onBackClick = { currentScreen = "stopwatch" },
            onStartClick = { totalSeconds ->
                viewModel.startCountdown(totalSeconds)
                currentScreen = "countdown"
            }
        )
        "countdown" -> CountdownScreen(
            viewModel = viewModel,
            onBackClick = {
                viewModel.pauseCountdown()
                currentScreen = "setTimer"
            }
        )
    }
}

@Composable
fun SetTimerScreen(onBackClick: () -> Unit, onStartClick: (Int) -> Unit) {
    var selectedHours by remember { mutableStateOf(0) }
    var selectedMinutes by remember { mutableStateOf(0) }
    var selectedSeconds by remember { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Set Timer",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPicker(
                    range = 0..23,
                    selectedValue = selectedHours,
                    onValueChange = { selectedHours = it },
                    label = "H"
                )
                Text(":", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                NumberPicker(
                    range = 0..59,
                    selectedValue = selectedMinutes,
                    onValueChange = { selectedMinutes = it },
                    label = "M"
                )
                Text(":", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                NumberPicker(
                    range = 0..59,
                    selectedValue = selectedSeconds,
                    onValueChange = { selectedSeconds = it },
                    label = "S"
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val total = selectedHours * 3600 + selectedMinutes * 60 + selectedSeconds
                    onStartClick(total)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StartGreen)
            ) {
                Text("Start Timer", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = ResetGray)
            ) {
                Text("Back", color = Color.White)
            }
        }
    }
}

@Composable
fun NumberPicker(
    range: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    label: String
) {
    val listState = rememberLazyListState()
    val itemHeight = 48.dp
    val visibleItemsCount = 3
    var isInitialized by remember { mutableStateOf(false) }
    val flingBehavior = rememberSnapFlingBehavior(listState)

    LaunchedEffect(Unit) {
        val startIndex = (selectedValue - range.first).coerceIn(0, range.count() - 1)
        listState.scrollToItem(startIndex)
        isInitialized = true
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (isInitialized && !listState.isScrollInProgress) {
            val centerIndex = listState.firstVisibleItemIndex
            val newValue = range.first + centerIndex.coerceIn(0, range.count() - 1)
            if (newValue != selectedValue) {
                onValueChange(newValue)
            }
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .height(itemHeight * visibleItemsCount)
                .width(60.dp)
        ) {
            LazyColumn(
                state = listState,
                flingBehavior = flingBehavior,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = itemHeight * (visibleItemsCount / 2))
            ) {
                items(range.count()) { index ->
                    val value = range.first + index
                    Box(
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = String.format(Locale.US, "%02d", value),
                            color = if (value == selectedValue) AccentBlue else Color.Gray,
                            fontSize = if (value == selectedValue) 24.sp else 18.sp,
                            fontWeight = if (value == selectedValue) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun CountdownScreen(viewModel: TimerViewModel, onBackClick: () -> Unit) {
    val hours = viewModel.remainingSeconds / 3600
    val minutes = (viewModel.remainingSeconds % 3600) / 60
    val seconds = viewModel.remainingSeconds % 60
    val timeText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    LaunchedEffect(viewModel.isCountdownFinished) {
        if (viewModel.isCountdownFinished) {
            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 800)
            delay(900)
            toneGen.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = if (viewModel.isCountdownFinished) "⏰ Time's up!" else "Countdown",
                color = if (viewModel.isCountdownFinished) MilestoneGold else Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .background(SurfaceDark, shape = RoundedCornerShape(24.dp))
                    .padding(horizontal = 40.dp, vertical = 28.dp)
            ) {
                Text(
                    text = timeText,
                    color = AccentBlue,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (viewModel.isCountdownRunning) {
                    Button(
                        onClick = { viewModel.pauseCountdown() },
                        colors = ButtonDefaults.buttonColors(containerColor = StopRed)
                    ) {
                        Text("Pause", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = { viewModel.resumeCountdown() },
                        colors = ButtonDefaults.buttonColors(containerColor = StartGreen)
                    ) {
                        Text("Resume", color = Color.White)
                    }
                }
                Button(
                    onClick = { viewModel.resetCountdown() },
                    colors = ButtonDefaults.buttonColors(containerColor = ResetGray)
                ) {
                    Text("Reset", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Back", color = Color.White)
            }
        }
    }
}

val BackgroundDark = Color(0xFF0F172A)
val BackgroundPurple = Color(0xFF1E1B3A)
val BackgroundTeal = Color(0xFF0F2E2E)
val SurfaceDark = Color(0xFF1E293B)
val AccentBlue = Color(0xFF38BDF8)
val RingTrack = Color(0xFF334155)
val StartGreen = Color(0xFF22C55E)
val StopRed = Color(0xFFEF4444)
val ResetGray = Color(0xFF64748B)
val MilestoneGold = Color(0xFFFACC15)

@Composable
fun TimerScreen(viewModel: TimerViewModel, onSetTimerClick: () -> Unit) {
    val hours = viewModel.elapsedSeconds / 3600
    val minutes = (viewModel.elapsedSeconds % 3600) / 60
    val seconds = viewModel.elapsedSeconds % 60
    val timeText = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (viewModel.isRunning) 1.04f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = if (viewModel.isRunning) 1f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    val ringTarget = (viewModel.elapsedSeconds % 60) / 60f
    val ringProgress by animateFloatAsState(
        targetValue = ringTarget,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "ringProgress"
    )

    val colorTransition = rememberInfiniteTransition(label = "bgColor")
    val bgColor1 by colorTransition.animateColor(
        initialValue = BackgroundDark,
        targetValue = if (viewModel.isRunning) BackgroundPurple else BackgroundDark,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgColor1"
    )
    val bgColor2 by colorTransition.animateColor(
        initialValue = SurfaceDark,
        targetValue = if (viewModel.isRunning) BackgroundTeal else SurfaceDark,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bgColor2"
    )

    var milestoneText by remember { mutableStateOf("") }
    var showMilestone by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.elapsedSeconds) {
        val totalSeconds = viewModel.elapsedSeconds

        if (totalSeconds == 0) {
            showMilestone = false
        } else if (totalSeconds % 60 == 0) {
            val minuteCount = totalSeconds / 60
            milestoneText = "🔥 $minuteCount minute${if (minuteCount > 1) "s" else ""} focused!"
            showMilestone = true

            val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 500)
            delay(600)
            toneGen.release()

            delay(2000)
            showMilestone = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(bgColor1, bgColor2)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "My Timer",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = showMilestone, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = milestoneText,
                    color = MilestoneGold,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(230.dp)
            ) {
                Canvas(modifier = Modifier.size(230.dp)) {
                    val strokeWidth = 10.dp.toPx()
                    val ringSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = RingTrack,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = ringSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = AccentBlue,
                        startAngle = -90f,
                        sweepAngle = 360f * ringProgress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = ringSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .background(SurfaceDark, shape = RoundedCornerShape(24.dp))
                        .border(
                            width = 3.dp,
                            color = AccentBlue.copy(alpha = glowAlpha),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = timeText,
                        color = AccentBlue,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { viewModel.start() },
                    colors = ButtonDefaults.buttonColors(containerColor = StartGreen)
                ) {
                    Text("Start", color = Color.White)
                }
                Button(
                    onClick = { viewModel.stop() },
                    colors = ButtonDefaults.buttonColors(containerColor = StopRed)
                ) {
                    Text("Stop", color = Color.White)
                }
                Button(
                    onClick = { viewModel.reset() },
                    colors = ButtonDefaults.buttonColors(containerColor = ResetGray)
                ) {
                    Text("Reset", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onSetTimerClick,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Set Timer", color = Color.White)
            }
        }
    }
}