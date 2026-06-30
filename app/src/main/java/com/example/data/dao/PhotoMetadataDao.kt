package com.example.data.dao

import androidx.room.*
import com.example.data.entity.PhotoMetadata
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoMetadataDao {
    @Query("SELECT * FROM photo_metadata ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<PhotoMetadata>>

    @Query("SELECT * FROM photo_metadata WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getPhotosByProject(projectId: Int): Flow<List<PhotoMetadata>>

    @Query("SELECT * FROM photo_metadata WHERE id = :id")
    suspend fun getPhotoById(id: Int): PhotoMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoMetadata): Long

    @Update
    suspend fun updatePhoto(photo: PhotoMetadata)

    @Delete
    suspend fun deletePhoto(photo: PhotoMetadata)
}
