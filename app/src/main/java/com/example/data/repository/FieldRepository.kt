package com.example.data.repository

import com.example.data.dao.PhotoMetadataDao
import com.example.data.dao.ProjectDao
import com.example.data.entity.PhotoMetadata
import com.example.data.entity.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FieldRepository(
    private val projectDao: ProjectDao,
    private val photoMetadataDao: PhotoMetadataDao
) {
    val allProjects: Flow<List<Project>> = projectDao.getAllProjects()
    val allPhotos: Flow<List<PhotoMetadata>> = photoMetadataDao.getAllPhotos()

    fun getPhotosByProject(projectId: Int): Flow<List<PhotoMetadata>> {
        return photoMetadataDao.getPhotosByProject(projectId)
    }

    suspend fun getProjectById(id: Int): Project? {
        return projectDao.getProjectById(id)
    }

    suspend fun getPhotoById(id: Int): PhotoMetadata? {
        return photoMetadataDao.getPhotoById(id)
    }

    suspend fun insertProject(project: Project): Long {
        return projectDao.insertProject(project)
    }

    suspend fun updateProject(project: Project) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: Project) {
        projectDao.deleteProject(project)
    }

    suspend fun insertPhoto(photo: PhotoMetadata): Long {
        return photoMetadataDao.insertPhoto(photo)
    }

    suspend fun updatePhoto(photo: PhotoMetadata) {
        photoMetadataDao.updatePhoto(photo)
    }

    suspend fun deletePhoto(photo: PhotoMetadata) {
        photoMetadataDao.deletePhoto(photo)
    }

    suspend fun prepopulateIfEmpty() {
        try {
            val currentList = allProjects.first()
            if (currentList.isEmpty()) {
                insertProject(Project(
                    name = "Tower Installation",
                    description = "5G Base Transceiver Station deployment and visual inspection.",
                    companyName = "Pro Communications Ltd",
                    inspectorName = "John Doe"
                ))
                insertProject(Project(
                    name = "CCTV Inspection",
                    description = "Perimeter security cameras audit and lens alignment verifications.",
                    companyName = "Apex Security Systems",
                    inspectorName = "John Doe"
                ))
                insertProject(Project(
                    name = "Road Survey",
                    description = "Asphalt quality check, geographic elevation slope, and pothole mapping across Highway 10.",
                    companyName = "Civil Tech Engineering",
                    inspectorName = "John Doe"
                ))
                insertProject(Project(
                    name = "Warehouse Audit",
                    description = "Logistics center high-bay shelving structures loading checks & safety compliance review.",
                    companyName = "Global Logistics Hub",
                    inspectorName = "John Doe"
                ))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
