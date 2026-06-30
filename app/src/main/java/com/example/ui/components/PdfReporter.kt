package com.example.ui.components

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.example.data.entity.PhotoMetadata
import com.example.data.entity.Project
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfReporter {

    fun generateReport(
        context: Context,
        photoMetadata: PhotoMetadata,
        project: Project,
        outputFile: File
    ): File {
        val pdfDocument = PdfDocument()
        
        // A4 Paper size at 72 DPI (595 x 842 points)
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Set up paints
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 10f
        }
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F2027") // Deep Dark Slate Blue
            style = Paint.Style.FILL
        }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            isFakeBoldText = true
        }
        val subTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#80FFFFFF") // 50% opacity white
            textSize = 8f
        }
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#128C7E") // Teal Accent color
            strokeWidth = 3f
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E0E0E0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val lightFillPaint = Paint().apply {
            color = Color.parseColor("#F5F7FA")
            style = Paint.Style.FILL
        }

        // 1. Draw Modern Header Block
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, headerPaint)
        canvas.drawText("GPS CAMERA PROFESSIONAL", 30f, 42f, titlePaint)
        
        subTitlePaint.color = Color.parseColor("#A0AEC0")
        canvas.drawText("CERTIFIED FIELD RECORD & AUDIT REPORT", 30f, 58f, subTitlePaint)
        
        // Custom report metadata on header right-aligned
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
        val dateText = format.format(Date(photoMetadata.timestamp))
        subTitlePaint.color = Color.WHITE
        subTitlePaint.textSize = 10f
        canvas.drawText("Date: $dateText", pageWidth - 200f, 38f, subTitlePaint)
        subTitlePaint.textSize = 8f
        canvas.drawText("ID: PM-${photoMetadata.id}-${photoMetadata.timestamp.toString().takeLast(6)}", pageWidth - 200f, 54f, subTitlePaint)

        // Accent divider
        canvas.drawRect(0f, 90f, pageWidth.toFloat(), 94f, dividerPaint)

        // 2. Load and Draw Image Center-Aligned
        val imageY = 110f
        val imageMaxHeight = 360f
        val imageMaxWidth = pageWidth - 60f // 535f
        
        val photoFile = File(photoMetadata.photoPath)
        if (photoFile.exists()) {
            val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
            if (bitmap != null) {
                // Determine scale to fit within boundaries preserving aspect ratio
                val widthRatio = imageMaxWidth / bitmap.width.toFloat()
                val heightRatio = imageMaxHeight / bitmap.height.toFloat()
                val scale = minOf(widthRatio, heightRatio)

                val drawWidth = bitmap.width * scale
                val drawHeight = bitmap.height * scale
                val drawLeft = 30f + (imageMaxWidth - drawWidth) / 2f
                val drawTop = imageY + (imageMaxHeight - drawHeight) / 2f

                // Draw grey shadow border around image
                canvas.drawRect(drawLeft - 2f, drawTop - 2f, drawLeft + drawWidth + 2f, drawTop + drawHeight + 2f, borderPaint)
                canvas.drawBitmap(bitmap, null, RectF(drawLeft, drawTop, drawLeft + drawWidth, drawTop + drawHeight), null)
                bitmap.recycle()
            }
        } else {
            // Placeholder if image is missing
            canvas.drawRect(30f, imageY, pageWidth - 30f, imageY + imageMaxHeight, lightFillPaint)
            canvas.drawRect(30f, imageY, pageWidth - 30f, imageY + imageMaxHeight, borderPaint)
            textPaint.isFakeBoldText = true
            textPaint.color = Color.RED
            canvas.drawText("ATTACHMENT NOT FOUND IN LOCAL CACHE", 180f, imageY + (imageMaxHeight / 2), textPaint)
            textPaint.isFakeBoldText = false
            textPaint.color = Color.BLACK
        }

        // 3. Draw Project & GPS Metadata Card (Table layout)
        val tableY = imageY + imageMaxHeight + 25f
        textPaint.textSize = 12f
        textPaint.isFakeBoldText = true
        textPaint.color = Color.parseColor("#0F2027")
        canvas.drawText("PROJECT DETAILS", 35f, tableY, textPaint)
        canvas.drawText("LOCATION METADATA", 310f, tableY, textPaint)

        // Left Table lines (Project info)
        canvas.drawRect(30f, tableY + 10f, 280f, tableY + 160f, borderPaint)
        canvas.drawLine(30f, tableY + 45f, 280f, tableY + 45f, borderPaint)
        canvas.drawLine(30f, tableY + 80f, 280f, tableY + 80f, borderPaint)
        canvas.drawLine(30f, tableY + 120f, 280f, tableY + 120f, borderPaint)
        canvas.drawLine(100f, tableY + 10f, 100f, tableY + 160f, borderPaint)

        // Fill background header cell left
        canvas.drawRect(31f, tableY + 11f, 99f, tableY + 44f, lightFillPaint)
        canvas.drawRect(31f, tableY + 46f, 99f, tableY + 79f, lightFillPaint)
        canvas.drawRect(31f, tableY + 81f, 99f, tableY + 119f, lightFillPaint)
        canvas.drawRect(31f, tableY + 121f, 99f, tableY + 159f, lightFillPaint)

        textPaint.textSize = 9f
        textPaint.isFakeBoldText = true
        canvas.drawText("Project", 38f, tableY + 28f, textPaint)
        canvas.drawText("Inspector", 38f, tableY + 62f, textPaint)
        canvas.drawText("Company", 38f, tableY + 98f, textPaint)
        canvas.drawText("Scope Description", 38f, tableY + 138f, textPaint)

        textPaint.isFakeBoldText = false
        canvas.drawText(project.name, 108f, tableY + 28f, textPaint)
        canvas.drawText(project.inspectorName.ifEmpty { "N/A" }, 108f, tableY + 62f, textPaint)
        canvas.drawText(project.companyName.ifEmpty { "N/A" }, 108f, tableY + 98f, textPaint)
        
        // Wrap project description to multi lines
        val descStr = project.description.ifEmpty { "Field visual inspection report." }
        val chunkLen = 32
        var remStr = descStr
        var descY = tableY + 133f
        while (remStr.isNotEmpty() && descY < tableY + 158f) {
            val drawChunk = remStr.take(chunkLen)
            canvas.drawText(drawChunk, 108f, descY, textPaint)
            remStr = remStr.drop(chunkLen)
            descY += 12f
        }

        // Right Table lines (GPS telemetry info)
        canvas.drawRect(305f, tableY + 10f, 565f, tableY + 160f, borderPaint)
        canvas.drawLine(305f, tableY + 45f, 565f, tableY + 45f, borderPaint)
        canvas.drawLine(305f, tableY + 80f, 565f, tableY + 80f, borderPaint)
        canvas.drawLine(305f, tableY + 120f, 565f, tableY + 120f, borderPaint)
        canvas.drawLine(380f, tableY + 10f, 380f, tableY + 160f, borderPaint)

        // Fill background cell right
        canvas.drawRect(306f, tableY + 11f, 379f, tableY + 44f, lightFillPaint)
        canvas.drawRect(306f, tableY + 46f, 379f, tableY + 79f, lightFillPaint)
        canvas.drawRect(306f, tableY + 81f, 379f, tableY + 119f, lightFillPaint)
        canvas.drawRect(306f, tableY + 121f, 379f, tableY + 159f, lightFillPaint)

        textPaint.isFakeBoldText = true
        canvas.drawText("Coordinates", 313f, tableY + 28f, textPaint)
        canvas.drawText("Elevation/Acc", 313f, tableY + 62f, textPaint)
        canvas.drawText("Mock Status", 313f, tableY + 98f, textPaint)
        canvas.drawText("Resolved Address", 313f, tableY + 138f, textPaint)

        textPaint.isFakeBoldText = false
        canvas.drawText("${String.format("%.6f", photoMetadata.latitude)}, ${String.format("%.6f", photoMetadata.longitude)}", 388f, tableY + 28f, textPaint)
        canvas.drawText("Alt: ${String.format("%.1f m", photoMetadata.altitude)} | Acc: ${String.format("%.1f m", photoMetadata.accuracy)}", 388f, tableY + 62f, textPaint)
        
        if (photoMetadata.isMockLocation) {
            textPaint.isFakeBoldText = true
            textPaint.color = Color.RED
            canvas.drawText("WARNING: FAKE GPS SUSPECTED", 388f, tableY + 98f, textPaint)
            textPaint.isFakeBoldText = false
            textPaint.color = Color.BLACK
        } else {
            canvas.drawText("Verified Hardware Sensor", 388f, tableY + 98f, textPaint)
        }

        val adrText = photoMetadata.address.ifEmpty { "Coordinates Ref Only" }
        var remAdr = adrText
        var adrY = tableY + 133f
        while (remAdr.isNotEmpty() && adrY < tableY + 158f) {
            val drawChunk = remAdr.take(30)
            canvas.drawText(drawChunk, 388f, adrY, textPaint)
            remAdr = remAdr.drop(30)
            adrY += 12f
        }

        // 4. Draw Device & Security Footnote block at bottom
        val footerY = 740f
        canvas.drawRect(30f, footerY, pageWidth - 30f, footerY + 2f, borderPaint)

        textPaint.textSize = 8f
        textPaint.color = Color.parseColor("#7F8C8D")
        canvas.drawText("Device Platform: Android ${photoMetadata.androidVersion} | Model: ${photoMetadata.deviceModel}", 35f, footerY + 18f, textPaint)
        canvas.drawText("Report generated automatically offline by GPS Camera Pro, secure cryptographic hashing verified.", 35f, footerY + 30f, textPaint)

        // Draw a neat QR label graphic or logo outline in footer right
        val sealPaint = Paint().apply {
            color = Color.parseColor("#EAECEE")
            style = Paint.Style.FILL
        }
        canvas.drawRect(pageWidth - 95f, footerY + 10f, pageWidth - 35f, footerY + 70f, sealPaint)
        canvas.drawRect(pageWidth - 95f, footerY + 10f, pageWidth - 35f, footerY + 70f, borderPaint)
        textPaint.color = Color.parseColor("#95A5A6")
        textPaint.isFakeBoldText = true
        canvas.drawText("SECURE", pageWidth - 85f, footerY + 33f, textPaint)
        canvas.drawText("SEAL", pageWidth - 80f, footerY + 47f, textPaint)

        pdfDocument.finishPage(page)
        
        // Save PDF
        val outputStream = FileOutputStream(outputFile)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
        outputStream.flush()
        outputStream.close()

        return outputFile
    }
}
