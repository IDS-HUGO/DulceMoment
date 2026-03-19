package com.example.dulcemoment.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.dulcemoment.ui.state.UiErrorType

@Composable
fun ErrorStateScreen(
    type: UiErrorType,
    message: String,
    onRetry: () -> Unit,
) {
    val fallback = when (type) {
        UiErrorType.NETWORK -> "Sin conexión"
        UiErrorType.PAYMENT_REJECTED -> "Pago rechazado"
        UiErrorType.OUT_OF_STOCK -> "Producto agotado"
        UiErrorType.SERVER -> "Error de servidor"
        UiErrorType.UNKNOWN -> "Error inesperado"
    }
    val composition = rememberLottieComposition(LottieCompositionSpec.Asset("error_generic.json"))

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LottieAnimation(composition = composition.value)
        Text(fallback, style = MaterialTheme.typography.titleLarge)
        Text(message, style = MaterialTheme.typography.bodyMedium)
        Button(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) { Text("Reintentar") }
    }
}
