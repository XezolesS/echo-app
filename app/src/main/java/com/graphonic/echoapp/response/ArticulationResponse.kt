package com.graphonic.echoapp.response

import com.google.gson.annotations.SerializedName

/**
 * Represents the "articulation" section of the analysis.
 */
data class ArticulationResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("duration")
    val duration: Double,

    @SerializedName("articulation_rate")
    val articulationRate: Double,

    @SerializedName("pause_ratio")
    val pauseRatio: Double,

    @SerializedName("accuracy_score")
    val accuracyScore: Double,

    @SerializedName("char_error_rate")
    val charErrorRate: Double,

    @SerializedName("reference_text")
    val referenceText: String?,

    @SerializedName("transcription")
    val transcription: String
) : AnalysisResult
