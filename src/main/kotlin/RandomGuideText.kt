/*
가이드 문장을 CSV 파일에서 읽어서 랜덤하게 반환 코틀린 함수 (RandomGuideTextManager)

테스트 출력 예시:
일상 대화 문장: 새로 산 시계가 마음에 들어요.
질문 문장: 주말에 친구들과 등산 가셨어요?

vsCode 한글 출력이 안되면 아래 실행 방법 참고.
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
gradle run

CSV 파일 형식:
번호,문장
1,새로 산 시계가 마음에 들어요.
2,주말에는 가족과 함께 공원에 다녀왔어요.
...

기능 명세서:
RandomGuideTextManager 객체 생성 후 .getRandomConversation() 호출하면 일상 대화 문장 랜덤 반환
RandomGuideTextManager 객체 생성 후 .getRandomQuestion() 호출하면 질문 문장 랜덤 반환
*/

import java.io.File

// 파일 경로
const val CONVERSATION_SENTENCES_FILE = "guide_sentences_100.csv"
const val QUESTION_SENTENCES_FILE = "guide_questions_100.csv"

class RandomGuideTextManager {
    private val conversationSentences: List<String> = loadSentences(CONVERSATION_SENTENCES_FILE)
    private val questionSentences: List<String> = loadSentences(QUESTION_SENTENCES_FILE)

    private fun loadSentences(filePath: String): List<String> {
        val file = File(filePath)
        return if (file.exists()) {
            val lines = file.readLines(Charsets.UTF_8)
            if (lines.isEmpty()) {
                emptyList()
            } else {
                lines.drop(1)
                    .mapNotNull { line ->
                        val commaIndex = line.indexOf(",")
                        if (commaIndex >= 0 && commaIndex < line.length - 1) {
                            line.substring(commaIndex + 1).trim()
                        } else {
                            null
                        }
                    }
                    .filter { it.isNotBlank() }
            }
        } else {
            println("파일을 찾을 수 없습니다. : $filePath")
            emptyList()
        }
    }

    fun getRandomConversation(): String = conversationSentences.randomOrNull() ?: "데이터 없음"
    fun getRandomQuestion(): String = questionSentences.randomOrNull() ?: "데이터 없음"
}

// 테스트용 main
fun main(args: Array<String>) {
    // 인코딩 불필요시 제거
    System.setOut(java.io.PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(java.io.PrintStream(System.err, true, Charsets.UTF_8))
    
    val manager = RandomGuideTextManager()
    
    println("일상 대화 문장: ${manager.getRandomConversation()}")
    println("질문 문장: ${manager.getRandomQuestion()}")
}