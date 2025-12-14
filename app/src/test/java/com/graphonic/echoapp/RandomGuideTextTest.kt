package com.graphonic.echoapp

import android.content.Context
import android.content.res.Resources
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class RandomGuideTextTest {

    private lateinit var mockContext: Context
    private lateinit var mockResources: Resources

    private lateinit var randomGuideText: RandomGuideText

    private val FAKE_RES_ID = 1

    @Before
    fun setUp() {
        val fakeCsvData = """
            id,sentence
            1,"첫 번째 문장입니다."
            2,"두 번째 문장입니다."
            3,"세 번째 문장입니다."
        """.trimIndent()

        val fakeInputStream = fakeCsvData.byteInputStream(Charsets.UTF_8)

        mockResources = mock {
            on { openRawResource(FAKE_RES_ID) } doReturn fakeInputStream
        }

        mockContext = mock {
            on { resources } doReturn mockResources
        }

        randomGuideText = GuideTextBuilder(mockContext)
            .add(FAKE_RES_ID)
            .build()
    }

    @Test
    fun `next() should return a sentence from the mocked file`() {
        val sentence = randomGuideText.next()

        val expectedSentences = listOf(
            "첫 번째 문장입니다.",
            "이것은 두 번째 문장입니다.",
            "세 번째 문장을 테스트합니다."
        )
        assertTrue(
            "Sentence should be one of the expected sentences",
            expectedSentences.contains(sentence)
        )
        assertNotEquals("Sentence should not be empty", "", sentence)
    }

    @Test
    fun `next() called multiple times should eventually return all sentences`() {
        val returnedSentences = mutableSetOf<String>()

        // Call next() enough times to exhaust the queue
        for (i in 1..3) {
            val sentence = randomGuideText.next()
            if (sentence.isNotEmpty()) {
                returnedSentences.add(sentence)
            }
        }

        // Assert that we have collected all 3 unique sentences
        assertEquals("Should have loaded all 3 unique sentences", 3, returnedSentences.size)
    }

    @Test
    fun `next() when queue is empty should trigger load and return a new sentence`() {
        // First call loads the queue and returns one sentence
        val firstSentence = randomGuideText.next()
        assertNotNull(firstSentence)
        assertTrue(firstSentence.isNotEmpty())

        // Exhaust the initial queue (which holds 3 sentences)
        randomGuideText.next()
        randomGuideText.next()

        // The queue is now empty. The next call should reload it automatically.
        val fourthSentence = randomGuideText.next()
        assertNotNull(fourthSentence)
        assertTrue(
            "After exhausting the queue, next() should reload and return a valid sentence.",
            fourthSentence.isNotEmpty()
        )
    }

    @Test
    fun `constructor with empty file list should return empty string`() {
        // Create an instance with no files
        val emptyGuideText = RandomGuideText(mockContext, emptyList())

        // Call next()
        val sentence = emptyGuideText.next()

        // Assert that the result is an empty string
        assertEquals("Should return an empty string if no files are provided", "", sentence)
    }

}
