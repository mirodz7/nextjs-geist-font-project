package com.almmrlab.di

import android.content.Context
import com.almmrlab.data.AppDatabase
import com.almmrlab.data.dao.*
import com.almmrlab.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = AppDatabase.getInstance(context)

    @Provides
    @Singleton
    fun providePerfumeFormulaDao(database: AppDatabase): PerfumeFormulaDao =
        database.perfumeFormulaDao()

    @Provides
    @Singleton
    fun provideRawMaterialDao(database: AppDatabase): RawMaterialDao =
        database.rawMaterialDao()

    @Provides
    @Singleton
    fun provideManufacturerDao(database: AppDatabase): ManufacturerDao =
        database.manufacturerDao()

    @Provides
    @Singleton
    fun provideRegisteredPerfumeDao(database: AppDatabase): RegisteredPerfumeDao =
        database.registeredPerfumeDao()

    @Provides
    @Singleton
    fun providePerfumeFormulaRepository(
        perfumeFormulaDao: PerfumeFormulaDao
    ): PerfumeFormulaRepository = PerfumeFormulaRepository(perfumeFormulaDao)

    @Provides
    @Singleton
    fun provideRawMaterialRepository(
        rawMaterialDao: RawMaterialDao
    ): RawMaterialRepository = RawMaterialRepository(rawMaterialDao)

    @Provides
    @Singleton
    fun provideManufacturerRepository(
        manufacturerDao: ManufacturerDao
    ): ManufacturerRepository = ManufacturerRepository(manufacturerDao)

    @Provides
    @Singleton
    fun provideRegisteredPerfumeRepository(
        registeredPerfumeDao: RegisteredPerfumeDao
    ): RegisteredPerfumeRepository = RegisteredPerfumeRepository(registeredPerfumeDao)
}
