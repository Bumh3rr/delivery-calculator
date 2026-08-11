package com.bumh3r.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

/**
 * Campo de texto numérico reutilizable con teclado decimal y mensaje de
 * error inline. Sin estado propio (state hoisting): el valor y el error
 * viven en el ViewModel que lo usa.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoNumerico(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    mensajeError: String?,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        label = { Text(etiqueta) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        isError = mensajeError != null,
        supportingText = mensajeError?.let { { Text(it) } },
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}
