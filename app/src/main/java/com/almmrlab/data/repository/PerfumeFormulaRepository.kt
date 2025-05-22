package com.almmrlab.data.repository

import com.almmrlab.data.dao.PerfumeFormulaDao
import com.almmrlab.data.entities.PerfumeFormula
import com.almmrlab.data.entities.FormulaStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PerfumeFormulaRepository @Inject constructor(
    private val perfumeFormulaDao: PerfumeFormulaDao
) {
    // Basic CRUD operations
    fun getAllFormulas(): Flow<List<PerfumeFormula>> = 
        perfumeFormulaDao.getAllFormulas()

    suspend fun getFormulaById(id: Long): PerfumeFormula? =
        perfumeFormulaDao.getFormulaById(id)

    fun getFormulasByStatus(status: FormulaStatus): Flow<List<PerfumeFormula>> =
        perfumeFormulaDao.getFormulasByStatus(status)

    fun searchFormulas(query: String): Flow<List<PerfumeFormula>> =
        perfumeFormulaDao.searchFormulas(query)

    suspend fun saveFormula(formula: PerfumeFormula): Long {
        return if (formula.id == 0L) {
            // New formula
            perfumeFormulaDao.insertFormula(formula)
        } else {
            // Existing formula
            perfumeFormulaDao.updateFormula(formula)
            formula.id
        }
    }

    suspend fun archiveFormula(id: Long) =
        perfumeFormulaDao.archiveFormula(id)

    // Version control operations
    fun getFormulaVersions(name: String): Flow<List<PerfumeFormula>> =
        perfumeFormulaDao.getFormulaVersions(name)

    suspend fun duplicateFormula(id: Long): Long =
        perfumeFormulaDao.duplicateFormula(id)

    suspend fun getLatestVersionNumber(name: String): Int =
        perfumeFormulaDao.getLatestVersionNumber(name) ?: 0

    // Analysis operations
    fun getFormulasByAlcoholRange(minPercentage: Double, maxPercentage: Double): Flow<List<PerfumeFormula>> =
        perfumeFormulaDao.getFormulasByAlcoholRange(minPercentage, maxPercentage)

    suspend fun getFormulaCountByPerfumer(perfumer: String): Int =
        perfumeFormulaDao.getFormulaCountByPerfumer(perfumer)

    // Business Logic
    suspend fun createNewVersion(formula: PerfumeFormula): Long {
        val latestVersion = getLatestVersionNumber(formula.name)
        val newFormula = formula.copy(
            id = 0,
            version = latestVersion + 1,
            creationDate = java.util.Date()
        )
        return perfumeFormulaDao.insertFormula(newFormula)
    }

    suspend fun updateFormulaStatus(id: Long, newStatus: FormulaStatus) {
        val formula = getFormulaById(id) ?: return
        val updatedFormula = formula.copy(
            status = newStatus,
            lastModified = java.util.Date()
        )
        perfumeFormulaDao.updateFormula(updatedFormula)
    }

    suspend fun calculateAlcoholPercentage(id: Long): Double {
        val formula = getFormulaById(id) ?: return 0.0
        
        // Calculate total volume
        val totalVolume = (formula.topNotes + formula.middleNotes + formula.baseNotes)
            .sumOf { it.quantity }

        // Calculate alcohol percentage based on note concentrations
        return (formula.topNotes + formula.middleNotes + formula.baseNotes)
            .sumOf { (it.quantity * it.concentration) } / totalVolume
    }
}
