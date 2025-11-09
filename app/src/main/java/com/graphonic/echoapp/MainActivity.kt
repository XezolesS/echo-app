package com.graphonic.echoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // UI 데모용 더미 상태
            var isRec by remember { mutableStateOf(false) }
            var amp by remember { mutableStateOf(demoList(seed = 1)) }
            var pace by remember { mutableStateOf(demoList(seed = 2)) }

            EchoScreen(
                isRecording = isRec,
                onToggleMic = { isRec = !isRec },
                onRefresh = {
                    amp = demoList(seed = (0..999).random())
                    pace = demoList(seed = (0..999).random())
                },
                ampValues = amp,
                ampCaption = "전반적인 발생 크기는 적정합니다.",
                ampMetaRightBottom = "최대 78% · 평균 54%",
                paceValues = pace,
                paceCaption = "전반적으로 약간 느린 발화입니다.",
                paceMetaRightBottom = "지수 2.6/초"
            )
        }
    }
}

private fun demoList(seed: Int): List<Float> {
    val r = Random(seed)
    return List(180) { (0.3f + r.nextFloat() * 0.4f).coerceIn(0f, 1f) }
}
