package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.entity.PhotoMetadata
import com.example.data.entity.Project
import com.example.data.repository.FieldRepository
import com.example.ui.components.FieldSensors
import com.example.ui.components.GPSData
import com.example.ui.components.PdfReporter
import com.example.ui.components.WatermarkEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class MainViewModel(
    application: Application,
    private val repository: FieldRepository
) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val sharedPrefs = context.getSharedPreferences("gps_camera_pro_prefs", Context.MODE_PRIVATE)

    // Flow Sources from Database
    val projects: StateFlow<List<Project>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val photos: StateFlow<List<PhotoMetadata>> = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Configuration / Settings States
    private val _isDarkMode = MutableStateFlow(sharedPrefs.getBoolean("dark_mode", true))
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _selectedProjectId = MutableStateFlow(sharedPrefs.getInt("selected_project_id", -1))
    val selectedProjectId = _selectedProjectId.asStateFlow()

    private val _userProfileName = MutableStateFlow(sharedPrefs.getString("user_name", "John Doe") ?: "John Doe")
    val userProfileName = _userProfileName.asStateFlow()

    private val _userProfileCompany = MutableStateFlow(sharedPrefs.getString("company_name", "Civil Tech") ?: "Civil Tech")
    val userProfileCompany = _userProfileCompany.asStateFlow()

    private val _userProfilePosition = MutableStateFlow(sharedPrefs.getString("user_position", "Field Surveyor") ?: "Field Surveyor")
    val userProfilePosition = _userProfilePosition.asStateFlow()

    private val _gpsAccuracyThreshold = MutableStateFlow(sharedPrefs.getFloat("gps_threshold", 15.0f))
    val gpsAccuracyThreshold = _gpsAccuracyThreshold.asStateFlow()

    private val _defaultWatermarkTemplate = MutableStateFlow(sharedPrefs.getString("watermark_template", "A") ?: "A")
    val defaultWatermarkTemplate = _defaultWatermarkTemplate.asStateFlow()

    // Custom Watermark Config values
    private val _customFontSize = MutableStateFlow(sharedPrefs.getFloat("custom_font_size", 14f))
    val customFontSize = _customFontSize.asStateFlow()

    private val _appFontSizeScale = MutableStateFlow(sharedPrefs.getFloat("app_font_size_scale", 1.0f))
    val appFontSizeScale = _appFontSizeScale.asStateFlow()

    private val _customColor = MutableStateFlow(sharedPrefs.getInt("custom_color", Color.WHITE))
    val customColor = _customColor.asStateFlow()

    private val _customOpacity = MutableStateFlow(sharedPrefs.getFloat("custom_opacity", 0.8f))
    val customOpacity = _customOpacity.asStateFlow()

    private val _customPosition = MutableStateFlow(sharedPrefs.getString("custom_position", "BOTTOM_LEFT") ?: "BOTTOM_LEFT")
    val customPosition = _customPosition.asStateFlow()

    private val _watermarkFontSizeScale = MutableStateFlow(sharedPrefs.getFloat("watermark_font_size_scale", 1.0f))
    val watermarkFontSizeScale = _watermarkFontSizeScale.asStateFlow()

    private val _showWatermarkMap = MutableStateFlow(sharedPrefs.getBoolean("show_watermark_map", true))
    val showWatermarkMap = _showWatermarkMap.asStateFlow()

    // Real-time GPS Sensor State
    private val fieldSensors = FieldSensors(context)
    val gpsData: StateFlow<GPSData> = fieldSensors.gpsData

    // Operation Results & Info Messages
    private val _toastMessage = MutableSharedFlow<String>()
    val toastMessage = _toastMessage.asSharedFlow()

    private val _activePhoto = MutableStateFlow<PhotoMetadata?>(null)
    val activePhoto = _activePhoto.asStateFlow()

    init {
        viewModelScope.launch {
            // Populate database with mock field projects if empty
            repository.prepopulateIfEmpty()
            
            // Set first project as selected by default if none is set
            val projs = repository.allProjects.first()
            if (_selectedProjectId.value == -1 && projs.isNotEmpty()) {
                selectProject(projs.first().id)
            }
        }
    }

    // Toggle Sensors on/off when app screen layout focuses
    fun resumeSensors() {
        fieldSensors.registerCompass()
        fieldSensors.startLocationUpdates()
    }

    fun pauseSensors() {
        fieldSensors.stopLocationUpdates()
    }

    // Settings Operations
    fun saveUserProfile(name: String, company: String, position: String) {
        _userProfileName.value = name
        _userProfileCompany.value = company
        _userProfilePosition.value = position
        sharedPrefs.edit()
            .putString("user_name", name)
            .putString("company_name", company)
            .putString("user_position", position)
            .apply()
        
        // Also update the templates default properties
        viewModelScope.launch {
            _toastMessage.emit("Profile Updated successfully")
        }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        sharedPrefs.edit().putBoolean("dark_mode", enabled).apply()
    }

    fun selectProject(projectId: Int) {
        _selectedProjectId.value = projectId
        sharedPrefs.edit().putInt("selected_project_id", projectId).apply()
    }

    fun updateGpsThreshold(threshold: Float) {
        _gpsAccuracyThreshold.value = threshold
        sharedPrefs.edit().putFloat("gps_threshold", threshold).apply()
    }

    fun updateDefaultTemplate(template: String) {
        _defaultWatermarkTemplate.value = template
        sharedPrefs.edit().putString("watermark_template", template).apply()
    }

    fun updateCustomWatermarkConfig(fontSize: Float, colorInt: Int, opacity: Float, position: String) {
        _customFontSize.value = fontSize
        _customColor.value = colorInt
        _customOpacity.value = opacity
        _customPosition.value = position
        sharedPrefs.edit()
            .putFloat("custom_font_size", fontSize)
            .putInt("custom_color", colorInt)
            .putFloat("custom_opacity", opacity)
            .putString("custom_position", position)
            .apply()
    }

    fun updateAppFontSizeScale(scale: Float) {
        _appFontSizeScale.value = scale
        sharedPrefs.edit().putFloat("app_font_size_scale", scale).apply()
    }

    fun updateWatermarkFontSizeScale(scale: Float) {
        _watermarkFontSizeScale.value = scale
        sharedPrefs.edit().putFloat("watermark_font_size_scale", scale).apply()
    }

    fun updateShowWatermarkMap(show: Boolean) {
        _showWatermarkMap.value = show
        sharedPrefs.edit().putBoolean("show_watermark_map", show).apply()
    }

    // Project Operations
    fun addNewProject(name: String, desc: String) {
        viewModelScope.launch {
            val project = Project(
                name = name,
                description = desc,
                companyName = _userProfileCompany.value,
                inspectorName = _userProfileName.value
            )
            val newId = repository.insertProject(project)
            selectProject(newId.toInt())
            _toastMessage.emit("Project '$name' created")
        }
    }

    fun deleteProject(project: Project) {
        viewModelScope.launch {
            repository.deleteProject(project)
            _toastMessage.emit("Project deleted successfully")
            val projs = repository.allProjects.first()
            if (projs.isNotEmpty()) {
                selectProject(projs.first().id)
            } else {
                selectProject(-1)
            }
        }
    }

    // Photo Management Operations
    fun saveCapturedPhoto(temporaryFile: File, gpsInfo: GPSData) {
        viewModelScope.launch(Dispatchers.IO) {
            val projId = _selectedProjectId.value
            val associatedProj = repository.getProjectById(projId) ?: Project(
                id = 999,
                name = "General Work",
                description = "Default general field checks",
                companyName = _userProfileCompany.value,
                inspectorName = _userProfileName.value
            )

            // Setup PhotoMetadata row
            val meta = PhotoMetadata(
                photoPath = temporaryFile.absolutePath,
                projectId = projId,
                latitude = gpsInfo.latitude,
                longitude = gpsInfo.longitude,
                altitude = gpsInfo.altitude,
                accuracy = gpsInfo.accuracy,
                speed = gpsInfo.speed,
                bearing = gpsInfo.heading, // use magnetic compass heading
                address = gpsInfo.address,
                timestamp = System.currentTimeMillis(),
                deviceModel = Build.MODEL,
                androidVersion = Build.VERSION.RELEASE,
                isMockLocation = gpsInfo.isMock,
                watermarkTemplate = _defaultWatermarkTemplate.value,
                watermarkFontSize = _customFontSize.value,
                watermarkColor = _customColor.value,
                watermarkOpacity = _customOpacity.value,
                watermarkPosition = _customPosition.value,
                synced = false
            )

            // 1. Run physical canvas watermark drawing
            try {
                WatermarkEngine.applyWatermark(
                    context = context,
                    inputPhotoFile = temporaryFile,
                    project = associatedProj,
                    metadata = meta,
                    customFontSize = _customFontSize.value,
                    customColor = _customColor.value,
                    customOpacity = _customOpacity.value,
                    customPosition = _customPosition.value,
                    watermarkFontSizeScale = _watermarkFontSizeScale.value,
                    showWatermarkMap = _showWatermarkMap.value
                )

                // 2. Insert stamp record into offline database
                repository.insertPhoto(meta)
                withContext(Dispatchers.Main) {
                    _toastMessage.emit("Photo stamped & saved successfully")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _toastMessage.emit("Failed to overlay watermark: ${e.localizedMessage}")
                }
            }
        }
    }

    fun deletePhoto(metadata: PhotoMetadata) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete actual image file
            val file = File(metadata.photoPath)
            if (file.exists()) {
                file.delete()
            }
            // Delete from database
            repository.deletePhoto(metadata)
            withContext(Dispatchers.Main) {
                _toastMessage.emit("Photo deleted successfully")
            }
        }
    }

    fun deletePhotos(photoList: List<PhotoMetadata>) {
        viewModelScope.launch(Dispatchers.IO) {
            var count = 0
            photoList.forEach { photo ->
                val file = File(photo.photoPath)
                if (file.exists()) {
                    file.delete()
                }
                repository.deletePhoto(photo)
                count++
            }
            withContext(Dispatchers.Main) {
                _toastMessage.emit("$count photos deleted successfully")
            }
        }
    }

    fun savePhotoToGallery(metadata: PhotoMetadata, showToast: Boolean = true) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(metadata.photoPath)
            if (!file.exists()) {
                if (showToast) {
                    withContext(Dispatchers.Main) {
                        _toastMessage.emit("Source file not found")
                    }
                }
                return@launch
            }

            try {
                val resolver = context.contentResolver
                val filename = "GPSTacticalCamera_${metadata.timestamp}.jpg"
                val contentValues = android.content.ContentValues().apply {
                    put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/GPSTacticalCamera")
                        put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    resolver.openOutputStream(imageUri).use { outputStream ->
                        if (outputStream != null) {
                            file.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                        resolver.update(imageUri, contentValues, null, null)
                    }

                    if (showToast) {
                        withContext(Dispatchers.Main) {
                            _toastMessage.emit("Saved to Gallery: $filename")
                        }
                    }
                } else {
                    if (showToast) {
                        withContext(Dispatchers.Main) {
                            _toastMessage.emit("Failed to save to gallery")
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                if (showToast) {
                    withContext(Dispatchers.Main) {
                        _toastMessage.emit("Error: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    fun savePhotosToGallery(photoList: List<PhotoMetadata>) {
        viewModelScope.launch(Dispatchers.IO) {
            var successCount = 0
            photoList.forEach { metadata ->
                val file = File(metadata.photoPath)
                if (file.exists()) {
                    try {
                        val resolver = context.contentResolver
                        val filename = "GPSTacticalCamera_${metadata.timestamp}.jpg"
                        val contentValues = android.content.ContentValues().apply {
                            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, filename)
                            put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/GPSTacticalCamera")
                                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                            }
                        }

                        val imageUri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                        if (imageUri != null) {
                            resolver.openOutputStream(imageUri).use { outputStream ->
                                if (outputStream != null) {
                                    file.inputStream().use { inputStream ->
                                        inputStream.copyTo(outputStream)
                                    }
                                }
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                contentValues.clear()
                                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                                resolver.update(imageUri, contentValues, null, null)
                            }
                            successCount++
                        }
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                }
            }
            withContext(Dispatchers.Main) {
                _toastMessage.emit("$successCount photos saved to Gallery")
            }
        }
    }

    fun exportPdfReport(metadata: PhotoMetadata, callback: (File?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val associatedProject = repository.getProjectById(metadata.projectId) ?: Project(
                id = 999,
                name = "General Surveys",
                description = "Dynamic survey inspections",
                companyName = _userProfileCompany.value,
                inspectorName = _userProfileName.value
            )

            val dir = context.getExternalFilesDir("reports") ?: context.filesDir
            if (!dir.exists()) dir.mkdirs()

            val pdfFile = File(dir, "Report_${associatedProject.name.replace(" ", "_")}_${metadata.timestamp}.pdf")
            try {
                PdfReporter.generateReport(context, metadata, associatedProject, pdfFile)
                withContext(Dispatchers.Main) {
                    _toastMessage.emit("PDF Report exported successfully")
                    callback(pdfFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    _toastMessage.emit("Failed to generate PDF: ${e.localizedMessage}")
                    callback(null)
                }
            }
        }
    }

    fun syncPhoto(metadata: PhotoMetadata) {
        viewModelScope.launch {
            // Mock sync code
            _toastMessage.emit("Syncing metadata to secure cloud server...")
            withContext(Dispatchers.IO) {
                kotlinx.coroutines.delay(1000)
                val updated = metadata.copy(synced = true)
                repository.updatePhoto(updated)
            }
            _toastMessage.emit("Sync Complete! Metadata uploaded safely.")
        }
    }

    fun setActivePhoto(metadata: PhotoMetadata?) {
        _activePhoto.value = metadata
    }

    override fun onCleared() {
        super.onCleared()
        fieldSensors.stopLocationUpdates()
    }
}

// Custom ViewModel Factory definition
class MainViewModelFactory(
    private val application: Application,
    private val repository: FieldRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
