package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.components.GPSData
import com.example.ui.viewmodel.MainViewModel
import com.google.accompanist.permissions.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executor

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val gpsData by viewModel.gpsData.collectAsState()
    val defaultTemplate by viewModel.defaultWatermarkTemplate.collectAsState()

    // Permissions (Camera and fine Location)
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Trigger resume updates on screen reveal
    LaunchedEffect(Unit) {
        viewModel.resumeSensors()
    }

    if (permissionsState.allPermissionsGranted) {
        CameraXViewComponent(viewModel, gpsData, defaultTemplate, onNavigateBack, isDark)
    } else {
        // Permission Requesting Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0F171E) else Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "HARDWARE PERMISSIONS NEEDED",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isDark) Color.White else Color.Black
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "GPS Camera Pro requires high-accuracy GPS telemetry and secure camera lenses to stamp and audit photo records off-grid.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { permissionsState.launchMultiplePermissionRequest() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("AUTHORIZE SUBSYSTEMS", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CameraXViewComponent(
    viewModel: MainViewModel,
    gpsData: GPSData,
    template: String,
    onNavigateBack: () -> Unit,
    isDark: Boolean
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val flashAlpha = remember { androidx.compose.animation.core.Animatable(0f) }
    val shutterSound = remember { android.media.MediaActionSound() }

    LaunchedEffect(Unit) {
        shutterSound.load(android.media.MediaActionSound.SHUTTER_CLICK)
    }

    // Viewport control states
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableStateOf(ImageCapture.FLASH_MODE_OFF) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }

    // CameraX elements
    val previewView = remember { PreviewView(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }
    var cameraInfo: CameraInfo? by remember { mutableStateOf(null) }

    // Dynamic state bindings
    LaunchedEffect(lensFacing) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        cameraProvider.unbindAll()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setFlashMode(flashMode)
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            cameraControl = camera.cameraControl
            cameraInfo = camera.cameraInfo
            
            // Setup initial zoom ratio values
            zoomRatio = 1f
            cameraControl?.setZoomRatio(1f)
        } catch (exc: Exception) {
            exc.printStackTrace()
        }
    }

    // Capture Trigger processing
    val mainExecutor = ContextCompat.getMainExecutor(context)
    val takePictureAction = {
        val capture = imageCapture
        if (capture != null) {
            // Setup temp output file to cache
            val outputDirectory = context.cacheDir
            val filename = "CAP_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) + ".jpg"
            val photoFile = File(outputDirectory, filename)

            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            capture.takePicture(
                outputOptions,
                mainExecutor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        // Play camera click audio feedback
                        shutterSound.play(android.media.MediaActionSound.SHUTTER_CLICK)

                        // Trigger screen blink (flash overlay)
                        coroutineScope.launch {
                            flashAlpha.snapTo(0.8f)
                            flashAlpha.animateTo(0f, animationSpec = androidx.compose.animation.core.tween(300))
                        }

                        // Stamp coordinates to database-backed Canvas
                        viewModel.saveCapturedPhoto(photoFile, gpsData)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        exception.printStackTrace()
                    }
                }
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Camera control bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 24.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                contentAlignment = Alignment.Center
            ) {
                // Control Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flash swap
                    IconButton(
                        onClick = {
                            flashMode = when (flashMode) {
                                ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                                ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                                else -> ImageCapture.FLASH_MODE_OFF
                            }
                            imageCapture?.flashMode = flashMode
                        },
                        modifier = Modifier.background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            imageVector = when (flashMode) {
                                ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                                else -> Icons.Default.FlashOff
                            },
                            contentDescription = "Flash Toggle",
                            tint = Color.White
                        )
                    }

                    // Main Capture Trigger! (TestTag: shutter_button)
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .background(Color.White, CircleShape)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { takePictureAction() }
                            .testTag("shutter_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = "Shutter Capture",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Camera Swapper (Back/Front)
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier.background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(
                            Icons.Default.FlipCameraAndroid,
                            contentDescription = "Swap Lenses",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
        ) {
            // 1. Live CameraX Viewfinder
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // Back button floating top left
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            // 2. Zoom Overlay Controller floating (Zoom ratio)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("ZOOM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                listOf(1f, 2f, 5f).forEach { zoom ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (zoomRatio == zoom) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1))
                                else Color.Transparent,
                                CircleShape
                            )
                            .border(1.dp, Color.White, CircleShape)
                            .clickable {
                                zoomRatio = zoom
                                cameraControl?.setZoomRatio(zoom)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${zoom.toInt()}x",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (zoomRatio == zoom) Color.Black else Color.White
                        )
                    }
                }
            }

            // 3. Simulated Watermark Blueprint overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "WATERMARK PREVIEW (${if (template == "A") "Corporate" else if (template == "B") "Minimalist" else if (template == "C") "Construction" else "Surveyor"})",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E676),
                            fontFamily = FontFamily.Monospace
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(5.dp).background(Color(0xFF00E676), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GPS LIVE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text("GPS Coordinates: ${String.format("%.6f", gpsData.latitude)}, ${String.format("%.6f", gpsData.longitude)}", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                    Text("Altitude: ${String.format("%.1f m", gpsData.altitude)} | Acc: ±${String.format("%.1f m", gpsData.accuracy)}", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                    Text("Compass Heading: ${gpsData.heading.toInt()}° North", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                    Text("Secured Timestamp: " + SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault()).format(Date()), fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                }
            }

            // 4. Mock location warning overlay
            if (gpsData.isMock) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(top = 70.dp)
                        .padding(horizontal = 24.dp)
                        .background(Color.Red, RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "⚠️ Possible fake location detected.",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // 5. White Shutter Flash overlay
            if (flashAlpha.value > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = flashAlpha.value))
                )
            }
        }
    }
}
