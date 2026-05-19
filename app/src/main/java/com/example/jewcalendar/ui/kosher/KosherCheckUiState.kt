package com.example.jewcalendar.ui.kosher

import com.example.jewcalendar.data.KosherCheckResult

data class KosherCheckUiState(
    val input: String = "",
    val isLoading: Boolean = false,
    val result: KosherCheckResult? = null,
    val error: String? = null
)
