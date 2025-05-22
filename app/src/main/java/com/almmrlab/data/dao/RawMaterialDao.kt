package com.almmrlab.data.dao

import androidx.room.*
import com.almmrlab.data.entities.RawMaterial
import com.almmrlab.data.entities.MaterialType
import kotlinx.coroutines.flow.Flow

@Dao
interface RawMaterialDao {
    @Query("SELECT * FROM raw_materials WHERE isArchived = 0 ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<RawMaterial>>

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    suspend fun getMaterialById(id: Long): RawMaterial?

    @Query("""
        SELECT * FROM raw_materials 
        WHERE type = :type 
        AND isArchived = 0 
        ORDER BY name ASC
    """)
    fun getMaterialsByType(type: MaterialType): Flow<List<RawMaterial>>

    @Query("""
        SELECT * FROM raw_materials 
        WHERE (name LIKE '%' || :query || '%' 
        OR olfactoryProfile LIKE '%' || :query || '%')
        AND isArchived = 0
    """)
    fun searchMaterials(query: String): Flow<List<RawMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: RawMaterial): Long

    @Update
    suspend fun updateMaterial(material: RawMaterial)

    @Query("UPDATE raw_materials SET isArchived = 1 WHERE id = :id")
    suspend fun archiveMaterial(id: Long)

    // Stock Management
    @Query("""
        SELECT * FROM raw_materials 
        WHERE stockLevel <= minimumStockLevel 
        AND isArchived = 0
    """)
    fun getLowStockMaterials(): Flow<List<RawMaterial>>

    @Query("""
        UPDATE raw_materials 
        SET stockLevel = stockLevel + :quantity,
            lastRestockDate = :timestamp,
            updatedAt = :timestamp
        WHERE id = :id
    """)
    suspend fun updateStock(id: Long, quantity: Double, timestamp: Long)

    // Safety and Compliance
    @Query("""
        SELECT * FROM raw_materials 
        WHERE ifraCategory = :category 
        AND isArchived = 0
    """)
    fun getMaterialsByIfraCategory(category: String): Flow<List<RawMaterial>>

    @Query("""
        SELECT * FROM raw_materials 
        WHERE ifraLimit IS NOT NULL 
        AND isArchived = 0
    """)
    fun getMaterialsWithRestrictions(): Flow<List<RawMaterial>>

    // Analytics
    @Query("""
        SELECT * FROM raw_materials 
        WHERE cost BETWEEN :minCost AND :maxCost 
        AND isArchived = 0
    """)
    fun getMaterialsByCostRange(minCost: Double, maxCost: Double): Flow<List<RawMaterial>>

    @Query("""
        SELECT * FROM raw_materials 
        WHERE volatility BETWEEN :minVolatility AND :maxVolatility 
        AND isArchived = 0
    """)
    fun getMaterialsByVolatilityRange(minVolatility: Double, maxVolatility: Double): Flow<List<RawMaterial>>

    // Batch Operations
    @Transaction
    suspend fun updateMaterialsStock(updates: Map<Long, Double>, timestamp: Long) {
        updates.forEach { (id, quantity) ->
            updateStock(id, quantity, timestamp)
        }
    }
}
