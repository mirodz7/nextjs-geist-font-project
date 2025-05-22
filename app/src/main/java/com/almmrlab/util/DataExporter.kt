package com.almmrlab.util

import android.content.Context
import com.almmrlab.data.entities.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExporter @Inject constructor(
    private val context: Context
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .create()

    fun exportPerfumeFormulasToExcel(formulas: List<PerfumeFormula>): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Perfume Formulas")

        // Create header style
        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        // Create headers
        val headers = listOf(
            "ID", "Name", "Description", "Creation Date", "Perfumer",
            "Version", "Alcohol %", "Status", "Last Modified"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }

        // Add data
        formulas.forEachIndexed { index, formula ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(formula.id)
            row.createCell(1).setCellValue(formula.name)
            row.createCell(2).setCellValue(formula.description)
            row.createCell(3).setCellValue(dateFormat.format(formula.creationDate))
            row.createCell(4).setCellValue(formula.perfumer)
            row.createCell(5).setCellValue(formula.version)
            row.createCell(6).setCellValue(formula.alcoholPercentage)
            row.createCell(7).setCellValue(formula.status.name)
            row.createCell(8).setCellValue(dateFormat.format(formula.lastModified))
        }

        // Autosize columns
        headers.indices.forEach { sheet.autoSizeColumn(it) }

        // Write to file
        val file = createExcelFile("perfume_formulas")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        return file
    }

    fun exportRawMaterialsToExcel(materials: List<RawMaterial>): File {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Raw Materials")

        val headerStyle = workbook.createCellStyle().apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }

        // Create headers
        val headers = listOf(
            "ID", "Name", "Type", "Olfactory Profile", "Synthetic",
            "Cost", "Stock Level", "Minimum Stock", "IFRA Category", "IFRA Limit"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { index, header ->
            headerRow.createCell(index).apply {
                setCellValue(header)
                cellStyle = headerStyle
            }
        }

        // Add data
        materials.forEachIndexed { index, material ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(material.id)
            row.createCell(1).setCellValue(material.name)
            row.createCell(2).setCellValue(material.type.name)
            row.createCell(3).setCellValue(material.olfactoryProfile)
            row.createCell(4).setCellValue(material.isSynthetic)
            row.createCell(5).setCellValue(material.cost ?: 0.0)
            row.createCell(6).setCellValue(material.stockLevel ?: 0.0)
            row.createCell(7).setCellValue(material.minimumStockLevel ?: 0.0)
            row.createCell(8).setCellValue(material.ifraCategory ?: "N/A")
            row.createCell(9).setCellValue(material.ifraLimit ?: 0.0)
        }

        headers.indices.forEach { sheet.autoSizeColumn(it) }

        val file = createExcelFile("raw_materials")
        FileOutputStream(file).use { workbook.write(it) }
        workbook.close()

        return file
    }

    fun exportToJson(data: Any, filename: String): File {
        val json = gson.toJson(data)
        val file = createJsonFile(filename)
        file.writeText(json)
        return file
    }

    fun exportPerfumeFormulaToJson(formula: PerfumeFormula): File {
        return exportToJson(formula, "formula_${formula.id}")
    }

    fun exportRawMaterialToJson(material: RawMaterial): File {
        return exportToJson(material, "material_${material.id}")
    }

    fun exportManufacturerToJson(manufacturer: Manufacturer): File {
        return exportToJson(manufacturer, "manufacturer_${manufacturer.id}")
    }

    fun exportRegisteredPerfumeToJson(perfume: RegisteredPerfume): File {
        return exportToJson(perfume, "registered_perfume_${perfume.id}")
    }

    private fun createExcelFile(prefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val fileName = "${prefix}_${timestamp}.xlsx"
        return File(context.getExternalFilesDir(null), fileName)
    }

    private fun createJsonFile(prefix: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val fileName = "${prefix}_${timestamp}.json"
        return File(context.getExternalFilesDir(null), fileName)
    }

    fun exportFullDatabase(): File {
        val exportData = DatabaseExport(
            exportDate = Date(),
            version = "1.0"
            // Add collections of entities here when exporting full database
        )
        return exportToJson(exportData, "full_database_export")
    }
}

data class DatabaseExport(
    val exportDate: Date,
    val version: String,
    val formulas: List<PerfumeFormula>? = null,
    val materials: List<RawMaterial>? = null,
    val manufacturers: List<Manufacturer>? = null,
    val registeredPerfumes: List<RegisteredPerfume>? = null
)
