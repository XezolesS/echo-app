package com.graphonic.echoapp

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.UnknownServiceException

class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private lateinit var recordColor: ColorStateList
    private lateinit var stopColor: ColorStateList

    private lateinit var recordButton: ImageButton;

    private lateinit var audioRecorder: AudioRecorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get Color
        recordColor = ContextCompat.getColorStateList(this, R.color.accent_processing)
        stopColor = ContextCompat.getColorStateList(this, R.color.primary_2)

        // Get button
        recordButton = findViewById<ImageButton>(R.id.record_button)

        // Initialize the new AudioRecorder class
        audioRecorder = AudioRecorder(this)

        requestPermission()
        registerButtonEvents()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Ensure recording is stopped if the activity is destroyed
        if (audioRecorder.isRecording) {
            audioRecorder.stop()
        }
    }

    private fun checkAudioPermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        if (checkAudioPermission()) {
            return
        }
        requestPermissions(
            arrayOf(android.Manifest.permission.RECORD_AUDIO),
            REQUEST_RECORD_AUDIO_PERMISSION
        )
    }

    private fun registerButtonEvents() {
        recordButton.setOnClickListener {
            if (!audioRecorder.isRecording) {
                startRecord()
            } else {
                stopRecordAndProcess()
            }
        }
    }

    private fun startRecord() {
        if (checkAudioPermission()) {
            audioRecorder.start()
            setRecordButtonTint(recordColor)
        } else {
            requestPermission()
        }
    }

    private fun stopRecordAndProcess() {
        // Stop Record
        audioRecorder.stop()
        setRecordButtonTint(stopColor)

        // Process speech
        lifecycleScope.launch {
            try {
                val response = EchoSpeechAnalyzer.analyzeSpeech(
                    serverUrl = "http://localhost:8000/analyze",
                    audioFile = File(audioRecorder.audioFilePath),
                    intensity = true,
                    speechrate = true,
                    intonation = true,
                    articulation = true,
                    refText = "This is a reference text.",
                    maxWorkers = 4
                )

                Log.d("EchoSpeech", "Server Result: ${response}")
            } catch (e: UnknownServiceException) {
                Toast.makeText(
                    applicationContext,
                    "Cannot connect to the service.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("EchoSpeech", "${e.message}")
            } catch (e: Exception) {
                Toast.makeText(
                    applicationContext,
                    "Connection timeout.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("EchoSpeech", "Error: ${e.message}")
            }
        }
    }

    private fun setRecordButtonTint(color: ColorStateList?) {
        if (color == null) {
            return
        }

        recordButton.backgroundTintList = color
    }

}
