package com.almmrlab.util

import android.content.Context
import android.content.SharedPreferences
import com.almmrlab.data.AppDatabase
import com.almmrlab.data.repository.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val context: Context,
    private val database: AppDatabase,
    private val perfumeFormulaRepository: PerfumeFormulaRepository,
    private val rawMaterialRepository: RawMaterialRepository,
    private val manufacturerRepository: ManufacturerRepository,
    private val registeredPerfumeRepository: RegisteredPerfumeRepository,
    private val dataExporter: DataExporter
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "sync_preferences",
        Context.MODE_PRIVATE
    )

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    suspend fun createBackup(): File {
        return withContext(Dispatchers.IO) {
            // Collect all data
            val backup = DatabaseBackup(
                timestamp = System.currentTimeMillis(),
                formulas = perfumeFormulaRepository.getAllFormulas().first(),
                materials = rawMaterialRepository.getAllMaterials().first(),
                manufacturers = manufacturerRepository.getAllManufacturers().first(),
                perfumes = registeredPerfumeRepository.getAllPerfumes().first()
            )

            // Export to file
            val backupFile = createBackupFile()
            backupFile.writeText(gson.toJson(backup))

            // Update backup metadata
            updateBackupMetadata(backupFile)

            backupFile
        }
    }

    suspend fun restoreFromBackup(backupFile: File): RestoreResult {
        return withContext(Dispatchers.IO) {
            try {
                val backup = gson.fromJson(backupFile.readText(), DatabaseBackup::class.java)

                // Validate backup
                if (!isValidBackup(backup)) {
                    return@withContext RestoreResult.Error("Invalid backup file")
                }

                // Clear existing data
                database.clearAllTables()

                // Restore data
                backup.formulas?.forEach { formula ->
                    perfumeFormulaRepository.saveFormula(formula)
                }

                backup.materials?.forEach { material ->
                    rawMaterialRepository.saveMaterial(material)
                }

                backup.manufacturers?.forEach { manufacturer ->
                    manufacturerRepository.saveManufacturer(manufacturer)
                }

                backup.perfumes?.forEach { perfume ->
                    registeredPerfumeRepository.savePerfume(perfume)
                }

                updateRestoreMetadata(backup.timestamp)

                RestoreResult.Success
            } catch (e: Exception) {
                RestoreResult.Error("Restore failed: ${e.message}")
            }
        }
    }

    fun getLastBackupInfo(): BackupInfo {
        val timestamp = prefs.getLong("last_backup_timestamp", 0)
        val path = prefs.getString("last_backup_path", null)
        val size = prefs.getLong("last_backup_size", 0)

        return BackupInfo(
            timestamp = if (timestamp > 0) Date(timestamp) else null,
            filePath = path,
            sizeInBytes = size
        )
    }

    fun getLastRestoreInfo(): RestoreInfo {
        val timestamp = prefs.getLong("last_restore_timestamp", 0)
        return RestoreInfo(
            timestamp = if (timestamp > 0) Date(timestamp) else null
        )
    }

    private fun createBackupFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val fileName = "almmr_backup_$timestamp.json"
        return File(context.getExternalFilesDir(null), fileName)
    }

    private fun updateBackupMetadata(backupFile: File) {
        prefs.edit().apply {
            putLong("last_backup_timestamp", System.currentTimeMillis())
            putString("last_backup_path", backupFile.absolutePath)
            putLong("last_backup_size", backupFile.length())
        }.apply()
    }

    private fun updateRestoreMetadata(timestamp: Long) {
        prefs.edit().apply {
            putLong("last_restore_timestamp", timestamp)
        }.apply()
    }

    private fun isValidBackup(backup: DatabaseBackup): Boolean {
        // Basic validation
        if (backup.timestamp <= 0) return false
        
        // At least one data set should be present
        if (backup.formulas.isNullOrEmpty() &&
            backup.materials.isNullOrEmpty() &&
            backup.manufacturers.isNullOrEmpty() &&
            backup.perfumes.isNullOrEmpty()) {
            return false
        }

        return true
    }

    suspend fun scheduleAutomaticBackup() {
        // Implementation for scheduling periodic backups
        // This could use WorkManager for periodic background work
    }

    suspend fun verifyDataIntegrity(): IntegrityCheckResult {
        return withContext(Dispatchers.IO) {
            try {
                // Perform various integrity checks
                val checks = mutableListOf<String>()

                // Check formula references
                val perfumes = registeredPerfumeRepository.getAllPerfumes().first()
                val formulas = perfumeFormulaRepository.getAllFormulas().first()
                val formulaIds = formulas.map { it.id }.toSet()

                perfumes.forEach { perfume ->
                    if (!formulaIds.contains(perfume.formulaId)) {
                        checks.add("Invalid formula reference in perfume ${perfume.id}")
                    }
                }

                // Add more integrity checks as needed

                if (checks.isEmpty()) {
                    IntegrityCheckResult.Success
                } else {
                    IntegrityCheckResult.Issues(checks)
                }
            } catch (e: Exception) {
                IntegrityCheckResult.Error("Integrity check failed: ${e.message}")
            }
        }
    }
}

data class DatabaseBackup(
    val timestamp: Long,
    val formulas: List<PerfumeFormula>?,
    val materials: List<RawMaterial>?,
    val manufacturers: List<Manufacturer>?,
    val perfumes: List<RegisteredPerfume>?
)

data class BackupInfo(
    val timestamp: Date?,
    val filePath: String?,
    val sizeInBytes: Long
)

data class RestoreInfo(
    val timestamp: Date?
)

sealed class RestoreResult {
    object Success : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

sealed class IntegrityCheckResult {
    object Success : IntegrityCheckResult()
    data class Issues(val problems: List<String>) : IntegrityCheckResult()
    data class Error(val message: String) : IntegrityCheckResult()
}
