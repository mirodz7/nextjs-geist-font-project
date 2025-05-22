package com.almmrlab.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.almmrlab.data.converters.RoomConverters
import com.almmrlab.data.dao.*
import com.almmrlab.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PerfumeFormula::class,
        RawMaterial::class,
        Manufacturer::class,
        RegisteredPerfume::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun perfumeFormulaDao(): PerfumeFormulaDao
    abstract fun rawMaterialDao(): RawMaterialDao
    abstract fun manufacturerDao(): ManufacturerDao
    abstract fun registeredPerfumeDao(): RegisteredPerfumeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "almmr_lab_database"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate database with essential data
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                prepopulateDatabase(database)
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration() // Only for development
                .build()
                
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateDatabase(database: AppDatabase) {
            // Add common raw materials
            database.rawMaterialDao().insertMaterial(
                RawMaterial(
                    name = "Bergamot Essential Oil",
                    type = MaterialType.TOP_NOTE,
                    olfactoryProfile = "Fresh, citrusy, slightly floral",
                    supplierLinks = listOf("supplier1.com", "supplier2.com"),
                    isSynthetic = false,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )

            // Add more essential raw materials here...
        }
    }
}
