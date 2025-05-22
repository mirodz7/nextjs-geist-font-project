package com.almmrlab.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "registered_perfumes",
    foreignKeys = [
        ForeignKey(
            entity = PerfumeFormula::class,
            parentColumns = ["id"],
            childColumns = ["formulaId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Manufacturer::class,
            parentColumns = ["id"],
            childColumns = ["manufacturerId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class RegisteredPerfume(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Basic Information
    val name: String,
    val formulaId: Long,
    val manufacturerId: Long,
    val registrationDate: Long,
    
    // Product Details
    val type: PerfumeType,
    val alcoholPercentage: Double,
    val bottleType: String,
    val bottleSize: Int, // in ml
    val batchNumber: String,
    
    // Scent Properties
    val scentFamily: String,
    val longevity: Int, // in hours
    val sillage: SillageRating,
    
    // Marketing & Branding
    val description: String,
    val targetMarket: String,
    val pricePoint: Double,
    val brandingConcept: String,
    
    // Assets
    val bottleImage: String?, // file path
    val packageImage: String?, // file path
    val marketingImages: List<String>, // file paths
    
    // Documentation
    val safetyReport: String?, // file path
    val stabilityReport: String?, // file path
    val ifraCompliance: String?, // file path
    
    // Identifiers
    val barcode: String?,
    val qrCode: String?,
    
    // Metadata
    val createdAt: Long,
    val updatedAt: Long,
    val status: PerfumeStatus,
    val isArchived: Boolean = false
)

enum class PerfumeType {
    PARFUM,
    EAU_DE_PARFUM,
    EAU_DE_TOILETTE,
    EAU_DE_COLOGNE,
    EAU_FRAICHE
}

enum class SillageRating {
    INTIMATE,
    MODERATE,
    STRONG,
    ENORMOUS
}

enum class PerfumeStatus {
    REGISTERED,
    IN_PRODUCTION,
    AVAILABLE,
    DISCONTINUED
}
