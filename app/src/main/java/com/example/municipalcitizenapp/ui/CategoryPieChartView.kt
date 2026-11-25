package com.example.municipalcitizenapp.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CategoryPieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var categoryData: Map<String, Int> = emptyMap()
    private val slicePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFFFFF")
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#0F172A")
        textSize = 34f
        typeface = Typeface.DEFAULT_BOLD
    }
    private val legendPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#475569")
        textSize = 28f
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val colors = listOf(
        Color.parseColor("#1E3A8A"), // Royal Blue
        Color.parseColor("#2563EB"), // Electric Blue
        Color.parseColor("#0284C7"), // Sky Blue
        Color.parseColor("#0D9488"), // Teal
        Color.parseColor("#16A34A"), // Emerald Green
        Color.parseColor("#D97706"), // Amber
        Color.parseColor("#DC2626"), // Crimson
        Color.parseColor("#7C3AED"), // Purple
        Color.parseColor("#64748B")  // Slate
    )

    private val rectF = RectF()

    fun setData(data: Map<String, Int>) {
        this.categoryData = data
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val legendLines = categoryData.size.coerceAtLeast(1)
        val desiredHeight = (320 + legendLines * 44 + 40)
        setMeasuredDimension(desiredWidth, desiredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val total = categoryData.values.sum()
        val centerX = width / 2f
        val centerY = 160f
        val radius = 130f

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        if (total == 0) {
            slicePaint.color = Color.parseColor("#E2E8F0")
            canvas.drawArc(rectF, 0f, 360f, true, slicePaint)
            canvas.drawCircle(centerX, centerY, radius * 0.55f, holePaint)
            textPaint.textAlign = Paint.Align.CENTER
            canvas.drawText("No Complaints", centerX, centerY + 12f, textPaint)
            return
        }

        var startAngle = -90f
        var colorIndex = 0

        categoryData.entries.forEach { entry ->
            val sweepAngle = (entry.value.toFloat() / total) * 360f
            slicePaint.color = colors[colorIndex % colors.size]
            canvas.drawArc(rectF, startAngle, sweepAngle, true, slicePaint)
            startAngle += sweepAngle
            colorIndex++
        }

        // Draw doughnut hole
        canvas.drawCircle(centerX, centerY, radius * 0.55f, holePaint)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("$total", centerX, centerY + 10f, textPaint)

        // Draw Legend List below Pie Chart
        var legendY = 340f
        colorIndex = 0

        categoryData.entries.forEach { entry ->
            val color = colors[colorIndex % colors.size]
            dotPaint.color = color

            // Draw color indicator dot
            canvas.drawCircle(32f, legendY - 10f, 12f, dotPaint)

            // Draw category label and count
            legendPaint.textAlign = Paint.Align.LEFT
            val pct = String.format("%.0f%%", (entry.value.toFloat() / total) * 100)
            val labelText = "${entry.key} (${entry.value} - $pct)"

            // Truncate long category names cleanly
            val displayLabel = if (labelText.length > 38) labelText.substring(0, 35) + "..." else labelText
            canvas.drawText(displayLabel, 60f, legendY, legendPaint)

            legendY += 44f
            colorIndex++
        }
    }
}
