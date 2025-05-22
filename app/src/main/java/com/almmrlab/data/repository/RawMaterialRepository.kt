package com.almmrlab.data.repository

import com.almmrlab.data.dao.RawMaterialDao
import com.almmrlab.data.entities.RawMaterial
import com.almmrlab.data.entities.MaterialType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RawMaterialRepository @Inject constructor(
    private val rawMaterialDao: RawMaterialDao
) {
    // Basic CRUD operations
    fun getAllMaterials(): Flow<List<RawMaterial>> =
        rawMaterialDao.getAllMaterials()

    suspend fun getMaterialById(id: Long): RawMaterial? =
        rawMaterialDao.getMaterialById(id)

    fun getMaterialsByType(type: MaterialType): Flow<List<RawMaterial>> =
        rawMaterialDao.getMaterialsByType(type)

    fun searchMaterials(query: String): Flow<List<RawMaterial>> =
        rawMaterialDao.searchMaterials(query)

    suspend fun saveMaterial(material: RawMaterial): Long {
        return if (material.id == 0L) {
            rawMaterialDao.insertMaterial(material)
        } else {
            rawMaterialDao.updateMaterial(material)
            material.id
        }
    }

    suspend fun archiveMaterial(id: Long) =
        rawMaterialDao.archiveMaterial(id)

    // Stock Management
    fun getLowStockMaterials(): Flow<List<RawMaterial>> =
        rawMaterialDao.getLowStockMaterials()

    suspend fun updateStock(id: Long, quantity: Double) {
        val timestamp = System.currentTimeMillis()
        rawMaterialDao.updateStock(id, quantity, timestamp)
    }

    suspend fun batchUpdateStock(updates: Map<Long, Double>) {
        val timestamp = System.currentTimeMillis()
        rawMaterialDao.updateMaterialsStock(updates, timestamp)
    }

    // Safety and Compliance
    fun getMaterialsByIfraCategory(category: String): Flow<List<RawMaterial>> =
        rawMaterialDao.getMaterialsByIfraCategory(category)

    fun getMaterialsWithRestrictions(): Flow<List<RawMaterial>> =
        rawMaterialDao.getMaterialsWithRestrictions()

    // Analytics and Reporting
    fun getMaterialsByCostRange(minCost: Double, maxCost: Double): Flow<List<RawMaterial>> =
        rawMaterialDao.getMaterialsByCostRange(minCost, maxCost)

    fun getMaterialsByVolatilityRange(minVolatility: Double, maxVolatility: Double): Flow<List<RawMaterial>> =
        rawMaterialDao.getMaterialsByVolatilityRange(minVolatility, maxVolatility)

    // Business Logic
    fun getStockStatus(): Flow<Map<String, Int>> {
        return getAllMaterials().map { materials ->
            materials.groupBy { 
                when {
                    it.stockLevel == null || it.minimumStockLevel == null -> "UNKNOWN"
                    it.stockLevel <= 0.0 -> "OUT_OF_STOCK"
                    it.stockLevel <= it.minimumStockLevel -> "LOW_STOCK"
                    else -> "IN_STOCK"
                }
            }.mapValues { it.value.size }
        }
    }

    fun getMaterialsByOlfactoryProfile(profile: String): Flow<List<RawMaterial>> {
        return getAllMaterials().map { materials ->
            materials.filter { 
                it.olfactoryProfile.contains(profile, ignoreCase = true)
            }
        }
    }

    suspend fun calculateRestockNeeds(): Map<RawMaterial, Double> {
        val lowStockMaterials = rawMaterialDao.getLowStockMaterials()
            .map { materials ->
                materials.associate { material ->
                    material to (material.minimumStockLevel?.minus(material.stockLevel ?: 0.0) ?: 0.0)
                }
            }
        return lowStockMaterials.first()
    }

    fun getSafetyCompliantMaterials(maxIfraLimit: Double): Flow<List<RawMaterial>> {
        return getAllMaterials().map { materials ->
            materials.filter { material ->
                material.ifraLimit == null || material.ifraLimit <= maxIfraLimit
            }
        }
    }

    suspend fun isCompatibleWith(materialId1: Long, materialId2: Long): Boolean {
        // This is a placeholder for complex compatibility logic
        // In a real implementation, this would check various chemical properties
        val material1 = getMaterialById(materialId1)
        val material2 = getMaterialById(materialId2)
        
        if (material1 == null || material2 == null) return false

        // Basic compatibility check (example)
        return material1.type != material2.type || // Different types are usually compatible
               (material1.volatility == null || material2.volatility == null || // If we don't know volatility, assume compatible
                kotlin.math.abs((material1.volatility - material2.volatility)) <= 0.3) // Similar volatility
    }
}
