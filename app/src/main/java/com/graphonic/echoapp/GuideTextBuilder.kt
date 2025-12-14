package com.graphonic.echoapp

import android.content.Context

class GuideTextBuilder(private val context: Context) {

    private var referenceFiles: ArrayList<Int> = ArrayList()

    fun add(resourceId: Int): GuideTextBuilder {
        referenceFiles.add(resourceId)
        return this
    }

    fun addShortNormalSentences(): GuideTextBuilder {
        add(R.raw.guidetext_short_normal)
        return this
    }

    fun addShortQuestionSentences(): GuideTextBuilder {
        add(R.raw.guidetext_short_question)
        return this
    }

    fun build(): RandomGuideText {
        return RandomGuideText(context, referenceFiles)
    }

}