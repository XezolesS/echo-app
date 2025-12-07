package com.graphonic.echoapp.response

import com.google.gson.annotations.SerializedName

/**
 * Represents the "speechrate" section of the analysis.
 */
data class SpeechRateResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("wpm")
    val wpm: Double,

    @SerializedName("cps")
    val cps: Double,

    @SerializedName("total_speech_time")
    val totalSpeechTime: Double,

    @SerializedName("total_words")
    val totalWords: Int,

    @SerializedName("total_characters")
    val totalCharacters: Int,

    @SerializedName("analysis_time")
    val analysisTime: Double,

    @SerializedName("transcript")
    val transcript: String
) : AnalysisResult
