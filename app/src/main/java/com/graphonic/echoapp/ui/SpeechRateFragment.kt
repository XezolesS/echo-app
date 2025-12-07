package com.graphonic.echoapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.graphonic.echoapp.R
import com.graphonic.echoapp.response.SpeechRateResponse
import kotlin.math.roundToInt

class SpeechRateFragment : Fragment() {

    private lateinit var textWpmValue: TextView
    private lateinit var textCpsValue: TextView
    private lateinit var textSummary: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_speech_rate, container, false)

        // Find views by their IDs
        textWpmValue = view.findViewById(R.id.text_wpm_value)
        textCpsValue = view.findViewById(R.id.text_cps_value)
        textSummary = view.findViewById(R.id.text_speech_rate_summary)

        return view
    }

    /**
     * Updates the UI with data from a SpeechRateSuccess object.
     */
    fun updateData(data: SpeechRateResponse) {
        textWpmValue.text = data.wpm.roundToInt().toString()
        textCpsValue.text = String.format("%.1f", data.cps)

        // Simple logic to provide a summary based on WPM
        textSummary.text = when {
            data.wpm > 170 -> "발성 속도가 매우 빠른 편입니다."
            data.wpm > 130 -> "평균적인 속도로 발성하고 있습니다."
            data.wpm > 90 -> "발성 속도가 약간 느린 편입니다."
            else -> "발성 속도가 매우 느린 편입니다."
        }
    }
}
