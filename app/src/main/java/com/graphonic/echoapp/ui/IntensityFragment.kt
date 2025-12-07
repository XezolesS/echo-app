package com.graphonic.echoapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.graphonic.echoapp.R
import com.graphonic.echoapp.response.IntensityResponse
import kotlin.math.roundToInt

class IntensityFragment : Fragment() {

    private lateinit var chartView: IntensityBarChartView
    private lateinit var textMaxDb: TextView
    private lateinit var textMinDb: TextView
    private lateinit var textAvgDb: TextView
    private lateinit var textStdDev: TextView
    private lateinit var textSummary: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_intensity, container, false)

        // Find all the views by their IDs
        chartView = view.findViewById(R.id.intensity_chart)
        textMaxDb = view.findViewById(R.id.text_max_db)
        textMinDb = view.findViewById(R.id.text_min_db)
        textAvgDb = view.findViewById(R.id.text_avg_db)
        textStdDev = view.findViewById(R.id.text_std_dev)
        textSummary = view.findViewById(R.id.text_summary)

        return view
    }

    fun updateData(intensityData: IntensityResponse) {
        val volumesOnly = intensityData.charVolumes
            .filter { it.character.isNotBlank() }
            .map { it.volume }

        // Calculate statistics
        val maxDb = volumesOnly.maxOrNull() ?: -1.0
        val minDb = volumesOnly.minOrNull() ?: -100.0
        val avgDb = volumesOnly.average()
        val stdDev = if (volumesOnly.isNotEmpty()) {
            val mean = avgDb
            val stdDevValue = volumesOnly.fold(0.0) { accumulator, next ->
                accumulator + (next - mean) * (next - mean)
            }
            kotlin.math.sqrt(stdDevValue / volumesOnly.size)
        } else 0.0

        // Update the UI
        chartView.setData(intensityData.charVolumes, minDb, maxDb)
        textMaxDb.text = "최대: ${maxDb.roundToInt()}dB"
        textMinDb.text = "최소: ${minDb.roundToInt()}dB"
        textAvgDb.text = "평균: ${avgDb.roundToInt()}dB"
        textStdDev.text = "표준편차: ${String.format("%.1f", stdDev)}dB"

        // You can add more complex logic here to change the summary text if needed
        // textSummary.text = "..."
    }
}