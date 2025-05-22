package com.almmrlab.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_materials")
data class RawMaterial(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: MaterialType,
    val olfactoryProfile: String,
    val supplierLinks: List<String>,
    
    // Optional Properties
    val cost: Double? = null,
    val volatility: Double? = null,
    val ifraCategory: String? = null,
    val ifraLimit: Double? = null,
    val safetyNotes: String? = null,
    
    // Material Properties
    val isSynthetic: Boolean,
    val density: Double? = null,
    val flashPoint: Double? = null,
    val solubility: String? = null,
    
    // Tracking
    val stockLevel: Double? = null,
    val minimumStockLevel: Double? = null,
    val lastRestockDate: Long? = null,
    
    // Metadata
    val createdAt: Long,
    val updatedAt: Long,
    val isArchived: Boolean = false
)

enum class MaterialType {
    TOP_NOTE,
    MIDDLE_NOTE,
    BASE_NOTE,
    FIXATIVE,
    SOLVENT,
    MODIFIER
}
