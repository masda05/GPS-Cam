package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.entity.PhotoMetadata
import com.example.data.entity.Project
import com.example.ui.components.GPSData
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    onNavigateToCamera: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onNavigateToProjects: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val gpsData by viewModel.gpsData.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val selectedId by viewModel.selectedProjectId.collectAsState()
    val inspectorName by viewModel.userProfileName.collectAsState()
    val companyName by viewModel.userProfileCompany.collectAsState()

    val context = LocalContext.current

    // Trigger resume updates on screen reveal
    LaunchedEffect(Unit) {
        viewModel.resumeSensors()
    }

    val activeProject = remember(projects, selectedId) {
        projects.find { it.id == selectedId }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "GPS CAMERA PRO",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setDarkMode(!isDark) }) {
                        Icon(
                            if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Dark Mode"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color(0xFF111E26) else Color(0xFFF5F7FA)
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0B141A) else Color(0xFFF9FBFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. Profile and Project Briefing Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF162530) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(
                                    if (isDark) Color(0xFF00E676).copy(alpha = 0.15f)
                                    else Color(0xFF0288D1).copy(alpha = 0.15f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Engineering,
                                contentDescription = null,
                                tint = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = inspectorName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else Color.Black
                            )
                            Text(
                                text = companyName,
                                fontSize = 11.sp,
                                color = if (isDark) Color(0xFF8899A6) else Color(0xFF666666)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = if (isDark) Color(0xFF23323D) else Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ACTIVE REPORT TAG PROJECT:",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (activeProject != null) {
                        Text(
                            text = activeProject.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            text = activeProject.description,
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF8899A6) else Color(0xFF666666),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "No Active Project Selected",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = "Configure a project inside Project directory",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF8899A6) else Color(0xFF666666)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Real-time Telemetry & Compass Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Compass (Left)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF162530) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "GYRO COMPASS",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                        )
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        // Drawing Rotating Compass Plate
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .rotate(-gpsData.heading), // Rotate against heading degree
                            contentAlignment = Alignment.Center
                        ) {
                            val ringColor = if (isDark) Color(0xFF23323D) else Color(0xFFE2E8F0)
                            val pointerColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)

                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val r = size.minDimension / 2
                                val center = Offset(size.width / 2, size.height / 2)

                                // Plate Circle
                                drawCircle(color = ringColor, radius = r, style = Stroke(2.dp.toPx()))
                                
                                // Cardinal points
                                drawCircle(color = pointerColor.copy(alpha = 0.3f), radius = 2.dp.toPx(), center = Offset(center.x, center.y - r + 10f))
                            }

                            // Heading indicators overlay
                            Text("N", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red, modifier = Modifier.align(Alignment.TopCenter))
                            Text("S", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, modifier = Modifier.align(Alignment.BottomCenter))
                            Text("W", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, modifier = Modifier.align(Alignment.CenterStart))
                            Text("E", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black, modifier = Modifier.align(Alignment.CenterEnd))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "${gpsData.heading.toInt()}° North",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isDark) Color.White else Color.Black
                        )
                    }
                }

                // GPS Telemetry (Right)
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(180.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF162530) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "GPS TELEMETRY",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                            )
                            
                            // Mock alert beacon
                            if (gpsData.isMock) {
                                Box(
                                    modifier = Modifier
                                        .background(Color.Red, CircleShape)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("FAKE", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val statusColor = when (gpsData.signalStrength) {
                                        3 -> Color(0xFF00E676)
                                        2 -> Color(0xFFFFD54F)
                                        else -> Color.Red
                                    }
                                    Box(modifier = Modifier.size(6.dp).background(statusColor, CircleShape))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        if (gpsData.hasLocation) "LINKED" else "NO LINK",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) Color.White else Color.Black
                                    )
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            HorizontalTelemetryRow("LAT", String.format("%.6f", gpsData.latitude), isDark)
                            HorizontalTelemetryRow("LNG", String.format("%.6f", gpsData.longitude), isDark)
                            HorizontalTelemetryRow("ALT", "${String.format("%.0f", gpsData.altitude)} m", isDark)
                            HorizontalTelemetryRow("PRE", "± ${String.format("%.1f", gpsData.accuracy)}m", isDark)
                        }

                        // Address string truncated
                        Text(
                            text = gpsData.address,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isDark) Color(0xFF00E676).copy(alpha = 0.8f) else Color(0xFF0288D1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Grid Navigation Modules
            Text(
                "FIELD TERMINAL UTILITIES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isDark) Color(0xFF8899A6) else Color(0xFF555555),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Dynamic grid layout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GridMenuCard(
                    title = "Take Stamped Photo",
                    subtitle = "Open Camera view",
                    icon = Icons.Default.Camera,
                    tint = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                    modifier = Modifier.weight(1f),
                    testTag = "nav_camera_button",
                    onClick = onNavigateToCamera,
                    isDark = isDark
                )
                GridMenuCard(
                    title = "Photo Gallery",
                    subtitle = "Verify local files",
                    icon = Icons.Default.PhotoLibrary,
                    tint = Color(0xFFE040FB),
                    modifier = Modifier.weight(1f),
                    testTag = "nav_gallery_button",
                    onClick = onNavigateToGallery,
                    isDark = isDark
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GridMenuCard(
                    title = "Projects",
                    subtitle = "Change active tag",
                    icon = Icons.Default.Folder,
                    tint = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f),
                    testTag = "nav_projects_button",
                    onClick = onNavigateToProjects,
                    isDark = isDark
                )
                GridMenuCard(
                    title = "Settings",
                    subtitle = "Watermarks & preferences",
                    icon = Icons.Default.Settings,
                    tint = Color(0xFF78909C),
                    modifier = Modifier.weight(1f),
                    testTag = "nav_settings_button",
                    onClick = onNavigateToSettings,
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Map preview navigation card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF162530) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = onNavigateToMap,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nav_map_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Map,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Live Map Plotter",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black
                        )
                        Text(
                            "See captured markers and precision buffers",
                            fontSize = 11.sp,
                            color = if (isDark) Color(0xFF8899A6) else Color(0xFF666666)
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Recent Local Photo Stamps Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "RECENT STAMPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isDark) Color(0xFF8899A6) else Color(0xFF555555)
                )
                Text(
                    "${photos.size} total",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (photos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .background(
                            color = if (isDark) Color(0xFF162530).copy(alpha = 0.5f) else Color(0xFFEAEEF2),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ImageNotSupported,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "No stamped photos captured yet",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(photos.take(6)) { photo ->
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .clickable {
                                    // Highlight in gallery directly
                                    onNavigateToGallery()
                                }
                        ) {
                            val imgFile = File(photo.photoPath)
                            if (imgFile.exists()) {
                                AsyncImage(
                                    model = imgFile,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            // Bottom project tag
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Lat " + String.format("%.3f", photo.latitude),
                                    fontSize = 8.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.align(Alignment.Center),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HorizontalTelemetryRow(label: String, value: String, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            color = if (isDark) Color(0xFF8899A6) else Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color.White else Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridMenuCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    testTag: String,
    onClick: () -> Unit,
    isDark: Boolean
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(95.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF162530) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
            
            Column {
                Text(
                    title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    fontSize = 10.sp,
                    color = if (isDark) Color(0xFF8899A6) else Color(0xFF888888),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
