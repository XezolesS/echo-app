package com.graphonic.echoapp

data class SpeechSpeedResult(
    val status: String,
    val fileName: String,
    val wpm: Double,
    val cps: Double,
    val totalSpeechTimeSeconds: Double,
    val totalWords: Int,
    val totalCharactersNoSpace: Int,
    val analysisTimeSeconds: Double,
    val transcript: String
)
