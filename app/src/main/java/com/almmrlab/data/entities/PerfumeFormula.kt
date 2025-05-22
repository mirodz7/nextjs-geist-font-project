package com.almmrlab.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "perfume_formulas")
data class PerfumeFormula(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val creationDate: Date,
    val perfumer: String,
    val version: Int,
    val alcoholPercentage: Double,
    
    // Scent Structure
    val topNotes: List<Note>,
    val middleNotes: List<Note>,
    val baseNotes: List<Note>,
    
    // Additional Metadata
    val status: FormulaStatus,
    val lastModified: Date,
    val isArchived: Boolean = false
)

data class Note(
    val materialId: Long,
    val quantity: Double,
    val concentration: Double
)

enum class FormulaStatus {
    DRAFT,
    IN_DEVELOPMENT,
    TESTING,
    APPROVED,
    PRODUCTION
}
