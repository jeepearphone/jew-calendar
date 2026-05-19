package com.example.jewcalendar.data

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend

class GeminiKosherClient {

    private val model = Firebase
        .ai(backend = GenerativeBackend.Companion.googleAI())
        .generativeModel("gemini-2.5-flash")

    suspend fun checkIngredients(text: String): KosherCheckResult {
        val prompt = buildPrompt(text)
        val response = model.generateContent(prompt)
        val answer = response.text?.trim()

        return KosherCheckResult(
            answer = answer.takeUnless { it.isNullOrBlank() }
                ?: "хз чо"
        )
    }

    private fun buildPrompt(text: String): String {
        return """
            Ты помощник для предварительной проверки кошерности продукта.
            Отвечай по-русски, коротко и понятно. простым шрифтом, брат, чиобы в ответе не было левых символов
            Важно:
                если по составу нельзя точно понять, скажи, что нужен хешер;
                отдельно отметь подозрительные ингредиенты: желатин, кармин, сычужный фермент,
              виноградные компоненты, животные жиры, ароматизаторы неизвестного происхождения,
              смешение молочного и мясного.
            Формат ответа:
            1. Вердикт: скорее кошерно / скорее не кошерно / нужен хешер / недостаточно данных.
            2. Почему.
            Состав или описание продукта:
            $text
        """.trimIndent()
    }
}