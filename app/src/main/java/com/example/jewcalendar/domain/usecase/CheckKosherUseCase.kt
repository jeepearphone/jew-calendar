package com.example.jewcalendar.domain.usecase

import com.example.jewcalendar.data.GeminiKosherClient
import com.example.jewcalendar.data.KosherCheckResult

class CheckKosherUseCase(
    private val geminiKosherClient: GeminiKosherClient = GeminiKosherClient()
) {
    suspend operator fun invoke(text: String): KosherCheckResult {
        return geminiKosherClient.checkIngredients(text)
    }
}
