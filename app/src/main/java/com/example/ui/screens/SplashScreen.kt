package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    viewModel: MainViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val username by viewModel.userProfileName.collectAsState()

    // Animators
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val scaleProgress by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    LaunchedEffect(Unit) {
        viewModel.resumeSensors()
        delay(2500) // Beautiful cinematic transition delay
        if (username == "John Doe" || username.isBlank()) {
            onNavigateToLogin()
        } else {
            onNavigateToDashboard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                    } else {
                        listOf(Color(0xFFECE9E6), Color(0xFFFFFFFF))
                    }
                )
            )
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Radar sweeping visual canvas
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                val lineColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Circles
                    drawCircle(color = lineColor.copy(alpha = 0.15f), radius = radius, style = Stroke(2f))
                    drawCircle(color = lineColor.copy(alpha = 0.25f), radius = radius * 0.6f, style = Stroke(2f))
                    drawCircle(color = lineColor.copy(alpha = 0.40f), radius = radius * 0.3f, style = Stroke(2f))

                    // Crosshairs
                    drawLine(lineColor.copy(alpha = 0.3f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 2f)
                    drawLine(lineColor.copy(alpha = 0.3f), Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), 2f)

                    // Rotating sweep line
                    val angleRad = Math.toRadians(radarRotation.toDouble())
                    val sweepX = center.x + radius * Math.cos(angleRad).toFloat()
                    val sweepY = center.y + radius * Math.sin(angleRad).toFloat()
                    drawLine(lineColor, center, Offset(sweepX, sweepY), 5f)

                    // Pulse target dot
                    drawCircle(
                        color = lineColor,
                        radius = 12f * scaleProgress,
                        center = Offset(center.x + (radius * 0.5f), center.y - (radius * 0.2f)),
                        alpha = scaleProgress
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "GPS CAMERA PRO",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDark) Color.White else Color(0xFF1A1A1A),
                letterSpacing = 1.5.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.testTag("app_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Professional Field Documentation Suite",
                fontSize = 13.sp,
                color = if (isDark) Color(0xFF91A3B0) else Color(0xFF666666),
                letterSpacing = 0.5.sp
            )
        }

        // Subtitle bottom indicator
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 50.dp)
        ) {
            Text(
                text = "SECURE LOCAL OFF-GRID KERNEL",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isDark) Color(0xFF00E676).copy(alpha = 0.6f) else Color(0xFF0288D1).copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
        }
    }
}
