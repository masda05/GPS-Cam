package com.example.ui.components

import android.content.Context
import android.graphics.*
import com.example.data.entity.PhotoMetadata
import com.example.data.entity.Project
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

object WatermarkEngine {

    fun applyWatermark(
        context: Context,
        inputPhotoFile: File,
        project: Project,
        metadata: PhotoMetadata,
        customFontSize: Float = 14f,
        customColor: Int = Color.WHITE,
        customOpacity: Float = 0.8f,
        customPosition: String = "BOTTOM_LEFT",
        watermarkFontSizeScale: Float = 1.0f,
        showWatermarkMap: Boolean = true
    ): File {
        // Load original bitmap
        val options = BitmapFactory.Options().apply {
            inMutable = true // Keep it mutable
        }
        val originalBitmap = BitmapFactory.decodeFile(inputPhotoFile.absolutePath, options) ?: return inputPhotoFile

        // Setup canvas
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        // Calculate scaling factors based on a base scale multiplied by custom size scale
        val baseWidth = 1080f
        val scale = (mutableBitmap.width / baseWidth) * watermarkFontSizeScale

        val template = metadata.watermarkTemplate
        when (template) {
            "A" -> drawTemplateCorporate(canvas, mutableBitmap.width, mutableBitmap.height, scale, project, metadata, showWatermarkMap)
            "B" -> drawTemplateMinimal(canvas, mutableBitmap.width, mutableBitmap.height, scale, project, metadata)
            "C" -> drawTemplateConstruction(canvas, mutableBitmap.width, mutableBitmap.height, scale, project, metadata, showWatermarkMap)
            "D" -> drawTemplateSurveyor(canvas, mutableBitmap.width, mutableBitmap.height, scale, project, metadata, showWatermarkMap)
            "E" -> drawTemplateCustom(canvas, mutableBitmap.width, mutableBitmap.height, scale, project, metadata, customFontSize, customColor, customOpacity, customPosition, showWatermarkMap)
            else -> drawTemplateCorporate(canvas, mutableBitmap.width, mutableBitmap.height, scale, project, metadata, showWatermarkMap)
        }

        // Save watermarked image overwriting original file
        val outputStream = FileOutputStream(inputPhotoFile)
        mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.flush()
        outputStream.close()
        mutableBitmap.recycle()
        originalBitmap.recycle()

        return inputPhotoFile
    }

    private fun drawMiniMap(
        canvas: Canvas,
        mapLeft: Float,
        mapTop: Float,
        mapSize: Float,
        scale: Float,
        metadata: PhotoMetadata
    ) {
        // Draw map frame background
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#121F28") // Dark slate green background
            style = Paint.Style.FILL
        }
        val mapRect = RectF(mapLeft, mapTop, mapLeft + mapSize, mapTop + mapSize)
        canvas.drawRoundRect(mapRect, 10f * scale, 10f * scale, bgPaint)

        // Seed generator deterministically using coordinates
        val seed = ((metadata.latitude * 100000).toLong() xor (metadata.longitude * 100000).toLong())
        val random = Random(seed)

        // Draw topographic/contour circular lines
        val contourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1500E676") // Subtle green contour
            style = Paint.Style.STROKE
            strokeWidth = 1f * scale
        }
        val centerCx = mapLeft + mapSize / 2f
        val centerCy = mapTop + mapSize / 2f
        for (r in 1..4) {
            val radius = (r * 15f * scale)
            canvas.drawCircle(centerCx, centerCy, radius, contourPaint)
        }

        // Draw coordinate grid lines (dotted grids)
        val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2200E676") // Subtle green grid lines
            style = Paint.Style.STROKE
            strokeWidth = 0.8f * scale
        }
        val gridSpacing = 30f * scale
        var gx = mapLeft + 10f * scale
        while (gx < mapLeft + mapSize) {
            canvas.drawLine(gx, mapTop, gx, mapTop + mapSize, gridPaint)
            gx += gridSpacing
        }
        var gy = mapTop + 10f * scale
        while (gy < mapTop + mapSize) {
            canvas.drawLine(mapLeft, gy, mapLeft + mapSize, gy, gridPaint)
            gy += gridSpacing
        }

        // Draw deterministic paths (representing map features like local pathways/creeks)
        val featurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4000E676") // Semi-transparent green
            style = Paint.Style.STROKE
            strokeWidth = 2.5f * scale
        }

        val pathCount = 2 + random.nextInt(3)
        for (p in 0 until pathCount) {
            val path = Path()
            val startOnLeft = random.nextBoolean()
            if (startOnLeft) {
                path.moveTo(mapLeft, mapTop + random.nextFloat() * mapSize)
                path.cubicTo(
                    mapLeft + mapSize * 0.3f, mapTop + random.nextFloat() * mapSize,
                    mapLeft + mapSize * 0.7f, mapTop + random.nextFloat() * mapSize,
                    mapLeft + mapSize, mapTop + random.nextFloat() * mapSize
                )
            } else {
                path.moveTo(mapLeft + random.nextFloat() * mapSize, mapTop)
                path.cubicTo(
                    mapLeft + random.nextFloat() * mapSize, mapTop + mapSize * 0.3f,
                    mapLeft + random.nextFloat() * mapSize, mapTop + mapSize * 0.7f,
                    mapLeft + random.nextFloat() * mapSize, mapTop + mapSize
                )
            }
            canvas.drawPath(path, featurePaint)
        }

        // Draw dynamic terrain boundary shapes (buildings or zones)
        val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E26B07A") // Light green zone fill
            style = Paint.Style.FILL
        }
        for (s in 0..1) {
            val sw = 20f * scale + random.nextFloat() * 40f * scale
            val sh = 15f * scale + random.nextFloat() * 30f * scale
            val sl = mapLeft + random.nextFloat() * (mapSize - sw)
            val st = mapTop + random.nextFloat() * (mapSize - sh)
            canvas.drawRect(sl, st, sl + sw, st + sh, sectorPaint)
        }

        // Draw reticle target marker at center
        val centerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        val outerCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * scale
        }
        val centerCrossPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            strokeWidth = 1f * scale
        }
        val clen = 8f * scale
        canvas.drawLine(centerCx - clen, centerCy, centerCx + clen, centerCy, centerCrossPaint)
        canvas.drawLine(centerCx, centerCy - clen, centerCx, centerCy + clen, centerCrossPaint)
        canvas.drawCircle(centerCx, centerCy, 3f * scale, centerPaint)
        canvas.drawCircle(centerCx, centerCy, 8f * scale, outerCenterPaint)

        // Cardinal Indicators N, S, E, W
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B2ECEFF1")
            textSize = 7f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }
        canvas.drawText("N", centerCx - 2.5f * scale, mapTop + 8f * scale, textPaint)
        canvas.drawText("S", centerCx - 2.5f * scale, mapTop + mapSize - 2f * scale, textPaint)
        canvas.drawText("W", mapLeft + 3f * scale, centerCy + 3f * scale, textPaint)
        canvas.drawText("E", mapLeft + mapSize - 8f * scale, centerCy + 3f * scale, textPaint)

        // Border frame outer line
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4400E676")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * scale
        }
        canvas.drawRoundRect(mapRect, 10f * scale, 10f * scale, borderPaint)

        // Map Scale Indicator bar
        val barY = mapTop + mapSize - 12f * scale
        canvas.drawLine(mapLeft + 10f * scale, barY, mapLeft + 40f * scale, barY, borderPaint)
        canvas.drawLine(mapLeft + 10f * scale, barY - 2f * scale, mapLeft + 10f * scale, barY + 2f * scale, borderPaint)
        canvas.drawLine(mapLeft + 40f * scale, barY - 2f * scale, mapLeft + 40f * scale, barY + 2f * scale, borderPaint)

        val scaleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 6f * scale
        }
        canvas.drawText("GPS LOC TYPE", mapLeft + 10f * scale, barY - 4f * scale, scaleTextPaint)
    }

    private fun drawTemplateCorporate(
        canvas: Canvas,
        width: Int,
        height: Int,
        scale: Float,
        project: Project,
        metadata: PhotoMetadata,
        showWatermarkMap: Boolean
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 15f * scale
        }

        val padding = 20 * scale
        val boxWidth = if (showWatermarkMap) {
            // Widen box width if scale is small so map fits comfortably
            720f * scale
        } else {
            520f * scale
        }
        val boxHeight = 290 * scale

        // Position: Bottom Left standard
        val boxLeft = padding
        val boxTop = height - boxHeight - padding
        val boxRight = boxLeft + boxWidth
        val boxBottom = height - padding

        // Draw translucent backdrop
        val rectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E6121D24") // Midnight blue/black
            style = Paint.Style.FILL
        }
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4433E0E0") // Cyan border
            style = Paint.Style.STROKE
            strokeWidth = 2f * scale
        }

        val r = RectF(boxLeft, boxTop, boxRight, boxBottom)
        canvas.drawRoundRect(r, 12f * scale, 12f * scale, rectPaint)
        canvas.drawRoundRect(r, 12f * scale, 12f * scale, borderPaint)

        // Draw text fields
        paint.color = Color.parseColor("#33E0E0") // Cyan header title
        paint.isFakeBoldText = true
        paint.textSize = 17f * scale
        val startX = boxLeft + 18f * scale
        var currentY = boxTop + 34f * scale
        canvas.drawText("FIELD DISPATCH PROTOCOL", startX, currentY, paint)

        paint.isFakeBoldText = false
        paint.textSize = 13.5f * scale
        paint.color = Color.WHITE

        val format = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault())
        val dateText = format.format(Date(metadata.timestamp))

        val lines = listOf(
            "Project  : ${project.name}",
            "Inspector: ${project.inspectorName} (${project.companyName})",
            "Lat/Lng  : ${String.format("%.6f", metadata.latitude)}, ${String.format("%.6f", metadata.longitude)}",
            "Accuracy : ${String.format("%.1fm", metadata.accuracy)} (Alt: ${String.format("%.0fm", metadata.altitude)})",
            "Bearing  : ${String.format("%.1f°", metadata.bearing)} | Speed: ${String.format("%.1f km/h", metadata.speed)}",
            "Address  : ${if (metadata.address.length > 35) metadata.address.take(35) + "..." else metadata.address}",
            "Date     : $dateText"
        )

        paint.color = Color.parseColor("#B3D4EC")
        for (line in lines) {
            currentY += 28f * scale
            if (line.contains(":")) {
                val parts = line.split(":", limit = 2)
                paint.color = Color.parseColor("#90CAF9") // Light blue labels
                paint.isFakeBoldText = true
                canvas.drawText(parts[0] + ":", startX, currentY, paint)

                val labelWidth = paint.measureText(parts[0] + ": ")
                paint.color = Color.WHITE
                paint.isFakeBoldText = false
                canvas.drawText(parts[1].trim(), startX + labelWidth, currentY, paint)
            } else {
                canvas.drawText(line, startX, currentY, paint)
            }
        }

        // Output Mini Map on the right side if showWatermarkMap is true
        if (showWatermarkMap) {
            val mapSize = 170f * scale
            val mapLeft = boxRight - mapSize - 20f * scale
            val mapTop = boxTop + (boxHeight - mapSize) / 2f
            drawMiniMap(canvas, mapLeft, mapTop, mapSize, scale, metadata)
        }
    }

    private fun drawTemplateMinimal(
        canvas: Canvas,
        width: Int,
        height: Int,
        scale: Float,
        project: Project,
        metadata: PhotoMetadata
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 15f * scale
            setShadowLayer(4f * scale, 0f, 2f * scale, Color.BLACK)
        }

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateText = format.format(Date(metadata.timestamp))

        val overlayText = "GPS CAMERA PRO - ${project.name} | Lat ${String.format("%.6f", metadata.latitude)} Lng ${String.format("%.6f", metadata.longitude)} | Alt ${String.format("%.0f", metadata.altitude)}m | $dateText"

        val x = 30f * scale
        val y = height - (30f * scale)

        canvas.drawText(overlayText, x, y, paint)
    }

    private fun drawTemplateConstruction(
        canvas: Canvas,
        width: Int,
        height: Int,
        scale: Float,
        project: Project,
        metadata: PhotoMetadata,
        showWatermarkMap: Boolean
    ) {
        val stripeHeight = 40f * scale
        val boxHeight = 240f * scale
        val boxWidth = if (showWatermarkMap) 790f * scale else 600f * scale

        // Position: Bottom Right
        val boxLeft = width - boxWidth - (20 * scale)
        val boxTop = height - boxHeight - (20 * scale)
        val boxRight = width - (20 * scale)
        val boxBottom = height - (20 * scale)

        // Draw background box
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFF3E0")
            style = Paint.Style.FILL
        }
        val rect = RectF(boxLeft, boxTop, boxRight, boxBottom)
        canvas.drawRect(rect, fillPaint)

        // Stripe Warn Yellow header
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFC107")
            style = Paint.Style.FILL
        }
        canvas.drawRect(boxLeft, boxTop, boxRight, boxTop + stripeHeight, headerPaint)

        // Zebra diagonal lines
        val zebraPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            strokeWidth = 5f * scale
            style = Paint.Style.STROKE
        }
        var i = boxLeft
        while (i < boxRight) {
            canvas.drawLine(i, boxTop, i + 15f * scale, boxTop + stripeHeight, zebraPaint)
            i += 30f * scale
        }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 15f * scale
            isFakeBoldText = true
        }

        val startX = boxLeft + 15 * scale
        var currentY = boxTop + stripeHeight + 28 * scale

        canvas.drawText("CONSTRUCTION LOGGING SIG", startX, currentY, textPaint)
        textPaint.isFakeBoldText = false
        textPaint.textSize = 13f * scale

        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateText = format.format(Date(metadata.timestamp))

        val lines = listOf(
            "SITE ID: ${project.name.uppercase()}",
            "OPERATOR: ${project.inspectorName.uppercase()}",
            "GEOREF: ${String.format("%.6f", metadata.latitude)}, ${String.format("%.6f", metadata.longitude)}",
            "ACCURACY: ${String.format("%.1f meters", metadata.accuracy)}",
            "LOC: ${if (metadata.address.length > 35) metadata.address.take(35) + "..." else metadata.address}",
            "TIMESTAMP: $dateText"
        )

        for (line in lines) {
            currentY += 23 * scale
            canvas.drawText(line, startX, currentY, textPaint)
        }

        if (showWatermarkMap) {
            val mapSize = 145f * scale
            val mapLeft = boxRight - mapSize - 18f * scale
            val mapTop = boxTop + stripeHeight + (boxHeight - stripeHeight - mapSize) / 2f
            drawMiniMap(canvas, mapLeft, mapTop, mapSize, scale, metadata)
        }
    }

    private fun drawTemplateSurveyor(
        canvas: Canvas,
        width: Int,
        height: Int,
        scale: Float,
        project: Project,
        metadata: PhotoMetadata,
        showWatermarkMap: Boolean
    ) {
        val reticlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E676")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * scale
        }

        // Draw center cross
        val cx = width / 2f
        val cy = height / 2f
        val r = 50f * scale
        canvas.drawCircle(cx, cy, r, reticlePaint)
        canvas.drawLine(cx - r - 20f, cy, cx + r + 20f, cy, reticlePaint)
        canvas.drawLine(cx, cy - r - 20f, cx, cy + r + 20f, reticlePaint)

        // Draw technical text list (HUD)
        val hudPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E676")
            textSize = 13f * scale
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault())
        val timestamp = format.format(Date(metadata.timestamp))

        val leftX = 30f * scale
        val rightX = width - 280f * scale

        // Left panels
        canvas.drawText("+AZIMUTH: ${String.format("%.1f° (North)", metadata.bearing)}", leftX, 60f * scale, hudPaint)
        canvas.drawText("+TILT AR: 0.0°", leftX, 85f * scale, hudPaint)
        canvas.drawText("+LATITUDE: ${String.format("%.6f", metadata.latitude)}°", leftX, 110f * scale, hudPaint)
        canvas.drawText("+LONGITUDE: ${String.format("%.6f", metadata.longitude)}°", leftX, 135f * scale, hudPaint)

        // Right panels
        canvas.drawText("+ELEVATION: ${String.format("%.1f m (E)", metadata.altitude)}", rightX, 60f * scale, hudPaint)
        canvas.drawText("+PRECISION: ${String.format("%.1f m", metadata.accuracy)}", rightX, 85f * scale, hudPaint)
        canvas.drawText("+INDEX ID: ${project.name}", rightX, 110f * scale, hudPaint)
        canvas.drawText("+CLK TIME: $timestamp", rightX, 135f * scale, hudPaint)

        // Surveyor Grid Mini map bottom right corner
        if (showWatermarkMap) {
            val mapSize = 160f * scale
            val mapLeft = width - mapSize - 30f * scale
            val mapTop = height - mapSize - 40f * scale
            drawMiniMap(canvas, mapLeft, mapTop, mapSize, scale, metadata)
        }
    }

    private fun drawTemplateCustom(
        canvas: Canvas,
        width: Int,
        height: Int,
        scale: Float,
        project: Project,
        metadata: PhotoMetadata,
        customFontSize: Float,
        customColor: Int,
        customOpacity: Float,
        customPosition: String,
        showWatermarkMap: Boolean
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = customColor
            alpha = (customOpacity * 255).toInt()
            textSize = customFontSize * scale
            isFakeBoldText = true
            setShadowLayer(3f * scale, 0f, 1f * scale, Color.BLACK)
        }

        val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val dateText = format.format(Date(metadata.timestamp))

        val lines = listOf(
            "Project: ${project.name}",
            "Inspector: ${project.inspectorName}",
            "GPS: ${String.format("%.6f", metadata.latitude)}, ${String.format("%.6f", metadata.longitude)}",
            "Altitude: ${String.format("%.1fm", metadata.altitude)} | Acc: ${String.format("%.1fm", metadata.accuracy)}",
            "DateTime: $dateText"
        )

        val padding = 30 * scale
        var currentY: Float
        val startX: Float

        when (customPosition) {
            "TOP_LEFT" -> {
                startX = padding
                currentY = padding + 20f * scale
            }
            "TOP_RIGHT" -> {
                startX = width - (280f * scale)
                currentY = padding + 20f * scale
            }
            "BOTTOM_RIGHT" -> {
                startX = width - (280f * scale)
                currentY = height - (lines.size * 25f * scale) - padding
            }
            else -> { // BOTTOM_LEFT
                startX = padding
                currentY = height - (lines.size * 25f * scale) - padding
            }
        }

        for (line in lines) {
            canvas.drawText(line, startX, currentY, textPaint)
            currentY += 24f * scale
        }

        // Draw Mini Map on the opposite corner to avoid overlaps
        if (showWatermarkMap) {
            val mapSize = 135f * scale
            val mapPadding = 30f * scale
            val mapLeft = if (customPosition.endsWith("RIGHT")) {
                mapPadding
            } else {
                width - mapSize - mapPadding
            }
            val mapTop = if (customPosition.startsWith("TOP")) {
                height - mapSize - mapPadding
            } else {
                mapPadding + 20f * scale
            }
            drawMiniMap(canvas, mapLeft, mapTop, mapSize, scale, metadata)
        }
    }
}
