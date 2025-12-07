package com.graphonic.echoapp

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile

class AudioRecorder(private val context: Context) {

    // AudioRecord settings
    private val RECORDER_SAMPLE_RATE = 44100
    private val RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO
    private val RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var bufferSize = 0

    val audioFilePath: String = "${context.externalCacheDir?.absolutePath}/myaudio.wav"
    var isRecording: Boolean = false
        private set

    init {
        bufferSize = AudioRecord.getMinBufferSize(
            RECORDER_SAMPLE_RATE,
            RECORDER_CHANNELS,
            RECORDER_AUDIO_ENCODING
        )
    }

    fun start() {
        if (isRecording) {
            Log.w("AudioRecorder", "Recording is already in progress.")
            return
        }

        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLE_RATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING,
                bufferSize
            )
        } catch (e: SecurityException) {
            Log.e(
                "AudioRecorder",
                "Audio permission not granted. Cannot initialize AudioRecord.",
                e
            )
            return
        }


        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioRecorder", "AudioRecord could not be initialized.")
            return
        }

        // Use a coroutine for background audio processing
        recordingJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d("AudioRecorder", "Recording started to: $audioFilePath")
            audioRecord?.startRecording()
            isRecording = true
            writeAudioDataToFile()
        }
    }

    fun stop() {
        if (!isRecording) return

        isRecording = false
        recordingJob?.cancel() // Signal the recording coroutine to stop

        // Stop and release the AudioRecord object
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null

        Log.d("AudioRecorder", "Recording stopped. Writing WAV header.")
        // After recording stops, write the proper WAV header
        addWavHeader(audioFilePath)
    }

    private suspend fun writeAudioDataToFile() {
        val data = ByteArray(bufferSize)
        val file = File(audioFilePath)
        val fileOutputStream = FileOutputStream(file)

        fileOutputStream.use { stream ->
            while (isRecording) {
                val read = audioRecord?.read(data, 0, bufferSize) ?: -1
                if (read != AudioRecord.ERROR_INVALID_OPERATION) {
                    try {
                        stream.write(data, 0, read)
                    } catch (e: IOException) {
                        Log.e("AudioRecorder", "Error writing to file: ${e.message}")
                    }
                }
            }
        }
    }

    private fun addWavHeader(filePath: String) {
        val file = File(filePath)
        if (!file.exists()) return

        val pcmDataSize = file.length()
        val wavFileSize = pcmDataSize + 36 // 44 bytes for header - 8 bytes for RIFF chunk
        val bitsPerSample: Short = 16
        val channels: Short = 1

        try {
            val wavFile = RandomAccessFile(file, "rw")
            wavFile.seek(0) // Go to the beginning of the file

            // Write WAV header
            wavFile.writeBytes("RIFF")
            wavFile.write(intToLittleEndian(wavFileSize))
            wavFile.writeBytes("WAVE")
            wavFile.writeBytes("fmt ")
            wavFile.write(intToLittleEndian(16))
            wavFile.write(shortToLittleEndian(1))
            wavFile.write(shortToLittleEndian(channels))
            wavFile.write(intToLittleEndian(RECORDER_SAMPLE_RATE.toLong()))
            wavFile.write(intToLittleEndian((RECORDER_SAMPLE_RATE * channels * bitsPerSample / 8).toLong()))
            wavFile.write(shortToLittleEndian((channels * bitsPerSample / 8).toShort()))
            wavFile.write(shortToLittleEndian(bitsPerSample))
            wavFile.writeBytes("data")
            wavFile.write(intToLittleEndian(pcmDataSize))

            wavFile.close()
            Log.d("AudioRecorder", "WAV header written successfully.")
        } catch (e: IOException) {
            Log.e("AudioRecorder", "Error writing WAV header: ${e.message}")
        }
    }

    private fun intToLittleEndian(value: Long): ByteArray {
        val buffer = ByteArray(4)
        buffer[0] = value.toByte()
        buffer[1] = (value ushr 8).toByte()
        buffer[2] = (value ushr 16).toByte()
        buffer[3] = (value ushr 24).toByte()
        return buffer
    }

    private fun shortToLittleEndian(value: Short): ByteArray {
        val buffer = ByteArray(2)
        buffer[0] = value.toByte()
        buffer[1] = (value.toInt() ushr 8).toByte()
        return buffer
    }

}
