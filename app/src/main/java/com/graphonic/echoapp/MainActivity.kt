package com.graphonic.echoapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

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
        val recordButton = findViewById<ImageButton>(R.id.record_button)
        recordButton.setOnClickListener {
            if (audioRecorder.isRecording) {
                audioRecorder.stop()
                val stopColor = ContextCompat.getColorStateList(this, R.color.primary_2)
                recordButton.backgroundTintList = stopColor
            } else {
                if (checkAudioPermission()) {
                    audioRecorder.start()
                    val recordColor =
                        ContextCompat.getColorStateList(this, R.color.accent_processing)
                    recordButton.backgroundTintList = recordColor
                } else {
                    requestPermission()
                }
            }
        }
    }
}
