package com.graphonic.echoapp.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import com.graphonic.echoapp.response.PitchContour
import kotlin.math.roundToInt

class PitchContourView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pitchContour: PitchContour? = null
    private var minF0: Double = 60.0
    private var maxF0: Double = 200.0

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF00A0B0") // Teal
        strokeWidth = dpToPx(2.5f)
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF666666")
        textAlign = Paint.Align.CENTER
        textSize = spToPx(12f)
    }

    private val axisLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF888888") // Lighter Grey
        textAlign = Paint.Align.LEFT
        textSize = spToPx(10f)
    }

    fun setData(data: PitchContour?) {
        this.pitchContour = data
        if (data != null) {
            val validF0s = data.f0Hz.filterNotNull()
            if (validF0s.isNotEmpty()) {
                // Add a buffer for better visualization
                minF0 = (validF0s.minOrNull() ?: 60.0) - 5.0
                maxF0 = (validF0s.maxOrNull() ?: 200.0) + 5.0
            }
        }
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val data = pitchContour ?: return

        val labelHeight = spToPx(20f)
        val chartHeight = height - labelHeight
        val axisLabelWidth = dpToPx(30f)
        val chartWidth = width - axisLabelWidth

        // --- Draw Pitch Contour Line ---
        var lastX = -1f
        var lastY = -1f
        data.charAxis.forEachIndexed { index, time ->
            val f0 = data.f0Hz.getOrNull(index)

            val currentX = ((time / data.chars.size) * chartWidth).toFloat()
            val currentY = f0?.let { normalize(it, minF0, maxF0) * chartHeight }

            if (currentY != null) {
                if (lastX >= 0 && lastY >= 0) {
                    canvas.drawLine(
                        lastX,
                        chartHeight - lastY,
                        currentX,
                        chartHeight - currentY,
                        linePaint
                    )
                }
                lastX = currentX
                lastY = currentY
            } else {
                // Reset last points if there's a break (unvoiced segment)
                lastX = -1f
                lastY = -1f
            }
        }

        // --- Draw Character Labels ---
        val charCount = data.chars.size
        data.chars.forEachIndexed { index, char ->
            val textX = (index + 0.5f) * (chartWidth / charCount)
            val textY = chartHeight + labelHeight - dpToPx(4f)
            canvas.drawText(char, textX, textY, textPaint)
        }

        // --- Draw Min/Max Hz Axis Labels ---
        val labelX = chartWidth + dpToPx(6f)

        // Max Hz Label (at the top)
        canvas.drawText("${maxF0.roundToInt()}Hz", labelX, axisLabelPaint.textSize, axisLabelPaint)

        // Min Hz Label (at the bottom)
        canvas.drawText("${minF0.roundToInt()}Hz", labelX, chartHeight, axisLabelPaint)
    }

    private fun normalize(value: Double, min: Double, max: Double): Float {
        if (value <= min) return 0f
        if (value >= max) return 1f
        val range = max - min
        if (range == 0.0) return 0.5f
        return ((value - min) / range).toFloat()
    }

    private fun spToPx(sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)

    private fun dpToPx(dp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
}
