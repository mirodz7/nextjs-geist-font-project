package com.almmrlab.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "manufacturers")
data class Manufacturer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val companyName: String,
    val contactPerson: String,
    val email: String,
    val phone: String,
    val address: String,
    
    // Certification and Compliance
    val certifications: List<String>,
    val complianceDocuments: List<String>,
    val qualityRating: Float,
    
    // Project Tracking
    val activeProjects: List<ProjectStatus>,
    val completedProjects: Int,
    val averageResponseTime: Int, // in days
    
    // Reviews and Ratings
    val qualityScore: Float,
    val communicationScore: Float,
    val reliabilityScore: Float,
    val costEffectivenessScore: Float,
    
    // File Attachments
    val contractFiles: List<String>,
    val sampleReports: List<String>,
    val productionReports: List<String>,
    
    // Metadata
    val createdAt: Long,
    val updatedAt: Long,
    val isActive: Boolean = true,
    val notes: String? = null
)

data class ProjectStatus(
    val formulaId: Long,
    val status: ProductionStage,
    val startDate: Long,
    val expectedCompletionDate: Long?,
    val actualCompletionDate: Long?,
    val sampleReceived: Boolean = false,
    val sampleApproved: Boolean = false,
    val productionQuantity: Int? = null,
    val notes: String? = null
)

enum class ProductionStage {
    FORMULA_SENT,
    SAMPLE_IN_PRODUCTION,
    SAMPLE_RECEIVED,
    SAMPLE_TESTING,
    SAMPLE_APPROVED,
    PRODUCTION_STARTED,
    PRODUCTION_COMPLETED,
    PROJECT_CLOSED
}
