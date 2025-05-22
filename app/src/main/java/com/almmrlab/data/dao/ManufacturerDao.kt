package com.almmrlab.data.dao

import androidx.room.*
import com.almmrlab.data.entities.Manufacturer
import com.almmrlab.data.entities.ProductionStage
import kotlinx.coroutines.flow.Flow

@Dao
interface ManufacturerDao {
    @Query("SELECT * FROM manufacturers WHERE isActive = 1 ORDER BY companyName ASC")
    fun getAllManufacturers(): Flow<List<Manufacturer>>

    @Query("SELECT * FROM manufacturers WHERE id = :id")
    suspend fun getManufacturerById(id: Long): Manufacturer?

    @Query("""
        SELECT * FROM manufacturers 
        WHERE (companyName LIKE '%' || :query || '%' 
        OR contactPerson LIKE '%' || :query || '%' 
        OR email LIKE '%' || :query || '%')
        AND isActive = 1
    """)
    fun searchManufacturers(query: String): Flow<List<Manufacturer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManufacturer(manufacturer: Manufacturer): Long

    @Update
    suspend fun updateManufacturer(manufacturer: Manufacturer)

    @Query("UPDATE manufacturers SET isActive = 0 WHERE id = :id")
    suspend fun deactivateManufacturer(id: Long)

    // Project Management
    @Query("""
        SELECT * FROM manufacturers 
        WHERE id IN (
            SELECT DISTINCT manufacturerId 
            FROM registered_perfumes 
            WHERE status = 'IN_PRODUCTION'
        )
        AND isActive = 1
    """)
    fun getActiveProductionManufacturers(): Flow<List<Manufacturer>>

    // Performance Analytics
    @Query("""
        SELECT * FROM manufacturers 
        WHERE qualityScore >= :minScore 
        AND isActive = 1 
        ORDER BY qualityScore DESC
    """)
    fun getManufacturersByQualityScore(minScore: Float): Flow<List<Manufacturer>>

    @Query("""
        SELECT * FROM manufacturers 
        WHERE reliabilityScore >= :minScore 
        AND isActive = 1 
        ORDER BY reliabilityScore DESC
    """)
    fun getManufacturersByReliabilityScore(minScore: Float): Flow<List<Manufacturer>>

    @Query("""
        SELECT * FROM manufacturers 
        WHERE averageResponseTime <= :maxDays 
        AND isActive = 1 
        ORDER BY averageResponseTime ASC
    """)
    fun getManufacturersByResponseTime(maxDays: Int): Flow<List<Manufacturer>>

    // Project Tracking
    @Query("""
        SELECT * FROM manufacturers 
        WHERE EXISTS (
            SELECT 1 FROM json_each(activeProjects) 
            WHERE json_extract(value, '$.status') = :stage
        )
        AND isActive = 1
    """)
    fun getManufacturersByProjectStage(stage: ProductionStage): Flow<List<Manufacturer>>

    // Certification Management
    @Query("""
        SELECT * FROM manufacturers 
        WHERE EXISTS (
            SELECT 1 FROM json_each(certifications) 
            WHERE value LIKE '%' || :certification || '%'
        )
        AND isActive = 1
    """)
    fun getManufacturersByCertification(certification: String): Flow<List<Manufacturer>>

    // Statistics
    @Query("""
        SELECT AVG(completedProjects) 
        FROM manufacturers 
        WHERE isActive = 1
    """)
    suspend fun getAverageCompletedProjects(): Float

    @Query("""
        SELECT AVG(
            (qualityScore + communicationScore + reliabilityScore + costEffectivenessScore) / 4.0
        ) 
        FROM manufacturers 
        WHERE isActive = 1
    """)
    suspend fun getAverageOverallRating(): Float
}
