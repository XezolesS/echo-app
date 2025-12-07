package com.graphonic.echoapp.response

import com.google.gson.annotations.SerializedName

sealed interface AnalysisResult

data class AnalysisResponse(
    @SerializedName("intensity")
    val intensity: AnalysisResult,

    @SerializedName("speechrate")
    val speechRate: AnalysisResult,

    @SerializedName("intonation")
    val intonation: AnalysisResult,

    @SerializedName("articulation")
    val articulation: AnalysisResult
)