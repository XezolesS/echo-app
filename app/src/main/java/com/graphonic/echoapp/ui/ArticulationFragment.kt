package com.graphonic.echoapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.graphonic.echoapp.R
import com.graphonic.echoapp.response.ArticulationResponse
import com.graphonic.echoapp.util.CharDifferenceParser
import kotlin.math.roundToInt

class ArticulationFragment : Fragment() {

    private lateinit var textReference: TextView
    private lateinit var textTranscriptionDiff: TextView
    private lateinit var textAccuracyScore: TextView
    private lateinit var textSummary: TextView

    // Store the reference text provided at the start of the analysis
    private var referenceText: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_articulation, container, false)
        textReference = view.findViewById(R.id.text_reference)
        textTranscriptionDiff = view.findViewById(R.id.text_transcription_diff)
        textAccuracyScore = view.findViewById(R.id.text_accuracy_score)
        textSummary = view.findViewById(R.id.text_articulation_summary)
        return view
    }

    fun updateData(data: ArticulationResponse) {
        referenceText = data.referenceText ?: ""

        // 1. Set the plain reference text
        textReference.text = referenceText

        // 2. Get the colored diff spannable and set it
        val coloredDiff = CharDifferenceParser.getColoredDiff(referenceText, data.transcription)
        textTranscriptionDiff.text = coloredDiff

        // 3. Set accuracy score
        val accuracy = data.accuracyScore.roundToInt()
        textAccuracyScore.text = "발음 정확도 $accuracy%"

        // 4. Set summary text based on score
        textSummary.text = when {
            accuracy >= 90 -> "발음이 매우 정확합니다."
            accuracy >= 70 -> "발음이 대체로 정확하지만, 일부 부정확한 부분이 있습니다."
            accuracy >= 50 -> "부정확한 발음이 많습니다. 조금 더 신경 써주세요."
            else -> "발음이 매우 부정확합니다. 연습이 많이 필요합니다."
        }
    }
}
