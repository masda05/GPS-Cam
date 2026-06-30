package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val appFontSizeScale by viewModel.appFontSizeScale.collectAsState()

    // Profile updates state
    val profileName by viewModel.userProfileName.collectAsState()
    val profileCompany by viewModel.userProfileCompany.collectAsState()
    val profilePosition by viewModel.userProfilePosition.collectAsState()

    var nameInput by remember(profileName) { mutableStateOf(profileName) }
    var companyInput by remember(profileCompany) { mutableStateOf(profileCompany) }
    var posInput by remember(profilePosition) { mutableStateOf(profilePosition) }

    // App Preferences state
    val activeTemplate by viewModel.defaultWatermarkTemplate.collectAsState()
    val accuracyThreshold by viewModel.gpsAccuracyThreshold.collectAsState()

    // Custom Watermark Config values
    val customSize by viewModel.customFontSize.collectAsState()
    val customColorVal by viewModel.customColor.collectAsState()
    val customOpacityVal by viewModel.customOpacity.collectAsState()
    val customPosVal by viewModel.customPosition.collectAsState()

    val watermarkFontSizeScale by viewModel.watermarkFontSizeScale.collectAsState()
    val showWatermarkMap by viewModel.showWatermarkMap.collectAsState()

    val watermarkColorPalette = listOf(
        android.graphics.Color.WHITE to "White",
        android.graphics.Color.YELLOW to "Yellow",
        android.graphics.Color.GREEN to "Green",
        android.graphics.Color.CYAN to "Cyan",
        android.graphics.Color.RED to "Red",
        android.graphics.Color.parseColor("#E040FB") to "Purple"
    )

    // Trigger sensors register
    LaunchedEffect(Unit) {
        viewModel.resumeSensors()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PREFERENCES CONTROL",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                .background(if (isDark) Color(0xFF0D161C) else Color(0xFFF8FAFC))
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Section 1: User profile modification card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF15222E) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "INSPECTOR PROFILE IDENTITY",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_name_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = companyInput,
                        onValueChange = { companyInput = it },
                        label = { Text("Company", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = posInput,
                        onValueChange = { posInput = it },
                        label = { Text("Professional Title", fontSize = 12.sp) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank() && companyInput.isNotBlank() && posInput.isNotBlank()) {
                                viewModel.saveUserProfile(nameInput.trim(), companyInput.trim(), posInput.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1),
                            contentColor = if (isDark) androidx.compose.ui.graphics.Color.Black else androidx.compose.ui.graphics.Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("save_profile_button")
                    ) {
                        Text(
                            "COMMIT ACCOUNT UPDATES",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Section 2: Watermark Layout Selectors
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF15222E) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Handyman,
                            contentDescription = null,
                            tint = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "STAMP MARKING SCHEME",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Active Watermark Template Scheme:", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Column of check buttons
                    listOf(
                        "A" to "Professional Corporate (Background Plate)",
                        "B" to "Minimalist Overlay (Clean Frame Drop-shadow)",
                        "C" to "Heavy Construction ( caution yellow strip headers)",
                        "D" to "Geodetic Surveyor (Compass Centering Reticle)",
                        "E" to "Custom Layout (Tweak size, position, and transparency)"
                    ).forEach { (code, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateDefaultTemplate(code) }
                                .padding(vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = (activeTemplate == code),
                                onClick = { viewModel.updateDefaultTemplate(code) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Tweak panel if Template E "Custom" is selected!
                    if (activeTemplate == "E") {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = if (isDark) Color(0xFF263544) else Color(0xFFECEFF1))
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            "DYNAMIC CUSTOM CONFIGURATORS:",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom font size slider
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Canvas Font Scale", fontSize = 11.sp, color = Color.Gray)
                                Text("${customSize.toInt()} dp", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = customSize,
                                onValueChange = { viewModel.updateCustomWatermarkConfig(it, customColorVal, customOpacityVal, customPosVal) },
                                valueRange = 10f..25f,
                                colors = SliderDefaults.colors(
                                    thumbColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1),
                                    activeTrackColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                                )
                            )
                        }

                        // Custom opacity slider
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Transparency", fontSize = 11.sp, color = Color.Gray)
                                Text("${(customOpacityVal * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Slider(
                                value = customOpacityVal,
                                onValueChange = { viewModel.updateCustomWatermarkConfig(customSize, customColorVal, it, customPosVal) },
                                valueRange = 0.2f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1),
                                    activeTrackColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                                )
                            )
                        }

                        // Custom Position dropdown/row selection
                        Text("Text Align Placement Position:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("TOP_LEFT", "TOP_RIGHT", "BOTTOM_LEFT", "BOTTOM_RIGHT").forEach { pos ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = if (customPosVal == pos) {
                                                if (isDark) Color(0xFF1E3228) else Color(0xFFE3F2FD)
                                            } else {
                                                if (isDark) Color(0xFF1F2E3C) else Color(0xFFECEFF1)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { viewModel.updateCustomWatermarkConfig(customSize, customColorVal, customOpacityVal, pos) }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(pos.replace("_", " "), fontSize = 8.sp, fontWeight = FontWeight.Bold, color = if (customPosVal == pos) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)) else Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Color picker
                        Text("Overlay Palette Accent:", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            watermarkColorPalette.forEach { (colorVal, name) ->
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(androidx.compose.ui.graphics.Color(colorVal))
                                        .border(
                                            width = if (customColorVal == colorVal) 3.dp else 1.dp,
                                            color = if (customColorVal == colorVal) (if (isDark) Color(0xFF00E676) else Color.Black) else Color.Gray,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.updateCustomWatermarkConfig(customSize, colorVal, customOpacityVal, customPosVal) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = if (isDark) Color(0xFF263544) else Color(0xFFECEFF1))
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "UNIFIED WATERMARK PREFERENCES (ALL SCHEMES):",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Watermark Font Size Scale Slider
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Watermark Font Scale Sizing", fontSize = 11.sp, color = Color.Gray)
                            Text(String.format("%.2fx", watermarkFontSizeScale), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Slider(
                            value = watermarkFontSizeScale,
                            onValueChange = { viewModel.updateWatermarkFontSizeScale(it) },
                            valueRange = 0.8f..2.5f,
                            colors = SliderDefaults.colors(
                                thumbColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1),
                                activeTrackColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Mini Map dynamic switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.updateShowWatermarkMap(!showWatermarkMap) }
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Show Mini Vector Map", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Draws dynamic localized compass map coordinates", fontSize = 10.sp, color = Color.Gray)
                        }
                        Switch(
                            checked = showWatermarkMap,
                            onCheckedChange = { viewModel.updateShowWatermarkMap(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1),
                                checkedTrackColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF1E3228) else androidx.compose.ui.graphics.Color(0xFFBBDEFB)
                            )
                        )
                    }
                }
            }

            // Section 3: GPS Alert Threshold
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF15222E) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.GpsFixed,
                            contentDescription = null,
                            tint = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "FIELD SECURITY & ALARMS",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("GPS Precision Alert Boundary", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("± ${accuracyThreshold.toInt()} meters", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1))
                    }
                    Text("Warn camera viewfinders if the satellite precision buffer exceeds this threshold.", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = accuracyThreshold,
                        onValueChange = { viewModel.updateGpsThreshold(it) },
                        valueRange = 5f..50f,
                        colors = SliderDefaults.colors(
                            thumbColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1),
                            activeTrackColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1)
                        )
                    )
                }
            }

            // Section 4: App Theme control Toggle
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF15222E) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Display Mode", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Optimize outdoor readability", fontSize = 11.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.setDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF0288D1),
                            checkedTrackColor = (if (isDark) androidx.compose.ui.graphics.Color(0xFF00E676) else androidx.compose.ui.graphics.Color(0xFF01579B)).copy(alpha = 0.5f)
                        )
                    )
                }
            }

            // Section 5: App UI Font Size Adjuster
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF15222E) else Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.testTag("font_size_setting_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = null,
                            tint = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "APP DISPLAY FONT SIZE",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        "Configure the readability sizing for all labels & parameters in field inspections.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0.85f to "Small",
                            1.0f to "Regular",
                            1.15f to "Large",
                            1.30f to "X-Large"
                        ).forEach { (scale, name) ->
                            val isSelected = appFontSizeScale == scale
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (isSelected) {
                                            if (isDark) Color(0xFF1F2E3C) else Color(0xFFE2E8F0)
                                        } else {
                                            if (isDark) Color(0xFF0D161C) else Color(0xFFF1F5F9)
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)) else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { viewModel.updateAppFontSizeScale(scale) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)) else Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
