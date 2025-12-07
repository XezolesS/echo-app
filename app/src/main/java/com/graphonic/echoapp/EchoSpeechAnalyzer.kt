package com.graphonic.echoapp

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

object EchoSpeechAnalyzer {

    private val JSON = "application/json; charset=utf-8".toMediaType()
    private val AUDIO_WAV = "audio/wav".toMediaType()
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    /**
     * Upload an audio file and request analyses.
     *
     * @param serverUrl full URL to /analyze endpoint (e.g. "http://192.168.0.10:8000/analyze").
     * @param audioFile local File referencing the audio (.wav)
     * @param intensity whether to run intensity analysis
     * @param speechrate whether to run speechrate analysis
     * @param intonation whether to run intonation analysis
     * @param articulation whether to run articulation analysis
     * @param refText optional reference text (for articulation)
     * @param maxWorkers optional concurrency limit (default 4)
     *
     * @return parsed server JSON as JsonObject
     *
     * Throws Exception on non-200 or network errors.
     */
    suspend fun analyzeSpeech(
        serverUrl: String,
        audioFile: File,
        intensity: Boolean = false,
        speechrate: Boolean = false,
        intonation: Boolean = false,
        articulation: Boolean = false,
        refText: String? = null,
        maxWorkers: Int = 4
    ): JsonObject = withContext(Dispatchers.IO) {
        val bodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", audioFile.name, audioFile.asRequestBody(AUDIO_WAV))
            .addFormDataPart("intensity", intensity.toString())
            .addFormDataPart("speechrate", speechrate.toString())
            .addFormDataPart("intonation", intonation.toString())
            .addFormDataPart("articulation", articulation.toString())

        // optional fields
        refText?.let { bodyBuilder.addFormDataPart("ref_text", it) }
        bodyBuilder.addFormDataPart("max_workers", maxWorkers.toString())

        val request = Request.Builder()
            .url(serverUrl)
            .post(bodyBuilder.build())
            .build()

        client.newCall(request).execute().use { resp ->
            val bodyString = resp.body?.string() ?: ""
            if (!resp.isSuccessful) {
                // include server message if present
                throw Exception("Server error: ${resp.code}. Body: $bodyString")
            }
            return@withContext gson.fromJson(bodyString, JsonObject::class.java)
        }
    }

}