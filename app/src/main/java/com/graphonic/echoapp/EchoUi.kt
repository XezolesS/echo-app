package com.graphonic.echoapp


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun EchoScreen(
    isRecording: Boolean,
    onToggleMic: () -> Unit,
    onRefresh: () -> Unit,
    ampValues: List<Float>,
    ampCaption: String,
    ampMetaRightBottom: String? = null,
    paceValues: List<Float>,
    paceCaption: String,
    paceMetaRightBottom: String? = null,
    speechResult: SpeechSpeedResult? = null
) {
    Surface(color = Color(0xFFF6F6F6)) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRefresh) { Icon(Icons.Outlined.Refresh, null) }
                Text("안녕하세요. 좋은 하루입니다.", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(48.dp))
            }

            Spacer(Modifier.height(28.dp))

            MicButton(recording = isRecording, onClick = onToggleMic)

            Spacer(Modifier.height(24.dp))

            SectionTitle("발성 크기 분석")
            ChartCard(
                values = ampValues,
                guideFractions = listOf(0.25f, 0.5f, 0.75f),
                guideLabelsRight = listOf("낮음", "적정", "높음"),
                metaRightBottom = ampMetaRightBottom
            )
            Caption(ampCaption)

            Spacer(Modifier.height(18.dp))




            //발화 속도 그래프 버전
            SectionTitle("발화 속도 분석")
            ChartCard(
                values = paceValues,
                guideFractions = listOf(0.25f, 0.5f, 0.75f),
                guideLabelsRight = listOf("느림", "보통", "빠름"),
                metaRightBottom = paceMetaRightBottom
            )

            //
            Caption(paceCaption)

//            SectionTitle("발화 속도 분석")
//            speechResult?.let { sr ->
//                PaceTimelineCard(
//                    result = sr,
//                    metaRightBottom = paceMetaRightBottom
//                )
//            }
//
//            Caption(paceCaption)






            //결과 창
//            speechResult?.let {
//                Spacer(Modifier.height(24.dp))
//                SpeechSpeedCard(result = it)
//            }
        }
    }
}


private fun formatSeconds(sec: Double): String {
    val total = sec.toInt()
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}

@Composable
private fun PaceTimelineCard(
    result: SpeechSpeedResult,
    metaRightBottom: String? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .background(Color.White)
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        // 윗부분: 회색 타임라인 + 글자 + 00:00 ~ 마지막 시간
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(MaterialTheme.shapes.small)
                .background(Color(0xFFE0E0E0))
        ) {
            val chars = result.transcript.replace(" ", "").map { it.toString() }
            val displayChars = if (chars.size > 12) chars.take(12) else chars

            // 가운데 글자들 (안-녕-하-세-요 ... 느낌)
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                displayChars.forEach { ch ->
                    Text(ch, style = MaterialTheme.typography.bodySmall)
                }
            }

            // 아래 00:00 ~ 00:07 이런 시간
            Row(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "00:00",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )
                Text(
                    formatSeconds(result.totalSpeechTimeSeconds),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // 아래쪽: 왼쪽에 "빠름 느림", 오른쪽에 평균 속도 텍스트
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Row {
                Text(
                    "빠름",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFD16B6B)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "느림",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF1976D2)
                )
            }
            metaRightBottom?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}


@Composable
fun SpeechSpeedCard(result: SpeechSpeedResult) {
    androidx.compose.material3.Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "파일: ${result.fileName}")
            Spacer(Modifier.height(8.dp))

            Text(text = "발화 속도 (WPM): ${"%.2f".format(result.wpm)}")
            Text(text = "초당 글자 수 (CPS): ${"%.2f".format(result.cps)}")
            Text(text = "속도 평가: ${speedLabel(result.wpm)}")

            Text(text = "총 발화 시간: ${result.totalSpeechTimeSeconds}초")
            Text(text = "단어 수: ${result.totalWords}")
            Text(text = "공백 제외 글자 수: ${result.totalCharactersNoSpace}")

            Spacer(Modifier.height(12.dp))

            Text(text = "내용")
            Text(
                text = result.transcript,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

fun speedLabel(wpm: Double): String =
    when {
        wpm < 80 -> "느린 속도"
        wpm < 150 -> "보통 속도"
        else -> "빠른 속도"
    }


@Composable private fun SectionTitle(t: String) =
    Text(t, style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp))

@Composable private fun Caption(t: String) =
    Text(t, style = MaterialTheme.typography.bodySmall, color = Color(0xFF444444),
        textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))

@Composable private fun MicButton(recording: Boolean, onClick: () -> Unit) {
    val bg = if (recording) Color(0xFF303F9F) else Color(0xFFE0E0E0)
    val fg = if (recording) Color.White else Color(0xFF424242)
    Box(
        modifier = Modifier.size(96.dp).clip(CircleShape).background(bg),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onClick, modifier = Modifier.fillMaxSize()) {
            Icon(if (recording) Icons.Filled.Mic else Icons.Outlined.Mic, null, tint = fg)
        }
    }
}

@Composable
private fun ChartCard(
    values: List<Float>,
    guideFractions: List<Float>,
    guideLabelsRight: List<String>,
    metaRightBottom: String? = null,
    modifier: Modifier = Modifier
        .fillMaxWidth().height(120.dp)
        .clip(MaterialTheme.shapes.medium).background(Color.White)
) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 10.dp)) {
            val w = size.width; val h = size.height
            guideFractions.forEachIndexed { i, f ->
                val y = h * (1f - f)
                drawLine(
                    color = if (i == 1) Color(0xFFD16B6B) else Color(0xFFE0E0E0),
                    start = androidx.compose.ui.geometry.Offset(0f, y),
                    end = androidx.compose.ui.geometry.Offset(w, y),
                    strokeWidth = if (i == 1) 2f else 1f
                )
            }
            if (values.isNotEmpty()) {
                val p = Path()
                val step = w / (values.size - 1).coerceAtLeast(1)
                values.forEachIndexed { idx, v ->
                    val x = idx * step
                    val y = h * (1f - v.coerceIn(0f, 1f))
                    if (idx == 0) p.moveTo(x, y) else p.lineTo(x, y)
                }
                drawPath(p, Color(0xFF616161), style = Stroke(width = 3f))
            }
        }
        Column(
            Modifier.align(Alignment.CenterEnd).padding(end = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            guideLabelsRight.reversed().forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E))
                Spacer(Modifier.height(10.dp))
            }
        }
        metaRightBottom?.let {
            Text(it, style = MaterialTheme.typography.labelSmall, color = Color(0xFF9E9E9E),
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 6.dp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F6F6)
@Composable private fun EchoPreview() {
    val rnd = Random(7)
    val a = List(180) { (0.35f + rnd.nextFloat() * 0.4f).coerceIn(0f, 1f) }
    val p = List(180) { (0.25f + rnd.nextFloat() * 0.5f).coerceIn(0f, 1f) }

    val previewResult = SpeechSpeedResult(
        status = "success",
        fileName = "preview.wav",
        wpm = 120.0,
        cps = 3.0,
        totalSpeechTimeSeconds = 7.0,
        totalWords = 30,
        totalCharactersNoSpace = 50,
        analysisTimeSeconds = 0.43,
        transcript = "안녕 하세요 반갑습니다 (프리뷰 예시)"
    )

    EchoScreen(
        isRecording = false, onToggleMic = {}, onRefresh = {},
        ampValues = a, ampCaption = "전반적 볼륨은 보통입니다.", ampMetaRightBottom = "최대 79% · 평균 52%",
        paceValues = p, paceCaption = "약간 느린 발화입니다.", paceMetaRightBottom = "지수 2.8/초",
        speechResult = previewResult
    )
}
