package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.entity.PhotoMetadata
import com.example.data.entity.Project
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val isDark by viewModel.isDarkMode.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val activePhoto by viewModel.activePhoto.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterId by remember { mutableIntStateOf(-1) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    var isSelectMode by remember { mutableStateOf(false) }
    val selectedPhotos = remember { mutableStateListOf<PhotoMetadata>() }

    // Start sensors
    LaunchedEffect(Unit) {
        viewModel.resumeSensors()
    }

    val filteredPhotos = remember(photos, searchQuery, selectedFilterId, projects) {
        photos.filter { photo ->
            // Filter by search query (address or device name)
            val matchesQuery = searchQuery.isBlank() || 
                    photo.address.contains(searchQuery, ignoreCase = true) || 
                    photo.deviceModel.contains(searchQuery, ignoreCase = true)

            // Filter by project ID
            val matchesProject = selectedFilterId == -1 || photo.projectId == selectedFilterId

            matchesQuery && matchesProject
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("gallery_screen"),
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectMode) {
                        Text(
                            "${selectedPhotos.size} SELECTED",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                        )
                    } else {
                        Text(
                            "DISPATCH PHOTO LIBRARY",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                },
                navigationIcon = {
                    if (isSelectMode) {
                        IconButton(onClick = {
                            isSelectMode = false
                            selectedPhotos.clear()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Exit Selection")
                        }
                    } else {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (isSelectMode) {
                        // Bulk Save option
                        IconButton(
                            onClick = {
                                if (selectedPhotos.isNotEmpty()) {
                                    viewModel.savePhotosToGallery(selectedPhotos.toList())
                                    isSelectMode = false
                                    selectedPhotos.clear()
                                }
                            },
                            enabled = selectedPhotos.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Save Selected to Gallery",
                                tint = if (selectedPhotos.isNotEmpty()) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)) else Color.Gray
                            )
                        }

                        // Bulk Delete Option
                        IconButton(
                            onClick = {
                                if (selectedPhotos.isNotEmpty()) {
                                    viewModel.deletePhotos(selectedPhotos.toList())
                                    isSelectMode = false
                                    selectedPhotos.clear()
                                }
                            },
                            enabled = selectedPhotos.isNotEmpty()
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = if (selectedPhotos.isNotEmpty()) Color.Red else Color.Gray
                            )
                        }
                    } else {
                        // Enter selection mode toggle
                        IconButton(onClick = { isSelectMode = true }) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Enable Select Mode",
                                tint = Color.Gray
                            )
                        }

                        // Filter option menu trigger
                        Box {
                            IconButton(onClick = { filterMenuExpanded = true }) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    tint = if (selectedFilterId != -1) (if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)) else Color.Gray
                                )
                            }
                            DropdownMenu(
                                expanded = filterMenuExpanded,
                                onDismissRequest = { filterMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Display All Projects") },
                                    onClick = {
                                        selectedFilterId = -1
                                        filterMenuExpanded = false
                                    }
                                )
                                projects.forEach { project ->
                                    DropdownMenuItem(
                                        text = { Text(project.name) },
                                        onClick = {
                                            selectedFilterId = project.id
                                            filterMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
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
                .padding(16.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by address or coordinates text...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("gallery_search")
            )

            if (filteredPhotos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No records found matching criteria",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) Color.White else Color.Black,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try clearing queries or capturing high-res photo inside active projects directory.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredPhotos) { photo ->
                        val isSelected = selectedPhotos.contains(photo)
                        GalleryGridItem(
                            photo = photo,
                            projects = projects,
                            isSelected = isSelected,
                            isSelectMode = isSelectMode,
                            onClick = {
                                if (isSelectMode) {
                                    if (isSelected) {
                                        selectedPhotos.remove(photo)
                                    } else {
                                        selectedPhotos.add(photo)
                                    }
                                } else {
                                    viewModel.setActivePhoto(photo)
                                }
                            },
                            onLongClick = {
                                if (!isSelectMode) {
                                    isSelectMode = true
                                    selectedPhotos.add(photo)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Expanded Bottom Sheet Dialog detail overlay
        if (activePhoto != null) {
            PhotoDetailViewOverlay(
                activePhoto!!,
                projects,
                viewModel,
                isDark,
                onClose = { viewModel.setActivePhoto(null) }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryGridItem(
    photo: PhotoMetadata,
    projects: List<Project>,
    isSelected: Boolean,
    isSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val project = remember(projects, photo.projectId) {
        projects.find { it.id == photo.projectId }
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) Color(0xFF00E676) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        val fileRef = File(photo.photoPath)
        if (fileRef.exists()) {
            AsyncImage(
                model = fileRef,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = Color.LightGray)
            }
        }

        // Selection overlay (dark tint plus indicator)
        if (isSelectMode) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isSelected) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.1f))
            ) {
                Card(
                    shape = RoundedCornerShape(percent = 50),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF00E676) else Color.Black.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom descriptive strip
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.65f))
                .padding(4.dp)
        ) {
            Column {
                Text(
                    text = project?.name ?: "General Work",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E676),
                    maxLines = 1
                )
                Text(
                    text = String.format("%.4f, %.4f", photo.latitude, photo.longitude),
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailViewOverlay(
    photo: PhotoMetadata,
    projects: List<Project>,
    viewModel: MainViewModel,
    isDark: Boolean,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val project = remember(projects, photo.projectId) {
        projects.find { it.id == photo.projectId } ?: Project(999, "General Surveys", "", "", "")
    }

    val formattedDate = remember(photo.timestamp) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        sdf.format(Date(photo.timestamp))
    }

    ModalBottomSheet(
        onDismissRequest = onClose,
        containerColor = if (isDark) Color(0xFF15222E) else Color.White,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 48.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AUDIT STAMP DETAILS",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color(0xFF00E676) else Color(0xFF0288D1)
                    )
                    Text(
                        text = project.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black
                    )
                }
                
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Big Photo Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                val file = File(photo.photoPath)
                if (file.exists()) {
                    AsyncImage(
                        model = file,
                        contentDescription = "Stamped Dispatch Photo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Metadata spreadsheet table
            Text(
                "VERIFIED METADATA READOUT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (isDark) Color(0xFF1F2E3C) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailParamRow("GPS Position", "${String.format("%.6f", photo.latitude)}, ${String.format("%.6f", photo.longitude)} (Acc: ±${String.format("%.1fm", photo.accuracy)})", isDark)
                DetailParamRow("Altitude (EGM)", "${String.format("%.1f", photo.altitude)} m", isDark)
                DetailParamRow("Azimuth/Heading", "${photo.bearing.toInt()}° North", isDark)
                DetailParamRow("Device Specs", "Android ${photo.androidVersion} | Model ${photo.deviceModel}", isDark)
                
                // Address wrapping
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Resolved Location", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    Text(
                        photo.address.ifEmpty { "N/A" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else Color.Black,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(180.dp),
                        maxLines = 2
                    )
                }
                
                DetailParamRow("Stamped Clock", formattedDate, isDark)
                
                // Mock Flag Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Security Scan Status", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                    if (photo.isMockLocation) {
                        Box(
                            modifier = Modifier
                                .background(Color.Red, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("⚠️ SPOOFING WARNING", fontSize = 8.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("SECURITY INTEGRITY SAFE", fontSize = 11.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action rows: Export PDF, Mock Sync, Delete Trash
            Button(
                onClick = {
                    viewModel.exportPdfReport(photo) { pdfFile ->
                        if (pdfFile != null && pdfFile.exists()) {
                            // Present direct Android Intent path to read the generated PDF!
                            try {
                                val uri: Uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    pdfFile
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share GPS Dispatch PDF Report"))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
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
                    .testTag("pdf_report_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("EXPORT PDF FIELD REPORT", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            FilledTonalButton(
                onClick = {
                    viewModel.savePhotoToGallery(photo)
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isDark) Color(0xFF1F2E3C) else Color(0xFFE2E8F0),
                    contentColor = if (isDark) Color.White else Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_to_gallery_button")
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("SAVE IMAGE TO GALLERY (JPEG)", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Sync button
                OutlinedButton(
                    onClick = { viewModel.syncPhoto(photo) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) Color.White else Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                ) {
                    Icon(
                        if (photo.synced) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = if (photo.synced) Color(0xFF00E676) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (photo.synced) "SYNCED" else "CLOUD SYNC", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }

                // Delete trash button
                Button(
                    onClick = {
                        viewModel.deletePhoto(photo)
                        onClose()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("delete_photo_button")
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("DELETE RECORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun DetailParamRow(label: String, value: String, isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isDark) Color.White else Color.Black)
    }
}
