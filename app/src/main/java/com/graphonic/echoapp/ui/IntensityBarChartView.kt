package com.graphonic.echoapp.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.graphonic.echoapp.response.CharVolume
import kotlin.math.roundToInt

class IntensityBarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var charVolumes: List<CharVolume> = emptyList()
    private var minDb: Double = -100.0
    private var maxDb: Double = 0.0

    // Paint objects for drawing
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF666666") // Grey text
        textAlign = Paint.Align.CENTER
        textSize = spToPx(12f)
    }
    private val axisLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF888888") // Lighter Grey
        textAlign = Paint.Align.LEFT
        textSize = spToPx(10f)
    }

    private val colorMax = Color.RED
    private val colorMin = Color.parseColor("#FF00A0B0") // Teal/Blue
    private val colorDefault = Color.DKGRAY

    private val barRect = RectF()

    fun setData(volumes: List<CharVolume>, min: Double, max: Double) {
        this.charVolumes = volumes
        this.minDb = (min - 5.0).coerceAtLeast(-100.0)
        this.maxDb = (max + 5.0).coerceAtMost(0.0)
        invalidate() // Request a redraw
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (charVolumes.isEmpty()) return

        // Calculate dimensions
        val axisLabelWidth = dpToPx(40f)
        val chartAreaWidth = width - axisLabelWidth
        val labelHeight = spToPx(20f)
        val chartHeight = height - labelHeight
        val barCount = charVolumes.size
        val barSpacing = dpToPx(8f)
        val totalSpacing = (barCount - 1) * barSpacing
        val barWidth = (width - totalSpacing) / barCount

        charVolumes.forEachIndexed { index, charVolume ->
            // --- Draw Bar ---
            if (charVolume.character.isNotBlank()) {
                val normalizedHeight = normalize(charVolume.volume, minDb, maxDb)
                val barHeight = normalizedHeight * chartHeight
                val xOffset = index * (barWidth + barSpacing)

                barPaint.color = when (charVolume.volume) {
                    maxDb -> colorMax
                    minDb -> colorMin
                    else -> colorDefault
                }

                barRect.set(
                    xOffset,
                    chartHeight - barHeight,
                    xOffset + barWidth,
                    chartHeight
                )
                canvas.drawRect(barRect, barPaint)
            }

            // --- Draw Label ---
            val textX = index * (barWidth + barSpacing) + (barWidth / 2)
            val textY = chartHeight + labelHeight - dpToPx(4f)
            canvas.drawText(charVolume.character, textX, textY, textPaint)
        }

        // --- Draw Min/Max dB Axis Labels ---
        val labelX = chartAreaWidth + dpToPx(4f)

        // Max dB Label (at the top)
        canvas.drawText("${maxDb.roundToInt()}dB", labelX, axisLabelPaint.textSize, axisLabelPaint)

        // Min dB Label (at the bottom)
        canvas.drawText("${minDb.roundToInt()}dB", labelX, chartHeight, axisLabelPaint)
    }

    private fun normalize(value: Double, min: Double, max: Double): Float {
        if (value <= min) return 0f
        if (value >= max) return 1f
        return ((value - min) / (max - min)).toFloat()
    }

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
