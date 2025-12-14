package com.graphonic.echoapp

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.graphonic.echoapp.response.ArticulationResponse
import com.graphonic.echoapp.response.IntensityResponse
import com.graphonic.echoapp.response.IntonationResponse
import com.graphonic.echoapp.response.SpeechRateResponse
import com.graphonic.echoapp.ui.ArticulationFragment
import com.graphonic.echoapp.ui.IntensityFragment
import com.graphonic.echoapp.ui.IntonationFragment
import com.graphonic.echoapp.ui.SpeechRateFragment
import kotlinx.coroutines.launch
import java.io.File
import java.net.UnknownServiceException

class MainActivity : AppCompatActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200

    private lateinit var recordColor: ColorStateList
    private lateinit var stopColor: ColorStateList

    private lateinit var textRandomizerButton: ImageButton
    private lateinit var recordButton: ImageButton

    private lateinit var speechText: TextView

    private lateinit var intensityFragment: IntensityFragment
    private lateinit var speechRateFragment: SpeechRateFragment
    private lateinit var intonationFragment: IntonationFragment
    private lateinit var articulationFragment: ArticulationFragment

    private lateinit var audioRecorder: AudioRecorder

    private lateinit var randomGuideText: RandomGuideText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recordColor = ContextCompat.getColorStateList(this, R.color.accent_processing)!!
        stopColor = ContextCompat.getColorStateList(this, R.color.primary_2)!!

        textRandomizerButton = findViewById(R.id.text_randomizer_button)
        recordButton = findViewById(R.id.record_button)

        speechText = findViewById(R.id.speech_text)

        audioRecorder = AudioRecorder(this)
        randomGuideText = GuideTextBuilder(this)
            .addShortNormalSentences()
            .addShortQuestionSentences()
            .build()

        setupFragments()
        requestPermission()
        registerButtonEvents()
        randomizeSpeechText()
    }

    private fun setupFragments() {
        // Find existing fragment or create a new one
        intensityFragment =
            supportFragmentManager.findFragmentById(R.id.intensity_fragment_container) as? IntensityFragment
                ?: IntensityFragment().also {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.intensity_fragment_container, it)
                        .hide(it)
                        .commit()
                }

        speechRateFragment =
            supportFragmentManager.findFragmentById(R.id.speech_rate_fragment_container) as? SpeechRateFragment
                ?: SpeechRateFragment().also {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.speech_rate_fragment_container, it)
                        .hide(it)
                        .commit()
                }

        intonationFragment =
            supportFragmentManager.findFragmentById(R.id.intonation_fragment_container) as? IntonationFragment
                ?: IntonationFragment().also {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.intonation_fragment_container, it)
                        .hide(it)
                        .commit()
                }

        articulationFragment =
            supportFragmentManager.findFragmentById(R.id.articulation_fragment_container) as? ArticulationFragment
                ?: ArticulationFragment().also {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.articulation_fragment_container, it)
                        .hide(it)
                        .commit()
                }
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
        // Text randomizer event
        textRandomizerButton.setOnClickListener {
            randomizeSpeechText()
        }

        // Record button event
        recordButton.setOnClickListener {
            if (!audioRecorder.isRecording) {
                startRecord()
            } else {
                stopRecordAndProcess()
            }
        }
    }

    private fun randomizeSpeechText() {
        speechText.text = randomGuideText.next()
    }

    private fun setRecordButtonTint(color: ColorStateList?) {
        if (color == null) {
            return
        }

        recordButton.backgroundTintList = color
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

        Toast.makeText(applicationContext, "Analyzing...", Toast.LENGTH_SHORT).show()
        recordButton.isEnabled = false

        lifecycleScope.launch {
            try {
                // Process speech
                val response = EchoSpeechAnalyzer.analyzeSpeech(
                    serverUrl = "http://localhost:8000/analyze",
                    audioFile = File(audioRecorder.audioFilePath),
                    intensity = true,
                    speechrate = true,
                    intonation = true,
                    articulation = true,
                    refText = speechText.text as String?,
                    maxWorkers = 4
                )

                Log.d("EchoSpeech", "Server Result: ${response}")

                // Visualize
                // Intensity
                when (response.intensity) {
                    is IntensityResponse -> {
                        intensityFragment.view?.post {
                            intensityFragment.updateData(response.intensity)
                            showFragment(intensityFragment)
                        }

                        Log.d("EchoSpeech", "Visualise intensity: ${response.intensity}")
                    }

                    else -> {
                        hideFragment(intensityFragment)
                    }
                }

                // Speech-rate
                when (response.speechRate) {
                    is SpeechRateResponse -> {
                        speechRateFragment.view?.post {
                            speechRateFragment.updateData(response.speechRate)
                            showFragment(speechRateFragment)
                        }

                        Log.d("EchoSpeech", "Visualise speech rate: ${response.speechRate}")
                    }

                    else -> {
                        hideFragment(speechRateFragment)
                    }
                }

                // Intonation
                when (response.intonation) {
                    is IntonationResponse -> {
                        intonationFragment.view?.post {
                            intonationFragment.updateData(response.intonation)
                            showFragment(intonationFragment)
                        }

                        Log.d("EchoSpeech", "Visualise intonation: ${response.intonation}")
                    }

                    else -> {
                        hideFragment(intonationFragment)
                    }
                }

                // Articulation
                when (response.articulation) {
                    is ArticulationResponse -> {
                        articulationFragment.view?.post {
                            articulationFragment.setReference(speechText.text as String)
                            articulationFragment.updateData(response.articulation)
                            showFragment(articulationFragment)
                        }

                        Log.d("EchoSpeech", "Visualise articulation: ${response.articulation}")
                    }

                    else -> {
                        hideFragment(articulationFragment)
                    }
                }

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
            } finally {
                recordButton.isEnabled = true
            }
        }
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction().show(fragment).commit()
    }

    private fun hideFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction().hide(fragment).commit()
    }

}
