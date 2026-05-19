package com.example.jewcalendar.ui.kosher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jewcalendar.domain.usecase.CheckKosherUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class KosherCheckViewModel(
    private val checkKosherUseCase: CheckKosherUseCase = CheckKosherUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(KosherCheckUiState())
    val uiState: StateFlow<KosherCheckUiState> = _uiState.asStateFlow()

    fun onInputChange(value: String) {
        _uiState.update {
            it.copy(input = value, error = null)
        }
    }

    fun checkKosher() {
        val text = _uiState.value.input.trim()
        if (text.isBlank()) {
            _uiState.update { it.copy(error = "Вставь состав или описание продукта.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, result = null, error = null) }

            runCatching {
                checkKosherUseCase(text)
            }.onSuccess { result ->
                _uiState.update {
                    it.copy(isLoading = false, result = result)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = throwable.message ?: "Не получилось проверить продукт."
                    )
                }
            }
        }
    }
}
