package com.almmrlab.data.repository

import com.almmrlab.data.dao.RegisteredPerfumeDao
import com.almmrlab.data.entities.RegisteredPerfume
import com.almmrlab.data.entities.PerfumeType
import com.almmrlab.data.entities.PerfumeStatus
import com.almmrlab.data.entities.SillageRating
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegisteredPerfumeRepository @Inject constructor(
    private val registeredPerfumeDao: RegisteredPerfumeDao
) {
    // Basic CRUD operations
    fun getAllPerfumes(): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.getAllPerfumes()

    suspend fun getPerfumeById(id: Long): RegisteredPerfume? =
        registeredPerfumeDao.getPerfumeById(id)

    fun searchPerfumes(query: String): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.searchPerfumes(query)

    suspend fun savePerfume(perfume: RegisteredPerfume): Long {
        val currentTime = System.currentTimeMillis()
        val perfumeWithBarcodes = if (perfume.id == 0L) {
            // New perfume registration
            perfume.copy(
                barcode = generateBarcode(perfume),
                qrCode = generateQRCode(perfume),
                createdAt = currentTime,
                updatedAt = currentTime
            )
        } else {
            // Updating existing perfume
            perfume.copy(updatedAt = currentTime)
        }

        return if (perfumeWithBarcodes.id == 0L) {
            registeredPerfumeDao.insertPerfume(perfumeWithBarcodes)
        } else {
            registeredPerfumeDao.updatePerfume(perfumeWithBarcodes)
            perfumeWithBarcodes.id
        }
    }

    suspend fun archivePerfume(id: Long) =
        registeredPerfumeDao.archivePerfume(id)

    // Filtering and Search
    fun getPerfumesByType(type: PerfumeType): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.getPerfumesByType(type)

    fun getPerfumesByStatus(status: PerfumeStatus): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.getPerfumesByStatus(status)

    fun getPerfumesByAlcoholRange(minPercentage: Double, maxPercentage: Double): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.getPerfumesByAlcoholRange(minPercentage, maxPercentage)

    fun getPerfumesByScentFamily(family: String): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.getPerfumesByScentFamily(family)

    // Production Tracking
    fun getPerfumesInProductionByManufacturer(manufacturerId: Long): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.getPerfumesInProductionByManufacturer(manufacturerId)

    // Market Analysis
    fun getPerfumesByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.getPerfumesByPriceRange(minPrice, maxPrice)

    fun getPerfumesByTargetMarket(market: String): Flow<List<RegisteredPerfume>> =
        registeredPerfumeDao.getPerfumesByTargetMarket(market)

    // Business Logic
    private fun generateBarcode(perfume: RegisteredPerfume): String {
        // Format: YYMMDD-TYPE-BATCH
        val date = java.text.SimpleDateFormat("yyMMdd").format(java.util.Date())
        val type = perfume.type.name.take(3)
        val batch = UUID.randomUUID().toString().take(6)
        return "$date-$type-$batch"
    }

    private fun generateQRCode(perfume: RegisteredPerfume): String {
        // Generate a unique QR code containing perfume details
        return "ALMMR-${UUID.randomUUID()}"
    }

    suspend fun updatePerfumeStatus(id: Long, newStatus: PerfumeStatus) {
        val perfume = getPerfumeById(id) ?: return
        val updatedPerfume = perfume.copy(
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        registeredPerfumeDao.updatePerfume(updatedPerfume)
    }

    fun getMarketAnalytics(): Flow<MarketAnalytics> {
        return getAllPerfumes().map { perfumes ->
            MarketAnalytics(
                totalPerfumes = perfumes.size,
                typeDistribution = perfumes.groupBy { it.type }
                    .mapValues { it.value.size },
                averagePrice = perfumes.map { it.pricePoint }.average(),
                popularScentFamilies = perfumes.groupBy { it.scentFamily }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)
                    .toMap(),
                marketSegmentation = perfumes.groupBy { it.targetMarket }
                    .mapValues { it.value.size }
            )
        }
    }

    suspend fun generateBatchReport(batchPrefix: String): BatchReport {
        val perfumes = registeredPerfumeDao.getPerfumesByBatchPrefix(batchPrefix).first()
        return BatchReport(
            batchPrefix = batchPrefix,
            totalPerfumes = perfumes.size,
            status = perfumes.groupBy { it.status }
                .mapValues { it.value.size },
            qualityMetrics = calculateQualityMetrics(perfumes)
        )
    }

    private fun calculateQualityMetrics(perfumes: List<RegisteredPerfume>): QualityMetrics {
        return QualityMetrics(
            averageLongevity = perfumes.map { it.longevity }.average(),
            sillageDistribution = perfumes.groupBy { it.sillage }
                .mapValues { it.value.size }
        )
    }
}

data class MarketAnalytics(
    val totalPerfumes: Int,
    val typeDistribution: Map<PerfumeType, Int>,
    val averagePrice: Double,
    val popularScentFamilies: Map<String, Int>,
    val marketSegmentation: Map<String, Int>
)

data class BatchReport(
    val batchPrefix: String,
    val totalPerfumes: Int,
    val status: Map<PerfumeStatus, Int>,
    val qualityMetrics: QualityMetrics
)

data class QualityMetrics(
    val averageLongevity: Double,
    val sillageDistribution: Map<SillageRating, Int>
)
