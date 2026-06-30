package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Project
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectManagementScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    // State sensor resume
    LaunchedEffect(Unit) {
        viewModel.resumeSensors()
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("projects_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "PROJECT DIRECTORY",
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
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                contentColor = if (isDark) Color.Black else Color.White,
                modifier = Modifier.testTag("add_project_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Project")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) Color(0xFF0D161C) else Color(0xFFF8FAFC))
                .padding(innerPadding)
        ) {
            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No Survey Projects Configured Yet.\nTap + to add.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(projects) { project ->
                        // Calculate photo stamp count for this project
                        val stampCount = remember(photos, project.id) {
                            photos.count { it.projectId == project.id }
                        }

                        ProjectCardItem(
                            project = project,
                            stamps = stampCount,
                            isActive = project.id == selectedProjectId,
                            isDark = isDark,
                            onSelect = { viewModel.selectProject(project.id) },
                            onDelete = { viewModel.deleteProject(project) }
                        )
                    }
                }
            }

            // Create New Project Dialog overlay
            if (showCreateDialog) {
                CreateProjectDialog(
                    isDark = isDark,
                    onDismiss = { showCreateDialog = false },
                    onConfirm = { name, desc ->
                        viewModel.addNewProject(name, desc)
                        showCreateDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ProjectCardItem(
    project: Project,
    stamps: Int,
    isActive: Boolean,
    isDark: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                if (isDark) Color(0xFF1E3228) else Color(0xFFE3F2FD)
            } else {
                if (isDark) Color(0xFF15222E) else Color.White
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isActive) Icons.Default.FolderOpen else Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (isActive) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)) else Color.Gray,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = project.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                }

                if (isActive) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isDark) Color(0x2200E676) else Color(0x220288D1),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ACTIVE TAG",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = project.description,
                fontSize = 12.sp,
                color = if (isDark) Color(0xFF8899A6) else Color(0xFF555555)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = if (isDark) Color(0xFF263544) else Color(0xFFECEFF1))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column {
                        Text("CAPTURED STAMPS", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text("$stamps files", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isActive) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)) else (if (isDark) Color.White else Color.Black))
                    }
                    Column {
                        Text("LEAD INSPECTOR", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(project.inspectorName.ifEmpty { "System Assigned" }, fontSize = 12.sp, fontWeight = FontWeight.Normal, color = if (isDark) Color.White else Color.Black)
                    }
                }

                // Delete Project Action if not active
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Clear,
                        contentDescription = "Delete",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CreateProjectDialog(
    isDark: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var err by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && desc.isNotBlank()) {
                        onConfirm(name.trim(), desc.trim())
                    } else {
                        err = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1),
                    contentColor = if (isDark) Color.Black else Color.White
                )
            ) {
                Text("CREATE DIRECTORY", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        },
        title = {
            Text(
                "NEW SURVEY PROJECT",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Inject a certified project sector folder to tag cameras and compile reports.", fontSize = 12.sp)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; err = false },
                    label = { Text("Project Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_name_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it; err = false },
                    label = { Text("Scope Description") },
                    minLines = 2,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("project_desc_input")
                )

                if (err) {
                    Text("All dialog fields are required.", color = Color.Red, fontSize = 11.sp)
                }
            }
        }
    )
}
