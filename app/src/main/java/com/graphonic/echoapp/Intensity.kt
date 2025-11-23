package com.graphonic.echoapp

// intensity 한 글자에 대한 정보
data class IntensityPoint(
    val char: String,
    val volume: Float,   // dB 값, [-100, 0] 정도
)

// 서버에서 intensity 배열만 따로 받는다고 가정한 응답
data class IntensityResponse(
    val intensity: List<IntensityPoint>
)

// dB 리스트를 0~1 리스트로 바꾸는 확장 함수
private const val MIN_DB = -100f
private const val MAX_DB = 0f

fun List<IntensityPoint>.toNormalizedAmps(
    minDb: Float = MIN_DB,
    maxDb: Float = MAX_DB
): List<Float> {
    return this.map { point ->
        val clamped = point.volume.coerceIn(minDb, maxDb)
        val ratio = (clamped - minDb) / (maxDb - minDb)  // [-100,0] -> [0,1]
        ratio.coerceIn(0f, 1f)
    }
}


//이런식으로 그래프에 쓸 값 생성
//val amps: List<Float> = response.intensity.toNormalizedAmps()


//임시 더미 데이터
fun dummyIntensity(): IntensityResponse {
    return IntensityResponse(
        intensity = listOf(
            IntensityPoint("가", -0.01f),
            IntensityPoint("나", -3.5f),
            IntensityPoint(" ", -100f),   // 공백(무음)
            IntensityPoint("다", -12.3f),
        )
    )
}