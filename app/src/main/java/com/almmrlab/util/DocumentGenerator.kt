package com.almmrlab.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.almmrlab.data.entities.RegisteredPerfume
import com.almmrlab.data.entities.PerfumeFormula
import com.almmrlab.data.entities.Manufacturer
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.*
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentGenerator @Inject constructor(
    private val context: Context
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun generatePerfumeReport(
        perfume: RegisteredPerfume,
        formula: PerfumeFormula,
        manufacturer: Manufacturer
    ): File {
        val reportFile = createReportFile(perfume.name)
        
        PdfWriter(reportFile).use { writer ->
            val pdfDoc = PdfDocument(writer)
            Document(pdfDoc).use { document ->
                // Add Header
                addHeader(document, perfume)

                // Add Basic Information
                addBasicInformation(document, perfume)

                // Add Formula Details
                addFormulaDetails(document, formula)

                // Add Scent Pyramid
                addScentPyramid(document, formula)

                // Add Manufacturing Details
                addManufacturingDetails(document, manufacturer)

                // Add QR Code and Barcode
                addIdentifiers(document, perfume)

                // Add Footer
                addFooter(document)
            }
        }

        return reportFile
    }

    private fun createReportFile(perfumeName: String): File {
        val fileName = "perfume_report_${perfumeName.replace(" ", "_")}_${
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        }.pdf"
        
        return File(context.getExternalFilesDir(null), fileName)
    }

    private fun addHeader(document: Document, perfume: RegisteredPerfume) {
        val header = Paragraph(perfume.name)
            .setFontSize(24f)
            .setTextAlignment(TextAlignment.CENTER)
            .setBold()
        document.add(header)

        document.add(Paragraph("Registration Date: ${dateFormat.format(Date(perfume.registrationDate))}"))
        document.add(Paragraph("Status: ${perfume.status}"))
    }

    private fun addBasicInformation(document: Document, perfume: RegisteredPerfume) {
        document.add(Paragraph("Basic Information").setBold().setFontSize(18f))

        val table = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()
        
        addTableRow(table, "Type", perfume.type.toString())
        addTableRow(table, "Alcohol %", "${perfume.alcoholPercentage}%")
        addTableRow(table, "Bottle Type", perfume.bottleType)
        addTableRow(table, "Size", "${perfume.bottleSize}ml")
        addTableRow(table, "Scent Family", perfume.scentFamily)
        addTableRow(table, "Longevity", "${perfume.longevity} hours")
        addTableRow(table, "Sillage", perfume.sillage.toString())

        document.add(table)
    }

    private fun addFormulaDetails(document: Document, formula: PerfumeFormula) {
        document.add(Paragraph("Formula Details").setBold().setFontSize(18f))
        document.add(Paragraph("Created by: ${formula.perfumer}"))
        document.add(Paragraph("Version: ${formula.version}"))
        document.add(Paragraph("Last Modified: ${dateFormat.format(formula.lastModified)}"))
    }

    private fun addScentPyramid(document: Document, formula: PerfumeFormula) {
        document.add(Paragraph("Scent Pyramid").setBold().setFontSize(18f))

        // Top Notes
        document.add(Paragraph("Top Notes:").setBold())
        formula.topNotes.forEach { note ->
            document.add(Paragraph("• ${note.quantity}% concentration"))
        }

        // Middle Notes
        document.add(Paragraph("Middle Notes:").setBold())
        formula.middleNotes.forEach { note ->
            document.add(Paragraph("• ${note.quantity}% concentration"))
        }

        // Base Notes
        document.add(Paragraph("Base Notes:").setBold())
        formula.baseNotes.forEach { note ->
            document.add(Paragraph("• ${note.quantity}% concentration"))
        }
    }

    private fun addManufacturingDetails(document: Document, manufacturer: Manufacturer) {
        document.add(Paragraph("Manufacturing Information").setBold().setFontSize(18f))
        
        val table = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()
        
        addTableRow(table, "Manufacturer", manufacturer.companyName)
        addTableRow(table, "Contact", manufacturer.contactPerson)
        addTableRow(table, "Certifications", manufacturer.certifications.joinToString(", "))

        document.add(table)
    }

    private fun addIdentifiers(document: Document, perfume: RegisteredPerfume) {
        document.add(Paragraph("Product Identifiers").setBold().setFontSize(18f))
        
        // Add Barcode
        perfume.barcode?.let { barcode ->
            val barcodeImage = generateBarcode(barcode)
            document.add(Image(barcodeImage.toImageData()))
            document.add(Paragraph(barcode))
        }

        // Add QR Code
        perfume.qrCode?.let { qrCode ->
            val qrCodeImage = generateQRCode(qrCode)
            document.add(Image(qrCodeImage.toImageData()))
            document.add(Paragraph(qrCode))
        }
    }

    private fun addFooter(document: Document) {
        document.add(Paragraph("Generated on: ${dateFormat.format(Date())}")
            .setTextAlignment(TextAlignment.RIGHT)
            .setFontSize(8f))
    }

    private fun addTableRow(table: Table, label: String, value: String) {
        table.addCell(Cell().add(Paragraph(label).setBold()))
        table.addCell(Cell().add(Paragraph(value)))
    }

    fun generateBarcode(content: String): Bitmap {
        // Implementation using ZXing library
        val writer = com.google.zxing.MultiFormatWriter()
        val bitMatrix = writer.encode(
            content,
            BarcodeFormat.CODE_128,
            400,
            100
        )
        return bitMatrixToBitmap(bitMatrix)
    }

    fun generateQRCode(content: String): Bitmap {
        val writer = QRCodeWriter()
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.MARGIN] = 2
        
        val bitMatrix = writer.encode(
            content,
            BarcodeFormat.QR_CODE,
            300,
            300,
            hints
        )
        return bitMatrixToBitmap(bitMatrix)
    }

    private fun bitMatrixToBitmap(bitMatrix: BitMatrix): Bitmap {
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }

        return bitmap
    }

    private fun Bitmap.toImageData(): com.itextpdf.io.image.ImageData {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return com.itextpdf.io.image.ImageDataFactory.create(stream.toByteArray())
    }
}
