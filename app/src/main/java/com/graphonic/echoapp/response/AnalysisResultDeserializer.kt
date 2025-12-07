package com.graphonic.echoapp.response

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class AnalysisResultDeserializer : JsonDeserializer<AnalysisResult> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): AnalysisResult {
        val jsonObject = json.asJsonObject

        val gson = Gson()

        // Check the status field within the object
        return when (jsonObject.get("status")?.asString) {
            "SUCCESS" -> {
                // Determine which specific success type it is by checking for unique fields
                when {
                    jsonObject.has("char_volumes") -> gson.fromJson(
                        jsonObject,
                        IntensityResponse::class.java
                    )

                    jsonObject.has("wpm") -> gson.fromJson(
                        jsonObject,
                        SpeechRateResponse::class.java
                    )

                    jsonObject.has("pitch_contour_char") -> gson.fromJson(
                        jsonObject,
                        IntonationResponse::class.java
                    )

                    jsonObject.has("articulation_rate") -> gson.fromJson(
                        jsonObject,
                        ArticulationResponse::class.java
                    )

                    else -> throw JsonParseException("Unknown SUCCESS type")
                }
            }

            "ERROR" -> {
                gson.fromJson(jsonObject, ErrorResponse::class.java)
            }

            else -> throw JsonParseException("Status field is missing or unknown")
        }
    }
}
