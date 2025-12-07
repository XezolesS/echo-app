package com.graphonic.echoapp.response

import com.google.gson.annotations.SerializedName

/**
 * Represents the main "intensity" object in the JSON response.
 */
data class IntensityResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("char_volumes")
    val charVolumes: List<CharVolume>
) : AnalysisResult

/**
 * Represents a single character-volume pair within the char_volumes list.
 */
data class CharVolume(
    @SerializedName("char")
    val character: String,

    @SerializedName("volume")
    val volume: Double
)
