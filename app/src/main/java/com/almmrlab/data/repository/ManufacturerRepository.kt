package com.almmrlab.data.repository

import com.almmrlab.data.dao.ManufacturerDao
import com.almmrlab.data.entities.Manufacturer
import com.almmrlab.data.entities.ProductionStage
import com.almmrlab.data.entities.ProjectStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManufacturerRepository @Inject constructor(
    private val manufacturerDao: ManufacturerDao
) {
    // Basic CRUD operations
    fun getAllManufacturers(): Flow<List<Manufacturer>> =
        manufacturerDao.getAllManufacturers()

    suspend fun getManufacturerById(id: Long): Manufacturer? =
        manufacturerDao.getManufacturerById(id)

    fun searchManufacturers(query: String): Flow<List<Manufacturer>> =
        manufacturerDao.searchManufacturers(query)

    suspend fun saveManufacturer(manufacturer: Manufacturer): Long {
        return if (manufacturer.id == 0L) {
            manufacturerDao.insertManufacturer(manufacturer)
        } else {
            manufacturerDao.updateManufacturer(manufacturer)
            manufacturer.id
        }
    }

    suspend fun deactivateManufacturer(id: Long) =
        manufacturerDao.deactivateManufacturer(id)

    // Project Management
    fun getActiveProductionManufacturers(): Flow<List<Manufacturer>> =
        manufacturerDao.getActiveProductionManufacturers()

    fun getManufacturersByProjectStage(stage: ProductionStage): Flow<List<Manufacturer>> =
        manufacturerDao.getManufacturersByProjectStage(stage)

    // Performance Analytics
    fun getTopPerformingManufacturers(minQualityScore: Float = 4.0f): Flow<List<Manufacturer>> =
        manufacturerDao.getManufacturersByQualityScore(minQualityScore)

    fun getReliableManufacturers(minReliabilityScore: Float = 4.0f): Flow<List<Manufacturer>> =
        manufacturerDao.getManufacturersByReliabilityScore(minReliabilityScore)

    fun getQuickResponseManufacturers(maxDays: Int = 7): Flow<List<Manufacturer>> =
        manufacturerDao.getManufacturersByResponseTime(maxDays)

    // Certification Management
    fun getManufacturersByCertification(certification: String): Flow<List<Manufacturer>> =
        manufacturerDao.getManufacturersByCertification(certification)

    // Business Logic
    suspend fun updateProjectStatus(
        manufacturerId: Long,
        formulaId: Long,
        newStatus: ProductionStage,
        notes: String? = null
    ) {
        val manufacturer = getManufacturerById(manufacturerId) ?: return
        val currentTime = System.currentTimeMillis()

        val updatedProjects = manufacturer.activeProjects.map { project ->
            if (project.formulaId == formulaId) {
                project.copy(
                    status = newStatus,
                    actualCompletionDate = if (newStatus == ProductionStage.PROJECT_CLOSED) currentTime else project.actualCompletionDate,
                    notes = notes ?: project.notes
                )
            } else project
        }

        val updatedManufacturer = manufacturer.copy(
            activeProjects = updatedProjects,
            updatedAt = currentTime
        )

        manufacturerDao.updateManufacturer(updatedManufacturer)
    }

    suspend fun assignNewProject(
        manufacturerId: Long,
        formulaId: Long,
        expectedCompletionDate: Long
    ) {
        val manufacturer = getManufacturerById(manufacturerId) ?: return
        val currentTime = System.currentTimeMillis()

        val newProject = ProjectStatus(
            formulaId = formulaId,
            status = ProductionStage.FORMULA_SENT,
            startDate = currentTime,
            expectedCompletionDate = expectedCompletionDate,
            actualCompletionDate = null
        )

        val updatedManufacturer = manufacturer.copy(
            activeProjects = manufacturer.activeProjects + newProject,
            updatedAt = currentTime
        )

        manufacturerDao.updateManufacturer(updatedManufacturer)
    }

    fun getManufacturerPerformanceMetrics(manufacturerId: Long): Flow<ManufacturerMetrics> {
        return getAllManufacturers().map { manufacturers ->
            val manufacturer = manufacturers.find { it.id == manufacturerId }
            manufacturer?.let {
                ManufacturerMetrics(
                    completedProjects = it.completedProjects,
                    averageResponseTime = it.averageResponseTime,
                    qualityScore = it.qualityScore,
                    reliabilityScore = it.reliabilityScore,
                    communicationScore = it.communicationScore,
                    costEffectivenessScore = it.costEffectivenessScore,
                    overallScore = (it.qualityScore + it.reliabilityScore + 
                                  it.communicationScore + it.costEffectivenessScore) / 4
                )
            } ?: ManufacturerMetrics()
        }
    }

    suspend fun updateManufacturerRatings(
        manufacturerId: Long,
        qualityScore: Float,
        communicationScore: Float,
        reliabilityScore: Float,
        costEffectivenessScore: Float
    ) {
        val manufacturer = getManufacturerById(manufacturerId) ?: return
        
        val updatedManufacturer = manufacturer.copy(
            qualityScore = (manufacturer.qualityScore + qualityScore) / 2,
            communicationScore = (manufacturer.communicationScore + communicationScore) / 2,
            reliabilityScore = (manufacturer.reliabilityScore + reliabilityScore) / 2,
            costEffectivenessScore = (manufacturer.costEffectivenessScore + costEffectivenessScore) / 2,
            updatedAt = System.currentTimeMillis()
        )

        manufacturerDao.updateManufacturer(updatedManufacturer)
    }
}

data class ManufacturerMetrics(
    val completedProjects: Int = 0,
    val averageResponseTime: Int = 0,
    val qualityScore: Float = 0f,
    val reliabilityScore: Float = 0f,
    val communicationScore: Float = 0f,
    val costEffectivenessScore: Float = 0f,
    val overallScore: Float = 0f
)
