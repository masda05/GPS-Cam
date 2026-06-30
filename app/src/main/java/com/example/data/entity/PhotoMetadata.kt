package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photo_metadata")
data class PhotoMetadata(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val photoPath: String,
    val projectId: Int,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val accuracy: Float = 0.0f,
    val speed: Float = 0.0f,
    val bearing: Float = 0.0f,
    val address: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deviceModel: String = "",
    val androidVersion: String = "",
    val isMockLocation: Boolean = false,
    val watermarkTemplate: String = "A", // "A" (Corporate), "B" (Minimal), "C" (Construction), "D" (Surveyor), "E" (Custom)
    val watermarkFontSize: Float = 14f,
    val watermarkColor: Int = -1, // -1 is Color.WHITE
    val watermarkOpacity: Float = 0.8f,
    val watermarkPosition: String = "BOTTOM_LEFT", // "BOTTOM_LEFT", "BOTTOM_RIGHT", "TOP_LEFT", "TOP_RIGHT"
    val synced: Boolean = false
)
