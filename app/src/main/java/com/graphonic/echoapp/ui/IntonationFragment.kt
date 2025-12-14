package com.graphonic.echoapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.graphonic.echoapp.R
import com.graphonic.echoapp.response.IntonationResponse

class IntonationFragment : Fragment() {

    private lateinit var pitchContourView: PitchContourView
    private lateinit var textSummary: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_intonation, container, false)
        pitchContourView = view.findViewById(R.id.pitch_contour_chart)
        textSummary = view.findViewById(R.id.text_intonation_summary)
        return view
    }

    fun updateData(data: IntonationResponse) {
        pitchContourView.setData(data.pitchContour)

        // Add summary logic based on pitch variation (standard deviation)
        val validPitches = data.pitchContour.f0Hz.filterNotNull()
        if (validPitches.isNotEmpty()) {
            val mean = validPitches.average()
            val stdDev = kotlin.math.sqrt(validPitches.map { (it - mean) * (it - mean) }.average())
            val normStd = stdDev / mean

            textSummary.text = when {
                normStd > 0.12 -> "음높이 변화가 다소 역동적입니다."
                normStd > 0.05 -> "자연스러운 억양으로 발성하고 있습니다."
                else -> "음높이가 단조로운 편입니다. 좀 더 생동감을 주는 것이 좋습니다."
            }
        } else {
            textSummary.text = "음성에서 유효한 음높이를 감지하지 못했습니다."
        }
    }
}
