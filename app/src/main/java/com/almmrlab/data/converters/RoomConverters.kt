package com.almmrlab.data.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.almmrlab.data.entities.*
import java.util.Date

class RoomConverters {
    private val gson = Gson()

    // Date Converters
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // List<String> Converters
    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return gson.toJson(value ?: emptyList<String>())
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // Note List Converters
    @TypeConverter
    fun fromNoteList(value: List<Note>?): String {
        return gson.toJson(value ?: emptyList<Note>())
    }

    @TypeConverter
    fun toNoteList(value: String): List<Note> {
        val listType = object : TypeToken<List<Note>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // ProjectStatus List Converters
    @TypeConverter
    fun fromProjectStatusList(value: List<ProjectStatus>?): String {
        return gson.toJson(value ?: emptyList<ProjectStatus>())
    }

    @TypeConverter
    fun toProjectStatusList(value: String): List<ProjectStatus> {
        val listType = object : TypeToken<List<ProjectStatus>>() {}.type
        return gson.fromJson(value, listType) ?: emptyList()
    }

    // Enum Converters
    @TypeConverter
    fun fromFormulaStatus(value: FormulaStatus): String = value.name

    @TypeConverter
    fun toFormulaStatus(value: String): FormulaStatus = 
        FormulaStatus.valueOf(value)

    @TypeConverter
    fun fromMaterialType(value: MaterialType): String = value.name

    @TypeConverter
    fun toMaterialType(value: String): MaterialType = 
        MaterialType.valueOf(value)

    @TypeConverter
    fun fromProductionStage(value: ProductionStage): String = value.name

    @TypeConverter
    fun toProductionStage(value: String): ProductionStage = 
        ProductionStage.valueOf(value)

    @TypeConverter
    fun fromPerfumeType(value: PerfumeType): String = value.name

    @TypeConverter
    fun toPerfumeType(value: String): PerfumeType = 
        PerfumeType.valueOf(value)

    @TypeConverter
    fun fromSillageRating(value: SillageRating): String = value.name

    @TypeConverter
    fun toSillageRating(value: String): SillageRating = 
        SillageRating.valueOf(value)

    @TypeConverter
    fun fromPerfumeStatus(value: PerfumeStatus): String = value.name

    @TypeConverter
    fun toPerfumeStatus(value: String): PerfumeStatus = 
        PerfumeStatus.valueOf(value)
}
