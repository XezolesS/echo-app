package com.graphonic.echoapp.response

import com.google.gson.annotations.SerializedName

/**
 * Models the error response from the server.
 */
data class ErrorResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("error_name")
    val errorName: String,

    @SerializedName("error_details")
    val errorDetails: String,

    @SerializedName("time")
    val time: String
) : AnalysisResult
    