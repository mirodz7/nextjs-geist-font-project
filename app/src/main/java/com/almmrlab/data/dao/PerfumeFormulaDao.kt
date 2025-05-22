package com.almmrlab.data.dao

import androidx.room.*
import com.almmrlab.data.entities.PerfumeFormula
import com.almmrlab.data.entities.FormulaStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfumeFormulaDao {
    @Query("SELECT * FROM perfume_formulas WHERE isArchived = 0 ORDER BY creationDate DESC")
    fun getAllFormulas(): Flow<List<PerfumeFormula>>

    @Query("SELECT * FROM perfume_formulas WHERE id = :id")
    suspend fun getFormulaById(id: Long): PerfumeFormula?

    @Query("SELECT * FROM perfume_formulas WHERE status = :status AND isArchived = 0")
    fun getFormulasByStatus(status: FormulaStatus): Flow<List<PerfumeFormula>>

    @Query("""
        SELECT * FROM perfume_formulas 
        WHERE (name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        OR perfumer LIKE '%' || :query || '%')
        AND isArchived = 0
    """)
    fun searchFormulas(query: String): Flow<List<PerfumeFormula>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFormula(formula: PerfumeFormula): Long

    @Update
    suspend fun updateFormula(formula: PerfumeFormula)

    @Query("UPDATE perfume_formulas SET isArchived = 1 WHERE id = :id")
    suspend fun archiveFormula(id: Long)

    @Query("SELECT * FROM perfume_formulas WHERE version > 1 AND name = :name ORDER BY version DESC")
    fun getFormulaVersions(name: String): Flow<List<PerfumeFormula>>

    @Transaction
    suspend fun duplicateFormula(id: Long): Long {
        val original = getFormulaById(id) ?: throw IllegalArgumentException("Formula not found")
        val copy = original.copy(
            id = 0,
            version = original.version + 1,
            creationDate = java.util.Date()
        )
        return insertFormula(copy)
    }

    @Query("""
        SELECT * FROM perfume_formulas 
        WHERE alcoholPercentage BETWEEN :minPercentage AND :maxPercentage 
        AND isArchived = 0
    """)
    fun getFormulasByAlcoholRange(minPercentage: Double, maxPercentage: Double): Flow<List<PerfumeFormula>>

    @Query("SELECT MAX(version) FROM perfume_formulas WHERE name = :name")
    suspend fun getLatestVersionNumber(name: String): Int?

    @Query("SELECT COUNT(*) FROM perfume_formulas WHERE perfumer = :perfumer AND isArchived = 0")
    suspend fun getFormulaCountByPerfumer(perfumer: String): Int
}
