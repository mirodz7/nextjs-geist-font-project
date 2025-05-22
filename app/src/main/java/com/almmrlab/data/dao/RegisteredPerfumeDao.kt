package com.almmrlab.data.dao

import androidx.room.*
import com.almmrlab.data.entities.RegisteredPerfume
import com.almmrlab.data.entities.PerfumeType
import com.almmrlab.data.entities.PerfumeStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RegisteredPerfumeDao {
    @Query("SELECT * FROM registered_perfumes WHERE isArchived = 0 ORDER BY registrationDate DESC")
    fun getAllPerfumes(): Flow<List<RegisteredPerfume>>

    @Query("SELECT * FROM registered_perfumes WHERE id = :id")
    suspend fun getPerfumeById(id: Long): RegisteredPerfume?

    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE (name LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        OR scentFamily LIKE '%' || :query || '%')
        AND isArchived = 0
    """)
    fun searchPerfumes(query: String): Flow<List<RegisteredPerfume>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerfume(perfume: RegisteredPerfume): Long

    @Update
    suspend fun updatePerfume(perfume: RegisteredPerfume)

    @Query("UPDATE registered_perfumes SET isArchived = 1 WHERE id = :id")
    suspend fun archivePerfume(id: Long)

    // Filtering Queries
    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE type = :type 
        AND isArchived = 0 
        ORDER BY name ASC
    """)
    fun getPerfumesByType(type: PerfumeType): Flow<List<RegisteredPerfume>>

    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE status = :status 
        AND isArchived = 0 
        ORDER BY registrationDate DESC
    """)
    fun getPerfumesByStatus(status: PerfumeStatus): Flow<List<RegisteredPerfume>>

    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE alcoholPercentage BETWEEN :minPercentage AND :maxPercentage 
        AND isArchived = 0
    """)
    fun getPerfumesByAlcoholRange(minPercentage: Double, maxPercentage: Double): Flow<List<RegisteredPerfume>>

    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE scentFamily = :family 
        AND isArchived = 0
    """)
    fun getPerfumesByScentFamily(family: String): Flow<List<RegisteredPerfume>>

    // Production Tracking
    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE manufacturerId = :manufacturerId 
        AND status = 'IN_PRODUCTION'
        AND isArchived = 0
    """)
    fun getPerfumesInProductionByManufacturer(manufacturerId: Long): Flow<List<RegisteredPerfume>>

    // Market Analysis
    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE pricePoint BETWEEN :minPrice AND :maxPrice 
        AND isArchived = 0
    """)
    fun getPerfumesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<RegisteredPerfume>>

    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE targetMarket LIKE '%' || :market || '%' 
        AND isArchived = 0
    """)
    fun getPerfumesByTargetMarket(market: String): Flow<List<RegisteredPerfume>>

    // Batch Code Management
    @Query("""
        SELECT * FROM registered_perfumes 
        WHERE batchNumber LIKE :batchPrefix || '%' 
        AND isArchived = 0
    """)
    fun getPerfumesByBatchPrefix(batchPrefix: String): Flow<List<RegisteredPerfume>>

    // Statistics
    @Query("""
        SELECT COUNT(*) FROM registered_perfumes 
        WHERE status = :status 
        AND isArchived = 0
    """)
    suspend fun getPerfumeCountByStatus(status: PerfumeStatus): Int

    @Query("""
        SELECT AVG(pricePoint) FROM registered_perfumes 
        WHERE status = 'AVAILABLE' 
        AND isArchived = 0
    """)
    suspend fun getAverageMarketPrice(): Double
}
