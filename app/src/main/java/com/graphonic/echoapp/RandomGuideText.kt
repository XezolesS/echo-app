package com.graphonic.echoapp

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

class RandomGuideText(
    private val context: Context,
    private val referenceFiles: List<Int>
) {

    private val MAX_BUFFER_SIZE = 50

    private var sentenceQueue: ArrayDeque<String> = ArrayDeque<String>()

    fun next(): String {
        if (sentenceQueue.isEmpty()) {
            load()
        }

        return sentenceQueue.removeFirst()
    }

    private fun load() {
        if (referenceFiles.isEmpty()) {
            Log.w("RandomGuideText", "No reference files available")
            return
        }

        var remains = MAX_BUFFER_SIZE
        val lines = mutableListOf<String>()
        var buffer = mutableListOf<String>()

        referenceFiles.forEachIndexed { i, fileId ->
            val inputStream = context.resources.openRawResource(fileId)
            val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))

            reader.useLines { lines ->
                lines.drop(1) // Skip header
                    .forEach { line ->
                        val sentence = line.split(",").getOrNull(1)
                        if (sentence != null) {
                            buffer.add(sentence)
                        }
                    }
            }

            buffer.shuffle()

            val take = (0..remains).random()
            lines.addAll(buffer.take(take))
            remains -= take

            Log.d("RandomGuideText", "Loaded $take sentences from file $i")

            buffer.clear()
        }

        lines.shuffle()
        sentenceQueue.addAll(lines)
    }
}