package com.graphonic.echoapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import kotlin.random.Random
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //1 발성 크기
        val intensityResponse: IntensityResponse = dummyIntensity()
        val ampFromServer: List<Float> = intensityResponse.intensity.toNormalizedAmps()

        //2 발화 속도
        val speedResultFromServer: SpeechSpeedResult = dummySpeechResult()

        setContent {
            // UI 데모용 상태
            var isRec by remember { mutableStateOf(false) }


            // 발화크기
            var amp by remember { mutableStateOf(ampFromServer) }
            var ampCaption by remember {
                mutableStateOf(buildAmpCaption(calcAmpStats(ampFromServer)))
            }
            var ampMeta by remember {
                mutableStateOf(buildAmpMeta(calcAmpStats(ampFromServer)))
            }

            // 발화속도
            var speechResult by remember { mutableStateOf(speedResultFromServer) }
            var pace by remember { mutableStateOf(buildPaceSeriesFromSpeech(speechResult)) }

            val paceCaption = buildPaceCaption(speechResult)
            val paceMeta = buildPaceMeta(speechResult)

            EchoScreen(
                isRecording = isRec,
                onToggleMic = { isRec = !isRec },
                onRefresh = {
                    // TODO: 나중에는 여기서 실제 서버 호출해서 새 결과 받기

                    //볼륨 부분
                    val newIntensity = dummyIntensity() //아직 더미
                    val newAmp = newIntensity.intensity.toNormalizedAmps()
                    amp = newAmp

                    val ampStats = calcAmpStats(newAmp)
                    ampCaption = buildAmpCaption(ampStats)
                    ampMeta = buildAmpMeta(ampStats)

                    //발화 속도
                    val newSpeed = dummySpeechResult()
                    speechResult = newSpeed
                    pace = buildPaceSeriesFromSpeech(newSpeed)

                    // speechResult = 서버에서 받은 발화 속도 결과
                },
                ampValues = amp,
                ampCaption = ampCaption,
                ampMetaRightBottom = ampMeta,
                paceValues = pace,
                paceCaption = paceCaption,
                paceMetaRightBottom = paceMeta,
                speechResult = speechResult
            )
        }
    }
}

// 발화 속도 더미 데이터
private fun dummySpeechResult(): SpeechSpeedResult =
    SpeechSpeedResult(
        status = "success",
        fileName = "untitled.wav",
        wpm = 25.71,
        cps = 1.43,
        totalSpeechTimeSeconds = 7.0,
        totalWords = 3,
        totalCharactersNoSpace = 10,
        analysisTimeSeconds = 0.43,
        transcript = "안녕 하세요 반갑습니다"
    )

//초당 글자수를 0~1로
private fun buildPaceSeriesFromSpeech(
    result: SpeechSpeedResult,
    points: Int = 180
): List<Float> {
    val base = normalizedSpeedFromCps(result.cps)
    val random = Random(13)
    return List(points) {
        val jitter = (random.nextFloat() - 0.5f) * 0.2f   // ±0.1 정도로 살짝 요동
        (base + jitter).coerceIn(0f, 1f)
    }
}

//0~1로 느림~빠름 정하기
private fun normalizedSpeedFromCps(cps: Double): Float =
    (cps / 6.0).toFloat().coerceIn(0f, 1f)

//글자로 표현
private fun buildPaceCaption(result: SpeechSpeedResult): String {
    val cps = result.cps
    val desc = when {
        cps < 1.0 -> "많이 느린 발성이고,"
        cps < 2.0 -> "약간 느린 발성이고,"
        cps < 4.5 -> "보통 속도의 발성이고,"
        else -> "다소 빠른 발성이고,"
    }
    return "전반적으로 $desc 과하게 빠르거나 느린 부분이 보입니다."
}

//평균 속도 (그래프 버전)
//private fun buildPaceMeta(result: SpeechSpeedResult): String {
//    val cpsText = "%.2f".format(result.cps)
//    return "평균 발화 속도: ${cpsText}자/초"
//}

//평균 속도
private fun buildPaceMeta(result: SpeechSpeedResult): String {
    val cpsText = "%.2f".format(result.cps)
    val recommended = 2.5
    val recText = "%.1f".format(recommended)
    return "평균 발화 속도: ${cpsText}음절/초\n권장 평균 발화 속도: ${recText}음절/초"
}


//Amp
private data class AmpStats(
    val peak: Float,
    val min: Float,
    val avg: Float,
    val stdDev: Float
)

// 0~1 리스트에서 피크/최소/평균/표준편차 계산
private fun calcAmpStats(values: List<Float>): AmpStats {
    if (values.isEmpty()) return AmpStats(0f, 0f, 0f, 0f)

    val peak = values.maxOrNull()!!
    val min = values.minOrNull()!!
    val avg = values.average().toFloat()

    val variance = values.fold(0f) { acc, v ->
        val d = v - avg
        acc + d * d
    } / values.size
    val stdDev = sqrt(variance)

    return AmpStats(peak, min, avg, stdDev)
}

private fun buildAmpCaption(stats: AmpStats): String {
    val avg = stats.avg
    val std = stats.stdDev

    val levelText = when {
        avg < 0.3f -> "전반적으로 발성 크기가 작고,"
        avg < 0.6f -> "전반적인 발성 크기는 적정하나,"
        else       -> "전반적으로 발성 크기가 큰 편이고,"
    }

    val varianceText = when {
        std < 0.08f -> "전체적으로 일정한 크기로 발성하고 있습니다."
        std < 0.16f -> "대체로 일정하지만 약간 크거나 작은 부분이 보입니다."
        else        -> "과하게 크거나 작은 부분이 보입니다."
    }

    return "$levelText $varianceText"
}

private fun buildAmpMeta(stats: AmpStats): String {
    fun p(v: Float) = "${(v * 100).toInt()}%"
    fun p1(v: Float) = "%.1f%%".format(v * 100)

    return "피크 ${p(stats.peak)} · 최소 ${p(stats.min)} · 평균 ${p(stats.avg)} · 표준편차 ${p1(stats.stdDev)}"
}