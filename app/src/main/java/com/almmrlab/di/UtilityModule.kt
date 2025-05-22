package com.almmrlab.di

import android.content.Context
import com.almmrlab.data.AppDatabase
import com.almmrlab.data.repository.*
import com.almmrlab.util.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UtilityModule {

    @Provides
    @Singleton
    fun provideAIManager(
        @ApplicationContext context: Context
    ): AIManager = AIManager(context)

    @Provides
    @Singleton
    fun provideDocumentGenerator(
        @ApplicationContext context: Context
    ): DocumentGenerator = DocumentGenerator(context)

    @Provides
    @Singleton
    fun provideDataExporter(
        @ApplicationContext context: Context
    ): DataExporter = DataExporter(context)

    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context,
        database: AppDatabase,
        perfumeFormulaRepository: PerfumeFormulaRepository,
        rawMaterialRepository: RawMaterialRepository,
        manufacturerRepository: ManufacturerRepository,
        registeredPerfumeRepository: RegisteredPerfumeRepository,
        dataExporter: DataExporter
    ): SyncManager = SyncManager(
        context,
        database,
        perfumeFormulaRepository,
        rawMaterialRepository,
        manufacturerRepository,
        registeredPerfumeRepository,
        dataExporter
    )

    @Provides
    @Singleton
    fun provideLanguageManager(
        @ApplicationContext context: Context
    ): LanguageManager = LanguageManager(context)
}

class LanguageManager @Inject constructor(
    private val context: Context
) {
    companion object {
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_ARABIC = "ar"
    }

    private val prefs = context.getSharedPreferences("language_prefs", Context.MODE_PRIVATE)

    fun setLanguage(languageCode: String) {
        prefs.edit().putString("selected_language", languageCode).apply()
        updateConfiguration(languageCode)
    }

    fun getCurrentLanguage(): String {
        return prefs.getString("selected_language", LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }

    fun isRTL(): Boolean {
        return getCurrentLanguage() == LANGUAGE_ARABIC
    }

    private fun updateConfiguration(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)
        
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    fun getLocalizedString(resourceId: Int): String {
        val config = context.resources.configuration
        config.setLocale(Locale(getCurrentLanguage()))
        val localizedContext = context.createConfigurationContext(config)
        return localizedContext.getString(resourceId)
    }

    fun getSupportedLanguages(): List<Language> {
        return listOf(
            Language(LANGUAGE_ENGLISH, "English", "English"),
            Language(LANGUAGE_ARABIC, "العربية", "Arabic")
        )
    }
}

data class Language(
    val code: String,
    val nativeName: String,
    val englishName: String
)
