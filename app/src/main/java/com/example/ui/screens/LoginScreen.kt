package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onNavigateToDashboard: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var company by remember { mutableStateOf("") }
    var position by remember { mutableStateOf("") }
    
    var showError by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF1C2833))
                    } else {
                        listOf(Color(0xFFECE9E6), Color(0xFFFFFFFF))
                    }
                )
            )
            .testTag("login_screen"),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF15222E).copy(alpha = 0.9f) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "FIELD SIGN-IN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                    letterSpacing = 1.sp
                )
                
                Text(
                    text = "Set inspector profile for watermarks",
                    fontSize = 12.sp,
                    color = if (isDark) Color(0xFF8899A6) else Color(0xFF666666),
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("Inspector Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Name") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                        focusedLabelColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it; showError = false },
                    label = { Text("Company Name") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = "Company") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                        focusedLabelColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("company_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it; showError = false },
                    label = { Text("Professional Position") },
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = "Position") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                        focusedLabelColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("position_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                if (showError) {
                    Text(
                        text = "All profile fields are mandatory for audit logs.",
                        color = Color.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && company.isNotBlank() && position.isNotBlank()) {
                            viewModel.saveUserProfile(name.trim(), company.trim(), position.trim())
                            onNavigateToDashboard()
                        } else {
                            showError = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                        contentColor = if (isDark) Color.Black else Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("login_button")
                ) {
                    Text(
                        text = "INITIALIZE TERMINAL",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
