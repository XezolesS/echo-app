package com.graphonic.echoapp

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.min

class RandomGuideText(
    private val context: Context,
    private val referenceFiles: List<Int>
) {

    private val MAX_BUFFER_SIZE = 50

    private var sentenceQueue: ArrayDeque<String> = ArrayDeque()

    fun next(): String {
        if (referenceFiles.isEmpty()) {
            Log.w("RandomGuideText", "No reference files available")
            return ""
        }

        if (sentenceQueue.isEmpty()) {
            Log.i("RandomGuideText", "Queue is empty. Loading new sentences...")
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
        val buffer = mutableListOf<String>()

        referenceFiles.forEachIndexed { i, fileId ->
            try {
                Log.d("RandomGuideText", "Loading sentences from file $fileId")
                val linesFromFile = readLinesFromResource(context, fileId)
                Log.d("RandomGuideText", "$linesFromFile")

                linesFromFile
                    .drop(1) // Skip header
                    .forEach { line ->
                        val sentence = line.split(",").getOrNull(1)
                        if (sentence != null) {
                            buffer.add(sentence)
                        }
                    }

                Log.d("RandomGuideText", "Load ${buffer.size} sentences from file $fileId")

                buffer.shuffle()

                val take = min(
                    if (i == referenceFiles.lastIndex && remains > 0)
                        remains else (0..remains).random(),
                    buffer.size
                )

                lines.addAll(buffer.take(take))
                remains -= take

                Log.d("RandomGuideText", "Select $take sentences from file $fileId")

                buffer.clear()
            } catch (e: Exception) {
                Log.e("RandomGuideText", "Error loading file $fileId", e)
            }
        }

        lines.shuffle()
        sentenceQueue.addAll(lines)
        Log.i(
            "RandomGuideText",
            "Finished loading. Total sentences in queue: ${sentenceQueue.size}"
        )
    }

    private fun readLinesFromResource(context: Context, resourceId: Int): List<String> {
        val inputStream = context.resources.openRawResource(resourceId)
        return inputStream.use {
            BufferedReader(InputStreamReader(inputStream, "UTF-8")).readLines()
        }
    }
}