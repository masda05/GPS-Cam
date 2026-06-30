package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GPSData
import com.example.ui.viewmodel.MainViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val gpsData by viewModel.gpsData.collectAsState()

    var mapModeEnabled by remember { mutableStateOf(false) } // Default to high-tech satellite radar layout, user can toggle real map!

    // Start sensors
    LaunchedEffect(Unit) {
        viewModel.resumeSensors()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("map_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "GEOGRAPHIC SATELLITE PLOTTER",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Toggle Mode between Real Maps SDK and Tactical Radar Tracking View
                    Button(
                        onClick = { mapModeEnabled = !mapModeEnabled },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                            contentColor = if (isDark) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (mapModeEnabled) "RADAR MONITOR" else "GOOGLE MAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF111E26) else Color(0xFFF5F7FA)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0B141A) else Color(0xFFF0F4F8))
                .padding(innerPadding)
        ) {
            if (mapModeEnabled) {
                // REAL GOOGLE MAPS INTEGRATION USING MAPS-COMPOSE
                val userLatLng = LatLng(gpsData.latitude, gpsData.longitude)
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(userLatLng, 16f)
                }

                // Dynamically update camera target center when coordinates adjust
                LaunchedEffect(gpsData.latitude, gpsData.longitude) {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 16f)
                }

                val mapUiSettings = remember {
                    MapUiSettings(zoomControlsEnabled = true, compassEnabled = true)
                }
                val mapProperties = remember(isDark) {
                    MapProperties(
                        isMyLocationEnabled = false, // We will draw our own styled precision marker!
                        mapType = MapType.HYBRID
                    )
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    uiSettings = mapUiSettings,
                    properties = mapProperties
                ) {
                    // Drawing styled target point
                    Marker(
                        state = rememberMarkerState(position = userLatLng),
                        title = "Active Core",
                        snippet = "Accuracy ±${String.format("%.1f", gpsData.accuracy)}m"
                    )

                    // Draw GPS accuracy radius ring buffer
                    Circle(
                        center = userLatLng,
                        radius = gpsData.accuracy.toDouble(),
                        fillColor = if (isDark) Color(0x3300E676) else Color(0x330288D1),
                        strokeColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                        strokeWidth = 2f
                    )
                }
            } else {
                // TACTICAL MILITARY RETRO GPS RADAR SCANNER
                RadarTrackingTacticalView(gpsData, isDark)
            }

            // Bottom Floating Telemetry Metadata Sheet
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF15222E).copy(alpha = 0.95f) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "RECEIVER RESOLVED:",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF8899A6) else Color(0xFF555555)
                            )
                        }
                        
                        // Signal accuracy rating
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (gpsData.accuracy < 10) Color(0x2200E676) else Color(0x22FFB300),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (gpsData.accuracy < 10) "PRECISION SEALED" else "BUFFER WARNING",
                                fontSize = 8.sp,
                                color = if (gpsData.accuracy < 10) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)) else Color(0xFFFFB300),
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LATITUDE", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text(String.format("%.6f°", gpsData.latitude), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("LONGITUDE", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text(String.format("%.6f°", gpsData.longitude), fontSize = 15.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ALTITUDE (MSL)", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text("${String.format("%.1f", gpsData.altitude)} meters", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("EST. ACCURACY", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text("± ${String.format("%.1f", gpsData.accuracy)} meters", fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = if (gpsData.accuracy < 8f) Color(0xFF00E676) else Color.Red)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = if (isDark) Color(0xFF23323D) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = gpsData.address,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            color = if (isDark) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RadarTrackingTacticalView(gpsData: GPSData, isDark: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepRot"
    )

    val signalPulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 180.dp),
        contentAlignment = Alignment.Center
    ) {
        val sweepColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)

        // Radial coordinate mesh
        Canvas(
            modifier = Modifier
                .size(310.dp)
                .rotate(rotation)
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxR = size.minDimension / 2

            // Gradients sweep line
            drawLine(
                color = sweepColor,
                start = center,
                end = Offset(center.x + maxR * Math.cos(0.0).toFloat(), center.y + maxR * Math.sin(0.0).toFloat()),
                strokeWidth = 5f
            )
        }

        // Static overlay grid drawing
        Canvas(modifier = Modifier.size(310.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxR = size.minDimension / 2

            // Coordinate rings
            drawCircle(color = sweepColor.copy(alpha = 0.15f), radius = maxR, style = Stroke(2f))
            drawCircle(color = sweepColor.copy(alpha = 0.25f), radius = maxR * 0.7f, style = Stroke(2f))
            drawCircle(color = sweepColor.copy(alpha = 0.35f), radius = maxR * 0.4f, style = Stroke(2f))

            // Crosshair lines
            drawLine(sweepColor.copy(alpha = 0.2f), Offset(0f, center.y), Offset(size.width, center.y), 2f)
            drawLine(sweepColor.copy(alpha = 0.2f), Offset(center.x, 0f), Offset(center.x, size.height), 2f)

            // Bearing ticks around frame
            val ticks = 12
            for (j in 0 until ticks) {
                val angle = Math.toRadians((j * (360.0 / ticks)))
                val start = Offset(
                    center.x + (maxR - 20f) * Math.cos(angle).toFloat(),
                    center.y + (maxR - 20f) * Math.sin(angle).toFloat()
                )
                val end = Offset(
                    center.x + maxR * Math.cos(angle).toFloat(),
                    center.y + maxR * Math.sin(angle).toFloat()
                )
                drawLine(sweepColor.copy(alpha = 0.5f), start, end, 3f)
            }

            // Target core marker
            drawCircle(
                color = sweepColor,
                radius = 20f * signalPulse,
                center = center,
                alpha = signalPulse
            )
        }

        // HUD tactical labels around frame corners
        Box(modifier = Modifier.size(310.dp)) {
            Text("RX CORES: 09/12", fontSize = 8.sp, color = sweepColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart))
            Text("AQU-CHANNELS: SIGMA-3", fontSize = 8.sp, color = sweepColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopEnd))
            Text("COMPASS AZM: ${gpsData.heading.toInt()}°", fontSize = 8.sp, color = sweepColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomStart))
            Text("SATELLITE REF: GPS/GLO", fontSize = 8.sp, color = sweepColor, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomEnd))
        }
    }
}
