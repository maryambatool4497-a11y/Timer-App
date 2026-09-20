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
import androidx.compose.foundation.layout.*
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
            onBackClick = { currentScreen = "stopwatch" }
        )
    }
}

@Composable
fun SetTimerScreen(onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Set Timer Screen (coming soon)",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBackClick,
                colors = ButtonDefaults.buttonColors(containerColor = ResetGray)
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