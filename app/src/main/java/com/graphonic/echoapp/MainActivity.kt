package com.graphonic.echoapp

import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String = ""
    private var isRecording: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        requestPermission()
        setRecordingPath(this)
        registerButtonEvents()

        // TEST
        val pymodule = EchoSpeechModuleWrapper(this)
        pymodule.HelloPython()
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
            REQUEST_RECORD_AUDIO_PERMISSION)
    }

    private fun setRecordingPath(context: Context) {
        // Save to the app's cache directory
        audioFilePath = "${context.externalCacheDir?.absolutePath}/myaudio.3gp"
    }

    private fun startRecording(context: Context) {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context) // for modern APIs (31 or newer)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder() // for older APIs
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("AudioRecorder", "prepare() failed: ${e.message}")
            }

            start()
        }

        isRecording = true
        Log.d("AudioRecorder", "Recording started at: $audioFilePath")
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Stop/Release failed: ${e.message}")
            }
        }

        mediaRecorder = null
        isRecording = false
        Log.d("AudioRecorder", "Recording stopped.")
    }

    private fun registerButtonEvents() {
        val recordButton = findViewById<ImageButton>(R.id.record_button)
        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()

                val stopColor = ContextCompat.getColorStateList(this, R.color.primary_2)
                recordButton.backgroundTintList = stopColor
            } else {
                startRecording(this)

                val recordColor = ContextCompat.getColorStateList(this, R.color.accent_processing)
                recordButton.backgroundTintList = recordColor
            }
        }

    }
}