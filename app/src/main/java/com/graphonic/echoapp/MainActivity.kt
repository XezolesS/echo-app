package com.graphonic.echoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //1 서버에서 결과 왔다고 가장한 더미 데이터
        val response: IntensityResponse = dummyIntensity()

        //2 더미 데이터를 0~1 값 리스트로 변환
        val ampFromServer: List<Float> = response.intensity.toNormalizedAmps()

        setContent {
            // UI 데모용 상태
            var isRec by remember { mutableStateOf(false) }
            // amp는 이제 intensity → 0~1 변환 결과 사용
            var amp by remember { mutableStateOf(ampFromServer) }
            // pace 더미
            var pace by remember { mutableStateOf(demoList(seed = 2)) }

            EchoScreen(
                isRecording = isRec,
                onToggleMic = { isRec = !isRec },
                onRefresh = {
                    // TODO: 나중에는 여기서 intensity 다시 불러와서 toNormalizedAmps() 호출
                    val newResponse = dummyIntensity()
                    amp = newResponse.intensity.toNormalizedAmps()
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

// 예전처럼 쓰던 더미 그래프 생성 함수 (pace 등에 계속 사용 가능)
private fun demoList(seed: Int): List<Float> {
    val r = Random(seed)
    return List(180) { (0.3f + r.nextFloat() * 0.4f).coerceIn(0f, 1f) }
}
