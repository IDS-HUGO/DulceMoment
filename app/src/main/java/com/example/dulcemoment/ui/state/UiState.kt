package com.example.dulcemoment.ui.state

enum class UiErrorType {
    NETWORK,
    PAYMENT_REJECTED,
    OUT_OF_STOCK,
    SERVER,
    UNKNOWN,
}

sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val type: UiErrorType) : UiState<Nothing>()
}
