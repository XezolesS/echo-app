package com.graphonic.echoapp.util

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import java.util.LinkedList

object CharDifferenceParser {
    // A data class to hold the result of a diff operation
    data class Diff(val operation: Operation, val text: String)

    // Enum to represent Insert, Delete, or Equal
    enum class Operation {
        INSERT, DELETE, EQUAL
    }

    /**
     * Creates a SpannableString with colored highlights for differences.
     * Deletions (words in ref but not in trans) are colored red.
     * Insertions (words in trans but not in ref) are not shown in this implementation.
     */
    fun getColoredDiff(ref: String, transcription: String): SpannableString {
        val diffs = diffMain(ref, transcription)
        val spannable = SpannableString(transcription)

        var currentIndex = 0
        for (diff in diffs) {
            when (diff.operation) {
                Operation.EQUAL -> {
                    currentIndex += diff.text.length
                }

                Operation.INSERT -> {
                    // Highlight inserted text in red
                    val endIndex = currentIndex + diff.text.length
                    if (endIndex <= spannable.length) {
                        spannable.setSpan(
                            ForegroundColorSpan(Color.RED),
                            currentIndex,
                            endIndex,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                    currentIndex = endIndex
                }

                Operation.DELETE -> {
                    // Deletions from the reference are not present in the transcription,
                    // so we don't advance the index. In a more complex view, you might show these as strikethrough.
                }
            }
        }
        return spannable
    }

    /**
     * A basic implementation of the Diff algorithm (Myers diff).
     * This compares two texts and returns a list of differences.
     */
    private fun diffMain(text1: String, text2: String): LinkedList<Diff> {
        // This is a simplified version. For a production app, a more robust third-party library is recommended.
        val diffs = LinkedList<Diff>()
        if (text1 == text2) {
            diffs.add(Diff(Operation.EQUAL, text1))
            return diffs
        }

        val words1 = text1.split("(?<=\\s)|(?=\\s)".toRegex())
        val words2 = text2.split("(?<=\\s)|(?=\\s)".toRegex())
        val lcs = longestCommonSubsequence(words1, words2)

        var i = 0
        var j = 0
        var k = 0
        while (k < lcs.size) {
            while (i < words1.size && words1[i] != lcs[k]) {
                diffs.add(Diff(Operation.DELETE, words1[i]))
                i++
            }
            while (j < words2.size && words2[j] != lcs[k]) {
                diffs.add(Diff(Operation.INSERT, words2[j]))
                j++
            }
            if (i < words1.size && j < words2.size) {
                diffs.add(Diff(Operation.EQUAL, lcs[k]))
                i++
                j++
                k++
            }
        }
        while (i < words1.size) {
            diffs.add(Diff(Operation.DELETE, words1[i]))
            i++
        }
        while (j < words2.size) {
            diffs.add(Diff(Operation.INSERT, words2[j]))
            j++
        }
        return diffs
    }

    private fun longestCommonSubsequence(a: List<String>, b: List<String>): List<String> {
        val lengths = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in a.indices) {
            for (j in b.indices) {
                if (a[i] == b[j]) {
                    lengths[i + 1][j + 1] = lengths[i][j] + 1
                } else {
                    lengths[i + 1][j + 1] = maxOf(lengths[i + 1][j], lengths[i][j + 1])
                }
            }
        }
        val result = mutableListOf<String>()
        var x = a.size
        var y = b.size
        while (x > 0 && y > 0) {
            if (lengths[x][y] == lengths[x - 1][y]) {
                x--
            } else if (lengths[x][y] == lengths[x][y - 1]) {
                y--
            } else {
                result.add(a[x - 1])
                x--
                y--
            }
        }
        return result.reversed()
    }
}
