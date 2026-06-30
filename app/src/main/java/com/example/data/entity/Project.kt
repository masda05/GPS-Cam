package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val companyName: String = "",
    val inspectorName: String = "",
    val createdTimestamp: Long = System.currentTimeMillis()
)
