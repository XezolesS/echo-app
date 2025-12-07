package com.graphonic.echoapp.response

import com.google.gson.annotations.SerializedName

/**
 * Represents the "intonation" section of the analysis.
 */
data class IntonationResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("char_summary")
    val charSummary: List<CharSummary>,

    @SerializedName("pitch_contour_char")
    val pitchContour: PitchContour
) : AnalysisResult

/**
 * Represents detailed metrics for a single character.
 */
data class CharSummary(
    @SerializedName("char")
    val character: String,

    @SerializedName("volume_db")
    val volumeDb: Double,

    @SerializedName("duration_sec")
    val durationSec: Double,

    // f0_hz can be null, so we make it nullable
    @SerializedName("f0_hz")
    val f0Hz: Double?
)

/**
 * Represents the pitch contour graph data.
 */
data class PitchContour(
    @SerializedName("char_axis")
    val charAxis: List<Double>,

    // f0_hz can contain null values, so the list should be of a nullable type
    @SerializedName("f0_hz")
    val f0Hz: List<Double?>,

    @SerializedName("chars")
    val chars: List<String>
)
