package com.bumh3r.ui.components

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val formateadorFecha = DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale("es", "MX"))

/** Convierte "yyyy-MM-dd" a una fecha legible en español, ej. "sáb. 9 ago. 2026". */
fun formatearFechaLegible(fecha: String): String {
    val texto = LocalDate.parse(fecha).format(formateadorFecha)
    return texto.replaceFirstChar { it.uppercase() }
}
